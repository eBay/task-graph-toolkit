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

/**
 * A future for propagating exceptions.
 * Needed for when tasks explicitly need to propagate application exceptions
 * from their dependencies.  Generally only need when the application requires
 * complex concurrency behavior with things like count down latches. 
 */
public class CallableTaskResultThrowable<T> extends SynchronousFuture<T> implements ICallableTaskFuture<T> {

    private final ApplicationException appException;

    public CallableTaskResultThrowable(ApplicationException e) {
        super(new CallableTaskNull<T>());
        this.appException = e;
    }

    @Override
    public T getNoThrow(ICallableTask<?> caller) {
        throw this.appException;
    }

}
