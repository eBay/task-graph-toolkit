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

import java.util.List;

import com.ebay.taskgraph.executor.ICallableTaskFuture;

/**
 * Task for visiting a single object provided by the output of a task.
 */
class SingleVisiteeFromTaskVisitorWorkflowExecutor<T> implements IWorkflowExecutor<T> {

    private final ICallableTaskFuture<T> task;
    private final List<VisitorTask<T>> visitors;

    public SingleVisiteeFromTaskVisitorWorkflowExecutor(
            ICallableTaskFuture<T> task,
            List<VisitorTask<T>> visitors) {
        this.task = task;
        this.visitors = visitors;
    }

    @Override
    public T execute(IWorkflow<T> workflow) {
        IVisiteeProvider<T> visiteeProvider = new TaskVisiteeProvider<T>(task, workflow.getTask());
        List<T> visitees = VisitorTask.visit(visiteeProvider, visitors, workflow);
        return visitees.isEmpty() ? null : visitees.get(0);
    }

}
