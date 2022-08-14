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

import com.ebay.ap.executor.CallableTaskResultNull;
import com.ebay.ap.executor.ICallableTaskFuture;

public class SampleResponseVisitorFactory implements ITaskFactory<WaitForCriticalDataOnlyBuilder, SampleVisitorResponse> {

    public static final SampleResponseVisitorFactory INSTANCE = new SampleResponseVisitorFactory();

    private static final ICallableTaskFuture<SampleVisitorResponse> NULL = new CallableTaskResultNull<>();

    @Override
    public ICallableTaskFuture<SampleVisitorResponse> create(WaitForCriticalDataOnlyBuilder builder) {

        ICallableTaskFuture<SampleVisitorResponse> taskFuture = NULL;
        SampleVisitorResponse visitorResponse = new SampleVisitorResponse();
        visitorResponse.setResponses(new ArrayList<String>());
        taskFuture = VisitorWorkflowTaskFactory.create(builder.sync, builder.workflow, "ResponseVisitorTask", visitorResponse, builder.responseVisitors);
        return taskFuture;
    }

}
