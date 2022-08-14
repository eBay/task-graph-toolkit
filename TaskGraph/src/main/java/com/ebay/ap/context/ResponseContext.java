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

package com.ebay.ap.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.ap.diagnostic.DiagnosticConfig;
import com.ebay.ap.diagnostic.DiagnosticHolder;
import com.ebay.ap.diagnostic.IDiagnosticHolder;
import com.ebay.ap.diagnostic.IProfiler;
import com.ebay.ap.diagnostic.Profiler;

/**
 * Context class containing errors and diagnostics.
 * Components of service implementations contain instances of this class
 * where they can report errors or diagnostic information.
 * 
 * Note: not thread safe.  Each task should contain its own instance.
 * Thread orchestration component should aggregate individual thread instances.
 */
//@NotThreadSafe
public class ResponseContext {

    public static final ResponseContext NULL = new ResponseContext(new DiagnosticConfig(false, false, false), "NULL");

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseContext.class);

    private static final String STRING_LIST_TYPE = "STRING_LIST";

    private final IDiagnosticHolder diagnostic;

    private final IProfiler profiler;

    private final DiagnosticConfig diagnosticConfig;

    private final String name;

    // following fields lazy initialized to keep basic task implementation as light as possible
    // not all tasks will need to record tracking and errors
    /**
     * Tracking objects for task
     */
    private Map<ITrackingKey, Object> tracking;

    /**
     * Any errors that need to be reported should be added here.
     */
    private ErrorHolder error;

    public ResponseContext() {
        this(DiagnosticConfig.NONE, "NULL");
    }

    public ResponseContext(DiagnosticConfig diagnosticConfig, String name) {
        this.diagnostic = DiagnosticHolder.getDiagnosticHolder(diagnosticConfig);
        this.diagnosticConfig = diagnosticConfig;
        this.name = name;
        this.profiler = Profiler.getProfiler(name, this.diagnosticConfig);
        // profiler isn't started right away because the context may be initialized during application creation
        // and we only want to start the profiler when the owning class starts its execution
    }

    /**
     * Create a new profiler instance.
     * @param name
     * @return
     */
    public IProfiler newProfiler(String name) {
        IProfiler profiler = Profiler.getProfiler(name, this.diagnosticConfig);
        // Assumes that the parent context (this) profiler has already been started and
        // so start the return profiler now.
        profiler.start();
        return profiler;
    }

    /**
     * Create a new context with the same diagnostic config.
     * @param name
     * @return
     */
    public ResponseContext newContext(String name) {
        ResponseContext rc = new ResponseContext(this.diagnosticConfig, name);
        // Assumes that the parent context (this) profiler has already been started and
        // so start the return profiler now.
        rc.getProfiler().start();
        return rc;
    }

    // TODO:2:ddolan - ideally get rid of this and add a method to add a single error to this context
    // use same pattern of lazy initialization as tracking
    public ErrorHolder getError() {
        if (null == this.error) {
            this.error = new ErrorHolder();
        }
        return this.error;
    }

    public IDiagnosticHolder getDiagnostic() {
        return diagnostic;
    }

    public DiagnosticConfig getDiagnosticConfig() {
        return this.diagnosticConfig;
    }

    public void add(ResponseContext context) {

        if (this == context) {
            LOGGER.error("Adding response context to itself!");
        } else {
            if (context.error != null) {
                this.getError().add(context.error);
            }
            this.diagnostic.addDiagnostic(context.getDiagnostic());
            this.profiler.add(context.getProfiler());
            if (context.tracking != null) {
                for (Entry<ITrackingKey, Object> entry : context.tracking.entrySet()) {
                    if (entry.getValue() instanceof List) {
                        // maintain backwards compatibility of list of strings
                        for (Object trackingString : (List<?>) entry.getValue()) {
                            this.addTracking(entry.getKey().getKey().toString(), trackingString.toString());
                        }
                    } else {
                        this.addTracking(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
    }

    public IProfiler getProfiler() {
        return this.profiler;
    }

    /**
     * @deprecated
     * Please use {@link #addTracking(ITrackingKey, Object)} instead
     * @param key
     * @param trackingString
     */
    @Deprecated
    public void addTracking(String key, String trackingString) {
        if (null == tracking) {
            tracking = new HashMap<>();
        }
        ITrackingKey trackingKey = new TrackingKey(STRING_LIST_TYPE, key);
        @SuppressWarnings("unchecked")
        List<String> trackingList = (List<String>) this.tracking.get(trackingKey);
        if (null == trackingList) {
            trackingList = new ArrayList<>();
            this.tracking.put(trackingKey, trackingList);
        }
        if (!trackingList.contains(trackingString)) {
            trackingList.add(trackingString);
        }
    }

    public void addTracking(ITrackingKey key, Object value) {
        if (null == tracking) {
            tracking = new HashMap<>();
        }
        if (this.tracking.containsKey(key)) {
            LOGGER.error("Trying to add tracking for existing key:" + key
                    + ", new value:" + value
                    + ", old value:" + this.tracking.get(key));
        } else {
            this.tracking.put(key, value);
        }
    }

    /**
     * @deprecated
     * Please use {@link #getTrackingByType(String)} instead
     * @param trackingKey
     * @return
     */
    @Deprecated
    public List<String> getTracking(String trackingKey) {
        if (null == tracking) {
            return Collections.emptyList();
        }
        @SuppressWarnings("unchecked")
        List<String> trackingList = (List<String>) this.tracking.get(new TrackingKey(STRING_LIST_TYPE, trackingKey));
        if (null == trackingList) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(trackingList);
    }

    public Map<String, List<String>> getTracking() {
        if (null == tracking) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> rval = new HashMap<>();
        for (Entry<ITrackingKey, Object> entry : this.tracking.entrySet()) {
            if (entry.getKey() != null && STRING_LIST_TYPE.equals(entry.getKey().getType())) {
                @SuppressWarnings("unchecked")
                List<String> listValue = (List<String>) entry.getValue();
                rval.put(entry.getKey().getKey().toString(), listValue);
            }
        }
        return Collections.unmodifiableMap(rval);
    }

    public Map<ITrackingKey, Object> getTrackingByType(String type) {
        if (null == tracking) {
            return Collections.emptyMap();
        }
        Map<ITrackingKey, Object> rval = new HashMap<>();
        for (Entry<ITrackingKey, Object> entry : this.tracking.entrySet()) {
            if (entry.getKey().getType().equals(type)) {
                rval.put(entry.getKey(), entry.getValue());
            }
        }
        return rval;
    }

    public String getName() {
        return this.name;
    }
}
