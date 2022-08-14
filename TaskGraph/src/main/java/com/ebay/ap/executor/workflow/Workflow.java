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

import com.ebay.ap.context.ResponseContext;
import com.ebay.ap.executor.ICallableTask;
import com.ebay.ap.executor.ICallableTaskExecutor;
import com.ebay.ap.executor.ICallableTaskFuture;

public class Workflow<V> implements IWorkflow<V> {

    /**
     * Task that owns the workflow.
     */
    private final ICallableTask<V> task;

    /**
     * Factory for creating nested workflows.
     */
    private IWorkflowFactory workflowFactory;

    /**
     * Executor keeping track of tasks added to the workflow.
     */
    private final ICallableTaskExecutor executor;

    /**
     * Life cycle of a workflow should be managed by a task.
     * Once the owning task is complete, it should be safe to collect response contexts of all the tasks belonging to the workflow.
     */
    public Workflow(ICallableTask<V> task, IWorkflowFactory workflowFactory, ICallableTaskExecutor executor) {
        this.task = task;
        this.workflowFactory = workflowFactory;
        this.executor = executor;
        this.task.getContext().getProfiler().start();
    }

    @Override
    public ICallableTask<V> getTask() {
        return this.task;
    }

    @Override
    public <T> ICallableTaskFuture<T> addTask(ICallableTask<T> task) {
        return this.executor.addTask(task);
    }

    /**
     * Add ResponseContexts of the tasks that make up the workflow to the parent task's context and return that.
     */
    @Override
    public ResponseContext collectResponseContext() {
        this.executor.collectResponseContext(this.task);
        ResponseContext aggregateContext = this.task.getContext();
        aggregateContext.getProfiler().stop();
        return aggregateContext;
    }

    /**
     * Create a new instance of the same type of workflow.
     */
    @Override
    public <T> IWorkflow<T> create(ICallableTask<T> task) {
        return this.workflowFactory.create(task);
    }

}
