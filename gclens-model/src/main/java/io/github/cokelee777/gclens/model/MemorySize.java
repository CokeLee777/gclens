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
package io.github.cokelee777.gclens.model;

/**
 * An immutable memory size value stored in bytes.
 *
 * @param bytes the size in bytes
 */
public record MemorySize(long bytes) {

  /**
   * Creates a memory size from a value in mebibytes (MiB).
   *
   * @param mb the size in MiB
   * @return the corresponding {@code MemorySize}
   */
  public static MemorySize ofMegabytes(long mb) {
    return new MemorySize(mb * 1024L * 1024L);
  }

  /**
   * Returns the size in whole mebibytes (truncating toward zero).
   *
   * @return the size in MiB
   */
  public long toMegabytes() {
    return bytes / (1024L * 1024L);
  }
}
