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

import java.util.Collections;
import java.util.List;

public class ProfilerNull implements IProfiler {

    public static final IProfiler INSTANCE = new ProfilerNull();

    private ProfilerNull() {
    }

    @Override
    public void add(IProfiler profiler) {
    }

    @Override
    public void add(IProfilerEntry entry) {
    }

    @Override
    public IProfilerEntry newEntry(String string) {
        return ProfilerEntryNull.INSTANCE;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void addData(String name, String value) {
    }

    @Override
    public void log() {
    }

    @Override
    public List<Diagnostic> getDiagnostics() {
        return Collections.emptyList();
    }

    @Override
    public ProfilerModel getModel(long requestStart) {
        return ProfilerEntryNull.NULL_MODEL;
    }

    @Override
    public String getName() {
        return ProfilerNull.class.getSimpleName();
    }

    @Override
    public long getStartTime() {
        return 0;
    }

    @Override
    public String getData(String name) {
        return null;
    }

}
