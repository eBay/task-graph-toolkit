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

import com.ebay.ap.context.ResponseContext;

/**
 * Wraps the target task in another task that is used to time out the task.
 */
public class CallableTimeoutDecorator<T> implements ICallableTask<T> {

    public static final ICallableTaskFuture<?>[] NULL_DEPENDENCIES = new ICallableTaskFuture<?>[0];

    private final ICallableTask<T> task;

    private final CallableTaskConfig taskConfig;

    private final ICallableTaskFuture<T> taskFuture;

    public CallableTimeoutDecorator(ICallableTask<T> task, ICallableTaskExecutor executor) {
        this.task = task;

        // make this a simple task so the parent future doesn't timeout
        // this prevents dual CAL logging in the case of a timeout
        this.taskConfig = CallableTaskConfig.simple(task.getTaskConfig().diagnosticConfig);

        // add the timeout task
        CallableTaskInvoker<T> invoker = new CallableTaskInvoker<>(this.task);
        this.taskFuture = executor.addTask(invoker);
    }

    @Override
    public T call() {
        // block for dependencies so the timeout only applies to the task's own execution time
        this.waitForDependencies();

        T rval = taskFuture.getNoThrow(this.task);
        return rval;
    }

    @Override
    public String getName() {
        return this.task.getName();
    }

    @Override
    public ResponseContext getContext() {
        return this.task.getContext();
    }

    @Override
    public CallableTaskConfig getTaskConfig() {
        // return the task with the adjusted timeout so the future of the parent task doesn't timeout
        return this.taskConfig;
    }

    @Override
    public void waitForDependencies() {
        this.task.waitForDependencies();
    }

    @Override
    public ICallableTaskFuture<?>[] getDependencies() {
        return this.task.getDependencies();
    }

    public static class CallableTaskInvoker<T> extends Task implements ICallableTask<T> {

        private static final String TASK_SUFFIX = "_timeoutTask";

        private final ICallableTask<T> task;

        public CallableTaskInvoker(ICallableTask<T> task) {
            super(task.getName() + TASK_SUFFIX,
                    new ResponseContext(task.getTaskConfig().diagnosticConfig, task.getContext().getName() + TASK_SUFFIX),
                    new CallableTaskConfig(task.getTaskConfig().diagnosticConfig, task.getTaskConfig().timeout),
                    task.getDependencies());

            // use same name for all timeout tasks, the dependency graph indicates which task it's associated with
            this.context.getProfiler().addData(Task.NODE_LABEL, "TimeoutTask");

            this.task = task;
        }

        @Override
        public T call() {
            return ProfileDecorator.profileTaskCall(this.task);
        }

    }
}