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

public class TrackingKey implements ITrackingKey {

    /** the type of tracking interface the value will be logged to */
    private final String type;

    /** the key in the tracking interface for the value */
    private final Object key;
    
    /** string representing the key plus type */
    private final String typekey;

    public TrackingKey(String type, Object key) {
        this.type = type;
        this.key = key;
        this.typekey = this.type + ":" + this.key;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public Object getKey() {
        return this.key;
    }

    @Override
    public String toString() {
        return this.typekey;
    }

    @Override
    public int hashCode() {
        return this.typekey.hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        if (null != other && other instanceof TrackingKey) {
            return this.typekey.equals(((TrackingKey) other).typekey);
        }
        return false;
    }
}
