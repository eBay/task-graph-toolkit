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

package com.ebay.taskgraph.context;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ErrorHolder {

    private List<IError> error = new CopyOnWriteArrayList<>();

    public void addError(IError error) {
        this.error.add(error);
    }

    public void add(ErrorHolder error) {
        this.error.addAll(error.error);
    }

    public boolean hasError(String providerId) {

        boolean rval = false;
        for (IError e : this.error) {
            if (e.getId().equals(providerId)) {
                rval = true;
                break;
            }
        }
        return rval;
    }

    public boolean hasError() {
        boolean rval = false;
        for (IError e : this.error) {
            if (e.isSevereError()) {
                rval = true;
                break;
            }
        }
        return rval;
    }

    public List<IError> getErrors() {
        return this.error;
    }
}
