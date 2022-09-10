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
 * Abstraction to allow clients to use their own JSON serialization library.
 */
public interface IJsonHelper {

    String writeAsString(Object o);

    String prettyPrint(Object o);

    String prettyPrint(String s);

    <T> T readJsonString(String s, Class<T> clazz);

    <T> T readJsonFile(String s, Class<?> clazz);

    <T> T readJsonFile(InputStream is, Class<T> clazz);

    <T> List<T> readJsonStringAsList(String s);

    <T> T convertValue(Object o, Class<T> clazz);

    <T> T readTestResourceJsonFile(String filename, Class<T> clazz);

    String getTextFromClassPath(String path);

}
