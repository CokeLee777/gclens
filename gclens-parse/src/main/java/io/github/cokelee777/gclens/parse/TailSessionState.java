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
package io.github.cokelee777.gclens.parse;

import io.github.cokelee777.gclens.model.GCLogVersion;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Mutable session state for incremental (tail) parsing. Not thread-safe; not intended for
 * serialization.
 */
public final class TailSessionState {

  private long nextReadOffset;
  private byte[] pendingPrefix;
  private final ArrayList<String> headerLines;
  private boolean headerComplete;
  private GCLogVersion detectedVersion;
  private Object lastFileKey;
  private long lastKnownSize;

  private TailSessionState() {
    this.pendingPrefix = new byte[0];
    this.headerLines = new ArrayList<>(32);
    this.detectedVersion = GCLogVersion.UNKNOWN;
  }

  /** Returns a fresh session with offset 0 and empty buffers. */
  public static TailSessionState initial() {
    return new TailSessionState();
  }

  /** Next byte offset to read from the log file. */
  public long getNextReadOffset() {
    return nextReadOffset;
  }

  public void setNextReadOffset(long nextReadOffset) {
    this.nextReadOffset = nextReadOffset;
  }

  /** Bytes carried over from the previous tick that do not yet end a complete UTF-8 line. */
  public byte[] getPendingPrefix() {
    return pendingPrefix;
  }

  public void setPendingPrefix(byte[] pendingPrefix) {
    this.pendingPrefix = pendingPrefix != null ? pendingPrefix : new byte[0];
  }

  /** Header lines collected so far (at most 20 while completing the header). */
  public List<String> getHeaderLines() {
    return headerLines;
  }

  public void clearHeaderLines() {
    headerLines.clear();
  }

  public boolean isHeaderComplete() {
    return headerComplete;
  }

  public void setHeaderComplete(boolean headerComplete) {
    this.headerComplete = headerComplete;
  }

  public GCLogVersion getDetectedVersion() {
    return detectedVersion;
  }

  public void setDetectedVersion(GCLogVersion detectedVersion) {
    this.detectedVersion = Objects.requireNonNull(detectedVersion, "detectedVersion");
  }

  /** Optional file key from {@link java.nio.file.attribute.BasicFileAttributes#fileKey()}. */
  public Object getLastFileKey() {
    return lastFileKey;
  }

  public void setLastFileKey(Object lastFileKey) {
    this.lastFileKey = lastFileKey;
  }

  public long getLastKnownSize() {
    return lastKnownSize;
  }

  public void setLastKnownSize(long lastKnownSize) {
    this.lastKnownSize = lastKnownSize;
  }

  /** Resets all fields to the same values as {@link #initial()}. */
  public void resetToInitial() {
    nextReadOffset = 0L;
    pendingPrefix = new byte[0];
    headerLines.clear();
    headerComplete = false;
    detectedVersion = GCLogVersion.UNKNOWN;
    lastFileKey = null;
    lastKnownSize = 0L;
  }
}
