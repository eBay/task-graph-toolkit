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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.ebay.ap.executor.ICallableTask;
import com.ebay.ap.executor.Task;

/**
 * Keep track of tasks that count down so dependency can use the future directly
 * instead of trying to figure out which task caused the latch to flip.
 */
public class SingleCountDownLatch<T> {

    private final CountDownLatch latch = new CountDownLatch(1);

    private SingleCountDownLatchTask<T> countDownTask;

    void countDown(SingleCountDownLatchTask<T> countDownTask) {
        this.countDownTask = countDownTask;
        this.latch.countDown();
    }

    T await(long timeout, ICallableTask<?> caller) throws InterruptedException {
        if (this.latch.await(timeout, TimeUnit.MILLISECONDS)) {
            // create a dependency from the caller to the latch task also
            Task.addDependency(this.countDownTask, caller);
            return this.countDownTask.getTask().getNoThrow(caller);
        } else {
            return null;
        }
    }

}
