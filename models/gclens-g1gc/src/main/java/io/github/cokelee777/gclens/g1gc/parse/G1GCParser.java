/*
 * Copyright 2026-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cokelee777.gclens.g1gc.parse;

import io.github.cokelee777.gclens.model.GCEvent;
import io.github.cokelee777.gclens.model.GCLogVersion;
import io.github.cokelee777.gclens.parse.GCLogLineParser;
import io.github.cokelee777.gclens.parse.GCLogParseException;
import io.github.cokelee777.gclens.parse.GCLogParser;
import io.github.cokelee777.gclens.parse.GCTailParser;
import io.github.cokelee777.gclens.parse.ParseResult;
import io.github.cokelee777.gclens.parse.ParsedLog;
import io.github.cokelee777.gclens.parse.TailSessionState;
import io.github.cokelee777.gclens.parse.TailTickResult;
import io.github.cokelee777.gclens.utils.GCLogVersionDetector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Parses G1 GC log files into structured events. */
public class G1GCParser implements GCLogParser, GCTailParser {

  private static final Logger log = LoggerFactory.getLogger(G1GCParser.class);

  private static final int HEADER_LINES = 20;
  private static final int EOF_TAIL_WINDOW = 64 * 1024;
  private static final long TAIL_READ_CHUNK = 1024L * 1024L;
  private static final long MAX_EOF_SUFFIX_READ = 1_000_000L;
  private static final int PENDING_WARN_BYTES = 1024 * 1024;

  /**
   * JVM unified logging: {@code [info][gc,…]}. Safer than a broad substring to reduce accidental
   * false positives in unrelated text.
   */
  private static final Pattern UNIFIED_GC_LOG_TAG =
      Pattern.compile("\\[info\\]\\s*\\[gc,[A-Za-z0-9,]+\\]");

  private final GCLogVersionDetector versionDetector;
  private final GCLogLineParser youngParser;
  private final GCLogLineParser mixedParser;
  private final GCLogLineParser fullParser;

  /** Creates a parser using default line parsers and a {@link GCLogVersionDetector}. */
  public G1GCParser() {
    this(
        new GCLogVersionDetector(),
        new G1YoungGCLineParser(),
        new G1MixedGCLineParser(),
        new G1FullGCLineParser());
  }

  /**
   * Creates a parser with the supplied collaborators (useful for testing).
   *
   * @param versionDetector detector for JDK log format version from header lines
   * @param youngParser parser for young GC lines
   * @param mixedParser parser for mixed GC lines
   * @param fullParser parser for full GC lines
   */
  public G1GCParser(
      GCLogVersionDetector versionDetector,
      GCLogLineParser youngParser,
      GCLogLineParser mixedParser,
      GCLogLineParser fullParser) {
    this.versionDetector = versionDetector;
    this.youngParser = youngParser;
    this.mixedParser = mixedParser;
    this.fullParser = fullParser;
  }

  @Override
  public ParsedLog parse(Path logPath) throws GCLogParseException {
    Objects.requireNonNull(logPath, "logPath must not be null");

    List<GCEvent> events = new ArrayList<>();
    List<String> parseWarnings = new ArrayList<>();
    List<String> headerBuffer = new ArrayList<>(HEADER_LINES);
    GCLogVersion version = GCLogVersion.UNKNOWN;

    try (BufferedReader reader = Files.newBufferedReader(logPath, StandardCharsets.UTF_8)) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (headerBuffer.size() < HEADER_LINES) {
          headerBuffer.add(line);
          if (headerBuffer.size() == HEADER_LINES) {
            version = versionDetector.detect(headerBuffer);
          }
        }
        switch (dispatchLine(line)) {
          case ParseResult.Success s -> events.add(s.event());
          case ParseResult.Warn w -> parseWarnings.add(w.line());
          case ParseResult.Skip ignored -> {}
        }
      }
      if (headerBuffer.size() < HEADER_LINES) {
        version = versionDetector.detect(headerBuffer);
      }
    } catch (IOException e) {
      throw new GCLogParseException("Failed to read log file: " + logPath, e);
    }

    return new ParsedLog(version, List.copyOf(events), List.copyOf(parseWarnings));
  }

  /**
   * Returns true for logs that are likely a JVM unified GC log (G1) — including a growing/empty
   * file, header-only <init> lines, or pause events (first 50 lines scanned).
   */
  @Override
  public boolean supports(Path logPath) {
    if (!Files.isReadable(logPath)) {
      return false;
    }
    if (Files.isDirectory(logPath)) {
      return false;
    }
    try {
      if (Files.size(logPath) == 0L) {
        return isPlausibleEmptyGcLogPath(logPath);
      }
    } catch (IOException e) {
      return false;
    }
    try (BufferedReader reader = Files.newBufferedReader(logPath, StandardCharsets.UTF_8)) {
      String line;
      int checked = 0;
      while ((line = reader.readLine()) != null && checked < 50) {
        if (isLikelyJvmGcSubsystemLine(line)) {
          return true;
        }
        if (line.contains("[gc]")
            && (line.contains("Pause Young")
                || line.contains("Pause Mixed")
                || line.contains("Pause Full"))) {
          return true;
        }
        checked++;
      }
    } catch (IOException e) {
      return false;
    }
    return false;
  }

  private static boolean isLikelyJvmGcSubsystemLine(String line) {
    return UNIFIED_GC_LOG_TAG.matcher(line).find();
  }

  /**
   * Zero-byte files are common before the JVM appends the first line. Accept only when the filename
   * is plausibly a GC log to avoid every empty file on the classpath being parsed as G1 when more
   * parsers are added.
   */
  private static boolean isPlausibleEmptyGcLogPath(Path logPath) {
    String n = logPath.getFileName().toString().toLowerCase();
    if (n.equals("gc.log")) {
      return true;
    }
    if (n.endsWith("-gc.log")
        || n.endsWith("_gc.log")
        || n.matches(".*-gc-.*")
        || n.contains("_gc_")
        || n.contains(".gclog")
        || n.contains("gclog")
        || n.contains("g1")
        || n.contains("jvm")
        || n.contains("heap")
        || n.contains("safepoint")) {
      return true;
    }
    if (n.endsWith(".log")) {
      return n.startsWith("gc-") || n.contains("-g1-");
    }
    return false;
  }

  @Override
  public boolean supportsTail(Path logPath) {
    return supports(logPath);
  }

  @Override
  public TailTickResult append(TailSessionState state, Path logPath) throws GCLogParseException {
    Objects.requireNonNull(state, "state must not be null");
    Objects.requireNonNull(logPath, "logPath must not be null");

    List<GCEvent> newEvents = new ArrayList<>();
    List<String> newWarnings = new ArrayList<>();

    try (RandomAccessFile raf = new RandomAccessFile(logPath.toFile(), "r")) {
      long fileSize = raf.length();
      long startOffset = state.getNextReadOffset();
      if (startOffset > fileSize) {
        throw new GCLogParseException(
            "Tail offset is past EOF (possible truncation): offset="
                + startOffset
                + " size="
                + fileSize);
      }

      byte[] carry = state.getPendingPrefix();
      for (long pos = startOffset; pos < fileSize; ) {
        long remaining = fileSize - pos;
        int chunk = (int) Math.min(TAIL_READ_CHUNK, Math.min(remaining, (long) Integer.MAX_VALUE));
        if ((long) carry.length + (long) chunk > (long) Integer.MAX_VALUE - 8) {
          throw new GCLogParseException(
              "Pending buffer plus next chunk is too large: pending="
                  + carry.length
                  + " chunk="
                  + chunk);
        }
        byte[] buf = new byte[chunk];
        raf.seek(pos);
        raf.readFully(buf);
        pos += chunk;
        byte[] combined = concat(carry, buf);
        carry = processCompleteLines(combined, state, newEvents, newWarnings);
      }
      warnIfPendingTooLarge(carry);

      state.setPendingPrefix(carry);
      state.setNextReadOffset(fileSize);

      if (state.getHeaderLines().size() < HEADER_LINES && carry.length == 0) {
        state.setDetectedVersion(versionDetector.detect(state.getHeaderLines()));
      }

    } catch (IOException e) {
      throw new GCLogParseException("Failed to tail-read log file: " + logPath, e);
    }

    return new TailTickResult(List.copyOf(newEvents), List.copyOf(newWarnings), state);
  }

  @Override
  public void syncCursorToEof(TailSessionState state, Path logPath) throws GCLogParseException {
    Objects.requireNonNull(state, "state must not be null");
    Objects.requireNonNull(logPath, "logPath must not be null");

    try {
      long size = Files.size(logPath);
      state.setNextReadOffset(size);
      state.setPendingPrefix(new byte[0]);
      fillHeaderFromFileStart(state, logPath);
    } catch (IOException e) {
      throw new GCLogParseException("Failed to sync tail cursor: " + logPath, e);
    }
  }

  @Override
  public void seedHeaderAndSeekEof(TailSessionState state, Path logPath)
      throws GCLogParseException {
    Objects.requireNonNull(state, "state must not be null");
    Objects.requireNonNull(logPath, "logPath must not be null");

    state.resetToInitial();
    try {
      long size = Files.size(logPath);
      state.clearHeaderLines();
      try (BufferedReader br = Files.newBufferedReader(logPath, StandardCharsets.UTF_8)) {
        String line;
        while (state.getHeaderLines().size() < HEADER_LINES && (line = br.readLine()) != null) {
          state.getHeaderLines().add(line);
        }
        state.setDetectedVersion(versionDetector.detect(state.getHeaderLines()));
        state.setHeaderComplete(true);
      }

      try (RandomAccessFile raf = new RandomAccessFile(logPath.toFile(), "r")) {
        state.setPendingPrefix(computeEofPending(raf, size));
      }
      state.setNextReadOffset(size);
    } catch (IOException e) {
      throw new GCLogParseException("Failed to seed tail session: " + logPath, e);
    }
  }

  private void fillHeaderFromFileStart(TailSessionState state, Path logPath) throws IOException {
    state.clearHeaderLines();
    state.setHeaderComplete(false);
    state.setDetectedVersion(GCLogVersion.UNKNOWN);
    try (BufferedReader reader = Files.newBufferedReader(logPath, StandardCharsets.UTF_8)) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (state.getHeaderLines().size() < HEADER_LINES) {
          state.getHeaderLines().add(line);
          if (state.getHeaderLines().size() == HEADER_LINES) {
            state.setDetectedVersion(versionDetector.detect(state.getHeaderLines()));
            state.setHeaderComplete(true);
            break;
          }
        }
      }
      if (!state.isHeaderComplete()) {
        state.setDetectedVersion(versionDetector.detect(state.getHeaderLines()));
        state.setHeaderComplete(true);
      }
    }
  }

  /**
   * Consumes every full {@code \n}-terminated UTF-8 line in {@code combined}, updates header and
   * emits parse results. Returns the trailing byte slice after the last {@code \n} (may be empty).
   */
  private byte[] processCompleteLines(
      byte[] combined, TailSessionState state, List<GCEvent> newEvents, List<String> newWarnings) {
    int lineStart = 0;
    for (int i = 0; i < combined.length; i++) {
      if (combined[i] != '\n') {
        continue;
      }
      int lineEndExclusive = i;
      String line = decodeLine(combined, lineStart, lineEndExclusive);
      handleCompleteLine(line, state, newEvents, newWarnings);
      lineStart = i + 1;
    }
    return Arrays.copyOfRange(combined, lineStart, combined.length);
  }

  private void handleCompleteLine(
      String line, TailSessionState state, List<GCEvent> newEvents, List<String> newWarnings) {
    // Must mirror full-file parse(): add each complete line to the first-20 buffer while size <
    // HEADER_LINES. (Do not use headerComplete to skip adds — sync/short-file EOF can mark
    // headerComplete true before all 20 lines have arrived; the file can still grow.)
    if (state.getHeaderLines().size() < HEADER_LINES) {
      state.getHeaderLines().add(line);
      if (state.getHeaderLines().size() == HEADER_LINES) {
        state.setDetectedVersion(versionDetector.detect(state.getHeaderLines()));
        state.setHeaderComplete(true);
      }
    }
    switch (dispatchLine(line)) {
      case ParseResult.Success s -> newEvents.add(s.event());
      case ParseResult.Warn w -> newWarnings.add(w.line());
      case ParseResult.Skip ignored -> {}
    }
  }

  private static String decodeLine(byte[] combined, int lineStart, int lineEndExclusive) {
    int len = lineEndExclusive - lineStart;
    String line;
    try {
      CharsetDecoder decoder =
          StandardCharsets.UTF_8
              .newDecoder()
              .onMalformedInput(CodingErrorAction.REPLACE)
              .onUnmappableCharacter(CodingErrorAction.REPLACE);
      line = decoder.decode(ByteBuffer.wrap(combined, lineStart, len)).toString();
    } catch (CharacterCodingException e) {
      line = new String(combined, lineStart, len, StandardCharsets.UTF_8);
    }
    if (line.indexOf('\0') >= 0) {
      line = line.replace("\0", "");
    }
    if (!line.isEmpty() && line.charAt(line.length() - 1) == '\r') {
      return line.substring(0, line.length() - 1);
    }
    return line;
  }

  /**
   * Returns bytes after the last {@code \n} in the file, walking backwards in {@link
   * #EOF_TAIL_WINDOW} steps so very long “lines” (no newlines) still find the true file suffix.
   */
  private static byte[] computeEofPending(RandomAccessFile raf, long fileSize) throws IOException {
    if (fileSize <= 0L) {
      return new byte[0];
    }
    long end = fileSize;
    while (end > 0L) {
      long start = Math.max(0L, end - (long) EOF_TAIL_WINDOW);
      int len = (int) Math.min((long) Integer.MAX_VALUE, end - start);
      raf.seek(start);
      byte[] w = new byte[len];
      raf.readFully(w);
      int lastNl = lastIndexOfByte(w, (byte) '\n');
      if (lastNl >= 0) {
        long suffixStart = start + (long) lastNl + 1L;
        return readSuffix(raf, fileSize, suffixStart);
      }
      if (start == 0L) {
        return readSuffix(raf, fileSize, 0L);
      }
      end = start;
    }
    return new byte[0];
  }

  private static byte[] readSuffix(RandomAccessFile raf, long fileSize, long suffixStart)
      throws IOException {
    long n = fileSize - suffixStart;
    if (n < 0L) {
      return new byte[0];
    }
    if (n > MAX_EOF_SUFFIX_READ) {
      log.warn(
          "EOF suffix without a preceding newline is {} bytes; truncating pending to last {} bytes for tail seed.",
          n,
          MAX_EOF_SUFFIX_READ);
      suffixStart = fileSize - MAX_EOF_SUFFIX_READ;
      n = MAX_EOF_SUFFIX_READ;
    }
    raf.seek(suffixStart);
    int readLen = (int) n;
    if (readLen < 0) {
      return new byte[0];
    }
    byte[] p = new byte[readLen];
    raf.readFully(p);
    return p;
  }

  private static byte[] concat(byte[] a, byte[] b) {
    if (a.length == 0) {
      return b;
    }
    if (b.length == 0) {
      return a;
    }
    byte[] out = Arrays.copyOf(a, a.length + b.length);
    System.arraycopy(b, 0, out, a.length, b.length);
    return out;
  }

  private static int lastIndexOfByte(byte[] arr, byte value) {
    for (int i = arr.length - 1; i >= 0; i--) {
      if (arr[i] == value) {
        return i;
      }
    }
    return -1;
  }

  private void warnIfPendingTooLarge(byte[] pending) {
    if (pending.length > PENDING_WARN_BYTES) {
      log.warn(
          "Tail pending buffer exceeds {} bytes ({}). Line may be incomplete or malformed.",
          PENDING_WARN_BYTES,
          pending.length);
    }
  }

  private ParseResult dispatchLine(String line) {
    if (!line.contains("[gc]")) {
      return new ParseResult.Skip();
    }
    if (line.contains("Pause Young")) return youngParser.parse(line);
    if (line.contains("Pause Mixed")) return mixedParser.parse(line);
    if (line.contains("Pause Full")) return fullParser.parse(line);
    return new ParseResult.Skip();
  }
}
