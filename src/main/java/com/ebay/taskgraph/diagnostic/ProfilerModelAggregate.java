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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Data model for aggregating profiling models.
 */
public class ProfilerModelAggregate {

    private int count;

    private long meanDuration = 0L;

    private long maxDuration = 0L;

    private long minDuration = Long.MAX_VALUE;

    private long percentile95th = 0L;

    private ProfilerModel profiler;

    public ProfilerModelAggregate(String name) {
        this.profiler = new ProfilerModel();
        this.profiler.setName(name);
        // children will be all of the individual profilers
        this.profiler.setChildren(new ArrayList<ProfilerModel>());
    }

    // create instance with aggregated results
    public ProfilerModelAggregate(ProfilerModelAggregate pma) {
        this.count = pma.profiler.getChildren().size();
        this.profiler = this.getAverage(pma);
    }

    public int getCount() {
        return count;
    }

    private ProfilerModel getAverage(ProfilerModelAggregate pma) {

        ProfilerModel average = new ProfilerModel();

        List<ProfilerModel> children = pma.profiler.getChildren();
        Collections.sort(children, new ProfileComparator());
        average.setName(pma.profiler.getName());

        int percentileIndex = (int) (.05 * (double) children.size());
        // this will contain median duration
        average.setDuration(children.get(children.size() / 2).getDuration());
        this.percentile95th = children.get(percentileIndex).getDuration();

        // make start time median
        Collections.sort(children, new ProfileStartTimeComparator());
        average.setStartTime(children.get(children.size() / 2).getStartTime());

        for (ProfilerModel pm : children) {
            long duration = pm.getDuration();
            this.meanDuration += duration;
            if (duration > this.maxDuration) {
                this.maxDuration = duration;
            }
            if (duration < this.minDuration) {
                this.minDuration = duration;
            }
        }

        this.meanDuration = this.meanDuration / children.size();
        return average;
    }

    private static class ProfileComparator implements Comparator<ProfilerModel>, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public int compare(ProfilerModel o1, ProfilerModel o2) {
            return (int) (o2.getDuration() - o1.getDuration());
        }

    }

    private static class ProfileStartTimeComparator implements Comparator<ProfilerModel>, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public int compare(ProfilerModel o1, ProfilerModel o2) {
            return (int) (o2.getStartTime() - o1.getStartTime());
        }

    }

    public void add(ProfilerModel model) {
        this.profiler.getChildren().add(model);
    }

    public ProfilerModel getModel() {
        return this.profiler;
    }

    public long getPercentile95th() {
        return this.percentile95th;
    }

    public long getMeanDuration() {
        return this.meanDuration;
    }

    public long getMinDuration() {
        return this.minDuration;
    }

    public long getMaxDuration() {
        return this.maxDuration;
    }

}
