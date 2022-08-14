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

import javax.ws.rs.core.Response.Status;

/**
 * Application exception that allows tasks to throw exceptions that are propagated to the top level application handler.
 * A task that throws this exception will abort the whole workflow.
 */
public class ApplicationException extends RuntimeException {
    
    private static final long serialVersionUID = -4501388273826057534L;

    public final Status status;
    
    public final Object entity;

    public ApplicationException(Throwable cause, Status status, Object errorResponse) {
        super(cause);
        this.status = status;
        this.entity = errorResponse;
    }

    public ApplicationException(Throwable t) {
        this(t, Status.INTERNAL_SERVER_ERROR, null);
    }

    public ApplicationException(Status status, Object errorResponse) {
        super(errorResponse != null ? errorResponse.toString() : null);
        this.status = status;
        this.entity = errorResponse;
    }

}
