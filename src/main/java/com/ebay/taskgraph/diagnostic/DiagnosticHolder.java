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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.HttpHeaders;

import com.ebay.taskgraph.service.IServiceInvoker;

public class DiagnosticHolder implements IDiagnosticHolder {

    private final DiagnosticConfig config;

    private Map<String, Diagnostic> diagnostics = Collections.synchronizedMap(new HashMap<String, Diagnostic>());
    private List<ThrowableDiagnostic> throwables = new ArrayList<>();
    private List<JsonDiagnostic> jsons = new ArrayList<>();
    private List<ServiceCallDiagnostic<?, ?>> serviceCalls = new ArrayList<>();

    private DiagnosticHolder(DiagnosticConfig diagnosticConfig) {
        this.config = diagnosticConfig;
    }

    public static IDiagnosticHolder getDiagnosticHolder(DiagnosticConfig showDiagnostics) {

        if (showDiagnostics.showDiagnostics || showDiagnostics.serviceDiagnostics) {
            return new DiagnosticHolder(showDiagnostics);
        }
        return DiagnosticHolderNull.INSTANCE;
    }

    /**
     * Publish all diagnostics.
     * This is an expensive call if there are Throwable or service call diagnostics.
     * Throwable stack traces are generated and the service request & responses are serialized.
     * @return all diagnostics
     */
    @Override
    public List<Diagnostic> getDiagnostics() {
        // add any stack traces & service calls to corresponding existing sender, if present
        for (ThrowableDiagnostic td : this.throwables) {
            appendOrAddDiagnostic(td.getThrowableDiagnostic());
        }
        for (ServiceCallDiagnostic<?, ?> scd : this.serviceCalls) {
            appendOrAddDiagnostic(scd.getServiceCallDiagnostic(this.config.serviceDiagnostics));
        }
        // in case the json object contains a reference to itself, remove object before serializing
        // can happen if requesting the diagnostics of a workflow task
        while (!this.jsons.isEmpty()) {
            JsonDiagnostic jd = this.jsons.remove(this.jsons.size() - 1);
            appendOrAddDiagnostic(jd.getJsonDiagnostic());
        }
        return new ArrayList<>(this.diagnostics.values());
    }

    private void appendOrAddDiagnostic(Diagnostic diag) {
        Diagnostic existingDiag = this.diagnostics.get(diag.getSender());
        if (existingDiag != null) {
            existingDiag.getValue().addAll(diag.getValue());
        } else {
            this.diagnostics.put(diag.getSender(), diag);
        }
    }

    @Override
    public void addThrowableDiagnostic(String sender, Throwable t) {
        if (this.config.showDiagnostics && t != null) {
            this.throwables.add(new ThrowableDiagnostic(sender, t));
        }
    }

    @Override
    public void addJsonDiagnostic(String sender, String type, Object o) {
        if (this.config.showDiagnostics) {
            this.jsons.add(new JsonDiagnostic(sender, type, o));
        }
    }

    @Override
    public void addDiagnostic(String sender, String diagnostic) {
        if (this.config.showDiagnostics) {
            Diagnostic currentValue = this.getDiagnostic(sender);
            currentValue.getValue().add(diagnostic);
        }
    }

    @Override
    public void addDiagnostic(IDiagnosticHolder diagnosticHolder) {
        if (diagnosticHolder instanceof DiagnosticHolder) {
            DiagnosticHolder dh = (DiagnosticHolder) diagnosticHolder;
            Set<String> keySet = dh.diagnostics.keySet();
            if (keySet != null && !keySet.isEmpty()) {
                for (String key : keySet) {
                    List<String> values = dh.getDiagnostic(key).getValue();
                    if (values != null && !values.isEmpty()) {
                        addDiagnostic(key, values);
                    }
                }
            }
            this.throwables.addAll(dh.throwables);
            this.jsons.addAll(dh.jsons);
            this.serviceCalls.addAll(dh.serviceCalls);
        } else {
            List<Diagnostic> diagnostics = diagnosticHolder.getDiagnostics();
            for (Diagnostic diag : diagnostics) {
                this.diagnostics.put(diag.getSender(), diag);
            }
        }
    }

    @Override
    public void addDiagnostic(String sender, List<String> diagnostic) {
        if (this.config.showDiagnostics) {
            Diagnostic currentValue = this.getDiagnostic(sender);
            if (diagnostic != null) {
                currentValue.getValue().addAll(diagnostic);
            }
        }
    }

    private Diagnostic getDiagnostic(String sender) {
        synchronized (this.diagnostics) {
            Diagnostic currentValue = this.diagnostics.get(sender);
            if (null == currentValue) {
                currentValue = DiagnosticHolder.createDiagnostic(sender);
                this.diagnostics.put(sender, currentValue);
            }
            return currentValue;
        }
    }

    static Diagnostic createDiagnostic(String sender) {
        Diagnostic diag = new Diagnostic();
        diag.setSender(sender);
        diag.setValue(Collections.synchronizedList(new ArrayList<String>()));
        return diag;
    }

    @Override
    public <T, V> void addServiceDiagnostic(
            String sender, IServiceInvoker<T, V> client, T request, V response, HttpHeaders headers) {
        this.serviceCalls.add(new ServiceCallDiagnostic<>(sender, client, request, response, headers));
    }

    @Override
    public boolean enabled() {
        return this.config.showDiagnostics;
    }

    @Override
    public DiagnosticConfig getConfig() {
        return this.config;
    }

}
