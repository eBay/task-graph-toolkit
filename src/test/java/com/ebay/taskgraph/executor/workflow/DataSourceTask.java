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

import javax.ws.rs.core.Response.Status;

import com.ebay.taskgraph.executor.ApplicationException;
import com.ebay.taskgraph.executor.CallableTaskConfig;
import com.ebay.taskgraph.executor.ICallableTask;
import com.ebay.taskgraph.executor.Task;

public class DataSourceTask extends Task implements ICallableTask<String> {

    private final String response;
    private final long sleep;

    public DataSourceTask(String name, CallableTaskConfig config, String response, long sleep) {
        super(name, config);
        this.response = response;
        this.sleep = sleep;
    }

    @Override
    public String call() throws Exception {

        if (-1L == this.sleep) {
            // non fatal exception
            throw new RuntimeException();
        }
        if (-2L == this.sleep) {
            // fatal exception
            throw new ApplicationException(Status.INTERNAL_SERVER_ERROR, response);
        }
        Thread.sleep(this.sleep);
        return this.response;
    }
    
    @Override
    public void waitForDependencies() {

        if (-3L == this.sleep) {
            throw new ApplicationException(Status.INTERNAL_SERVER_ERROR, response);
        }
    }

}
