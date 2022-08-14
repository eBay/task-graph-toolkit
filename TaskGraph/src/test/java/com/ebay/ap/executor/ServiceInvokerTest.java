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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;

import com.ebay.ap.workflow.service.IServiceInvoker;

public class ServiceInvokerTest implements IServiceInvoker<Integer, Integer> {

    private final int value;

    public ServiceInvokerTest(int value) {
        this.value = value;
    }

    @Override
    public Integer getResponse(Integer request, HttpHeaders headers) {
        switch (this.value)  {
        case -1:
            throw new RuntimeException("blah");

        case -2:
            throw new ApplicationException(Status.BAD_REQUEST, null);
        }
        return this.value;
    }

    @Override
    public String getRequestHeadersDiagnostic(HttpHeaders headers) {
        return "Header Diagnostics";
    }

    @Override
    public String getRequestDiagnostic(Integer request) {
        return request.toString();
    }

    @Override
    public String getResponseDiagnostic(Integer response) {
        return response.toString();
    }

    @Override
    public List<String> convertResponseDiagnostics(Integer response) {
        List<String> rval = new ArrayList<>();
        rval.add(response.toString());
        return rval;
    }

}
