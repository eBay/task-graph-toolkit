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

package com.ebay.ap.diagnostic;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.ws.rs.core.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.ap.util.JsonHelper;

public class DiagnosticConfig {

    public static final String SHOWDIAG = "showdiag";
    public static final String SERVICEDOWNLOAD = "forceServiceDownload";
    public static final String PROFILE = "profile";
    public static final String TASKDIAG = "taskdiag";
    public static final String TASKMOCKS = "taskmocks";

    public static final DiagnosticConfig NONE;

    private static final long MAX_COUNT = Long.MAX_VALUE / 2;
    private static long COUNT = 0;

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagnosticConfig.class);
    
    static {
        NONE = new DiagnosticConfig(false, false, false);
    }

    public final boolean showDiagnostics;

    public final boolean serviceDiagnostics;

    public final boolean profile;
    
    /**
     * Set of task names that we want to add the task result to the diagnostics.
     */
    private final Set<String> taskDiagnostics;
    
    /**
     * Mock data for tasks passed in taskmocks header
     */
    private final Map<String, Object> taskMocks;

    public DiagnosticConfig(HttpHeaders headers) {
        this(headers, 0d);
    }

    public DiagnosticConfig(HttpHeaders headers, double profilePercentage) {
        this(isTrue(headers, SHOWDIAG),
                isTrue(headers, SERVICEDOWNLOAD),
                isTrue(headers, PROFILE) || doProfile(profilePercentage, COUNT),
                initializeTaskDiagnostics(getHeaderValue(headers, TASKDIAG)),
                initializeTaskData(getHeaderValue(headers, TASKMOCKS)));
    }

    public DiagnosticConfig(boolean showDiagnostics, boolean serviceDiagnostics, boolean profile) {
        this(showDiagnostics, serviceDiagnostics, profile, Collections.<String>emptySet(), Collections.<String, Object>emptyMap());
        incrementCount();
    }

    public DiagnosticConfig(boolean showDiagnostics,
            boolean serviceDiagnostics,
            boolean profile,
            Set<String> taskDiagnostics,
            Map<String, Object> taskmocks) {
        this.showDiagnostics = showDiagnostics;
        this.serviceDiagnostics = serviceDiagnostics;
        this.profile = profile;
        this.taskDiagnostics = taskDiagnostics;
        this.taskMocks = taskmocks;
        incrementCount();
    }

    private static Set<String> initializeTaskDiagnostics(String headerValue) {
        Set<String> taskDiagnostics = Collections.emptySet();
        if (headerValue != null && !headerValue.isEmpty()) {
            taskDiagnostics = new HashSet<>();
            String[] values = headerValue.split(",");
            for (String val : values) {
                taskDiagnostics.add(val.trim());
            }
        }
        return taskDiagnostics;
    }


    private static Map<String, Object> initializeTaskData(String headerValue) {
        Map<String, Object> taskData = Collections.emptyMap();
        if (headerValue != null && !headerValue.isEmpty()) {
            taskData = new HashMap<>();
            List<Object> props = JsonHelper.readJsonStringAsList(headerValue);
            if (props != null) {
                for (Object prop : props) {
                    TaskData td = JsonHelper.convertValue(prop, TaskData.class);
                    taskData.put(td.getName(), td.getValue());
                }
            }
        }
        return taskData;
    }

    private static void incrementCount() {
        if (++COUNT > MAX_COUNT) {
            COUNT = 0;
        }
    }

    private static boolean isTrue(HttpHeaders headers, String headerName) {

        String val = getHeaderValue(headers, headerName);
        return val != null && "1".equals(val);
    }

    private static String getHeaderValue(HttpHeaders headers, String headerName) {
        if (null == headers || null == headers.getRequestHeaders()) {
            return null;
        }
        List<String> val = null;
        try {
            for (Entry<String, List<String>> header : headers.getRequestHeaders().entrySet()) {
                if (headerName.equalsIgnoreCase(header.getKey())) {
                    val = header.getValue();
                    break;
                }
            }
        } catch (IllegalStateException e) {
            // sometimes throws illegal state exception in the proxy head impl
            // not sure why, but switched from injecting into resource member variable
            // to parameter injection of the resource method
            LOGGER.error("Error accessing header", e);
        }
        return val != null && val.size() > 0 ? val.get(0) : null;
    }

    static boolean doProfile(double profilePercentage, long count) {

        if (profilePercentage > .1) {
            profilePercentage = .1; // max out at 10%
        }
        if (profilePercentage <= 0.) {
            return false;
        }
        long profileMod = (long) (1d / profilePercentage);
        return 0 == count % profileMod;
    }

    public boolean publishDiagnostics() {
        return this.showDiagnostics || this.serviceDiagnostics || this.profile;
    }

    public boolean taskDiagnosticEnabled(String name) {
        return this.taskDiagnostics.contains(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T getTaskData(String name, T originalTaskResult) {
        Object data = this.taskMocks.get(name);
        T taskData = originalTaskResult;
        if (data != null) {
            if (data instanceof String && "null".equals(data.toString())) {
                taskData = null;
            } else if (null == originalTaskResult) {
                // if the current task result is null, assume the mock is of the correct type
                // only works if the mock is initialized with the proper type in a unit test
                taskData = (T) data;
            } else if (originalTaskResult.getClass().equals(data.getClass())) {
                // if the current task result type matches the mock value, again only works for unit tests
                taskData = (T) data;
            } else {
                // otherwise try and convert the generically deserialized type to the expected task type
                taskData = (T) JsonHelper.convertValue(data, originalTaskResult.getClass());
            }
        }
        return taskData;
    }

}
