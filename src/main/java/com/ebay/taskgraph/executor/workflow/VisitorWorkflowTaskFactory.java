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

import com.ebay.taskgraph.executor.CallableTaskConfig;
import com.ebay.taskgraph.executor.ICallableTaskFuture;

/**
 * Creates tasks used for grouping a set of visitor subtasks under a separate workflow.
 * Useful for visualizing the workflow in the profiler tasks view
 */
public class VisitorWorkflowTaskFactory<T> {

    /**
     * Factory method for multi visitee visitor.
     */
    public static <T, V> ICallableTaskFuture<List<T>> createMultiVisitee(
            CallableTaskConfig syncTask,
            IWorkflow<V> callerContext,
            String name,
            IVisiteeProvider<T> visiteeProvider,
            List<VisitorTask<T>> visitors) {

        IWorkflowExecutor<List<T>> executor = new MultiVisiteeVisitorWorkflowExecutor<T>(visiteeProvider, visitors);
        WorkflowTask<List<T>> task = new WorkflowTask<>(name, syncTask, callerContext, executor);
        return callerContext.addTask(task);
    }

    /**
     * Factory method for multi visitees provided by the result of a task.
     */
    public static <T, V> ICallableTaskFuture<List<T>> createMultiVisitee(
            CallableTaskConfig syncTask,
            IWorkflow<V> callerContext,
            String name,
            ICallableTaskFuture<List<T>> visiteeProvider,
            List<VisitorTask<T>> visitors) {

        IWorkflowExecutor<List<T>> executor = new MultiVisiteeFromTaskVisitorWorkflowExecutor<T>(visiteeProvider, visitors);
        WorkflowTask<List<T>> task = new WorkflowTask<>(name, syncTask, callerContext, executor);
        return callerContext.addTask(task);
    }

    /**
     * Factory method for single visitee and list of visitors.
     */
    public static <T, V> ICallableTaskFuture<T> create(
            CallableTaskConfig syncTask,
            IWorkflow<V> callerContext,
            String name,
            T visitee,
            List<VisitorTask<T>> visitors) {

        IVisiteeProvider<T> visiteeProvider = new SingleVisiteeProvider<>(visitee);
        IWorkflowExecutor<T> executor = new SingleVisiteeVisitorWorkflowExecutor<T>(visiteeProvider, visitors);
        WorkflowTask<T> task = new WorkflowTask<>(name, syncTask, callerContext, executor);
        return callerContext.addTask(task);
    }

    /**
     * Factory method for single visitee and single visitor.
     */
    public static <T, V> ICallableTaskFuture<T> create(
            CallableTaskConfig syncTask,
            IWorkflow<V> callerContext,
            String name,
            T visitee,
            VisitorTask<T> visitor) {
        IVisiteeProvider<T> visiteeProvider = new SingleVisiteeProvider<>(visitee);
        List<VisitorTask<T>> visitors = new ArrayList<>(1);
        visitors.add(visitor);
        IWorkflowExecutor<T> executor = new SingleVisiteeVisitorWorkflowExecutor<T>(visiteeProvider, visitors);
        WorkflowTask<T> task = new WorkflowTask<>(name, syncTask, callerContext, executor);
        return callerContext.addTask(task);
    }

    /**
     * Factory method for single visitee visitor that's the result of a task.
     */
    public static <T, V> ICallableTaskFuture<T> create(
            CallableTaskConfig syncTask,
            IWorkflow<V> callerContext,
            String name,
            ICallableTaskFuture<T> task,
            List<VisitorTask<T>> visitors) {

        IWorkflowExecutor<T> executor = new SingleVisiteeFromTaskVisitorWorkflowExecutor<T>(task, visitors);
        WorkflowTask<T> workflowTask = new WorkflowTask<>(name, syncTask, callerContext, executor);
        return callerContext.addTask(workflowTask);
    }

}
