/*
 * Copyright 2022 eBay Inc.
 *  Author/Developer: Damian Dolan
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ebay.ap.diagnostic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovingAverageAggregateProfiler {

    /** limit amount of entries per command */
    public static final int MAX_ENTRIES = 2000;

    private static Logger LOGGER = LoggerFactory.getLogger(MovingAverageAggregateProfiler.class);

    private final String command;

    private final Map<String, MovingAverageAggregateProfilerEntry> entries = new LinkedHashMap<>();

    public MovingAverageAggregateProfiler(String command) {
        this.command = command;
    }

    public void addProfiler(
            ProfilerModel profiler,
            String context) {

        String entry = Profiler.getEntryName(context, profiler.getName());
        MovingAverageAggregateProfilerEntry pma = entries.get(entry);
        if (null == pma) {
            if (entries.size() < MAX_ENTRIES) {
                pma = new MovingAverageAggregateProfilerEntry(profiler);
                entries.put(entry, pma);
            } else {
                LOGGER.error("Maximum aggregate profiler entries exceeded: " + MAX_ENTRIES + ", command: " + this.command);
            }
        } else {
            pma.add(profiler);
        }
        if (profiler.getChildren() != null) {
            for (ProfilerModel p : profiler.getChildren()) {
                addProfiler(p, entry);
            }
        }
    }

    public void addProfiler(ProfilerModel model) {
        this.addProfiler(model, "");
    }

    public List<MovingAverageAggregateProfilerEntry> getModel() {
        List<MovingAverageAggregateProfilerEntry> rval = new ArrayList<>(this.entries.size());
        for (Entry<String, MovingAverageAggregateProfilerEntry> entry : this.entries.entrySet()) {
            // clone the entries so we can move serialization of the models out of the synchronized block
            // in the AggregateProfiler
            // have the entry name include the context so they're unique
            rval.add(new MovingAverageAggregateProfilerEntry(entry.getKey(), entry.getValue()));
        }
        return rval;
    }

}
