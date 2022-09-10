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

import com.ebay.taskgraph.executor.CallableTaskConfig;
import com.ebay.taskgraph.executor.ICallableTask;
import com.ebay.taskgraph.executor.ICallableTaskFuture;
import com.ebay.taskgraph.executor.Task;

public class OptionalTimeoutTask extends Task implements ICallableTask<SampleOrchestrationResponse> {
    
    private final ICallableTaskFuture<String> critical;
    private final ICallableTaskFuture<String> optional1;
    private final ICallableTaskFuture<String> optional2;
    private final long duration;

    protected OptionalTimeoutTask(CallableTaskConfig config,
                                  ICallableTaskFuture<String> critical,
                                  ICallableTaskFuture<String> optional1,
                                  ICallableTaskFuture<String> optional2,
                                  long duration) {
        super(config, optional2, optional1, critical);
        this.critical = critical;
        this.optional1 = optional1;
        this.optional2 = optional2;
        this.duration = duration;
    }

    @Override
    public SampleOrchestrationResponse call() throws Exception {
        
        SampleOrchestrationResponse rval = new SampleOrchestrationResponse();
        rval.setOptionalData(this.optional1.getNoThrow(this) + '_' + this.optional2.getNoThrow(this));
        rval.setCriticalData(this.critical.getNoThrow(this));

        Thread.sleep(this.duration); // pretend this task is doing something with the result
        return rval;
    }

}
