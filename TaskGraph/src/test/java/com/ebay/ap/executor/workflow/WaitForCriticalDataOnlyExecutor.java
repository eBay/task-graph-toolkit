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

// illustrates orchestration of a task waiting only long enough for its critical data
// if optional data arrives before critical data, include that as well
public class WaitForCriticalDataOnlyExecutor implements IWorkflowExecutor<SampleOrchestrationResponse> {

    public final SampleOrchestrationRequest request;

    public WaitForCriticalDataOnlyExecutor(SampleOrchestrationRequest request) {
        this.request = request;
    }

    @Override
    public SampleOrchestrationResponse execute(IWorkflow<SampleOrchestrationResponse> workflow) {
        WaitForCriticalDataOnlyBuilder builder = new WaitForCriticalDataOnlyBuilder(workflow, this.request);
        return builder.getTask(WaitOnlyForCriticalTask.Factory.INSTANCE).getNoThrow(workflow.getTask());
    }

}
