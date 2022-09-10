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
import java.util.List;

import com.ebay.taskgraph.executor.Task;

/**
 * Container for profiler data returned by a service.
 * Allows dependent service profiler data to be included in the parent service profiler response.
 * Task names and other data that may have collisions with the parent service are changed to add
 * the context of the particular service in order to make them unique.
 */
public class ProfilerFromModel implements IProfiler {

    private final ProfilerModel model;
    private long startTime;

    public ProfilerFromModel(ProfilerModel pm, IProfiler parent) {
        this.model = pm;
        this.startTime = parent.getStartTime();
        renameTask(pm, pm, parent.getName() + '.');

        // link the parent task to the top level profiler model
        ProfilerProperty prop = new ProfilerProperty();
        prop.setName(Task.DEPENDENCIES_META_DATA_KEY);
        prop.setValue(parent.getName());
        pm.getData().add(prop);
        
        // set top level profiler model parent to make it link with the caller task in the profiler tool thread view
        prop = new ProfilerProperty();
        prop.setName(Task.PARENT_TASK);
        prop.setValue(parent.getName());
        pm.getData().add(prop);
        
        // prefix all the thread names with the context of the parent
        renameThreads(pm, parent.getName(), parent.getData(Task.TASK_THREAD));
    }

    private void renameThreads(ProfilerModel pm, String parent, String parentThread) {
        if (pm.getData() != null) {
            ProfilerProperty parentProp = null;
            ProfilerProperty threadProp = null;
            for (ProfilerProperty prop : pm.getData()) {
                if (Task.TASK_THREAD.equals(prop.getName()) && prop.getValue() != null) {
                    threadProp = prop;
                }          
                if (Task.PARENT_TASK.equals(prop.getName()) && prop.getValue() != null) {
                    parentProp = prop;
                }          
            }
            if (null == parentProp) {
                // assign parent task if not specified, groups tasks under owning task properly
                parentProp = new ProfilerProperty();
                parentProp.setName(Task.PARENT_TASK);
                parentProp.setValue(parent);
                pm.getData().add(parentProp);
            }
            if (null == threadProp) {
                // no thread property assign the parent thred name
                threadProp = new ProfilerProperty();
                threadProp.setName(Task.TASK_THREAD);
                threadProp.setValue(parentThread);
                pm.getData().add(threadProp);
            } else {
                threadProp.setValue(parent + '.' + threadProp.getValue());
            }
        }
        if (pm.getChildren() != null) {
            for (ProfilerModel child : pm.getChildren()) {
                renameThreads(child, parent, parentThread);
            }
        }
    }

    /**
     * Rename all tasks to avoid duplicates with other tasks.
     */
    private static void renameTask(ProfilerModel root, ProfilerModel pm, String prefix) {
        if (isTask(pm)) {
            String currentName = getTaskName(pm);
            String newName = prefix + currentName;
            updateTaskName(pm, currentName, newName);
            updateDependencies(root, currentName, newName);
        }
        if (pm.getChildren() != null) {
            for (ProfilerModel child : pm.getChildren()) {
                renameTask(root, child, prefix);
            }
        }
    }

    private static boolean isTask(ProfilerModel pm) {
        boolean rval = false;
        if (pm.getData() != null) {
            for (ProfilerProperty prop : pm.getData()) {
                if (Task.EXEC_TYPE.equals(prop.getName())) {
                    rval = true;
                    break;
                }
            }
        }
        return rval;
    }

    private static void updateDependencies(ProfilerModel pm, String currentName, String newName) {
        if (pm.getData() != null) {
            for (ProfilerProperty prop : pm.getData()) {
                if (Task.DEPENDENCIES_META_DATA_KEY.equals(prop.getName())) {
                    if (prop.getValue().equals(currentName)) {
                        prop.setValue(prop.getValue().replace(currentName, newName));
                    } else if (prop.getValue().endsWith(Task.DEPENDENCIES_SEPARATOR + currentName)) {
                        prop.setValue(prop.getValue().replace(Task.DEPENDENCIES_SEPARATOR + currentName, Task.DEPENDENCIES_SEPARATOR + newName));
                    } else if (prop.getValue().startsWith(currentName + Task.DEPENDENCIES_SEPARATOR)) {
                        prop.setValue(prop.getValue().replace(currentName + Task.DEPENDENCIES_SEPARATOR, newName + Task.DEPENDENCIES_SEPARATOR));
                    } else if (prop.getValue().contains(Task.DEPENDENCIES_SEPARATOR + currentName + Task.DEPENDENCIES_SEPARATOR)) {
                        prop.setValue(prop.getValue().replace(Task.DEPENDENCIES_SEPARATOR + currentName + Task.DEPENDENCIES_SEPARATOR,
                                Task.DEPENDENCIES_SEPARATOR + newName + Task.DEPENDENCIES_SEPARATOR));
                    }
                }
                // also update any tasks that refer to the current task as its parent
                if (Task.PARENT_TASK.equals(prop.getName())
                      && prop.getValue() != null
                      && prop.getValue().equals(currentName)) {
                    prop.setValue(prop.getValue().replace(currentName, newName));
                }
            }
        }
        if (pm.getChildren() != null) {
            for (ProfilerModel child : pm.getChildren()) {
                updateDependencies(child, currentName, newName);
            }
        }
    }

    private static void updateTaskName(ProfilerModel pm, String currentName, String newName) {
        boolean updated = false;
        boolean hasNodeLabel = false;
        // will have properties after this
        if (null == pm.getData()) {
            pm.setData(new ArrayList<ProfilerProperty>());
        }
        // check if there's an actual task name in the properties
        for (ProfilerProperty prop : pm.getData()) {
            if (Task.TASK_NAME.equals(prop.getName())) {
                prop.setValue(newName);
                updated = true;
            }
            if (Task.NODE_LABEL.equals(prop.getName())) {
                hasNodeLabel = true;
            }
        }
        // if not updated add a new task name property based on the profiler name
        if (!updated) {
            ProfilerProperty prop = new ProfilerProperty();
            prop.setName(Task.TASK_NAME);
            prop.setValue(newName);
            pm.getData().add(prop);
        }
        // keep the node label as the current task name unless a label already exists
        if (!hasNodeLabel) {
            ProfilerProperty prop = new ProfilerProperty();
            prop.setName(Task.NODE_LABEL);
            prop.setValue(currentName);
            pm.getData().add(prop);
        }
    }

    private static String getTaskName(ProfilerModel pm) {
        String taskName = pm.getName();
        // check if there's an actual task name in the properties
        if (pm.getData() != null) {
            for (ProfilerProperty prop : pm.getData()) {
                if (Task.TASK_NAME.equals(prop.getName())) {
                    taskName = prop.getValue();
                    break;
                }
            }
        }
        return taskName;
    }

    @Override
    public ProfilerModel getModel(long requestStart) {

        // only update times once (aggregate profiler & request scope diagnostics will generate the model)
        if (this.startTime > 0L) {
            // add start time to all entries so they are shifted relative to the owning task in the parent service
            updateStartTimes(this.model, this.startTime - requestStart);    
            this.startTime = -1L;
        }
        return this.model;
    }

    private static void updateStartTimes(ProfilerModel pm, long startTime) {
        pm.setStartTime(pm.getStartTime() + startTime);
        if (pm.getChildren() != null) {
            for (ProfilerModel child : pm.getChildren()) {
                updateStartTimes(child, startTime);
            }
        }
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
    public String getData(String name) {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void add(IProfiler profiler) {
    }

    @Override
    public void add(IProfilerEntry entry) {
    }

    @Override
    public IProfilerEntry newEntry(String string) {
        return null;
    }

    @Override
    public void log() {
    }

    @Override
    public List<Diagnostic> getDiagnostics() {
        return null;
    }

    @Override
    public long getStartTime() {
        return 0;
    }

}
