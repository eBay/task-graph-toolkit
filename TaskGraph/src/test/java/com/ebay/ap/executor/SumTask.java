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

public class SumTask extends Task implements ICallableTask<Integer> {

    private final ICallableTaskFuture<Integer>[] numbers;

    @SafeVarargs
    protected SumTask(CallableTaskConfig config, ICallableTaskFuture<Integer> ... numbers) {
        super(config, numbers);
        this.numbers = numbers;
    }

    @Override
    public Integer call() throws Exception {
        int sum = 0;
        for (ICallableTaskFuture<Integer> number : this.numbers) {
            Integer val = number.getNoThrow(this); 
            if (val != null) {
                sum += val;
            }
        }
        return sum;
    }

}