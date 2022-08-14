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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.ebay.ap.context.ResponseContext;

/**
 * Decorator for logging task's parent and the thread it runs on.
 */
public class TaskThreadLoggingDecorator<T> implements ICallableTask<T> {

    // keep track of tasks running on threads
    // allows the parent task of new tasks to be found
    private static final Map<String, String> CURRENT_THREAD_TASK = Collections.synchronizedMap(new HashMap<String, String>());

    private final ICallableTask<T> task;
    private final String parentTask;

    public TaskThreadLoggingDecorator(ICallableTask<T> task) {
        this.task = task;
        // parent task is what's currently running the executor
        this.parentTask = TaskThreadLoggingDecorator.getCurrentTask();
    }

    @Override
    public T call() throws Exception {
        
        // log the task thread
        Task.logParentTaskAndThread(this.task, this.parentTask);
        
        // this task is now executing on the current thread
        String previousTask = TaskThreadLoggingDecorator.setCurrentTask(this.task.getName());

        T result = this.task.call();

        // restore previous task
        TaskThreadLoggingDecorator.setCurrentTask(previousTask);

        return result;
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

    /**
     * @return current task executing on this thread
     */
    private static String getCurrentTask() {
        return CURRENT_THREAD_TASK.get(Thread.currentThread().getName());
    }
    
    /**
     * Set task on current thread
     */
    private static String setCurrentTask(String taskName) {
        String rval = CURRENT_THREAD_TASK.get(Thread.currentThread().getName());
        CURRENT_THREAD_TASK.put(Thread.currentThread().getName(), taskName);
        return rval;
    }

}
