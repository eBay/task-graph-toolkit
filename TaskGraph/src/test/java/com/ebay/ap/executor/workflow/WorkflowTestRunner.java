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

import com.ebay.ap.diagnostic.DiagnosticConfig;
import com.ebay.ap.executor.CallableTaskConfig;
import com.ebay.ap.executor.ProfilerHelper;

public class WorkflowTestRunner {

    private static final DiagnosticConfig DIAGNOSTIC_CONFIG = new DiagnosticConfig(false, false, true);

    /**
     * Create a workflow task to run the workflow executor passed in and print out the profiler result.
     * @param executor
     * @param workflowFactory
     * @return
     */
    public static <T> T runTest(IWorkflowExecutor<T> executor, IWorkflowFactory workflowFactory, String name) {
        return runTest(executor, workflowFactory, name, true);
    }

    public static <T> T runTest(
            IWorkflowExecutor<T> executor,
            IWorkflowFactory workflowFactory,
            String name,
            boolean printProfiler) {

        WorkflowTask<T> task = new WorkflowTask<>(name, CallableTaskConfig.simple(DIAGNOSTIC_CONFIG), workflowFactory, executor);
        try {
            return task.call();
        } finally {
            if (printProfiler) {
                ProfilerHelper.print(task.getContext());
            }
        }
    }

}
