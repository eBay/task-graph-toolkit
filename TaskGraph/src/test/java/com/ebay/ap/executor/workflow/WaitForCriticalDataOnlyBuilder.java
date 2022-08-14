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

import java.util.ArrayList;
import java.util.List;

import com.ebay.ap.executor.CallableTaskConfig;
import com.ebay.ap.executor.ICallableTask;
import com.ebay.ap.executor.ICallableTaskFuture;

public class WaitForCriticalDataOnlyBuilder implements IWorkflowBuilder {

    private static final long TIMEOUT = 100000L;

    public final SampleOrchestrationRequest request;

    public final IWorkflow<SampleOrchestrationResponse> workflow;

    public final CallableTaskConfig async;

    public final CallableTaskConfig sync;
    
    public final List<VisitorTask<SampleVisitorResponse>> responseVisitors = new ArrayList<>();

    private final TaskInstanceHolder<WaitForCriticalDataOnlyBuilder> tasks;


    public WaitForCriticalDataOnlyBuilder(IWorkflow<SampleOrchestrationResponse> workflow, SampleOrchestrationRequest request) {
        this.request = request;
        this.workflow = workflow;
        this.tasks = new TaskInstanceHolder<>(this);
        this.async = new CallableTaskConfig(workflow.getTask().getContext().getDiagnosticConfig(), TIMEOUT);
        this.sync = CallableTaskConfig.synch(workflow.getTask().getContext().getDiagnosticConfig());
    }

    @Override
    public <T> ICallableTaskFuture<T> addTask(ICallableTask<T> task) {
        return this.workflow.addTask(task);
    }

    public <T> ICallableTaskFuture<T> getTask(ITaskFactory<WaitForCriticalDataOnlyBuilder, T> taskFactory) {
      return this.tasks.get(taskFactory);
    }

}
