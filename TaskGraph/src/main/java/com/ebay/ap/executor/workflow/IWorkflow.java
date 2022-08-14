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

package com.ebay.ap.executor.workflow;

import com.ebay.ap.context.ResponseContext;
import com.ebay.ap.executor.ICallableTask;
import com.ebay.ap.executor.ICallableTaskFuture;

/**
 * A workflow of tasks that results in a final response of type V
 * Extends workflow factory in order to create nested workflows of tasks.
 */
public interface IWorkflow<V> extends IWorkflowFactory {
    
    /**
     * @return task that owns the workflow
     */
    ICallableTask<V> getTask();

    <T> ICallableTaskFuture<T> addTask(ICallableTask<T> task);

    ResponseContext collectResponseContext();

}
