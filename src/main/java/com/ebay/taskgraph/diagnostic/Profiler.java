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

package com.ebay.taskgraph.diagnostic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.taskgraph.util.JsonHelper;

public class Profiler implements IProfiler, IProfilerEntry {

    public static final String PROFILER_DIAGNOSTIC_NAME = "Profiler";

    private static final Logger LOGGER = LoggerFactory.getLogger(Profiler.class);

    static class Entry implements IProfilerEntry {

        /**
         * The absolute time we started profiling.  This will be used later to calculate the start time
         * relative to start of the request processing.
         */
        private long absoluteStartTime = -1L;

        /**
         * Time spent.
         */
        private long duration = -1L;

        private final String name;
        
        /**
         * Keep track of data outside of CAL transaction
         * so it can be initialized before profiler is started.
         */
        private Map<String, String> data = new LinkedHashMap<>();

        public Entry(String name) {
            this.name = name;
        }

        @Override
        public void start() {
            // don't start twice
            if (this.absoluteStartTime < 0L) {
                this.absoluteStartTime = System.nanoTime();
            }
        }

        @Override
        public void stop() {
            // if we failed to start the profiler, keep the time at -1
            // also only stop on the first invocation
            if (this.absoluteStartTime > 0 && this.duration < 0) {
                this.duration = System.nanoTime() - this.absoluteStartTime;
            }
        }

        @Override
        public ProfilerModel getModel(long requestStart) {
            long startTime = this.absoluteStartTime;
            if (startTime > 0) {
                // calculate start time relative to request start only if the entry was started
                startTime -= requestStart;
            }
            ProfilerModel pm = new ProfilerModel();
            pm.setName(this.name);
            pm.setStartTime(startTime);
            pm.setDuration(this.duration);
            if (!this.data.isEmpty()) {
                List<ProfilerProperty> properties = new ArrayList<>();
                for (java.util.Map.Entry<String, String> entry : this.data.entrySet()) {
                    ProfilerProperty property = new ProfilerProperty();
                    property.setName(entry.getKey());
                    property.setValue(entry.getValue());
                    properties.add(property);
                }
                pm.setData(properties);
            }
            return pm;
        }

        @Override
        public void addData(String name, String value) {
            this.data.put(name, value);
        }

        @Override
        public String getData(String name) {
            return this.data.get(name);
        }
    }

    /** the entry that times the overall profiler from instantiation to collection */
    private final Entry entry;

    /**
     * list of child entries
     * synchronized because sometimes tasks share the same response context even though they shouldn't
     */
    private final List<IProfilerEntry> entries = Collections.synchronizedList(new ArrayList<IProfilerEntry>());
    
    public Profiler(String name) {
        this.entry = new Entry(name);
    }

    @Override
    public void start() {
        this.entry.start();
    }

    @Override
    public void stop() {
        this.entry.stop();
    }

    @Override
    public void add(IProfilerEntry entry) {
        entry.stop();
    }

    @Override
    public void add(IProfiler profiler) {
        // in case profiler wasn't explicitly stopped
        // the assumption is that the profiler is being added to a higher level profiler (this)
        // and so all its work is done
        profiler.stop();

        // don't bother adding null profilers, it's confusing to show these in the log
        // there are valid cases when profiling where null profilers are also present (internal service calls)
        if (profiler != ProfilerNull.INSTANCE) {
            this.entries.add(profiler);
        }
    }

    @Override
    public void addData(String name, String value) {
        this.entry.addData(name, value);
    }

    @Override
    public ProfilerModel getModel(long requestStart) {
        ProfilerModel pm = this.entry.getModel(requestStart);
        if (this.entries.size() > 0) {
            List<ProfilerModel> children = new ArrayList<>(this.entries.size());
            for (IProfilerEntry e : this.entries) {
                ProfilerModel model = e.getModel(requestStart);
                children.add(model);
            }
            pm.setChildren(children);
        }
        return pm;
    }

    public static String getEntryName(String context, String name) {
        if (context != null && context.length() > 0) {
            name = removeDuplicatePath(context, name);
            name = context + '.' + name; 
        }
        return name;
    }

    /**
     * Sometimes the name of the profiler entry already contains part of the context.
     * Remove this duplication.
     * @param context
     * @param name
     * @return
     */
    private static String removeDuplicatePath(String context, final String name) {

        int finalIndex = 0;
        StringBuilder pre = new StringBuilder();
        String remainder = name;
        int index ;
        while ((index = remainder.indexOf('.')) > 0) {
            pre.append(remainder.substring(0, index));
            if (context.contains(pre.toString())) {
                ++index;
                pre.append('.');
                finalIndex += index;
                remainder = remainder.substring(index);
            } else {
                break;
            }
        }
        if (finalIndex > 0) {
            remainder = name.substring(finalIndex);
        }
        return remainder;
    }

    public static IProfiler getProfiler(String name, DiagnosticConfig diagnosticConfig) {
        if (diagnosticConfig.showDiagnostics || diagnosticConfig.profile) {
            return new Profiler(name);
        }
        return ProfilerNull.INSTANCE;
    }

    @Override
    public IProfilerEntry newEntry(String entryName) {
        // prefix every entry name with the parent profiler name
        // this helps distinguish the CAL transactions for each of the individual entries
        IProfilerEntry entry = new Entry(this.entry.name + '.' + entryName);
        entry.start();
        this.entries.add(entry);
        return entry;
    }

    @Override
    public void log() {
        this.stop();
        if (LOGGER.isDebugEnabled()) {
            // check for duplicates in profiler entries
            // it could mean cyclic reference that would result in an infinite loop
            Map<String, List<Entry>> entries = new HashMap<>();
            validate(entries, this);
            
            // don't log to stdout, it's just as easy to copy from diagnostic output
            //ProfilerModel pm = this.getModel(this.entry.absoluteStartTime);
            //LOGGER.debug(JsonHelper.prettyPrint(pm));
        }
    }


    private static void validate(Map<String, List<Entry>> entries, Profiler entry) {
        validateEntry(entries, entry.entry);
        if (entry.entries.size() > 0) {
            for (IProfilerEntry e : entry.entries) {
                if (e instanceof Profiler) {
                    validate(entries, (Profiler) e);
                } else if (e instanceof Entry) {
                    validateEntry(entries, (Entry) e);
                }
            }
        }
    }

    private static void validateEntry(Map<String, List<Entry>> entries, Entry entry) {
        List<Entry> exists = entries.get(entry.name);
        if (exists != null) {
            // check entry not currently in the list
            for (int i = exists.size() - 1; i >= 0 ; --i) {
                if (exists.get(i) == entry) {
                    // prohibit duplicate entries 
                    throw new RuntimeException("duplicate profiler entry (possible cycle): " + entry.name);
                }
            }
            exists.add(entry);
        } else {
            exists = new ArrayList<>();
            exists.add(entry);
            entries.put(entry.name, exists);
        }
    }

    /**
     * Create a formatted string of the profiler result.
     * @param requestStart 
     */
    private Diagnostic getProfilerDiagnostic(long requestStart) {

        Diagnostic d = new Diagnostic();
        d.setSender(PROFILER_DIAGNOSTIC_NAME);
        List<String> value = new ArrayList<>(1);
        try {
            value.add(JsonHelper.writeAsString(this.getModel(requestStart)));
        } catch (RuntimeException e) {
            value.add(e.getMessage());
        }
        d.setValue(value);
        return d;
    }

    @Override
    public List<Diagnostic> getDiagnostics() {
        List<Diagnostic> rval = new ArrayList<>(1);
        rval.add(this.getProfilerDiagnostic(this.entry.absoluteStartTime));
        return rval;
    }

    @Override
    public String getName() {
        return this.entry.name;
    }

    @Override
    public long getStartTime() {
        return this.entry.absoluteStartTime;
    }

    @Override
    public String getData(String name) {
        return this.entry.getData(name);
    }

}
