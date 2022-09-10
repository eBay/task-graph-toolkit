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
import org.junit.Test;

import com.ebay.taskgraph.diagnostic.DiagnosticConfig;

public class CyclicDependencyTest {

    @Test
    public void cyclicDependencyTest() {
        
        CyclicDependencyExecutor executor = new CyclicDependencyExecutor();
        WorkflowTask<SampleOrchestrationResponse> task = new WorkflowTask<>(
                CallableTaskConfig.simple(new DiagnosticConfig(false, false, true)), TestWorkflowFactory.INSTANCE, executor);
        SampleOrchestrationResponse response = task.call();
        Assert.assertNull(response.getCriticalData());
    }

    private static class CyclicDependencyExecutor implements IWorkflowExecutor<SampleOrchestrationResponse> {

        @Override
        public SampleOrchestrationResponse execute(IWorkflow<SampleOrchestrationResponse> workflow) {
            SampleOrchestrationResponse rval = new SampleOrchestrationResponse();
            try {
                WaitForCriticalDataOnlyBuilder builder = new WaitForCriticalDataOnlyBuilder(workflow, null);
                ICallableTaskFuture<String> task = builder.getTask(Factory1.INSTANCE);
                rval.setCriticalData(task.getNoThrow(workflow.getTask()));
            } catch (WorkflowException e) {
                Assert.assertTrue(true);
            }
            return rval;
        }
        
    }

    public static class Factory1 implements ITaskFactory<WaitForCriticalDataOnlyBuilder, String> {
    
        public static final Factory1 INSTANCE = new Factory1();
    
        @Override
        public ICallableTaskFuture<String> create(WaitForCriticalDataOnlyBuilder builder) {
            return builder.addTask(new OptionalTask(builder.sync, builder.getTask(Factory2.INSTANCE), "1", builder.request.firstTime));
        }
    
    }
    
    public static class Factory2 implements ITaskFactory<WaitForCriticalDataOnlyBuilder, String> {
    
        public static final Factory2 INSTANCE = new Factory2 ();
    
        @Override
        public ICallableTaskFuture<String> create(WaitForCriticalDataOnlyBuilder builder) {
            return builder.addTask(new OptionalTask(builder.sync, builder.getTask(Factory3.INSTANCE), "2", builder.request.firstTime));
        }
    
    }
    
    public static class Factory3 implements ITaskFactory<WaitForCriticalDataOnlyBuilder, String> {
    
        public static final Factory3 INSTANCE = new Factory3();
    
        @Override
        public ICallableTaskFuture<String> create(WaitForCriticalDataOnlyBuilder builder) {
            return builder.addTask(new OptionalTask(builder.sync, builder.getTask(Factory1.INSTANCE), "3", builder.request.firstTime));
        }
    
    }

}
