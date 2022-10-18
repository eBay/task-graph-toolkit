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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Future wrapper that logs exceptions to appropriate task.  Allows exceptions thrown
 * by tasks to be reported independently in CAL.
 */
public class CallableTaskFuture<RESULT> implements ICallableTaskFuture<RESULT> {

    public static final TimeUnit TIMEOUT_UNIT = TimeUnit.MILLISECONDS;

    private final Future<RESULT> future;

    private final ICallableTask<RESULT> task;

    /**
     * Only do the get on the future once.
     */
    private volatile Result<RESULT> result = null;
    
    /**
     * In case the future throws an ApplicationException
     */
    private volatile ApplicationException applicationException = null;

    public CallableTaskFuture(Future<RESULT> future, ICallableTask<RESULT> task) {
        this.future = future;
        this.task = task;
    }

    /**
     * Block and wait for task response.  Handle concurrency & timeout exceptions.
     * Synchronized in case there are multiple dependent tasks that  may call this concurrently.
     * Uses the timeout value from the task configuration.
     */
    @Override
    public synchronized RESULT getNoThrow(ICallableTask<?> caller) {

        Task.addDependency(this.task, caller);
        
        if (this.applicationException != null) {
            // first off, propagate application exception so we don't execute synchronous tasks again
            throw this.applicationException;
        }

        if (null == this.result) {
            RESULT result = null;
            try {
                // Note: the time out for tasks associated with the future need to account for the time
                // the task blocks waiting for its dependencies.
                // To accurately define a time out for the processing of the task itself use ExecType.AYNC_TIMOUT
                // task type.  Or, if a task is making an external service call, it's usual to allow the read timeout
                // of the service call to limit the time a task takes.
                result = this.future.get(this.task.getTaskConfig().timeout, TIMEOUT_UNIT);
            } catch (TimeoutException e) {
                // mark the task's execution as having stopped even though the thread of execution continues to run 
                // an exception will unblock any dependent tasks so stopping the task will indicate this in the profiler tool
                this.task.getContext().getProfiler().stop();
                logException(e);
            } catch (ExecutionException e) {
                // unwrap the execution exception to get the true cause of the exception can be logged
                logException(e.getCause());
            } catch (Throwable t) {
                logException(t);
            }
            
            // log the result of the task if diagnostics is enabled specifically for the task
            if (this.task.getTaskConfig().diagnosticConfig.taskDiagnosticEnabled(this.task.getName())) {
                Task.addTaskResponseDiagnostic(this.task, result);
            }

            // check if a result has been pre configured for the task
            // type safe cast as we're passing in the specific class to be deserialized to
            result = this.task.getTaskConfig().diagnosticConfig.getTaskData(this.task.getName(), result);

            // only create a result if task completes normally or an exception was handled
            // previously this was done in a finally block but this prevented
            // ApplicationExceptions from being propagated properly
            this.result = new Result<>(result);
        }
        return this.result.result;
    }

    /**
     * Force clients to use getNoThrow so that dependencies are reliably tracked.
     */
    @Override
    public RESULT get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException("use getNoThrow() so that task dependencies are tracked");
    }

    /**
     * Force clients to use getNoThrow so that dependencies are reliably tracked.
     */
    @Override
    public RESULT get() throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException("use getNoThrow() so that task dependencies are tracked");
    }

    /**
     * Logs any exception to the task.
     * @param t
     */
    private void logException(Throwable t) {
        // save application exceptions so it can be rethrown for other tasks with this dependency
        if (t instanceof ApplicationException) {
            this.applicationException = (ApplicationException) t;
        }
        Task.logTaskException(this.task, t);
    }

    /**
     * inner class containing result, could be null in the case of an exception
     */
    private static class Result<RESULT> {

        final RESULT result;

        private Result(RESULT result) {
            this.result = result;
        }

    }

    @Override
    public ICallableTask<RESULT> getTask() {
        return this.task;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return this.future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return this.future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return this.result != null || this.applicationException != null || this.future.isDone();
    }

}
