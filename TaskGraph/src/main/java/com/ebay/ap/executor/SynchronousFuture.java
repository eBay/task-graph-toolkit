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

package com.ebay.ap.executor;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Future that executes a task synchronously in the calling task's thread.
 */
public class SynchronousFuture<T> implements Future<T> {

    private final ICallableTask<T> task;

    public SynchronousFuture(ICallableTask<T> task) {
        this.task = task;
    }

    @Override
    public T get() {

        try {
            return this.task.call();
        } catch (ApplicationException bae) {
            // propagate application exceptions
            throw bae;
        } catch (Throwable e) {
            // other exceptions are logged by the profile task decorator
            return null;
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) {
        return get();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    public ICallableTask<T> getTask() {
        return this.task;
    }
}
