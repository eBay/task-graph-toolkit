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

package com.ebay.taskgraph.service;

import java.util.List;
import javax.ws.rs.core.HttpHeaders;

/**
 * Generic interface that invokers of provider services need to implement.
 * Allows these responses to be easily mocked out with ServiceMock.
 */
public interface IServiceInvoker<T, V> {

    V getResponse(T request, HttpHeaders headers);

    /**
     * Serialize the request http headers to a string for the diagnostic
     * @param headers
     * @return
     */

    String getRequestHeadersDiagnostic(HttpHeaders headers);

    /**
     * Serialize the request to a string for the diagnostic response.
     * 
     * @param request
     * @return
     */
    String getRequestDiagnostic(T request);

    /**
     * Serialize the response to a string for the diagnostic response.
     * 
     * @param response
     * @return
     */
    String getResponseDiagnostic(V response);

    /**
     * Convert external diagnostics in the response to a list of strings.
     * 
     * @param response
     * @return
     */
    List<String> convertResponseDiagnostics(V response);
}
