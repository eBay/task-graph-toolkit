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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Simple orchestrator that used Java executor service.
 */
public class JavaCallableTaskExecutor implements ICallableTaskExecutor {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private final Map<String, ICallableTaskFuture<?>> tasks = new HashMap<>();

    @Override
    public <T> ICallableTaskFuture<T> addTask(ICallableTask<T> task) {

        // do this before decorating because ASYNC_TIMEOUT tasks have a decorator that changes the original task config
        boolean isAsync = CallableTaskExecutorHelper.isAsync(task);

        task = CallableTaskExecutorHelper.getDecoratedTask(this, task);
        
        Future<T> future;
        if (isAsync) {
            future = EXECUTOR.submit(task);
        } else {
            future = new SynchronousFuture<T>(task);
        }
        CallableTaskFuture<T> result = new CallableTaskFuture<T>(future, task);
        this.tasks.put(task.getName(), result);
        return result;
    }

    /**
     * Add all task response contexts to the workflow instance. 
     * Make sure to call this ONCE AND ONLY ONCE after the workflow execution is done. 
     * It is assumed that there would be ONLY ONE CLIENT dealing with the workflow object.
     */
    @Override
    public void collectResponseContext(ICallableTask<?> parentTask) {
        for (Entry<String, ICallableTaskFuture<?>> task : this.tasks.entrySet()) {
            parentTask.getContext().add(task.getValue().getTask().getContext());
        }
    }

    public static void shutdown() {
        EXECUTOR.shutdown();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ICallableTaskFuture<T> getTask(String name) {
        return (ICallableTaskFuture<T>) this.tasks.get(name);
    }

}
