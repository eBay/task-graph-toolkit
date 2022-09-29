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

package com.ebay.taskgraph.service;

import com.ebay.taskgraph.executor.CallableTaskConfig;
import com.ebay.taskgraph.executor.ICallableTask;
import com.ebay.taskgraph.executor.NumberTask;
import com.ebay.taskgraph.util.JacksonJsonHelperTest;

import org.junit.Assert;
import org.junit.Test;

import com.ebay.taskgraph.diagnostic.DiagnosticConfig;

public class ServiceInvokerDiagnosticDecoratorTest {
    
    static {
        JacksonJsonHelperTest.INSTANCE.getClass();
    }

    @Test
    public void test() {
        
        IServiceInvoker<Integer, Integer> invoker = new ServiceInvokerTest(5);
        ICallableTask<Integer> task = new NumberTask(new CallableTaskConfig(DiagnosticConfig.NONE, 100), 0);
        IServiceInvoker<Integer, Integer> decorator = new ServiceInvokerDiagnosticDecorator<>(invoker, task);
        long response = decorator.getResponse(5, null);
        Assert.assertEquals(5L, response);
        Assert.assertEquals("{}", decorator.getRequestHeadersDiagnostic(new Headers()));
        Assert.assertEquals("4", decorator.getRequestDiagnostic(4));
        Assert.assertEquals("6", decorator.getResponseDiagnostic(6));
        Assert.assertNull(invoker.convertResponseDiagnostics(8));
    }

    @Test
    public void testException() {
        
        IServiceInvoker<Integer, Integer> invoker = new ServiceInvokerTest(-1);
        ICallableTask<Integer> task = new NumberTask(new CallableTaskConfig(DiagnosticConfig.NONE, 100), 0);
        IServiceInvoker<Integer, Integer> decorator = new ServiceInvokerDiagnosticDecorator<>(invoker, task);
        Assert.assertNull(decorator.getResponse(5, null));
    }

    @Test
    public void testValidationException() {
        
        IServiceInvoker<Integer, Integer> invoker = new ServiceInvokerTest(-2);
        ICallableTask<Integer> task = new NumberTask(new CallableTaskConfig(DiagnosticConfig.NONE, 100), 0);
        IServiceInvoker<Integer, Integer> decorator = new ServiceInvokerDiagnosticDecorator<>(invoker, task);
        try {
            decorator.getResponse(5, null);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }
}
