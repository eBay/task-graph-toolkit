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

package com.ebay.ap.executor.workflow;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.ap.executor.ICallableTaskFuture;

/**
 * collection of tasks that need to be shared as dependencies for other tasks in the workflow
 */
public class TaskInstanceHolder<V extends IWorkflowBuilder> {

    static final Logger LOGGER = LoggerFactory.getLogger(TaskInstanceHolder.class);

    private final Map<String, TaskInstance<?, V>> tasks = new HashMap<>();

    private final V workflowBuilder;

    public TaskInstanceHolder(V workflowBuilder) {
        this.workflowBuilder = workflowBuilder;
    }

    public <T> ICallableTaskFuture<T> get(ITaskFactory<V, T> factory) {

        // type safe cast as the map key is defined with the specific type of the factory
        // this guarantees the task instance will return a future of that type
        @SuppressWarnings("unchecked")
        TaskInstance<T, V> taskInstance = (TaskInstance<T, V>) this.tasks.get(factory.getClass().getCanonicalName());

        if (null == taskInstance) {
            taskInstance = new TaskInstance<>(factory);
            this.tasks.put(factory.getClass().getCanonicalName(), taskInstance);
        }
        return taskInstance.get(this.workflowBuilder);
    }

}
