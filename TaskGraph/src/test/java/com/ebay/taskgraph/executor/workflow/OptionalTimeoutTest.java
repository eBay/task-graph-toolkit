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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ebay.taskgraph.diagnostic.DiagnosticConfig;

// test that an optional task inside the workflow times out without including the time waiting for its dependencies
public class OptionalTimeoutTest {
    
    private static final long OPTIONAL_TIMEOUT = 200L;
    private static final long OPTIONAL_DURATION_LESS_THAN_TIMEOUT = OPTIONAL_TIMEOUT / 2;
    private static final long OPTIONAL_DURATION_GREATER_THAN_TIMEOUT = OPTIONAL_TIMEOUT * 4;
    private static final long CRITICAL_TIME = OPTIONAL_TIMEOUT * 3 >> 1; // critical time is longer than optional timeout but shorter than task that times out
    private static final long CRITICAL_TIMEOUT = CRITICAL_TIME * 2;
    private static final DiagnosticConfig DIAG_CONFIG = new DiagnosticConfig(false, false, true);
    private static final long FINAL_RESPONSE_TASK_DURATION = OPTIONAL_TIMEOUT / 2;
    private static final long EXECUTION_BUFFER = OPTIONAL_TIMEOUT / 2;

    @BeforeClass
    public static void warmup() {
        // warmup execution so we can get more reliable timing
        OptionalTimeoutTest.warmup(TestWorkflowFactory.INSTANCE);
    }
    
    @Test
    public void optionalWithinTimeoutTest() {
        OptionalTimeoutTest.optionalWithinTimeoutTest(TestWorkflowFactory.INSTANCE);
    }
    
    @Test
    public void optionalWithinTimeoutSyncCriticalTest() {
        OptionalTimeoutTest.optionalWithinTimeoutSyncCriticalTest(TestWorkflowFactory.INSTANCE);
    }

    @Test
    public void optionalOverTimeoutTest() {
        OptionalTimeoutTest.optionalOverTimeoutTest(TestWorkflowFactory.INSTANCE);
    }

    // tests timing out inline execution of code or services with client libs that don't allow timeouts to be configured?
    @Test
    public void bothOptionalOverTimeoutTest() {
        OptionalTimeoutTest.bothOptionalOverTimeoutTest(TestWorkflowFactory.INSTANCE);
    }

    @Test
    public void optionalOverTimeoutTestSyncCriticalTest() {
        OptionalTimeoutTest.optionalOverTimeoutTestSyncCriticalTest(TestWorkflowFactory.INSTANCE);
    }


    public static void warmup(IWorkflowFactory workflowFactory) {
        OptionalTimeoutTest.optionalTimeoutTest(workflowFactory,OPTIONAL_DURATION_GREATER_THAN_TIMEOUT, "warmup");
    }

    private static SampleOrchestrationResponse optionalTimeoutTest(IWorkflowFactory workflowFactory, long optionalTime, String name) {
        CallableTaskConfig criticalConfig = new CallableTaskConfig(DIAG_CONFIG, CRITICAL_TIMEOUT);
        return optionalTimeoutTest(workflowFactory,optionalTime, OPTIONAL_DURATION_LESS_THAN_TIMEOUT, criticalConfig, name);
    }

    private static SampleOrchestrationResponse optionalTimeoutTest(IWorkflowFactory workflowFactory, long optionalTime1, long optionalTime2, CallableTaskConfig criticalConfig, String name) {
        return optionalTimeoutTest(DIAG_CONFIG, workflowFactory,optionalTime1, optionalTime2, FINAL_RESPONSE_TASK_DURATION, criticalConfig, CallableTaskConfig.synch(DIAG_CONFIG), name, true);
    }
    
    public static SampleOrchestrationResponse optionalTimeoutTest(
            DiagnosticConfig diagnosticConfig,
            IWorkflowFactory workflowFactory,
            long optionalTime1,
            long optionalTime2,
            long finalResponseTaskDuration,
            CallableTaskConfig criticalConfig,
            CallableTaskConfig optionalTaskConfig,
            String name,
            boolean printProfiler) {
        
        long time = System.currentTimeMillis();

        OptionalTimeoutTestExecutor executor = new OptionalTimeoutTestExecutor(
                optionalTime1,
                optionalTime2,
                finalResponseTaskDuration,
                criticalConfig);

        SampleOrchestrationResponse response = WorkflowTestRunner.runTest(executor, workflowFactory, name, printProfiler);
        
        time = System.currentTimeMillis() - time;

        if (null == response) {
            System.err.print("null response:");
        } else {
            response.setProcessingTime(time);
        }
        return response;
    }

    private static class OptionalTimeoutTestExecutor implements IWorkflowExecutor<SampleOrchestrationResponse> {

        final long optionalTime1;
        final long optionalTime2;
        final long finalResponseTaskDuration;
        final CallableTaskConfig criticalConfig;
        
        protected OptionalTimeoutTestExecutor(
                long optionalTime1,
                long optionalTime2,
                long finalResponseTaskDuration,
                CallableTaskConfig criticalConfig) {
            this.optionalTime1 = optionalTime1;
            this.optionalTime2 = optionalTime2;
            this.finalResponseTaskDuration = finalResponseTaskDuration;
            this.criticalConfig = criticalConfig;
        }

        @Override
        public SampleOrchestrationResponse execute(IWorkflow<SampleOrchestrationResponse> workflow) {
            DataSourceTask criticalTask = new DataSourceTask("critical", criticalConfig, "critical", CRITICAL_TIME);

            ICallableTaskFuture<String> criticalTaskFuture = workflow.addTask(criticalTask);

            CallableTaskConfig optionalConfig = new CallableTaskConfig(workflow.getTask().getContext().getDiagnosticConfig(), OPTIONAL_TIMEOUT, CallableTaskConfig.ExecType.ASYNC_TIMEOUT);
            OptionalTask optionalTask1 = new OptionalTask(optionalConfig, criticalTaskFuture, "optional1", optionalTime1);
            ICallableTaskFuture<String> optionalTaskFuture1 = workflow.addTask(optionalTask1);
            OptionalTask optionalTask2 = new OptionalTask(optionalConfig, criticalTaskFuture, "optional2", optionalTime2);
            ICallableTaskFuture<String> optionalTaskFuture2 = workflow.addTask(optionalTask2);

            CallableTaskConfig finalTaskConfig = new CallableTaskConfig(workflow.getTask().getContext().getDiagnosticConfig(), OPTIONAL_TIMEOUT + CRITICAL_TIME + finalResponseTaskDuration + 200);
            OptionalTimeoutTask responseBuilder = new OptionalTimeoutTask(finalTaskConfig,
                    criticalTaskFuture, optionalTaskFuture1, optionalTaskFuture2, finalResponseTaskDuration);
            SampleOrchestrationResponse response = workflow.addTask(responseBuilder).getNoThrow(workflow.getTask());

            return response;
        }
        
    }
    
    public static void optionalWithinTimeoutTest(IWorkflowFactory workflowFactory) {
        // will complete within task timeout
        SampleOrchestrationResponse response = optionalTimeoutTest(workflowFactory,OPTIONAL_DURATION_LESS_THAN_TIMEOUT, "optionalWithinTimeoutTest");

        Assert.assertEquals("critical", response.getCriticalData());
        Assert.assertEquals("optional1_optional2", response.getOptionalData());
    }
    
    public static void optionalWithinTimeoutSyncCriticalTest(IWorkflowFactory workflowFactory) {
        // will complete within task timeout
        SampleOrchestrationResponse response = optionalTimeoutTest(workflowFactory,OPTIONAL_DURATION_LESS_THAN_TIMEOUT, OPTIONAL_DURATION_LESS_THAN_TIMEOUT, CallableTaskConfig.synch(DIAG_CONFIG), "optionalWithinTimeoutSyncCriticalTest");

        Assert.assertEquals("critical", response.getCriticalData());
        Assert.assertEquals("optional1_optional2", response.getOptionalData());
    }

    public static void optionalOverTimeoutTest(IWorkflowFactory workflowFactory) {
        
        // won't complete within task timeout
        SampleOrchestrationResponse response = optionalTimeoutTest(workflowFactory,OPTIONAL_DURATION_GREATER_THAN_TIMEOUT, "optionalOverTimeoutTest");
        
        Assert.assertEquals("critical", response.getCriticalData());
        Assert.assertEquals("null_optional2", response.getOptionalData());

        long expectedTime = CRITICAL_TIME + OPTIONAL_TIMEOUT + FINAL_RESPONSE_TASK_DURATION + EXECUTION_BUFFER;        
        Assert.assertTrue("processTime:" + response.getProcessingTime() + ", expectedTime:" + expectedTime, response.getProcessingTime() < expectedTime);
    }

    // tests timing out inline execution of code or services with client libs that don't allow timeouts to be configured?
    public static void bothOptionalOverTimeoutTest(IWorkflowFactory workflowFactory) {
        
        // won't complete within task timeout
        SampleOrchestrationResponse response = optionalTimeoutTest(workflowFactory,OPTIONAL_DURATION_GREATER_THAN_TIMEOUT, OPTIONAL_DURATION_GREATER_THAN_TIMEOUT, CallableTaskConfig.synch(DIAG_CONFIG), "bothOptionalOverTimeoutTest");
                
        Assert.assertEquals("critical", response.getCriticalData());
        Assert.assertEquals("null_null", response.getOptionalData());

        long expectedTime = CRITICAL_TIME + OPTIONAL_TIMEOUT + FINAL_RESPONSE_TASK_DURATION + EXECUTION_BUFFER;
        Assert.assertTrue("processTime:" + response.getProcessingTime() + ", expectedTime:" + expectedTime, response.getProcessingTime() < expectedTime);
    }

    public static void optionalOverTimeoutTestSyncCriticalTest(IWorkflowFactory workflowFactory) {
        // won't complete within task timeout
        SampleOrchestrationResponse response = optionalTimeoutTest(workflowFactory,OPTIONAL_DURATION_GREATER_THAN_TIMEOUT, OPTIONAL_DURATION_LESS_THAN_TIMEOUT, CallableTaskConfig.synch(DIAG_CONFIG), "optionalOverTimeoutTestSyncCriticalTest");

        Assert.assertEquals("critical", response.getCriticalData());
        Assert.assertEquals("null_optional2", response.getOptionalData());
    }

}
