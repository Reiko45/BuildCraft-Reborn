/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc.search;

import java.util.List;

/** A quick search over names (1.12's {@code ISuffixArray}): finds everything whose name contains some text. */
public interface ISuffixArray<T> {
    void add(T obj, String name);

    void generate();

    SearchResult<T> search(String substring, int maxResults);

    final class SearchResult<T> {
        public final List<T> results;
        public final int realResultCount;

        public SearchResult(List<T> results, int realResultCount) {
            this.results = results;
            this.realResultCount = realResultCount;
        }

        public SearchResult(List<T> results) {
            this(results, results.size());
        }

        public boolean hasAllResults() {
            return results.size() == realResultCount;
        }
    }
}
