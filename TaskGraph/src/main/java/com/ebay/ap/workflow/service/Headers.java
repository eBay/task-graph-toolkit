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

package com.ebay.ap.workflow.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Simple headers implementation backed by HashMap.
 */
public class Headers implements HttpHeaders {

    private MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();

    public void add(String name, String value) {
        if (value != null) {
            String[] values = value.split(",");
            List<String> vl = new ArrayList<String>(values.length);
            for (int i = 0; i < values.length; ++i) {
                vl.add(values[i]);
            }
            this.headers.put(name, vl);
        }
    }

    public void add(String name, List<String> header) {
        this.headers.put(name, header);
    }

    @Override
    public List<String> getRequestHeader(String name) {
        return this.headers.get(name);
    }

    @Override
    public String getHeaderString(String name) {
        List<String> values = getRequestHeader(name);
        if (values != null) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (String val : values) {
                if (i > 0) {
                    // per specification: "if the HTTP header is present more than once then the values
                    // of joined together and separated by a ',' character."
                    sb.append(',');
                }
                sb.append(val);
                i++;
            }
            return sb.toString();
        }
        return null;
    }

    @Override
    public MultivaluedMap<String, String> getRequestHeaders() {
        return this.headers;
    }

    @Override
    public List<MediaType> getAcceptableMediaTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Locale> getAcceptableLanguages() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MediaType getMediaType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Locale getLanguage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Cookie> getCookies() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getDate() {
        return null;
    }

    @Override
    public int getLength() {
        return -1;
    }

}
