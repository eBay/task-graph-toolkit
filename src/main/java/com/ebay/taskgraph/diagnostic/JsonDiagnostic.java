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
import java.util.List;

import com.ebay.taskgraph.util.JsonHelper;

/**
 * Diagnostic that holds an object that will be serialized to JSON when the diagnostic response is generated.
 * Deferring the serialization removes the overhead from the task processing so the profiling of the task execution is more accurate.
 */
public class JsonDiagnostic {

    private final String sender;
    private final String type;
    private final Object object;

    public JsonDiagnostic(String sender, String type, Object o) {
        this.sender = sender;
        this.type = type;
        this.object = o;
    }

    public Diagnostic getJsonDiagnostic() {

        List<String> value = new ArrayList<>();
        try {
            String diag = JsonHelper.writeAsString(this.object);
            value.add(this.type + ':' + diag);
        } catch (Throwable t) {
            String diag = this.object != null ? this.object.toString() : "null";
            value.add(this.type + ':' + diag);
            value.add(this.type + " JSON exception:" + t.getMessage());
        }

        Diagnostic diagnostic = new Diagnostic();
        diagnostic.setSender(this.sender);
        diagnostic.setValue(value);
        return diagnostic;
    }
}
