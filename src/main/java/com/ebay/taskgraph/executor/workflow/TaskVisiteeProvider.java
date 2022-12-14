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

import java.util.ArrayList;
import java.util.List;

import com.ebay.taskgraph.executor.ICallableTask;
import com.ebay.taskgraph.executor.ICallableTaskFuture;

/**
 * Provides the visitee from the response of a task
 */
public class TaskVisiteeProvider<T> implements IVisiteeProvider<T> {
    
    private final ICallableTaskFuture<T> task;
    private final ICallableTask<?> caller;

    public TaskVisiteeProvider(ICallableTaskFuture<T> task, ICallableTask<?> caller) {
        this.task = task;
        this.caller = caller;
    }

    @Override
    public List<T> get() {
        List<T> visitees = new ArrayList<>(1);
        T visitee = this.task.getNoThrow(caller);
        if (visitee != null) {
            visitees.add(visitee);
        }
        return visitees;
    }

}
