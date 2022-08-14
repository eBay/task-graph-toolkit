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

import com.ebay.ap.diagnostic.IProfilerEntry;
import com.ebay.ap.executor.CallableTaskConfig;
import com.ebay.ap.executor.ICallableTask;
import com.ebay.ap.executor.ICallableTaskFuture;
import com.ebay.ap.executor.Task;

public class WaitOnlyForCriticalTask extends Task implements ICallableTask<SampleOrchestrationResponse> {

    public static class Factory implements ITaskFactory<WaitForCriticalDataOnlyBuilder, SampleOrchestrationResponse> {

        public static final ITaskFactory<WaitForCriticalDataOnlyBuilder, SampleOrchestrationResponse> INSTANCE = new Factory();

        @Override
        public ICallableTaskFuture<SampleOrchestrationResponse> create(WaitForCriticalDataOnlyBuilder builder) {

            ICallableTaskFuture<String> criticalTask = builder.getTask(DataSourceFactory.Factory1.INSTANCE);
            ICallableTaskFuture<String> optionalTask = builder.getTask(DataSourceFactory.Factory2.INSTANCE);
            ICallableTaskFuture<SampleVisitorResponse> responseVisitorTask = builder.getTask(SampleResponseVisitorFactory.INSTANCE);
            
            WaitOnlyForCriticalTask task = new WaitOnlyForCriticalTask(builder.sync,
                    criticalTask,
                    optionalTask,
                    responseVisitorTask);
            
            return builder.addTask(task);
        }

    }

    private final ICallableTaskFuture<String> critical;
    private final ICallableTaskFuture<String> optional;
    private final ICallableTaskFuture<SampleVisitorResponse> responseVisitorTask;

    public WaitOnlyForCriticalTask(CallableTaskConfig config,
            ICallableTaskFuture<String> critical,
            ICallableTaskFuture<String> optional,
            ICallableTaskFuture<SampleVisitorResponse> responseVisitorTask) {
        super(config, critical, optional, responseVisitorTask);
        this.critical = critical;
        this.optional = optional;
        this.responseVisitorTask = responseVisitorTask;
    }

    @Override
    public SampleOrchestrationResponse call() throws Exception {
        
        SampleOrchestrationResponse rval = new SampleOrchestrationResponse();
        IProfilerEntry pe = this.getContext().getProfiler().newEntry("get_critical");
        rval.setCriticalData(this.critical.getNoThrow(this));
        this.getContext().getProfiler().add(pe);
        
        // if optional task is done, get its result
        pe = this.getContext().getProfiler().newEntry("get_optional");
        if (this.optional.isDone()) {
            rval.setOptionalData(this.optional.getNoThrow(this));
        }
        this.getContext().getProfiler().add(pe);
        
        pe = this.getContext().getProfiler().newEntry("response_visitor");
        rval.setVisitorResponse(this.responseVisitorTask.getNoThrow(this));
        this.getContext().getProfiler().add(pe);

        pe = this.getContext().getProfiler().newEntry("task_processing");
        Thread.sleep(40L); // pretend this task is doing something with the result
        this.getContext().getProfiler().add(pe);
        return rval;
    }
    
    @Override
    public void waitForDependencies() {
        // only block for critical data
        this.critical.getNoThrow(this);
    }

}
