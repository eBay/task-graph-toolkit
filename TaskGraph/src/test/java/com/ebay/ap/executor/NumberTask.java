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

public class NumberTask extends Task implements ICallableTask<Integer> {

    private final Integer value;

    protected NumberTask(CallableTaskConfig config, int val) {
        super(NumberTask.class.getSimpleName() + Integer.toString(val), config);
        this.value = val;
    }

    @Override
    public Integer call() throws InterruptedException {
        switch (this.value)  {
        case -1:
            throw new ApplicationException(Status.BAD_REQUEST, null);

        case -2:
            throw new RuntimeException();

        case -3:
            throw new ApplicationException(new RuntimeException());

        case -4:
        case -5:
            Thread.sleep(100);
            break;
        }
        return this.value;
    }

}
