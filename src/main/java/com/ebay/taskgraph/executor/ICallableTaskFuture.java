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

import java.util.concurrent.Future;

public interface ICallableTaskFuture<T> extends Future<T> {

    ICallableTask<T> getTask();

    /**
     * Handles future exceptions and returns null.
     * Logs exceptions to the task.
     * Adds the caller to the list of dependencies in the profiler task meta data.
     * Ensure caller is a task so the dependency graph is tracked properly.
     * 
     * Note: throws ApplicationException so tasks can have errors propagated to the service response. 
     */
    T getNoThrow(ICallableTask<?> caller);

}
