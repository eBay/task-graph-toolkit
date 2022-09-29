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

package com.ebay.taskgraph.executor.workflow;

import com.ebay.taskgraph.executor.ICallableTaskFuture;

/**
 * Caches an instance of a particular task in a workflow.
 * This allows multiple downstream dependencies to reference a single instance.
 * Also enables transitive dependencies to be wired up recursively.
 * 
 * Not threaad safe.  Generally workflows should be created synchronously based
 * on application request and configuration.  If a particular task requires 
 * extra orchestration based on its execution time dependencies, it can create
 * its own workflow synchronously when its call() method is invoked.
 */
//@NotThreadSafe
public class TaskInstance<V, S extends IWorkflowBuilder> {

    /**
     * Use a separate flag for determining if the instance has been created or not.
     * This prevents the factory logic being repeated in the case where the instance should be null.
     */
    private boolean initialized = false;
    
    /**
     * Another flag to detect cyclic dependencies.
     */
    private boolean initializing = false;

    private ICallableTaskFuture<V> instance;

    private final ITaskFactory<S, V> factory;

    public TaskInstance(ITaskFactory<S, V> factory) {
        this.factory = factory;
    }

    public ICallableTaskFuture<V> get(S builder) {
        if (!this.initialized) {
            if (this.initializing) {
                throw new WorkflowException("Cycle detected on task factory: " + this.factory.getClass().getCanonicalName());
            }
            this.initializing = true;
            this.instance = this.factory.create(builder);
            this.initialized = true;
        }
        return this.instance;
    }
}
