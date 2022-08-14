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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.ap.context.ResponseContext;
import com.ebay.ap.diagnostic.DiagnosticConfig;
import com.ebay.ap.executor.CallableTaskConfig.ExecType;
import com.ebay.ap.executor.workflow.WorkflowException;

/**
 * Handles boiler plate methods of ICallableTask
 */
public abstract class Task {

    // label for the task in profiler tasks view
    // allows applications to give pithier names for tasks to improve readability of profiler tasks view
    public static final String NODE_LABEL = "node_label";  

    // unique fully qualified task name in case there are multiple instances of the same class
    public static final String TASK_NAME = "task_name";

    public static final String DEPENDENCIES_META_DATA_KEY = "dependencies";
    public static final String DEPENDENCIES_SEPARATOR = ":";
    public static final String EXEC_TYPE = "execType";
    public static final String PARENT_TASK = "parent_task";
    public static final String TASK_THREAD = "task_thread";

    static Logger LOGGER = LoggerFactory.getLogger(Task.class);
  
    private static final String EXCEPTION_META_DATA_KEY = "exception";

    protected final String taskName;
    protected final ResponseContext context;
    protected final ICallableTaskFuture<?>[] dependencies;

    // only callable result should care about this, not super classes
    private final CallableTaskConfig config;

    protected Task(CallableTaskConfig config, ICallableTaskFuture<?> ... dependencies) {

        this.config = config;
        this.taskName = this.getClass().getSimpleName();
        this.context = createResponseContext(config, this.taskName);
        this.dependencies = dependencies;
        setExecutionType();
    }

    protected Task(String taskName, CallableTaskConfig config, ICallableTaskFuture<?> ... dependencies) {
        this(createResponseContext(config, taskName), config, dependencies);
    }

    protected Task(ResponseContext rc, CallableTaskConfig config, ICallableTaskFuture<?> ... dependencies) {
        this(rc.getName(), rc, config, dependencies);
    }

    protected Task(String taskName, DiagnosticConfig diagnosticConfig, ICallableTaskFuture<?> ... dependencies) {
        this(taskName, new CallableTaskConfig(diagnosticConfig, CallableTaskConfig.ExecType.SYNC), dependencies);
    }

    protected Task(String name, ResponseContext rc, CallableTaskConfig config, ICallableTaskFuture<?> ... dependencies) {
        this.config = config;
        this.taskName = name;
        this.context = rc;
        this.dependencies = dependencies;
        setExecutionType();
        // in case task name is different than response context name
        // save the task name in profiler data
        if (!rc.getName().equals(name)) {
            rc.getProfiler().addData(TASK_NAME, name);
        }
    }

    private static ResponseContext createResponseContext(
            CallableTaskConfig config,
            String taskName) {

        return new ResponseContext(config.diagnosticConfig, taskName);
    }

    private void setExecutionType() {
        if (this.context.getDiagnosticConfig().profile || this.context.getDiagnosticConfig().showDiagnostics) {
            this.context.getProfiler().addData(EXEC_TYPE, config.execType.toString());
        }
        // validate dependencies
        if (this.dependencies != null) {
            for (int i = 0; i < this.dependencies.length; ++i) {
                if (null == this.dependencies[i]) {
                    throw new WorkflowException("Null dependency in task: " + this.taskName);
                }
            }
        }
    }

    public final String getName() {
        return this.taskName;
    }

    public final ResponseContext getContext() {
        return this.context;
    }

    public final CallableTaskConfig getTaskConfig() {
        return this.config;
    }

    public final ICallableTaskFuture<?>[] getDependencies() {
        return this.dependencies;
    }

    /**
     * Create and log an event for an exception within a task.  Can't do this in a
     * generic way with the kernel logger because the type of exception is always associated
     * with the class that does the logging.  In order to easily see the exceptions
     * grouped by task name in CAL, we need to log an event with the task name as the type.
     *  
     * @param task
     * @param t
     * @throws Throwable 
     */
    public static void logTaskException(ICallableTask<?> task, Throwable t) {

        if (t instanceof JavaSupplierException) {
            // in case the executor is using CompletableFutures
            // the task will be wrapped in a SupplierCallable that handles checked exceptions from the Callable interface
            // this enables the checked exception to be logged
            t = t.getCause();
        }

        // record exception in profiler
        addProfileException(task, t);

        // don't log application exceptions
        // will be logged by top level application handler and/or Raptor exception handler
        if (t instanceof ApplicationException) {
            // propagate application exceptions to top level application handler
            throw (ApplicationException) t;
        }

        task.getContext().getDiagnostic().addThrowableDiagnostic(task.getContext().getName(), t);

        // log debug message so we get stack trace in console
        // don't log as error because we'll get duplicate exceptions in CAL
        //
        // need the logback configuration for this package to be debug
        // src/main/webapp/META-INF/configuration/Dev/config/com/ebay/aero/kernal/logging/logback/logback.xml
        // 
        //<logger name="com.ebay.ap.executor" level="DEBUG" additivity="false">
        //  <appender-ref ref="STDOUT" />
        //</logger>
        LOGGER.debug(task.getContext().getName(), t);
    }

    private static void addProfileException(ICallableTask<?> task, Throwable t) {
        task.getContext().getProfiler().addData(EXCEPTION_META_DATA_KEY, t.getClass().getSimpleName());
    }

    /**
     * Block for all dependencies.
     * Called by both the profile decorator of the task itself and also the future of synchronous tasks.
     */
    public void waitForDependencies() {

        // Block for all the task's dependencies before delegating to the task for execution.
        // Allows the CAL transaction and profiler to report time spent purely in task execution. 

        List<ICallableTaskFuture<?>> asyncDependencies = new ArrayList<>();

        // first block for any synchronous task dependencies
        // so any async ones can run in parallel
        for (ICallableTaskFuture<?> dep : dependencies) {
            if (ExecType.ASYNC.equals(dep.getTask().getTaskConfig().execType)
                    || ExecType.ASYNC_TIMEOUT.equals(dep.getTask().getTaskConfig().execType)) {
                asyncDependencies.add(dep);
            } else {
                dep.getNoThrow((ICallableTask<?>) this);
            }
        }

        // then block for async dependencies
        for (ICallableTaskFuture<?> dep : asyncDependencies) {
            dep.getNoThrow((ICallableTask<?>) this);
        }
    }

    /**
     * Add a dependency to the specified task from the caller.
     */
    public static void addDependency(ICallableTask<?> task, ICallableTask<?> caller) {

        ResponseContext rc = task.getContext();
        if (caller != null) {
            addDependency(rc, caller.getName());
        } else {
            LOGGER.error("Task {} called with null caller", task.getName());
        }
    }

    private static void addDependency(ResponseContext rc, String caller) {

        if (rc.getDiagnosticConfig().profile || rc.getDiagnosticConfig().showDiagnostics) {
            String dependencies = rc.getProfiler().getData(DEPENDENCIES_META_DATA_KEY);
            if (null == dependencies) {
                rc.getProfiler().addData(DEPENDENCIES_META_DATA_KEY, caller);
            } else {
                boolean found = false;
                String[] dependencyList = dependencies.split(DEPENDENCIES_SEPARATOR);
                for (String d : dependencyList) {
                    if (d.equals(caller)) {
                        found = true;
                    }
                }
                if (!found) {
                    dependencies = dependencies + DEPENDENCIES_SEPARATOR + caller;
                    rc.getProfiler().addData(DEPENDENCIES_META_DATA_KEY, dependencies);
                }
            }
        }
    }

    /**
     * Save relationship between task thread and the parent task that instantiated the task.
     */
    static void logParentTaskAndThread(ICallableTask<?> task, String parentTask) {

        ResponseContext rc = task.getContext();
        if (rc.getDiagnosticConfig().profile || rc.getDiagnosticConfig().showDiagnostics) {
            String taskThread = Thread.currentThread().getName();
            rc.getProfiler().addData(PARENT_TASK, parentTask);
            if (taskThread.contains("Workflow")) {
                // only need thread number for workflow threads
                int i = taskThread.indexOf('-');
                if (i > 0) {
                    taskThread = taskThread.substring(i + 1);
                }
            }
            rc.getProfiler().addData(TASK_THREAD, taskThread);
        }
    }

    /**
     * Log task response and its dependent task results to the diagnostics.
     */
    static void addTaskResponseDiagnostic(ICallableTask<?> task, Object result) {
        // log the inputs to the task
        for (ICallableTaskFuture<?> dep : task.getDependencies()) {
            task.getContext().getDiagnostic().addJsonDiagnostic(task.getName(), dep.getTask().getName(), dep.getNoThrow(task));
        }
        // log the result of the task
        task.getContext().getDiagnostic().addJsonDiagnostic(task.getName(), "Response", result);
    }

}
