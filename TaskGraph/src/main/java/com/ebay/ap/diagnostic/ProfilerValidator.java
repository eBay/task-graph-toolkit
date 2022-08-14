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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ebay.ap.executor.Task;

/**
 * Validates profiler data.  Ensures all dependencies specified have entries and that there are no duplicate tasks.
 */
public class ProfilerValidator {

    public static void validate(ProfilerModel profiler) {
        // verify we can find all the dependencies
        Map<String, Boolean> entries = new HashMap<>();
        collectEntries(entries, profiler);
        validateDependencies(entries, profiler);
    }

    private static void collectEntries(Map<String, Boolean> entries, ProfilerModel profiler) {
        String taskName = getTaskName(profiler);
        if (taskName != null) {
            if (entries.containsKey(taskName)) {
                throw new RuntimeException("Duplicate task name: " + profiler.getName());
            }
            entries.put(taskName, Boolean.TRUE);
        }
        if (profiler.getChildren() != null) {
            for (ProfilerModel child : profiler.getChildren()) {
                collectEntries(entries, child);
            }
        }
    }

    private static String getTaskName(ProfilerModel profiler) {
        String taskName = null;
        boolean isTask = false;

        if (profiler.getData() != null) {
            for (ProfilerProperty prop : profiler.getData()) {
                if (Task.EXEC_TYPE.equals(prop.getName())) {
                    isTask = true;
                }
                if (Task.TASK_NAME.equals(prop.getName())) {
                    taskName = prop.getValue();
                }
            }
        }
        if (isTask && null == taskName) {
            taskName = profiler.getName();
        }
        return taskName;
    }

    private static void validateDependencies(Map<String, Boolean> entries, ProfilerModel entry) {
        validateDependencies(entries, entry.getData());
        if (entry.getChildren() != null) {
            for (ProfilerModel child : entry.getChildren()) {
                validateDependencies(entries, child);
            }
        }
    }

    private static void validateDependencies(Map<String, Boolean> entries, List<ProfilerProperty> props) {
        if (props != null) {
            for (ProfilerProperty prop : props) {
                if (Task.DEPENDENCIES_META_DATA_KEY.equals(prop.getName())) {
                    String dependencies = prop.getValue();
                    if (dependencies != null) {
                        String[] deps = dependencies.split(":");
                        for (String d : deps) {
                            if (!entries.containsKey(d)) {
                                throw new RuntimeException("No profiler entry for dependency: " + d);
                            }
                        }
                    }
                }
            }
        }
    }

}
