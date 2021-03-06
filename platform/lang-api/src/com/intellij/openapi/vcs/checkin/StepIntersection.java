/*
 * Copyright 2000-2011 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.openapi.vcs.checkin;

import com.intellij.openapi.util.TextRange;
import com.intellij.util.Function;
import com.intellij.util.PairConsumer;
import com.intellij.util.containers.Convertor;
import com.intellij.util.containers.PeekableIteratorWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class StepIntersection {
  /**
   * Iterate over intersected ranges in two lists, sorted by TextRange.
   */
  public static <T, V> void processIntersections(@NotNull List<T> elements1,
                                                 @NotNull List<V> elements2,
                                                 @NotNull Convertor<T, TextRange> convertor1,
                                                 @NotNull Convertor<V, TextRange> convertor2,
                                                 @NotNull PairConsumer<T, V> intersectionConsumer) {
    PeekableIteratorWrapper<T> peekIterator1 = new PeekableIteratorWrapper<>(elements1.iterator());

    outerLoop:
    for (V item2 : elements2) {
      TextRange range2 = convertor2.convert(item2);

      while (peekIterator1.hasNext()) {
        T item1 = peekIterator1.peek();
        TextRange range1 = convertor1.convert(item1);

        if (range1.intersects(range2)) {
          intersectionConsumer.consume(item1, item2);
        }

        if (range2.getEndOffset() < range1.getEndOffset()) {
          continue outerLoop;
        }
        else {
          peekIterator1.next();
        }
      }

      break outerLoop;
    }
  }

  public static <T, V> void processElementIntersections(@NotNull T element1,
                                                        @NotNull List<V> elements2,
                                                        @NotNull Convertor<T, TextRange> convertor1,
                                                        @NotNull Convertor<V, TextRange> convertor2,
                                                        @NotNull PairConsumer<T, V> intersectionConsumer) {
    TextRange range1 = convertor1.convert(element1);
    int index = binarySearch(elements2, range1.getStartOffset(), value -> convertor2.convert(value).getEndOffset());
    if (index < 0) index = -index - 1;

    for (int i = index; i < elements2.size(); i++) {
      V item2 = elements2.get(i);
      TextRange range2 = convertor2.convert(item2);

      if (range1.intersects(range2)) {
        intersectionConsumer.consume(element1, item2);
      }

      if (range2.getStartOffset() > range1.getEndOffset()) break;
    }
  }

  private static <T> int binarySearch(@NotNull List<T> elements, int value, @NotNull Function<T, Integer> convertor) {
    int low = 0;
    int high = elements.size() - 1;

    while (low <= high) {
      int mid = (low + high) / 2;

      int midValue = convertor.fun(elements.get(mid));
      if (midValue < value) {
        low = mid + 1;
      }
      else if (midValue > value) {
        high = mid - 1;
      }
      else {
        return mid;
      }
    }
    return -(low + 1);
  }
}
