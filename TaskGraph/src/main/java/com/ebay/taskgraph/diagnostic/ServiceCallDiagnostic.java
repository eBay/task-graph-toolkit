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

package com.ebay.taskgraph.diagnostic;

import java.util.List;
import javax.ws.rs.core.HttpHeaders;

import com.ebay.taskgraph.service.IServiceInvoker;

public class ServiceCallDiagnostic<T, V> {

    private final String sender;

    private final IServiceInvoker<T, V> client;

    private final T request;

    private final V response;

    private final HttpHeaders headers;

    public ServiceCallDiagnostic(
            String sender,
            IServiceInvoker<T, V> client,
            T request,
            V response,
            HttpHeaders headers) {

        this.sender = sender;
        this.client = client;
        this.request = request;
        this.response = response;
        this.headers = headers;
    }


    public Diagnostic getServiceCallDiagnostic(boolean serviceDiagnostics) {

        String diagName = this.sender;
        Diagnostic diag = DiagnosticHolder.createDiagnostic(diagName);
        // always log diagnostic response from service
        List<String> serviceDiag = this.getServiceDiagnostic();
        if (serviceDiag != null) {
            diag.getValue().addAll(serviceDiag);
        }

        // log request/response if requested
        if (serviceDiagnostics) {
            this.getRequestHeadersDiagnostic(diag.getValue());
            this.getRequestDiagnostic(diag.getValue());
            if (this.response != null) {
                this.getResponseDiagnostic(diag.getValue());
            }
        }
        return diag;
    }

    private List<String> getServiceDiagnostic() {

        try {
            return this.client.convertResponseDiagnostics(this.response);
        } catch (Throwable t) {
            return ThrowableDiagnostic.getThrowableDiagnosticValue(t);
        }
    }

    private void getRequestDiagnostic(List<String> diag) {

        try {
            diag.add("Request:" + this.client.getRequestDiagnostic(this.request));
        } catch (Throwable t) {
            diag.addAll(ThrowableDiagnostic.getThrowableDiagnosticValue(t));
        }
    }

    private void getResponseDiagnostic(List<String> diag) {

        try {
            diag.add("Response:" + this.client.getResponseDiagnostic(this.response));
        } catch (Throwable t) {
            diag.addAll(ThrowableDiagnostic.getThrowableDiagnosticValue(t));
        }
    }

    private void getRequestHeadersDiagnostic(List<String> diag) {
        String headers = null;
        try {
            headers = this.client.getRequestHeadersDiagnostic(this.headers);
        } catch (Throwable t) {
            diag.addAll(ThrowableDiagnostic.getThrowableDiagnosticValue(t));
            return;
        }
        if (headers != null && !headers.isEmpty()) {
            diag.add("Headers:" + headers);
        }
    }
}
