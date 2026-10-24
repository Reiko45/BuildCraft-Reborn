/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMap;

/** A simple (and not very memory efficient) suffix array: every suffix of every word of every name, sorted, so a
 * search is a range lookup. Words of the search text must all match. */
public class SimpleSuffixArray<T> implements ISuffixArray<T> {
    private final Object2ObjectSortedMap<String, List<T>> suffixArray = new Object2ObjectAVLTreeMap<>();

    @Override
    public void add(T obj, String name) {
        int end = name.length();
        for (int s = name.length() - 1; s >= 0; s--) {
            char c = name.charAt(s);
            if (c == '\n' || c == ' ') {
                // A search can't go over a word or line boundary
                end = s;
                continue;
            }
            suffixArray.computeIfAbsent(name.substring(s, end), k -> new ArrayList<>()).add(obj);
        }
    }

    @Override
    public void generate() {
        // Everything is sorted as it is added
    }

    @Override
    public SearchResult<T> search(String substring, int maxResults) {
        Set<T> entries = new LinkedHashSet<>();
        boolean first = true;
        for (String s : substring.split(" ")) {
            if (s.isEmpty()) {
                continue;
            }
            Set<T> found = first ? entries : new HashSet<>();
            for (List<T> values : suffixArray.subMap(s, s + Character.MAX_VALUE).values()) {
                found.addAll(values);
            }
            if (!first) {
                entries.retainAll(found);
            }
            first = false;
        }
        List<T> results = new ArrayList<>(entries);
        int realResultCount = results.size();
        if (results.size() > maxResults) {
            results.subList(maxResults, results.size()).clear();
        }
        return new SearchResult<>(results, realResultCount);
    }
}
