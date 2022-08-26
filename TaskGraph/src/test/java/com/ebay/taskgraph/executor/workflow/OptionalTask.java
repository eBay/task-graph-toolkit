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

// task that blocks on critical data task
public class OptionalTask extends Task implements ICallableTask<String> {

    private final ICallableTaskFuture<String> critical;
    private final String response;
    private final long sleep;

    protected OptionalTask(CallableTaskConfig config,
                           ICallableTaskFuture<String> criticalTask,
                           String response,
                           long sleep) {
        super("OptionalTask_" + response, config, criticalTask);
        this.critical = criticalTask;
        this.response = response;
        this.sleep = sleep;
    }

    @Override
    public String call() throws Exception {
        
        this.critical.getNoThrow(this);
        Thread.sleep(this.sleep);
        return this.response;
    }

}
