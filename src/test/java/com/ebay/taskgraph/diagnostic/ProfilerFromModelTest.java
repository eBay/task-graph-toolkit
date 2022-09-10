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

import com.ebay.taskgraph.executor.CallableTaskConfig.ExecType;
import com.ebay.taskgraph.executor.Task;
import com.ebay.taskgraph.util.JacksonJsonHelperTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class ProfilerFromModelTest {

    @Test
    public void profilerFromModelTest() {
        ProfilerModel pm = JacksonJsonHelperTest.INSTANCE.readTestResourceJsonFile("sample_profiler.json", ProfilerModel.class);
        // check the source profiler is valid
        ProfilerValidator.validate(pm);
        Profiler parentProfiler = new Profiler("test");
        IProfiler profiler = new ProfilerFromModel(pm, parentProfiler);
        pm = profiler.getModel(0);
        // need the parent profiler model
        ProfilerModel parent = new ProfilerModel();
        parent.setName("test");
        parent.setChildren(Collections.singletonList(pm));
        ProfilerProperty prop = new ProfilerProperty();
        prop.setName(Task.EXEC_TYPE);
        prop.setValue(ExecType.ASYNC.name());
        List<ProfilerProperty> data = new ArrayList<>();
        data.add(prop);
        parent.setData(data);
        // check the transformed profiler is valid
        ProfilerValidator.validate(parent);
    }
}
