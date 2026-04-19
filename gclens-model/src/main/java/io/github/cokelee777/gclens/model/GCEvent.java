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

import java.time.Duration;

/** A parsed garbage collection event with timing and heap occupancy information. */
public interface GCEvent {

  /**
   * The GC type category of this event.
   *
   * @return the GC type category
   */
  GCType gcType();

  /**
   * The timestamp offset from the beginning of the log.
   *
   * @return the event timestamp
   */
  Duration timestamp();

  /**
   * The pause duration of the event.
   *
   * @return the pause duration
   */
  Duration duration();

  /**
   * The heap usage before the event.
   *
   * @return the pre-collection heap usage
   */
  MemorySize heapBefore();

  /**
   * The heap usage after the event.
   *
   * @return the post-collection heap usage
   */
  MemorySize heapAfter();

  /**
   * The total committed heap size reported for the event.
   *
   * @return the committed heap size
   */
  MemorySize heapTotal();
}
