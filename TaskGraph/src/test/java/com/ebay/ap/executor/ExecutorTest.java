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

package com.ebay.ap.executor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.ebay.ap.diagnostic.Diagnostic;
import com.ebay.ap.diagnostic.DiagnosticConfig;
import com.ebay.ap.executor.CallableTaskConfig.ExecType;
import com.ebay.ap.util.JacksonJsonHelperTest;
import com.ebay.ap.workflow.service.Headers;

public class ExecutorTest {

    static {
        JacksonJsonHelperTest.INSTANCE.getClass();
    }

    private static final long TIMEOUT = 1000000L;
    private static final DiagnosticConfig DIAGNOSTIC_CONFIG = new DiagnosticConfig(true, true, true);
    private static final CallableTaskConfig TASK_CONFIG = new CallableTaskConfig(DIAGNOSTIC_CONFIG, TIMEOUT);
    private static final CallableTaskConfig SYNC_TASK_CONFIG = new CallableTaskConfig(DIAGNOSTIC_CONFIG, ExecType.SYNC);
    private static final CallableTaskConfig SIMPLE_TASK_CONFIG = new CallableTaskConfig(DIAGNOSTIC_CONFIG, ExecType.SIMPLE);
    
    @Test
    public void testCallable() {
        ExecutorTest.test(new JavaCallableTaskExecutor(), new CallableTaskConfig(DIAGNOSTIC_CONFIG, TIMEOUT));
    }
    
    @Test
    public void testTaskDiag() {
        ExecutorTest.testTaskDiag(new JavaCallableTaskExecutor());
    }
    
    @Test
    public void testTaskMocks() {
        ExecutorTest.testTaskMocks(new JavaCallableTaskExecutor());
    }

    @Test
    public void testSyncCallable() {
        ExecutorTest.test(new JavaCallableTaskExecutor(), SYNC_TASK_CONFIG);
    }

    @Test
    public void testSimpleCallable() {
        ExecutorTest.test(new JavaCallableTaskExecutor(), SIMPLE_TASK_CONFIG);
    }

    @Test
    public void testApplicationException() {
        ExecutorTest.testApplicationExceptionStatus(new JavaCallableTaskExecutor());
        ExecutorTest.testApplicationExceptionThrowable(new JavaCallableTaskExecutor());
    }

    @Test
    public void testException() {
        ExecutorTest.testException(new JavaCallableTaskExecutor());
    }

    @Test
    public void testSyncApplicationException() {
        ExecutorTest.testSyncApplicationException(new JavaCallableTaskExecutor());
    }

    @Test
    public void testSyncException() {
        ExecutorTest.testSyncException(new JavaCallableTaskExecutor());
    }

    @Test
    public void testTimeout() {
        ExecutorTest.testTimeout(new JavaCallableTaskExecutor());
    }
    
    @Test
    public void testAsyncDependency() {
        ExecutorTest.testAsyncDependency(new JavaCallableTaskExecutor());
    }
    
    @Test
    public void testAsyncDependencyWithSynchDependency() {
        ExecutorTest.testAsyncDependencyWithSynchDependency(new JavaCallableTaskExecutor());
    }

    public static void test(ICallableTaskExecutor executor, CallableTaskConfig taskConfig) {

        TaskTest task = new TaskTest("test", executor, taskConfig, 6L);
        task.call();
    }

    // test adding specific task result to diagnostics
    public static void testTaskDiag(ICallableTaskExecutor executor) {

        Headers headers = new Headers();
        headers.add("showdiag", "1");
        headers.add("taskdiag", Arrays.asList("NumberTask1 , NumberTask2"));
        
        DiagnosticConfig diagConfig = new DiagnosticConfig(headers);
        CallableTaskConfig taskConfig = new CallableTaskConfig(diagConfig, TIMEOUT);
        TaskTest task = new TaskTest("testTaskDiag", executor, taskConfig, 6L);
        task.call();
        List<Diagnostic> diags = task.context.getDiagnostic().getDiagnostics();
        Assert.assertNotNull(diags);
        for (Diagnostic diag : diags) {
            if ("NumberTask1".equals(diag.getSender())) {
                Assert.assertEquals("Response:1", diag.getValue().get(0));
            }
            if ("NumberTask2".equals(diag.getSender())) {
                Assert.assertEquals("Response:2", diag.getValue().get(0));
            }
        }
    }

    // test mocking out task response
    public static void testTaskMocks(ICallableTaskExecutor executor) {

        Headers headers = new Headers();
        headers.add("taskmocks", Arrays.asList("[{\"name\":\"NumberTask1\",\"value\":4}]"));
        
        DiagnosticConfig diagConfig = new DiagnosticConfig(headers);
        CallableTaskConfig taskConfig = new CallableTaskConfig(diagConfig, TIMEOUT);
        TaskTest task = new TaskTest("testTaskMocks", executor, taskConfig, 9L);
        task.call();
    }
    
    private static class TaskTest extends Task implements ICallableTask<Long> {

        private final ICallableTaskExecutor executor;
        private final CallableTaskConfig taskConfig;
        private final long expectedResult;

        protected TaskTest(String test, ICallableTaskExecutor executor, CallableTaskConfig taskConfig, long expectedResult) {
            super(test, CallableTaskConfig.simple(DIAGNOSTIC_CONFIG));
            this.executor = executor;
            this.taskConfig = taskConfig;
            this.expectedResult = expectedResult;
        }

        @Override
        public Long call() {
            try {
                this.context.getProfiler().start();
                ICallableTaskFuture<Integer> one = executor.addTask(new NumberTask(taskConfig, 1));
                ICallableTaskFuture<Integer> two = executor.addTask(new NumberTask(taskConfig, 2));
                ICallableTaskFuture<Integer> three = executor.addTask(new NumberTask(taskConfig, 3));
                ICallableTaskFuture<Integer> four = new CallableTaskResultNull<Integer>();
                
                ICallableTaskFuture<Integer> sum = executor.addTask(new SumTask(taskConfig, one, two, three, four));
                long result = sum.getNoThrow(this);
                Assert.assertEquals(expectedResult, result);
                Assert.assertFalse(sum.isCancelled());
                Assert.assertFalse(sum.cancel(false));
                Assert.assertTrue(sum.isDone());
                
                // force clients to use getNoThrow so dependencies are tracked 
                try {
                    result = sum.get();
                } catch (UnsupportedOperationException e) {
                    Assert.assertTrue(true);
                } catch (Throwable e) {
                    Assert.assertTrue(false);
                }
                try {
                    result = sum.get(TIMEOUT, TimeUnit.MILLISECONDS);
                } catch (UnsupportedOperationException e) {
                    Assert.assertTrue(true);
                } catch (Throwable e) {
                    Assert.assertTrue(false);
                }
                result = sum.getNoThrow(this);
                Assert.assertEquals(expectedResult, result);
                return result;
            } finally {
                executor.collectResponseContext(this);
                this.context.getProfiler().stop();
                ProfilerHelper.print(this.context);
            }
        }

    }

    public static void test(ICallableTaskExecutor executor) {
        test(executor, new CallableTaskConfig(DIAGNOSTIC_CONFIG, TIMEOUT));
    }

    private static void testThrowable(String test, ICallableTaskExecutor executor, NumberTask taskThatThrows) {
        TestThrowable task = new TestThrowable(test, executor, taskThatThrows);
        long result = task.call();
        Assert.assertEquals(4L, result);
    }

    private static class TestThrowable extends Task implements ICallableTask<Long> {

        private final ICallableTaskExecutor executor;
        private final NumberTask taskThatThrows;

        protected TestThrowable(String test, ICallableTaskExecutor executor, NumberTask taskThatThrows) {
            super(test, CallableTaskConfig.simple(DIAGNOSTIC_CONFIG));
            this.executor = executor;
            this.taskThatThrows = taskThatThrows;
        }

        @Override
        public Long call() {
            try {
                this.context.getProfiler().start();
                CallableTaskConfig taskConfig = new CallableTaskConfig(DIAGNOSTIC_CONFIG, TIMEOUT);
                ICallableTaskFuture<Integer> one = executor.addTask(new NumberTask(taskConfig, 1));
                ICallableTaskFuture<Integer> two = executor.addTask(taskThatThrows);
                ICallableTaskFuture<Integer> three = executor.addTask(new NumberTask(taskConfig , 3));

                ICallableTaskFuture<Integer> sum = executor.addTask(new SumTask(taskConfig, one, two, three));
                long result = sum.getNoThrow(this);
                return result;
            } finally {
                executor.collectResponseContext(this);
                this.context.getProfiler().stop();
                ProfilerHelper.print(this.context);
            }
        }

    }

    public static void testApplicationExceptionStatus(ICallableTaskExecutor executor) {
        try { 
            testThrowable("testApplicationExceptionStatus", executor, new NumberTask(TASK_CONFIG, -1));
            Assert.assertTrue(false);
        } catch (ApplicationException e) {
            Assert.assertTrue(true);
        }
    }

    public static void testApplicationExceptionThrowable(ICallableTaskExecutor executor) {
        try { 
            // test other application exception constructor
            testThrowable("testApplicationExceptionThrowable", executor, new NumberTask(TASK_CONFIG, -3));
            Assert.assertTrue(false);
        } catch (ApplicationException e) {
            Assert.assertTrue(true);
        }
    }

    public static void testException(ICallableTaskExecutor executor) {
        testThrowable("testException", executor, new NumberTask(TASK_CONFIG, -2));
    }

    public static void testSyncApplicationException(ICallableTaskExecutor executor) {
        try {
            testThrowable("testSyncApplicationException", executor, new NumberTask(SYNC_TASK_CONFIG, -1));
            Assert.assertTrue(false);
        } catch (ApplicationException e) {
            Assert.assertTrue(true);
        }
    }

    public static void testSyncException(ICallableTaskExecutor executor) {
        testThrowable("testSyncException", executor, new NumberTask(SYNC_TASK_CONFIG, -2));
    }

    public static void testTimeout(ICallableTaskExecutor executor) {
        testThrowable("testTimeout", executor, new NumberTask(new CallableTaskConfig(DIAGNOSTIC_CONFIG, 50), -4));
    }
    
    public static void testAsyncDependency(ICallableTaskExecutor executor) {

        long time = System.currentTimeMillis();
        TestAsyncDependency task = new TestAsyncDependency(executor);
        long result = task.call();
        time = System.currentTimeMillis() - time;
        Assert.assertEquals(-9L, result);
        Assert.assertTrue(time >  150L);  // should take at least 200ms
    }
    
    private static class TestAsyncDependency extends Task implements ICallableTask<Integer> {

        private final ICallableTaskExecutor executor;

        protected TestAsyncDependency(ICallableTaskExecutor executor) {
            super(CallableTaskConfig.simple(DIAGNOSTIC_CONFIG));
            this.executor = executor;
        }

        @Override
        public Integer call() {
            try {
                this.context.getProfiler().start();
                // test Task.waitForDependencies() allows async dependencies to run in parallel while executing sync tasks
                
                CallableTaskConfig synctask = new CallableTaskConfig(DIAGNOSTIC_CONFIG, ExecType.SYNC);
                ICallableTaskFuture<Integer> one = executor.addTask(new NumberTask(synctask, -4));  // sleep 100 ms
                ICallableTaskFuture<Integer> two = executor.addTask(new NumberTask(synctask, -5));  // sleep 100 ms
                ICallableTaskFuture<Integer> sum = executor.addTask(new SumTask(synctask, one, two));
                return sum.getNoThrow(this);
            } finally {
                executor.collectResponseContext(this);
                this.context.getProfiler().stop();
                ProfilerHelper.print(this.context);
            }
        }

    }

    public static void testAsyncDependencyWithSynchDependency(ICallableTaskExecutor executor) {

        TestAsyncDependencyWithSynchDependency task = new TestAsyncDependencyWithSynchDependency(executor);
        long time = System.currentTimeMillis();
        long result = task.call();
        time = System.currentTimeMillis() - time;
        Assert.assertEquals(-9L, result);
        Assert.assertTrue(time <  150L);  // should take a little over 100ms only
        
    }
    
    private static class TestAsyncDependencyWithSynchDependency extends Task implements ICallableTask<Integer> {

        private final ICallableTaskExecutor executor;

        protected TestAsyncDependencyWithSynchDependency(ICallableTaskExecutor executor) {
            super(CallableTaskConfig.simple(DIAGNOSTIC_CONFIG));
            this.executor = executor;
        }

        @Override
        public Integer call() {
            try {
                this.context.getProfiler().start();
                // now make first dependency synchronous
                CallableTaskConfig synctask = new CallableTaskConfig(DIAGNOSTIC_CONFIG, ExecType.SYNC);
                CallableTaskConfig asynctask = new CallableTaskConfig(DIAGNOSTIC_CONFIG, TIMEOUT);
                ICallableTaskFuture<Integer> one = executor.addTask(new NumberTask(asynctask, -4)); // sleep 100 ms
                ICallableTaskFuture<Integer> two = executor.addTask(new NumberTask(synctask, -5));  // sleep 100 ms
                ICallableTaskFuture<Integer> sum = executor.addTask(new SumTask(synctask, one, two));
                return sum.getNoThrow(this);
            } finally {
                this.context.getProfiler().stop();
                executor.collectResponseContext(this);
                ProfilerHelper.print(this.context);
            }
        }

    }

}
