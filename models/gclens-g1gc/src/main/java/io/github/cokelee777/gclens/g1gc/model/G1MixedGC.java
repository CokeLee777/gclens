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
package io.github.cokelee777.gclens.g1gc.model;

import io.github.cokelee777.gclens.model.GCEvent;
import io.github.cokelee777.gclens.model.GCType;
import io.github.cokelee777.gclens.model.MemorySize;
import java.time.Duration;

/**
 * A parsed G1 mixed collection event.
 *
 * @param timestamp offset from the beginning of the log
 * @param duration pause duration of this collection
 * @param heapBefore heap usage before the collection
 * @param heapAfter heap usage after the collection
 * @param heapTotal committed heap size reported for this event
 */
public record G1MixedGC(
    Duration timestamp,
    Duration duration,
    MemorySize heapBefore,
    MemorySize heapAfter,
    MemorySize heapTotal)
    implements GCEvent {

  @Override
  public GCType gcType() {
    return GCType.MIXED_GC;
  }
}
