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
import com.ebay.taskgraph.executor.ICallableTaskFuture;

public class OptionalResponseVisitor extends VisitorTask<SampleVisitorResponse> {

    private final ICallableTaskFuture<String> sourceData;
    private final ICallableTaskFuture<String> visitedData;

    public OptionalResponseVisitor(CallableTaskConfig taskConfig, ICallableTaskFuture<String> sourceData, ICallableTaskFuture<String> visitedData) {
        // don't pass the optional data task to the super class because the profile decorator will block for it
        super(taskConfig, visitedData);
        this.sourceData = sourceData;
        this.visitedData = visitedData; 
    }

    @Override
    public void visit(SampleVisitorResponse response) {
        if (this.sourceData.isDone()) {
            String data = this.sourceData.getNoThrow(this);
            if (data != null) {
                String visitedData = this.visitedData.getNoThrow(this);
                if (visitedData != null) {
                    response.getResponses().add(data + "_" + visitedData);
                }
            }
        }
    }

}
