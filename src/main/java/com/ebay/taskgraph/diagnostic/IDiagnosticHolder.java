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

public interface IDiagnosticHolder {

    List<Diagnostic> getDiagnostics();

    boolean enabled();

    void addThrowableDiagnostic(String sender, Throwable t);

    void addJsonDiagnostic(String sender, String type, Object o);

    void addDiagnostic(IDiagnosticHolder diagnostics);

    void addDiagnostic(String sender, String diagnostic);

    void addDiagnostic(String sender, List<String> diagnostic);

    <T, V> void addServiceDiagnostic(
            String sender,
            IServiceInvoker<T, V> client,
            T request,
            V response,
            HttpHeaders headers);

    DiagnosticConfig getConfig();
}
