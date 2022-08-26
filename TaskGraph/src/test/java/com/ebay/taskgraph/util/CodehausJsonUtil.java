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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.impl.Indenter;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.util.DefaultPrettyPrinter;

public class CodehausJsonUtil {

    public static final String TEST_RESOURCE_DIR = "src/test/resources/";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ObjectMapper PRETTY_MAPPER = new ObjectMapper();

    static {
      // ensure we use LF for pretty print indentation
      System.setProperty("line.separator", "\n");
      MAPPER.setSerializationInclusion(Inclusion.NON_EMPTY);
      MAPPER.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      MAPPER.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
      // process time stamps same as deployed service
      MAPPER.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
      MAPPER.setDateFormat(dateFormat);

      PRETTY_MAPPER.setSerializationInclusion(Inclusion.NON_EMPTY);
      PRETTY_MAPPER.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      PRETTY_MAPPER.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
      // process time stamps same as deployed service
      PRETTY_MAPPER.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
      PRETTY_MAPPER.configure(SerializationConfig.Feature.SORT_PROPERTIES_ALPHABETICALLY, true);
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    /** Pretty print json node. */
    public static String prettyPrint(String content) {

        try {
            Object node = PRETTY_MAPPER.readTree(content);
            return prettyPrintObject(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String prettyPrintObject(Object node) {

        DefaultPrettyPrinter pp = getPrettyPrinter();
        StringWriter sw = new StringWriter();
        try {
            PRETTY_MAPPER.writer(pp).writeValue(sw, node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sw.toString();
    }

    public static String writeAsString(Object node) {

        try {
            return MAPPER.writeValueAsString(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static DefaultPrettyPrinter getPrettyPrinter() {
        Indenter indenter = new DefaultPrettyPrinter.Lf2SpacesIndenter();
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.indentObjectsWith(indenter);
        return pp;
    }

    @SuppressWarnings("unchecked")
    public static <T> T readJsonString(String content, Class<?> clazz) {

        try {
            return (T) MAPPER.readValue(content, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T readJsonString(String content, TypeReference<T> reference) {

        try {
            return (T) MAPPER.readValue(content, reference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T readJsonFile(String filename, Class<?> clazz) {
        InputStream is = null;
        T response = null;
        try {
            is = new FileInputStream(new File(filename));

            response = (T) MAPPER.readValue(is, clazz);

            if (null == response) {
                throw new RuntimeException(getErrorString(clazz, filename));
            }
        } catch (IOException e) {
            throw new RuntimeException(getErrorString(clazz, filename), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new RuntimeException("Couldn't close input stream for: " + filename, e);
                }
            }
        }
        return response;
    }

    @SuppressWarnings("unchecked")
    public static <T> T readJsonFile(InputStream is, Class<?> clazz) {
        try {
            return (T) MAPPER.readValue(is, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T readJsonFileByClassPath(String classpath, Class<?> clazz) {
        T response = null;
        InputStream is = null;
        try {
            is = CodehausJsonUtil.class.getResourceAsStream(classpath);
            if (is == null) {
                throw new IllegalStateException(
                        "JSON file:= " + classpath + " is not found on the classpath.");
            }

            response = (T) MAPPER.readValue(is, clazz);

            if (null == response) {
                throw new RuntimeException(getErrorString(clazz, classpath));
            }
        } catch (IOException e) {
            throw new RuntimeException(getErrorString(clazz, classpath), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new RuntimeException("Couldn't close input stream for: " + classpath, e);
                }
            }
        }
        return response;
    }

    @SuppressWarnings("unchecked")
    public static <T> T readJsonFileByClassPath(String classpath, TypeReference<T> type) {
        T response = null;
        InputStream is = null;
        try {
            is = CodehausJsonUtil.class.getResourceAsStream(classpath);
            if (is == null) {
                throw new IllegalStateException(
                        "JSON file:= " + classpath + " is not found on the classpath.");
            }

            response = (T) CodehausJsonUtil.getMapper().readValue(is, type);

            if (null == response) {
                throw new RuntimeException(getErrorString(type.getClass(), classpath));
            }
        } catch (IOException e) {
            throw new RuntimeException(getErrorString(type.getClass(), classpath));
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new RuntimeException("Couldn't close input stream for: " + classpath, e);
                }
            }
        }

        return response;
    }

    public static String getTextFromClassPath(String classpath) {
        String responseText = null;

        try (InputStream is = CodehausJsonUtil.class.getResourceAsStream(classpath)) {
            if (is == null) {
                throw new IllegalStateException("File " + classpath + " is not found on the classpath.");
            }

            responseText = IOUtils.toString(is);
        } catch (IOException e) {
            return null;
        }

        return responseText;
    }

    private static String getErrorString(Class<?> clazz, String filename) {
        return "Couldn't parse class:[" + clazz.getCanonicalName() + "] from:[" + filename + "]";
    }

    public static <T> T readTestResourceJsonFile(String filename, Class<?> clazz) {
        return readJsonFile(TEST_RESOURCE_DIR + filename, clazz);
    }
}
