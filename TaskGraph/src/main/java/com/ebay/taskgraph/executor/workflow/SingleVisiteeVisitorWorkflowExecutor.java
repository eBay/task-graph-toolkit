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

/**
 * Task for visiting a single object.
 */
class SingleVisiteeVisitorWorkflowExecutor<T> implements IWorkflowExecutor<T> {

    private final IVisiteeProvider<T> visiteeProvider;
    private final List<VisitorTask<T>> visitors;

    SingleVisiteeVisitorWorkflowExecutor(
            IVisiteeProvider<T> visiteeProvider,
            List<VisitorTask<T>> visitors) {
        this.visiteeProvider = visiteeProvider;
        this.visitors = visitors;
    }

    @Override
    public T execute(IWorkflow<T> workflow) {
        List<T> visitees = VisitorTask.visit(this.visiteeProvider, visitors, workflow);
        return visitees.isEmpty() ? null : visitees.get(0);
    }

}
