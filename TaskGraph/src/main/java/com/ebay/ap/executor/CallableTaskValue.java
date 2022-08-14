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

import com.ebay.ap.context.ResponseContext;
import com.ebay.ap.diagnostic.DiagnosticConfig;

/**
 * Defines a task in terms of a constant value.  Useful in unit tests for defining inputs to tasks under test.
 */
public class CallableTaskValue<T> extends Task implements ICallableTask<T> {

    private static final CallableTaskConfig TASK_CONFIG = CallableTaskConfig.simple(DiagnosticConfig.NONE);

    private final T value;

    public CallableTaskValue(T value) {
        this(value, ResponseContext.NULL);
    }

    public CallableTaskValue(T value, ResponseContext rc) {
        this(value, value != null ? value.getClass().getSimpleName() : "NullTask", rc);
    }

    public CallableTaskValue(T value, String taskName, ResponseContext rc) {
        super(taskName, rc, TASK_CONFIG);
        this.value = value;
    }

    @Override
    public T call() {
        return this.value;
    }

}
