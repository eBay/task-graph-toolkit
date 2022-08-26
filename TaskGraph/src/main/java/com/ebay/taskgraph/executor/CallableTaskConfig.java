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

import com.ebay.taskgraph.diagnostic.DiagnosticConfig;

/**
 * Container for parameters required for task initialization.
 */
public class CallableTaskConfig {
    
    public enum ExecType {
        SYNC,           // synchronous task that runs in the thread of caller of getNoThrow() on the task future
        ASYNC,          // task submitted to the executor service
        ASYNC_TIMEOUT,  // task submitted to the executor service with an additional task to timeout the execution
        SIMPLE,         // simple synchronous task that doesn't require CAL logging
    }

    public final DiagnosticConfig diagnosticConfig;
    public final long timeout;
    public final ExecType execType;
    
    public CallableTaskConfig(DiagnosticConfig diagnosticConfig, long timeout, ExecType execType) {
        this.diagnosticConfig = diagnosticConfig;
        this.timeout = timeout;
        this.execType = execType;
    }
    
    // tasks configured with a timeout are assumed to be asynchronous
    public CallableTaskConfig(DiagnosticConfig diagnosticConfig, long timeout) {
        this(diagnosticConfig, timeout, ExecType.ASYNC);
    }

    public CallableTaskConfig(DiagnosticConfig diagnosticConfig, ExecType execType) {
        this(diagnosticConfig, Long.MAX_VALUE, execType);
    }
    
    // create helpers
    public static CallableTaskConfig synch(DiagnosticConfig diagnosticConfig) {
        return new CallableTaskConfig(diagnosticConfig, ExecType.SYNC);
    }

    public static CallableTaskConfig simple(DiagnosticConfig diagnosticConfig) {
        return new CallableTaskConfig(diagnosticConfig, ExecType.SIMPLE);
    }
}
