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

package com.ebay.taskgraph.diagnostic;

import java.util.List;

/**
 * Data model for logging diagnostics.
 */
public class ProfilerModel {

    private String name;

    private long startTime;

    private long duration;

    private List<ProfilerModel> children;

    private List<ProfilerProperty> data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public List<ProfilerModel> getChildren() {
        return children;
    }

    public void setChildren(List<ProfilerModel> children) {
        this.children = children;
    }

    public List<ProfilerProperty> getData() {
        return data;
    }

    public void setData(List<ProfilerProperty> data) {
        this.data = data;
    }
}
