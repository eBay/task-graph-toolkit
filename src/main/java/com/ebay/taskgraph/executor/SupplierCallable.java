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

package com.ebay.taskgraph.executor;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Wrapper to convert a Callable to a Supplier.
 * For implementations of ICallableExecutor that need compatibility with CompletableFuture
 */
public class SupplierCallable<T> implements Supplier<T> {

    private final Callable<T> task;

    public SupplierCallable(Callable<T> task) {
        this.task = task;
    }

    @Override
    public T get() {
        try {
            return this.task.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new JavaSupplierException(e);
        }
    }
  
}
