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

package com.ebay.ap.executor.workflow;

public class SampleOrchestrationRequest {

    public final long firstTime;
    public final long secondTime;
    public final boolean addOptional;
    public final String criticalData;
    
    public SampleOrchestrationRequest(long firstTime,
            long secondTime,
            boolean addOptional,
            String criticalData) {

        this.firstTime = firstTime;
        this.secondTime = secondTime;
        this.addOptional = addOptional;
        this.criticalData = criticalData;
    }
    
    public SampleOrchestrationRequest(long firstTime, long secondTime, boolean addOptional) {
        this(firstTime, secondTime, addOptional, "critical");
    }
    
    public SampleOrchestrationRequest(long firstTime, long secondTime) {
        this(firstTime, secondTime, true, "critical");
    }
}
