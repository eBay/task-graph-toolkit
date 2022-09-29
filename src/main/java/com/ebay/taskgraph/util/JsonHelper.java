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

package com.ebay.taskgraph.util;

import java.io.InputStream;
import java.util.List;

/**
 * JSON helper class where clients inject their specific implementations.
 */
public class JsonHelper {
  
    private static IJsonHelper HELPER;
    
    public static void setHelper(IJsonHelper helper) {
        if (null == HELPER) {
            // first come first, first served
            HELPER = helper;
        }
    }

    public static String writeAsString(Object o) {
        return HELPER.writeAsString(o);
    }

    public static String prettyPrint(Object o) {
        return HELPER.prettyPrint(o);
    }

    public static String prettyPrint(String s) {
        return HELPER.prettyPrint(s);
    }

    public static <T> T readJsonString(String s, Class<T> clazz) {
        return HELPER.readJsonString(s, clazz);
    }

    public static <T> T readJsonFile(String s, Class<?> clazz) {
        return HELPER.readJsonFile(s, clazz);
    }

    public static <T> T readJsonFile(InputStream is, Class<T> clazz) {
        return HELPER.readJsonFile(is, clazz);
    }

    public static <T> List<T> readJsonStringAsList(String s) {
        return HELPER.readJsonStringAsList(s);
    }

    public static <T> T convertValue(Object o, Class<T> clazz) {
        return HELPER.convertValue(o, clazz);
    }

    public static <T> T readTestResourceJsonFile(String filename, Class<T> clazz) {
        return HELPER.readTestResourceJsonFile(filename, clazz);
    }

    public static String getTextFromClassPath(String path) {
        return HELPER.getTextFromClassPath(path);
    }

}
