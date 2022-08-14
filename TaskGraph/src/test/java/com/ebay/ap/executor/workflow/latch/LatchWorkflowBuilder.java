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

package com.ebay.ap.executor.workflow.latch;

import com.ebay.ap.executor.CallableTaskConfig;
import com.ebay.ap.executor.ICallableTask;
import com.ebay.ap.executor.ICallableTaskFuture;
import com.ebay.ap.executor.workflow.IWorkflow;
import com.ebay.ap.executor.workflow.IWorkflowBuilder;
import com.ebay.ap.executor.workflow.SampleOrchestrationRequest;
import com.ebay.ap.executor.workflow.SampleOrchestrationResponse;
import com.ebay.ap.executor.workflow.TaskInstance;

public class LatchWorkflowBuilder implements IWorkflowBuilder {

    public final SampleOrchestrationRequest request;

    public final IWorkflow<?> workflow;
    public final CallableTaskConfig async;
    public final CallableTaskConfig sync;

    // countdown latch will be the trigger for the response task
    SingleCountDownLatch<String> done = new SingleCountDownLatch<>();

    public final TaskInstance<String, LatchWorkflowBuilder> task1Instance =
            new TaskInstance<>(new SingleCountdownLatchTaskFactory<>(done, new DataSourceFactory1()));

    public final TaskInstance<String, LatchWorkflowBuilder> task2Instance =
            new TaskInstance<>(new SingleCountdownLatchTaskFactory<>(done, new DataSourceFactory2()));

    public LatchWorkflowBuilder(IWorkflow<?> workflow, SampleOrchestrationRequest request) {
        this.request = request;
        this.workflow = workflow;
        this.async = new CallableTaskConfig(workflow.getTask().getContext().getDiagnosticConfig(), 100L);
        this.sync = CallableTaskConfig.synch(workflow.getTask().getContext().getDiagnosticConfig());
    }

    public ICallableTaskFuture<SampleOrchestrationResponse> build() {
        return new LatchTestResponseBuilderTask.Factory().create(this);
    }

    @Override
    public <T> ICallableTaskFuture<T> addTask(ICallableTask<T> task) {
        return this.workflow.addTask(task);
    }

}
