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

import com.ebay.taskgraph.executor.ApplicationException;
import com.ebay.taskgraph.executor.workflow.IWorkflow;
import com.ebay.taskgraph.executor.workflow.IWorkflowExecutor;
import com.ebay.taskgraph.executor.workflow.IWorkflowFactory;
import junit.framework.Assert;

import org.junit.Test;

import com.ebay.taskgraph.executor.workflow.SampleOrchestrationRequest;
import com.ebay.taskgraph.executor.workflow.SampleOrchestrationResponse;
import com.ebay.taskgraph.executor.workflow.TestWorkflowFactory;
import com.ebay.taskgraph.executor.workflow.WorkflowTestRunner;

/**
 * Test illustrating a pattern for blocking on 2 tasks but unblocking as soon as
 * one of the tasks completes.
 */
public class WaitForFastestTest {

    @Test
    public void firstFastest() {
        WaitForFastestTest.firstFastest(TestWorkflowFactory.INSTANCE);
    }
    
    @Test
    public void secondFastest() {
        WaitForFastestTest.secondFastest(TestWorkflowFactory.INSTANCE);
    }

    @Test
    public void firstException() {
        WaitForFastestTest.firstException(TestWorkflowFactory.INSTANCE);
    }

    @Test
    public void firstFatalException() {
        WaitForFastestTest.firstFatalException(TestWorkflowFactory.INSTANCE);
    }

    @Test
    public void dependencyFatalException() {
        WaitForFastestTest.dependencyFatalException(TestWorkflowFactory.INSTANCE);
    }

    @Test
    public void bothException() {
        WaitForFastestTest.bothException(TestWorkflowFactory.INSTANCE);
    }

    private static SampleOrchestrationResponse waitForFastest(
          IWorkflowFactory workflowFactory, long firstTime, long secondTime, String name) {
        
        // illustrates orchestration of a task waiting for one of two tasks to finish
        // whichever finishes first, its data will be included in the critical data response
        WaitForFastestExecutor executor = new WaitForFastestExecutor(firstTime, secondTime);
        return WorkflowTestRunner.runTest(executor, workflowFactory, name);
    }

    private static class WaitForFastestExecutor implements IWorkflowExecutor<SampleOrchestrationResponse> {

        private final long firstTime;
        private final long secondTime;

        protected WaitForFastestExecutor(long firstTime, long secondTime) {
            this.firstTime = firstTime;
            this.secondTime = secondTime;
        }

        @Override
        public SampleOrchestrationResponse execute(IWorkflow<SampleOrchestrationResponse> workflow) {
            LatchWorkflowBuilder builder = new LatchWorkflowBuilder(
                    workflow, new SampleOrchestrationRequest(firstTime, secondTime));
            return builder.build().getNoThrow(workflow.getTask());
        }
        
    }

    public static void firstFastest(IWorkflowFactory workflowFactory) {
        
        SampleOrchestrationResponse response = waitForFastest(workflowFactory,50L, 80L, "firstFastest");
        Assert.assertEquals("firstResponse", response.getCriticalData());
    }
    
    public static void secondFastest(IWorkflowFactory workflowFactory) {
        
        SampleOrchestrationResponse response = waitForFastest(workflowFactory,80L, 50L, "secondFastest");
        Assert.assertEquals("secondResponse", response.getCriticalData());
    }

    public static void firstException(IWorkflowFactory workflowFactory) {
        
        SampleOrchestrationResponse response = waitForFastest(workflowFactory,-1L, 50L, "firstException");
        Assert.assertEquals("secondResponse", response.getCriticalData());
    }

    public static void firstFatalException(IWorkflowFactory workflowFactory) {
        
        try {
            waitForFastest(workflowFactory,-2L, 50L, "firstFatalException");
            Assert.assertTrue(false);
        } catch (ApplicationException e) {
            Assert.assertTrue(true);
        }
    }

    public static void dependencyFatalException(IWorkflowFactory workflowFactory) {
        
        try {
            waitForFastest(workflowFactory,-2L, 50L, "dependencyFatalException");
            Assert.assertTrue(false);
        } catch (ApplicationException e) {
            Assert.assertTrue(true);
        }
    }

    public static void bothException(IWorkflowFactory workflowFactory) {
  
        // this will time out on the latch, so make the workflow time short
        SampleOrchestrationResponse response = waitForFastest(workflowFactory, -1L, -1L, "bothException");
        Assert.assertEquals(null, response.getCriticalData());
    }
}
