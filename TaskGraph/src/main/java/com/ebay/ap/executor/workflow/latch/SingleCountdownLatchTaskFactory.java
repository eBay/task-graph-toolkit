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

package com.ebay.ap.executor.workflow.latch;

import com.ebay.ap.executor.ICallableTaskFuture;
import com.ebay.ap.executor.workflow.ITaskFactory;
import com.ebay.ap.executor.workflow.IWorkflowBuilder;

/**
 * Create a task instance with a count down latch decorator.
 */
public class SingleCountdownLatchTaskFactory<V, S extends IWorkflowBuilder> implements ITaskFactory<S, V> {

    private final SingleCountDownLatch<V> latch;

    // factory that will create an instance of the task that counts down the latch when it's completed
    private final ITaskFactory<S, V> factory;

    public SingleCountdownLatchTaskFactory(SingleCountDownLatch<V> latch, ITaskFactory<S, V> factory) {
        this.latch = latch;
        this.factory = factory;
    }

    @Override
    public ICallableTaskFuture<V> create(S builder) {
        ICallableTaskFuture<V> task = this.factory.create(builder);
        return builder.addTask(new SingleCountDownLatchTask<V>(task, this.latch));
    }

}
