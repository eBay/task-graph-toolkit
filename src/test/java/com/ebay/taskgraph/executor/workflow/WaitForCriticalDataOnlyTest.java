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

import com.ebay.taskgraph.executor.ApplicationException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test to show how to wait only for a critical piece of data and
 * only include optional data if the provider task is completed in the same time
 * frame as the critical data provider task.
 */
public class WaitForCriticalDataOnlyTest {

    @Test
    public void optionalReady() {
        WaitForCriticalDataOnlyTest.optionalReady(TestWorkflowFactory.INSTANCE);
    }

    @Test
    public void optionalNotReady() {
        WaitForCriticalDataOnlyTest.optionalNotReady(TestWorkflowFactory.INSTANCE);
    }

    @Test
    public void noOptional() {
        WaitForCriticalDataOnlyTest.noOptional(TestWorkflowFactory.INSTANCE);
    }

    @Test
    public void optionalException() {
        WaitForCriticalDataOnlyTest.optionalException(TestWorkflowFactory.INSTANCE);
    }

    @Test
    public void optionalFatalException() {
        WaitForCriticalDataOnlyTest.optionalFatalException(TestWorkflowFactory.INSTANCE);
    }

    @Test
    public void criticalException() {
        WaitForCriticalDataOnlyTest.criticalException(TestWorkflowFactory.INSTANCE);
    }

    @Test
    public void criticalFatalException() {
        WaitForCriticalDataOnlyTest.criticalFatalException(TestWorkflowFactory.INSTANCE);
    }
    
    @Test
    public void criticalFatalExceptionRaceCondtion() {
        WaitForCriticalDataOnlyTest.criticalFatalExceptionRaceCondtion(TestWorkflowFactory.INSTANCE);
    }
    
    @Test
    public void criticalFatalExceptionSyncTask() {
        WaitForCriticalDataOnlyTest.criticalFatalExceptionSyncTask(TestWorkflowFactory.INSTANCE);
    }

    public static void optionalReady(IWorkflowFactory workflowFactory) {

        // in this case optional data will return in 50ms so will be included
        SampleOrchestrationRequest request = new SampleOrchestrationRequest(100L, 50L);
        SampleOrchestrationResponse response = runTest(request, workflowFactory, "optionalReady");
        Assert.assertEquals("critical", response.getCriticalData());
        Assert.assertEquals("optional", response.getOptionalData());
        Assert.assertNotNull(response.getVisitorResponse());
        Assert.assertEquals(2, response.getVisitorResponse().getResponses().size());
        Assert.assertEquals("critical_visited", response.getVisitorResponse().getResponses().get(0));
        Assert.assertEquals("optional_visited", response.getVisitorResponse().getResponses().get(1));
    }

    public static void optionalNotReady(IWorkflowFactory workflowFactory) {

        SampleOrchestrationRequest request = new SampleOrchestrationRequest(50L, 100L);
        SampleOrchestrationResponse response = runTest(request, workflowFactory, "optionalNotReady");
        Assert.assertEquals("critical", response.getCriticalData());
        Assert.assertNull(response.getOptionalData());
        Assert.assertNotNull(response.getVisitorResponse());
        Assert.assertEquals(1, response.getVisitorResponse().getResponses().size());
        Assert.assertEquals("critical_visited", response.getVisitorResponse().getResponses().get(0));
    }

    public static void noOptional(IWorkflowFactory workflowFactory) {

        SampleOrchestrationRequest request = new SampleOrchestrationRequest(50L, 100L, false);
        SampleOrchestrationResponse response = runTest(request, workflowFactory, "noOptional");
        Assert.assertEquals("critical", response.getCriticalData());
        Assert.assertNull(response.getOptionalData());
        Assert.assertNotNull(response.getVisitorResponse());
        Assert.assertEquals(1, response.getVisitorResponse().getResponses().size());
        Assert.assertEquals("critical_visited", response.getVisitorResponse().getResponses().get(0));
    }

    public static void optionalException(IWorkflowFactory workflowFactory) {

        SampleOrchestrationRequest request = new SampleOrchestrationRequest(50L, -1L);
        SampleOrchestrationResponse response = runTest(request, workflowFactory, "optionalException");
        Assert.assertEquals("critical", response.getCriticalData());
        Assert.assertNull(response.getOptionalData());
    }

    public static void optionalFatalException(IWorkflowFactory workflowFactory) {

        try {
            // simulate fatal exception in optional task
            // even though it's optional, this type of exception should fail the whole workflow
            // this is so that unit test validations will propagate in this case
            SampleOrchestrationRequest request = new SampleOrchestrationRequest(50L, -2L);
            runTest(request, workflowFactory, "optionalFatalException");
            Assert.assertTrue(false);
        } catch (ApplicationException e) {
            Assert.assertTrue(true);
        }
    }

    public static void criticalException(IWorkflowFactory workflowFactory) {

        SampleOrchestrationRequest request = new SampleOrchestrationRequest(-1L, 50L);
        SampleOrchestrationResponse response = runTest(request, workflowFactory, "criticalException");
        Assert.assertNull(response.getCriticalData());
        Assert.assertNull(response.getOptionalData());
    }

    public static void criticalFatalException(IWorkflowFactory workflowFactory) {

        try {
            // simulate fatal exception in critical task
            SampleOrchestrationRequest request = new SampleOrchestrationRequest(-2L, 50L);
            runTest(request, workflowFactory, "criticalFatalException");
            Assert.assertTrue(false);
        } catch (ApplicationException e) {
            Assert.assertTrue(true);
        }
    }
    
    private static class CriticalFatalExceptionRaceCondtion implements IWorkflowExecutor<SampleOrchestrationResponse> {

        @Override
        public SampleOrchestrationResponse execute(IWorkflow<SampleOrchestrationResponse> workflow) {
            SampleOrchestrationRequest request = new SampleOrchestrationRequest(-2L, 50L);
            WaitForCriticalDataOnlyBuilder builder = new WaitForCriticalDataOnlyBuilder(workflow, request);
            try {
                builder.getTask(DataSourceFactory.Factory1.INSTANCE).getNoThrow(workflow.getTask());
            } catch (ApplicationException e) {
                // simulate the exception pending in the workflow
            }
            // this should also throw ApplicationException
            return new WaitOnlyForCriticalTask.Factory().create(builder).getNoThrow(workflow.getTask());
        }
        
    }

    public static void criticalFatalExceptionRaceCondtion(IWorkflowFactory workflowFactory) {
        runExpectedApplicationException(new CriticalFatalExceptionRaceCondtion(), workflowFactory, "criticalFatalExceptionRaceCondtion");
    }

    private static void runExpectedApplicationException(IWorkflowExecutor<SampleOrchestrationResponse> executor, IWorkflowFactory workflowFactory, String name) {
        try {
            WorkflowTestRunner.runTest(executor, workflowFactory, name);
            Assert.assertTrue(false);
        } catch (ApplicationException e) {
            Assert.assertTrue(true);
        }
    }
    
    private static class CriticalFatalExceptionSyncTask implements IWorkflowExecutor<SampleOrchestrationResponse> {

        @Override
        public SampleOrchestrationResponse execute(IWorkflow<SampleOrchestrationResponse> workflow) {
            SampleOrchestrationRequest request = new SampleOrchestrationRequest(100L, 50L, true, "throw");
            WaitForCriticalDataOnlyBuilder builder = new WaitForCriticalDataOnlyBuilder(workflow, request);
            SampleOrchestrationResponse response = null;
            try {
                response = new WaitOnlyForCriticalTask.Factory().create(builder).getNoThrow(workflow.getTask());
            } catch (ApplicationException e) {
                // simulate the exception pending in the synchronous response visitor workflow task
            }
            // this should also throw ApplicationException but will try and execute the synchronous task again
            // verifies that synchronous task isn't executed 2X because profiler validation will fail with duplicate entries
            builder.getTask(SampleResponseVisitorFactory.INSTANCE).getNoThrow(workflow.getTask());
            return response;
        }
        
    }

    public static void criticalFatalExceptionSyncTask(IWorkflowFactory workflowFactory) {
        runExpectedApplicationException(new CriticalFatalExceptionSyncTask(), workflowFactory, "criticalFatalExceptionSyncTask");
    }

    private static SampleOrchestrationResponse runTest(SampleOrchestrationRequest request, IWorkflowFactory workflowFactory, String name) {
        WaitForCriticalDataOnlyExecutor executor = new WaitForCriticalDataOnlyExecutor(request);
        return WorkflowTestRunner.runTest(executor, workflowFactory, name);
    }

}
