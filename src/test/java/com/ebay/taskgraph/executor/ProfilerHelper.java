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

import java.util.List;

import com.ebay.taskgraph.context.ResponseContext;
import com.ebay.taskgraph.diagnostic.Diagnostic;
import com.ebay.taskgraph.diagnostic.ProfilerValidator;

public class ProfilerHelper {
    
    public static void print(ResponseContext rc) {
        List<Diagnostic> diag = rc.getProfiler().getDiagnostics();
        if (diag != null && diag.size() > 0 && diag.get(0).getValue() != null && diag.get(0).getValue().size() > 0) {
            System.out.println(diag.get(0).getValue().get(0));
        }
        ProfilerValidator.validate(rc.getProfiler());
    }

}
