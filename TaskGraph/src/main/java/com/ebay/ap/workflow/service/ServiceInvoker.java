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

package com.ebay.ap.workflow.service;

import java.util.List;
import javax.ws.rs.core.HttpHeaders;

import com.ebay.ap.util.JsonHelper;

public abstract class ServiceInvoker<T, V> implements IServiceInvoker<T, V> {

    @Override
    public String getRequestHeadersDiagnostic(HttpHeaders headers) {
        return getHeadersDiagnostic(headers);
    }

    @Override
    public String getRequestDiagnostic(T request) {
        return JsonHelper.writeAsString(request);
    }

    @Override
    public String getResponseDiagnostic(V response) {
        return JsonHelper.writeAsString(response);
    }

    @Override
    public List<String> convertResponseDiagnostics(V response) {
        return null;
    }

    private String getHeadersDiagnostic(HttpHeaders headers) {
        if (null == headers) {
            return null;
        }
        return JsonHelper.writeAsString(headers.getRequestHeaders());
    }
}
