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

import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import com.ebay.ap.diagnostic.IProfilerEntry;
import com.ebay.ap.workflow.service.IServiceInvoker;
import com.ebay.ap.workflow.service.ServiceInvokerExceptionError;

/**
 * Invokes a service through the IServiceInvoker interface and tracks any exception thrown.
 * Typically invocation is done through an orchestration task thread with initialization
 * of the request done in the code that creates the task.
 *
 * @param <T>
 * @param <S>
 */
public class ServiceInvokerDiagnosticDecorator<T, S> implements IServiceInvoker<T, S> {

    /**
     * Client com.ebay.raptor.searchsvc.universal.app.invoker is either the actual remote service client or the service mock implementation.
     */
    private final IServiceInvoker<T, S> client;

    /**
     * Direct service invocations we may not want to log request/response.
     */
    private final boolean logServiceDiagnostic;

    /**
     * Owning task where we should log the diagnostics.
     */
    private ICallableTask<?> parentTask;

    public ServiceInvokerDiagnosticDecorator(
            IServiceInvoker<T, S> client,
            ICallableTask<?> task,
            boolean logServiceDiagnostic) {

        this.client = client;
        this.parentTask = task;
        this.logServiceDiagnostic = logServiceDiagnostic;
    }

    public ServiceInvokerDiagnosticDecorator(
            IServiceInvoker<T, S> client,
            ICallableTask<?> task) {

        this(client, task, true);
    }

    public S getResponse(T request, HttpHeaders headers) {

        S response = null;
        IProfilerEntry entry = this.parentTask.getContext().getProfiler().newEntry("getResponse"); 
        try {
            response = this.client.getResponse(request, headers);

            if (this.logServiceDiagnostic) {
                // Use the task name to log the request & response in case there are multiple instances of the same task.
                // It's fine to log errors to the common response context name because they are generally only useful in the aggregate.
                // Service requests & responses are most useful when debugging individual instances of a use case where you need to
                // distinguish between the diagnostics of each instance of the task.
                this.parentTask.getContext().getDiagnostic().addServiceDiagnostic(
                        this.parentTask.getName(), this.client, request, response, headers);
            }
        } catch (ApplicationException bae) {
            // mock implementation of service invoker may throw validation exceptions
            // which are now ApplicationExceptions, so propagate these
            throw bae;
        } catch (Throwable t) {
            Task.logTaskException(this.parentTask, t);
            this.parentTask.getContext().getDiagnostic().addServiceDiagnostic(
                    this.parentTask.getContext().getName(), this.client, request, null, headers);
            this.parentTask.getContext().getError().addError(
                    new ServiceInvokerExceptionError(this.parentTask.getContext().getName(), t));
        } finally {
            this.parentTask.getContext().getProfiler().add(entry);
        }
        return response;
    }

    @Override
    public String getRequestHeadersDiagnostic(HttpHeaders headers) {
        return this.client.getRequestHeadersDiagnostic(headers);
    }

    @Override
    public String getRequestDiagnostic(T request) {
        return this.client.getRequestDiagnostic(request);
    }

    @Override
    public String getResponseDiagnostic(S response) {
        return this.client.getResponseDiagnostic(response);
    }

    @Override
    public List<String> convertResponseDiagnostics(S response) {
        return this.client.convertResponseDiagnostics(response);
    }

}
