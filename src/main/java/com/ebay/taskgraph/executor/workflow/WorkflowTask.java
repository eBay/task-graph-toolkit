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

import com.ebay.taskgraph.context.ResponseContext;
import com.ebay.taskgraph.executor.CallableTaskConfig;
import com.ebay.taskgraph.executor.ICallableTask;
import com.ebay.taskgraph.executor.Task;

/**
 * Task that creates a workflow, calls the workflow executor and then collects the response contexts of tasks executed.
 */
public class WorkflowTask<T> extends Task implements ICallableTask<T> {

    private final IWorkflowFactory workflowFactory;
    private final IWorkflowExecutor<T> executor;

    public WorkflowTask(CallableTaskConfig config, IWorkflowFactory workflowFactory, IWorkflowExecutor<T> task) {
        this(task.getClass().getSimpleName(), config, workflowFactory, task);
    }

    public WorkflowTask(CallableTaskConfig config, ResponseContext rc, IWorkflowFactory workflowFactory, IWorkflowExecutor<T> executor) {
        this(rc, config, workflowFactory, executor);
    }

    public WorkflowTask(String taskName, CallableTaskConfig config, IWorkflowFactory workflowFactory, IWorkflowExecutor<T> executor) {
        this(new ResponseContext(config.diagnosticConfig, taskName), config, workflowFactory, executor);
    }

    public WorkflowTask(ResponseContext rc, CallableTaskConfig config, IWorkflowFactory workflowFactory, IWorkflowExecutor<T> executor) {
        super(rc, config);
        this.workflowFactory = workflowFactory;
        this.executor = executor;
    }

    /**
     * Execute the workflow and collect response contexts of the tasks added to the workflow during execution.
     */
    @Override
    public T call() {
        IWorkflow<T> workflow = this.workflowFactory.create(this);
        try {
            return this.executor.execute(workflow);
        } finally {
            workflow.collectResponseContext();
        }
    }

}
