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

public class SampleOrchestrationResponse {

    private String criticalData;
    
    private String optionalData;

    private SampleVisitorResponse visitorResponse;
    
    private long processingTime;

    public String getCriticalData() {
        return criticalData;
    }

    public void setCriticalData(String criticalData) {
        this.criticalData = criticalData;
    }

    public String getOptionalData() {
        return optionalData;
    }

    public void setOptionalData(String optionalData) {
        this.optionalData = optionalData;
    }

    public SampleVisitorResponse getVisitorResponse() {
        return this.visitorResponse;
    }

    public void setVisitorResponse(SampleVisitorResponse visitorResponse) {
        this.visitorResponse = visitorResponse;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }

}
