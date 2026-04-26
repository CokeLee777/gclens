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
package io.github.cokelee777.gclens.cli;

import io.github.cokelee777.gclens.cli.command.AnalyzeCommand;
import io.github.cokelee777.gclens.cli.command.WatchCommand;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/** Picocli entry point for the GCLens CLI. */
@Command(
    name = "gclens",
    subcommands = {AnalyzeCommand.class, WatchCommand.class},
    description = "GCLens — JVM GC log analyzer")
public class GCLensReporter implements Runnable {

  /** Creates the root Picocli command without subcommand state. */
  public GCLensReporter() {}

  @Spec @Nullable CommandSpec spec;

  /** Prints the top-level usage help when no subcommand is provided. */
  @Override
  public void run() {
    Objects.requireNonNull(spec, "spec must not be null");

    spec.commandLine().usage(System.out);
  }

  /**
   * Launches the CLI application.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    int exitCode = new CommandLine(new GCLensReporter()).execute(args);
    System.exit(exitCode);
  }
}
