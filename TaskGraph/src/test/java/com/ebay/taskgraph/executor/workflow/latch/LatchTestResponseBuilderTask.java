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

package com.ebay.taskgraph.executor.workflow.latch;

import com.ebay.taskgraph.executor.CallableTaskConfig;
import com.ebay.taskgraph.executor.ICallableTask;
import com.ebay.taskgraph.executor.ICallableTaskFuture;
import com.ebay.taskgraph.executor.Task;
import com.ebay.taskgraph.executor.workflow.ITaskFactory;
import com.ebay.taskgraph.executor.workflow.SampleOrchestrationResponse;

public class LatchTestResponseBuilderTask extends Task implements ICallableTask<SampleOrchestrationResponse> {

    public static class Factory implements ITaskFactory<LatchWorkflowBuilder, SampleOrchestrationResponse> {

        @Override
        public ICallableTaskFuture<SampleOrchestrationResponse> create(LatchWorkflowBuilder builder) {

            // create the data fetching tasks
            builder.task1Instance.get(builder);
            builder.task2Instance.get(builder);

            ICallableTaskFuture<String> responseFuture = builder.addTask(
                    new WaitForSingleCountDownLatchTask<>(builder.async, builder.done)); 

            return builder.addTask(new LatchTestResponseBuilderTask(
                    builder.sync,
                    responseFuture));
        }

    }

    private final ICallableTaskFuture<String> responseFuture;

    public LatchTestResponseBuilderTask(CallableTaskConfig config, ICallableTaskFuture<String> responseFuture) {
        super(config, responseFuture);        
        this.responseFuture = responseFuture;
    }

    @Override
    public SampleOrchestrationResponse call() throws Exception {

        String response = responseFuture.getNoThrow(this);
        SampleOrchestrationResponse result = new SampleOrchestrationResponse();
        result.setCriticalData(response);
        // pretend there's some other processing required in this task to make the profiler view more readable
        Thread.sleep(20L);
        return result;
    }

}
