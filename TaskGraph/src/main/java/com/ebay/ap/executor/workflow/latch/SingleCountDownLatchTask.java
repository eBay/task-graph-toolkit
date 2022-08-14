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

import com.ebay.ap.executor.ApplicationException;
import com.ebay.ap.executor.CallableTaskResultThrowable;
import com.ebay.ap.executor.ICallableTask;
import com.ebay.ap.executor.ICallableTaskFuture;
import com.ebay.ap.executor.Task;

/**
 * Decorator task to count down on a latch when the task is done.
 */
public class SingleCountDownLatchTask<T> extends Task implements ICallableTask<T> {

    private ICallableTaskFuture<T> task;

    private final SingleCountDownLatch<T> latch;

    public SingleCountDownLatchTask(ICallableTaskFuture<T> task, SingleCountDownLatch<T> latch) {
        super(task.getTask().getName() + "_latch", task.getTask().getTaskConfig());
        this.task = task;
        this.latch = latch;
    }

    @Override
    public T call() throws Exception {
        T rval = null;
        try {
            rval = this.task.getNoThrow(this);
            if (rval != null) {
                this.latch.countDown(this);
            }
        } catch (ApplicationException e) {
            // in the event that the task throws an application
            // exception, make sure the latch is released and we propagate the exception
            this.task = new CallableTaskResultThrowable<T>(e);
            this.latch.countDown(this);
        }
        return rval;
    }

    public ICallableTaskFuture<T> getTask() {
        return this.task;
    }

}
