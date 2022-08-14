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

import com.ebay.ap.executor.CallableTaskResultNull;
import com.ebay.ap.executor.ICallableTaskFuture;

public class DataSourceFactory {
    
    public static class Factory1 implements ITaskFactory<WaitForCriticalDataOnlyBuilder, String> {
    
        public static final Factory1 INSTANCE = new Factory1();
    
        @Override
        public ICallableTaskFuture<String> create(WaitForCriticalDataOnlyBuilder builder) {

            ICallableTaskFuture<String> task = builder.addTask(new DataSourceTask("critical", builder.async, builder.request.criticalData, builder.request.firstTime));
            builder.responseVisitors.add(new CriticalResponseVisitor(builder.sync, task, builder.getTask(DataSourceFactory.Factory3.INSTANCE)));
            return task;
        }
    
    }
    
    public static class Factory2 implements ITaskFactory<WaitForCriticalDataOnlyBuilder, String> {
    
        public static final Factory2 INSTANCE = new Factory2 ();
    
        @Override
        public ICallableTaskFuture<String> create(WaitForCriticalDataOnlyBuilder builder) {
            if (builder.request.addOptional) {
                ICallableTaskFuture<String> task = builder.addTask(new DataSourceTask("optional", builder.async, "optional", builder.request.secondTime));
                builder.responseVisitors.add(new OptionalResponseVisitor(builder.sync, task, builder.getTask(DataSourceFactory.Factory3.INSTANCE)));
                return task;
            }
            return new CallableTaskResultNull<>();
        }
    
    }
    
    public static class Factory3 implements ITaskFactory<WaitForCriticalDataOnlyBuilder, String> {
    
        public static final Factory3 INSTANCE = new Factory3();
    
        @Override
        public ICallableTaskFuture<String> create(WaitForCriticalDataOnlyBuilder builder) {
            return builder.addTask(new DataSourceTask("visitor_data", builder.sync, "visited", 20L));
        }
    
    }

}
