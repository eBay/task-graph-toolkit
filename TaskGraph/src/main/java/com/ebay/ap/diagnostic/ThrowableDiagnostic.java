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

import java.util.ArrayList;
import java.util.List;

class ThrowableDiagnostic {

    private final String sender;

    private final Throwable throwable;

    ThrowableDiagnostic(String sender, Throwable throwable) {
        this.sender = sender;
        this.throwable = throwable;
    }

    static List<String> getThrowableDiagnosticValue(Throwable t) {

        List<String> value = new ArrayList<>();
        value.add(t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName());
        ThrowableDiagnostic.addStackTrace(value, t);
        int maxDepth = 10;  // sometimes exception causes will refer to themselves
        while (t.getCause() != null && maxDepth-- > 0) {
            t = t.getCause();
            value.add("Caused by: " + t.getMessage());
            ThrowableDiagnostic.addStackTrace(value, t);
        }
        return value;
    }

    private static void addStackTrace(List<String> diagnostic, Throwable t) {

        for (StackTraceElement s : t.getStackTrace()) {
            diagnostic.add(s.toString());
        }
    }

    public Diagnostic getThrowableDiagnostic() {
        Diagnostic diagnostic = new Diagnostic();
        diagnostic.setSender(this.sender);
        List<String> value = getThrowableDiagnosticValue(this.throwable);
        diagnostic.setValue(value);
        return diagnostic;
    }

}
