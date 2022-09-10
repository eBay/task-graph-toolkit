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
import com.ebay.taskgraph.executor.ICallableTaskFuture;

public class CriticalResponseVisitor extends VisitorTask<SampleVisitorResponse> {

    private final ICallableTaskFuture<String> sourceData;
    private final ICallableTaskFuture<String> visitedData;

    public CriticalResponseVisitor(CallableTaskConfig taskConfig, ICallableTaskFuture<String> sourceData, ICallableTaskFuture<String> visitedData) {
        super(taskConfig, sourceData, visitedData);
        this.sourceData = sourceData;
        this.visitedData = visitedData; 
    }

    @Override
    public void visit(SampleVisitorResponse response) {
        String data = this.sourceData.getNoThrow(this);
        if (data != null) {
            if ("throw".equals(data)) {
                throw new ApplicationException(Status.INTERNAL_SERVER_ERROR, data);
            }
            String visitedData = this.visitedData.getNoThrow(this);
            if (visitedData != null) {
                response.getResponses().add(data + "_" + visitedData);
            }
        }
    }

}
