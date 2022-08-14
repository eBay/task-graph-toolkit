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
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.ebay.ap.diagnostic.DiagnosticConfig;
import com.ebay.ap.executor.CallableTaskConfig;

import junit.framework.Assert;

import com.ebay.ap.executor.CallableTaskFutureValue;
import com.ebay.ap.executor.ICallableTaskFuture;
import com.ebay.ap.executor.ProfilerHelper;

public class VisitorsTest {

    private static final DiagnosticConfig DIAGNOSTIC_CONFIG = new DiagnosticConfig(false, false, true);
    private static final CallableTaskConfig SYNC_TASK = CallableTaskConfig.synch(DIAGNOSTIC_CONFIG);

    @Test
    public void singleVisitorTest() throws Exception {
        runTest(new SingleVisitorTest());
    }

    private static class SingleVisitorTest implements IWorkflowExecutor<SampleVisitorResponse> {

        @Override
        public SampleVisitorResponse execute(IWorkflow<SampleVisitorResponse> workflow) {
            SampleVisitorResponse visitorResponse = new SampleVisitorResponse();
            visitorResponse.setResponses(new ArrayList<String>());
            
            VisitorTask<SampleVisitorResponse> visitor = new SimpleResponseVisitor(SYNC_TASK);
            visitorResponse = VisitorWorkflowTaskFactory.create(SYNC_TASK, workflow, "VisitorTask", visitorResponse, visitor).getNoThrow(workflow.getTask());
            return visitorResponse;
        }
        
    }

    @Test
    public void singleVisitorWithTaskTest() throws Exception {
        runTest(new SingleVisitorWithTaskTest());
    }

    private static class SingleVisitorWithTaskTest implements IWorkflowExecutor<SampleVisitorResponse> {

        @Override
        public SampleVisitorResponse execute(IWorkflow<SampleVisitorResponse> workflow) {
            SampleVisitorResponse visitorResponse = new SampleVisitorResponse();
            visitorResponse.setResponses(new ArrayList<String>());
            ICallableTaskFuture<SampleVisitorResponse> visitorResponseFuture = new CallableTaskFutureValue<>(visitorResponse);
            
            VisitorTask<SampleVisitorResponse> visitor = new SimpleResponseVisitor(SYNC_TASK);
            visitorResponse = VisitorWorkflowTaskFactory.create(SYNC_TASK, workflow, "VisitorTask", visitorResponseFuture, Collections.singletonList(visitor)).getNoThrow(workflow.getTask());
            return visitorResponse;
        }
        
    }

    @Test
    public void multiVisitorTest() throws Exception {
        runTest(new MultiVisitorTest());
    }
    
    private static class MultiVisitorTest implements IWorkflowExecutor<SampleVisitorResponse> {

        @Override
        public SampleVisitorResponse execute(IWorkflow<SampleVisitorResponse> workflow) {
            SampleVisitorResponse visitorResponse = new SampleVisitorResponse();
            visitorResponse.setResponses(new ArrayList<String>());
            final List<SampleVisitorResponse> visitorResponses = Collections.singletonList(visitorResponse);
            IVisiteeProvider<SampleVisitorResponse> visiteeProvider = new IVisiteeProvider<SampleVisitorResponse>() {

                @Override
                public List<SampleVisitorResponse> get() {
                    return visitorResponses;
                }
                
            };
            
            VisitorTask<SampleVisitorResponse> visitor = new SimpleResponseVisitor(SYNC_TASK);
            VisitorWorkflowTaskFactory.createMultiVisitee(SYNC_TASK, workflow, "VisitorTask", visiteeProvider, Collections.singletonList(visitor)).getNoThrow(workflow.getTask());
            return visitorResponse;
        }
        
    }

    @Test
    public void multiVisitorWithTaskTest() throws Exception {
        runTest(new MultiVisitorWithTaskTest());
    }

    private static class MultiVisitorWithTaskTest implements IWorkflowExecutor<SampleVisitorResponse> {

        @Override
        public SampleVisitorResponse execute(IWorkflow<SampleVisitorResponse> workflow) {
            SampleVisitorResponse visitorResponse = new SampleVisitorResponse();
            visitorResponse.setResponses(new ArrayList<String>());
            final List<SampleVisitorResponse> visitorResponses = Collections.singletonList(visitorResponse);
            ICallableTaskFuture<List<SampleVisitorResponse>> visiteeProvider = new CallableTaskFutureValue<>(visitorResponses);

            VisitorTask<SampleVisitorResponse> visitor = new SimpleResponseVisitor(SYNC_TASK);
            VisitorWorkflowTaskFactory.createMultiVisitee(SYNC_TASK, workflow, "VisitorTask", visiteeProvider, Collections.singletonList(visitor)).getNoThrow(workflow.getTask());
            return visitorResponse;
        }
        
    }

    private void runTest(IWorkflowExecutor<SampleVisitorResponse> executor) throws Exception {
        WorkflowTask<SampleVisitorResponse> task = new WorkflowTask<>(CallableTaskConfig.simple(DIAGNOSTIC_CONFIG), TestWorkflowFactory.INSTANCE, executor);
        SampleVisitorResponse visitorResponse = task.call();
        Assert.assertEquals("true", visitorResponse.getResponses().get(0));
        ProfilerHelper.print(task.getContext());
    }

}
