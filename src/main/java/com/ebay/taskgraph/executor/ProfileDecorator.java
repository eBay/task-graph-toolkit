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

package com.ebay.taskgraph.executor;

import com.ebay.taskgraph.context.ResponseContext;
import com.ebay.taskgraph.diagnostic.IProfilerEntry;

/**
 * Decorator of ExecType.SIMPLE task for profiling only, no CAL transaction.
 */
public class ProfileDecorator<T> implements ICallableTask<T> {

    private static final String WAIT_DEPS = "wait_deps";

    private final ICallableTask<T> task;

    public ProfileDecorator(ICallableTask<T> task) {
        this.task = task;
    }

    @Override
    public T call() {

        // block for dependencies so profiling of the task shows only time spent in this task's execution
        IProfilerEntry pe = this.getContext().getProfiler().newEntry(WAIT_DEPS);
        try {
            this.task.waitForDependencies();
        } finally {
            this.getContext().getProfiler().add(pe);
        }

        return profileTaskCall(this.task);
    }

    static <T> T profileTaskCall(ICallableTask<T> task) {
        
        task.getContext().getProfiler().start();

        T rval = null;
        try {
            rval = task.call();
        } catch (ApplicationException bae) {
            throw bae;
        } catch (Throwable t) {
            Task.logTaskException(task, t);
        } finally {
            task.getContext().getProfiler().stop();
        }
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
        return this.task.getTaskConfig();
    }

    @Override
    public void waitForDependencies() {
        this.task.waitForDependencies();
    }

    @Override
    public ICallableTaskFuture<?>[] getDependencies() {
        return this.task.getDependencies();
    }

}
