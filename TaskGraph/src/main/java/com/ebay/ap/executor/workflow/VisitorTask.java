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

import java.util.List;

import com.ebay.ap.context.ResponseContext;
import com.ebay.ap.executor.CallableTaskConfig;
import com.ebay.ap.executor.ICallableTask;
import com.ebay.ap.executor.ICallableTaskFuture;
import com.ebay.ap.executor.Task;

/**
 * Wraps a visitor in a simple task to aid profiling.
 */
public abstract class VisitorTask<T> extends Task implements ICallableTask<Boolean>, IVisitor<T> {

    private List<T> visitees = null;

    public VisitorTask(CallableTaskConfig config, ICallableTaskFuture<?> ... dependencies) {
        super(config, dependencies);
    }

    public VisitorTask(String name, CallableTaskConfig config, ICallableTaskFuture<?> ... dependencies) {
        super(name, config, dependencies);
    }
    
    public VisitorTask(String name, ResponseContext context, CallableTaskConfig config, ICallableTaskFuture<?> ... dependencies) {
        super(name, context, config, dependencies);
    }

    private void setVisitees(List<T> visitees) {
        this.visitees = visitees;
    }

    @Override
    public Boolean call() {
        if (null == this.visitees) {
            throw new WorkflowException("Need to VisitorTask.visit() method so visitees are initialized.");
        }
        for (T visitee : this.visitees) {
            try {
                this.visit(visitee);
            } catch (Throwable t) {
                Task.logTaskException(this, t);
            }
        }
        return Boolean.TRUE;
    }

    static <T, V> List<T> visit(IVisiteeProvider<T> provider, List<VisitorTask<T>> visitors, IWorkflow<V> workflow) {
        List<T> visitees = provider.get();
        if (visitees != null && visitees.size() > 0) {
            for (VisitorTask<T> visitor : visitors) {
                visitor.setVisitees(visitees);
                workflow.addTask(visitor).getNoThrow(workflow.getTask());
            }
        }
        return visitees;
    }

}
