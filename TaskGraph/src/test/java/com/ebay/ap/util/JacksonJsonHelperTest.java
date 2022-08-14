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

package com.ebay.ap.util;

import java.io.InputStream;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;

/**
 * Legacy jackson implementation of the JSON helper methods.
 */
public class JacksonJsonHelperTest implements IJsonHelper {
  
    public static final IJsonHelper INSTANCE = new JacksonJsonHelperTest();
  
    static {
        JsonHelper.setHelper(INSTANCE);
    }
  
    private JacksonJsonHelperTest() {
    }

    @Override
    public String writeAsString(Object o) {
        return CodehausJsonUtil.writeAsString(o);
    }

    @Override
    public <T> T readJsonString(String s, Class<T> clazz) {
        return CodehausJsonUtil.readJsonString(s, clazz);
    }

    @Override
    public String prettyPrint(Object o) {
        return CodehausJsonUtil.prettyPrintObject(o);
    }

    @Override
    public String prettyPrint(String s) {
        return CodehausJsonUtil.prettyPrint(s);
    }

    @Override
    public <T> T readJsonFile(String s, Class<?> clazz) {
        return CodehausJsonUtil.readJsonFile(s, clazz);
    }

    @Override
    public <T> List<T> readJsonStringAsList(String s) {
        return CodehausJsonUtil.readJsonString(s, new TypeReference<List<T>>() {});
    }

    @Override
    public <T> T convertValue(Object o, Class<T> clazz) {
        return CodehausJsonUtil.getMapper().convertValue(o, clazz);
    }

    @Override
    public <T> T readTestResourceJsonFile(String filename, Class<T> clazz) {
        return CodehausJsonUtil.readTestResourceJsonFile(filename, clazz);
    }

    @Override
    public <T> T readJsonFile(InputStream is, Class<T> clazz) {
        return CodehausJsonUtil.readJsonFile(is, clazz);
    }

    @Override
    public String getTextFromClassPath(String path) {
        return CodehausJsonUtil.getTextFromClassPath(path);
    }

}
