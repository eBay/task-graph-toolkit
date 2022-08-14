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

package com.ebay.ap.diagnostic;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import com.ebay.ap.workflow.service.IServiceInvoker;

/**
 * Null object implementation of diagnostics.
 */
public class DiagnosticHolderNull implements IDiagnosticHolder {

    public static final DiagnosticHolderNull INSTANCE = new DiagnosticHolderNull();
    public static final DiagnosticConfig NULL = new DiagnosticConfig(false, false, false);

    @Override
    public List<Diagnostic> getDiagnostics() {
        return Collections.emptyList();
    }

    @Override
    public void addThrowableDiagnostic(String sender, Throwable t) {
    }

    @Override
    public void addJsonDiagnostic(String sender, String type, Object o) {
    }

    @Override
    public void addDiagnostic(IDiagnosticHolder diagnostics) {
    }

    @Override
    public void addDiagnostic(String sender, String diagnostic) {
    }

    @Override
    public void addDiagnostic(String sender, List<String> diagnostic) {
    }

    @Override
    public <T, V> void addServiceDiagnostic(
            String sender,
            IServiceInvoker<T, V> client,
            T request,
            V response,
            HttpHeaders headers) {
    }

    @Override
    public boolean enabled() {
        return false;
    }

    @Override
    public DiagnosticConfig getConfig() {
        return NULL;
    }
}
