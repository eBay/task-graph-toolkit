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

import com.ebay.ap.context.IError;

public class ServiceInvokerExceptionError implements IError {

    public static final String ID = "SERVICE_PROVIDER_ERROR";

    private final String sender;

    private final Throwable exception;

    public ServiceInvokerExceptionError(String sender, Throwable t) {
        this.sender = sender;
        this.exception = t;
    }

    @Override
    public String getId() {
        return ServiceInvokerExceptionError.ID;
    }

    @Override
    public boolean isSevereError() {
        return false;
    }

    @Override
    public String getCause() {
        return this.sender + ':' + this.exception.getMessage();
    }

    @Override
    public String getExceptionId() {
        return this.exception.getClass().getCanonicalName();
    }

    @Override
    public Object[] getParams() {
        return IError.NULL_PARAMS;
    }

}
