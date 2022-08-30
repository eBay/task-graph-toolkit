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

package com.ebay.taskgraph.context;

import java.util.*;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ebay.taskgraph.diagnostic.Diagnostic;
import com.ebay.taskgraph.diagnostic.DiagnosticConfig;
import com.ebay.taskgraph.diagnostic.ProfilerModel;
import com.ebay.taskgraph.util.JacksonJsonHelperTest;
import com.ebay.taskgraph.util.JsonHelper;

import static org.junit.Assert.*;

public class ResponseContextTest {
    
    @BeforeClass
    public static void init() {
        JacksonJsonHelperTest.INSTANCE.getClass();
    }

    @Test
    public void stringTrackingTypeAddedToTrackingMap(){
        ResponseContext responseContext = new ResponseContext();
        responseContext.addTracking("strTag", "strVal");

        Map<String, List<String>> tracking = responseContext.getTracking();
        assertNotNull(tracking);
        assertNotNull(tracking.get("strTag"));
        assertEquals("strVal", tracking.get("strTag").get(0));
    }

    @Test
    public void strTagKeyAndIntValAddedToTrackingCanBeRetrievedByType(){
        ResponseContext responseContext = new ResponseContext();
        responseContext.addTracking(new TrackingKey("TAG", "strTagKey"), 3432);

        Map<ITrackingKey, Object> tracking = responseContext.getTrackingByType("TAG");
        TrackingKey tagKey = new TrackingKey("TAG", "strTagKey");
        assertNotNull(tracking);
        assertNotNull(tracking.get(tagKey));
        assertEquals("strTagKey", tagKey.getKey());
        assertEquals(3432, tracking.get(tagKey));
    }

    @Test
    public void strTagKeyTypeAndStrValAddedToTrackingCanBeRetrievedByType(){
        ResponseContext responseContext = new ResponseContext();
        responseContext.addTracking(new TrackingKey("TAG", "strTagKey"), "strVal");

        Map<ITrackingKey, Object> tracking = responseContext.getTrackingByType("TAG");
        TrackingKey tagKey = new TrackingKey("TAG", "strTagKey");
        assertNotNull(tracking);
        assertNotNull(tracking.get(tagKey));
        assertEquals("strTagKey", tagKey.getKey());
        assertEquals("strVal", tracking.get(tagKey));
        
        // check duplicate key doesn't change first value
        responseContext.addTracking(new TrackingKey("TAG", "strTagKey"), "strVal2");
        assertEquals("strVal", tracking.get(tagKey));
    }

    @Test
    public void strTagKeyTypeAndStrValAddedToTrackingCannotBeRetriedWithoutType(){
        ResponseContext responseContext = new ResponseContext();
        responseContext.addTracking(new TrackingKey("TAG", "strTagKey"), "strVal");

        Map<String, List<String>> tracking = responseContext.getTracking();
        assertNotNull(tracking);
        assertEquals(0, tracking.size());
    }

    @Test
    public void intTagKeyTypeAndBoolValAddedToTrackingCanBeRetrievedByType(){
        ResponseContext responseContext = new ResponseContext();
        responseContext.addTracking(new TrackingKey("FLAG", 100), Boolean.TRUE);

        Map<ITrackingKey, Object> tracking = responseContext.getTrackingByType("FLAG");
        TrackingKey tagKey = new TrackingKey("FLAG", 100);
        assertNotNull(tracking);
        assertNotNull(tracking.get(tagKey));
        assertEquals(100, tagKey.getKey());
        assertEquals(Boolean.TRUE, tracking.get(tagKey));
    }

    @Test
    public void getTrackingReturnsEmptyMapWhenNoDataIsAdded(){
        ResponseContext responseContext = new ResponseContext();
        Map<String, List<String>> tracking = responseContext.getTracking();
        assertNotNull(tracking);
        assertEquals(true, tracking.isEmpty());
    }

    @Test
    public void getTrackingByStrTypeReturnsEmptyListWhenNoDataIsAdded(){
        ResponseContext responseContext = new ResponseContext();
        List<String> tracking = responseContext.getTracking("strType");
        assertNotNull(tracking);
        assertEquals(0, tracking.size());
    }

    @Test
    public void getTrackingByTypeReturnsEmptyListWhenNoDataIsAdded(){
        ResponseContext responseContext = new ResponseContext();
        Map<ITrackingKey, Object> tracking = responseContext.getTrackingByType("someType");
        assertNotNull(tracking);
        assertEquals(true, tracking.isEmpty());
    }

    @Test
    public void aggregateTracking(){
        ResponseContext responseContext = new ResponseContext();
        responseContext.addTracking("strTag", "strVal");

        Map<String, List<String>> tracking = responseContext.getTracking();
        assertNotNull(tracking);
        assertNotNull(tracking.get("strTag"));
        assertEquals("strVal", tracking.get("strTag").get(0));

        ResponseContext responseContext1 = responseContext.newContext("1");
        responseContext1.addTracking("strTag", "strVal2");
        responseContext1.addTracking("strTag2", "strTag2Val");
        
        responseContext.add(responseContext1);

        tracking = responseContext.getTracking();
        assertNotNull(tracking);
        assertNotNull(tracking.get("strTag"));
        assertEquals("strVal", tracking.get("strTag").get(0));
        assertEquals("strVal2", tracking.get("strTag").get(1));
        assertEquals("strTag2Val", tracking.get("strTag2").get(0));

        assertEquals("strTag2Val", responseContext.getTracking("strTag2").get(0));
        assertEquals(0, responseContext.getTracking("strTag3").size());

    }

    @Test
    public void profilerTest(){
        ResponseContext responseContext = new ResponseContext(new DiagnosticConfig(false, false, true), "top");
        responseContext.getProfiler().start();
        ResponseContext responseContext1 = responseContext.newContext("mid");
        responseContext1.getProfiler().add(responseContext1.newProfiler("prof"));
        responseContext.add(responseContext1);
        responseContext.getProfiler().stop();
        ProfilerModel profilerModel = responseContext.getProfiler().getModel(0);
        String profiler = JsonHelper.writeAsString(profilerModel);
        assertNotNull(profiler);
        assertTrue(profiler.contains("top"));
        assertTrue(profiler.contains("mid"));
        assertTrue(profiler.contains("prof"));
    }

    @Test
    public void errorTest(){
        ResponseContext responseContext = new ResponseContext();
        responseContext.getError().addError(createError("1234", true));
        ResponseContext responseContext1 = responseContext.newContext("1");
        responseContext1.getError().addError(createError("4321", false));
        responseContext.add(responseContext1);
        assertFalse(responseContext.getError().hasError("123"));
        assertTrue(responseContext.getError().hasError("1234"));
        assertTrue(responseContext.getError().hasError("4321"));
        assertEquals(2, responseContext.getError().getErrors().size());
        assertFalse(responseContext1.getError().hasError());
        assertTrue(responseContext.getError().hasError());
    }
    
    private IError createError(final String id, final boolean severeError) {
        return new IError() {

            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getCause() {
                return null;
            }

            @Override
            public String getExceptionId() {
                return null;
            }

            @Override
            public boolean isSevereError() {
                return severeError;
            }

            @Override
            public Object[] getParams() {
                return null;
            }
        };
    }
    
    @Test
    public void jsonDiagnosticTest() {
        ResponseContext rc = new ResponseContext(new DiagnosticConfig(true, false, false), "test");
        rc.getDiagnostic().addJsonDiagnostic("blah", "error", createError("123", true));
        List<Diagnostic> diags = rc.getDiagnostic().getDiagnostics();
        Assert.assertEquals(1, diags.size());
        Assert.assertTrue(diags.get(0).getValue().get(0).startsWith("error:{\""));
    }
}
