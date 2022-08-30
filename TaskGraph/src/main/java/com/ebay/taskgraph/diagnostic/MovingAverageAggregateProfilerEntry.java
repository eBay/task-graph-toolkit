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
 * Exponential moving average of a particular profiler entry instance.
 * Tracks min and max duration.
 * Tracks average start time and duration.
 * Also stores profiler meta data.
 */
public class MovingAverageAggregateProfilerEntry {

    private static final int SAMPLE_SIZE = 100;

    private static final int MILLI_SECONDS_PER_CHAR = 10;

    // 10 seconds max
    private static final long MAX_TIME = 10000;

    private String name;

    private long count;

    private long min;

    private long max;

    private long start;

    private long duration;

    private List<ProfilerProperty> data;

    public MovingAverageAggregateProfilerEntry() {        
    }

    public MovingAverageAggregateProfilerEntry(ProfilerModel pm) {
        this.count = 1;
        this.name = pm.getName();
        this.min = pm.getDuration();
        this.max = pm.getDuration();
        this.duration = pm.getDuration();
        this.start = pm.getStartTime();
        this.setData(pm.getData());
    }

    public MovingAverageAggregateProfilerEntry(String name, MovingAverageAggregateProfilerEntry value) {
        this.name = name;
        this.count = value.count;
        this.min = value.min;
        this.max = value.max;
        this.duration = value.duration;
        this.start = value.start;
        this.data = value.data;
    }

    public void add(ProfilerModel pm) {

        // if duration is negative, the profiler was instantiated but never started
        // this could be because a task's dependency throws an application exception
        // this is normal error condition so don't log 
        if (pm.getDuration() < 0) {
            return;
        }
        // calculate moving averages of start time & duration
        long sampleSize = getSampleSize();
        this.start *= sampleSize;
        this.start += pm.getStartTime();
        this.start /= sampleSize + 1;

        this.duration *= sampleSize;
        this.duration += pm.getDuration();
        this.duration /= sampleSize + 1;
        ++this.count;

        // track absolute min
        if (pm.getDuration() < this.min) {
            this.min = pm.getDuration();
        }
        // track absolute max
        if (pm.getDuration() > this.max) {
            this.max = pm.getDuration();
        }
    }

    private long getSampleSize() {
        // ramp up to SAMPLE_SIZE, so early samples don't weigh so heavily
        return this.count < SAMPLE_SIZE ? this.count : SAMPLE_SIZE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public List<ProfilerProperty> getData() {
        return data;
    }

    public void setData(List<ProfilerProperty> data) {
        this.data = data;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(Long.toString(this.getCount()));
        sb.append(':');
        int spaces = getSpaces(this.min);
        for (int i = 0; i < spaces; ++i) {
            sb.append(' ');
        }
        sb.append('|');
        long startAfterMin = this.start - this.min;
        spaces = getSpaces(startAfterMin);
        for (int i = 0; i < spaces; ++i) {
            sb.append('_');
        }
        sb.append('|');
        spaces = getSpaces(this.duration);
        for (int i = 0; i < spaces; ++i) {
            sb.append('-');
        }
        sb.append('|');
        long maxAfterDuration = this.max - this.start - this.duration;
        spaces = getSpaces(maxAfterDuration);
        for (int i = 0; i < spaces; ++i) {
            sb.append('_');
        }
        sb.append('|');
        sb.append(this.name);
        sb.append(':');
        sb.append(this.min);
        sb.append(':');
        sb.append(this.start);
        sb.append(':');
        sb.append(this.duration);
        sb.append(':');
        sb.append(this.max);
        return sb.toString();
    }

    /**
     * Get spaces for time.  Limit count in case the time is off due to profiler
     * not being stopped or started properly.
     * @param time
     * @return
     */
    private static int getSpaces(long time) {

        if (time < 0) {
            time = 0;
        }
        if (time > MAX_TIME) {
            time = MAX_TIME;
        }
        int spaces = (int) (time / MILLI_SECONDS_PER_CHAR);
        return spaces;
    }

}
