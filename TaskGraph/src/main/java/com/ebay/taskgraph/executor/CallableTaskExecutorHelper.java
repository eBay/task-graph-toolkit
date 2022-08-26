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

import com.ebay.taskgraph.executor.CallableTaskConfig.ExecType;
import com.ebay.taskgraph.executor.workflow.WorkflowException;

public class CallableTaskExecutorHelper {

    // add profiling decorators
    public static <T> ICallableTask<T> getDecoratedTask(ICallableTaskExecutor executor, ICallableTask<T> task) {

        if (executor.getTask(task.getName()) != null) {
            throw new WorkflowException("Attempted to add a duplicate key: " + task.getName());
        }

        if (ExecType.ASYNC_TIMEOUT.equals(task.getTaskConfig().execType)) {
            task = new CallableTimeoutDecorator<>(task, executor);
        } else {
            task = new ProfileDecorator<T>(task);
        }

        // if we're profiling, create decorator for logging parent task and thread
        if (task.getContext().getDiagnosticConfig().profile || task.getContext().getDiagnosticConfig().showDiagnostics) {
            task = new TaskThreadLoggingDecorator<T>(task);
        }
        
        return task;
    }
    
    // return true if task is async
    public static boolean isAsync(ICallableTask<?> task) {
      
        return ExecType.ASYNC.equals(task.getTaskConfig().execType)
                || ExecType.ASYNC_TIMEOUT.equals(task.getTaskConfig().execType);
    }

}
