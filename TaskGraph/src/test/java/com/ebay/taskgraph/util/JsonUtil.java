package com.ebay.taskgraph.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter.Indenter;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {

    public static final String TEST_RESOURCE_DIR = "src/test/resources/";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static  {
        // ensure we use LF for pretty print indentation
        System.setProperty("line.separator", "\n");
    }

    /**
     * Pretty print json node.
     */
    public static String prettyPrint(String content) {

        try {
            // don't mix using JsonNode & Object/Map which complicates things unnecessarily
            Object node = MAPPER.readValue(content, Object.class);
            return prettyPrintObject(node);
        } catch (IOException e) {
            throw new RuntimeException("error parsing JSON: " + content, e);
        }
    }

    public static String prettyPrintObject(Object obj) {

        DefaultPrettyPrinter pp = getPrettyPrinter();
        try {
            return MAPPER.writer(pp).writeValueAsString(obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String writeAsString(Object node) {

        try {
            return MAPPER.writeValueAsString(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static DefaultPrettyPrinter getPrettyPrinter() {
        Indenter indenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.indentObjectsWith(indenter);
        return pp;
    }


    public static <T> T readJsonString(String content, Class<T> clazz) {

        try {
            return (T) MAPPER.readValue(content, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


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
    public static <T> T  readJsonFile(InputStream is, Class<?> clazz) {
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
        try  {
            is = JsonUtil.class.getResourceAsStream(classpath);
            if (is == null) {
                throw new IllegalStateException("JSON file:= " + classpath + " is not found on the classpath.");
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


    public static <T> T readJsonFileByClassPath(String classpath, TypeReference<T> type) {
        T response = null;
        InputStream is = null;
        try {
            is = JsonUtil.class.getResourceAsStream(classpath);
            if (is == null) {
                throw new IllegalStateException("JSON file:= " + classpath + " is not found on the classpath.");
            }

            response = (T) MAPPER.readValue(is, type);

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

        try (InputStream is = JsonUtil.class.getResourceAsStream(classpath)) {
            if (is == null) {
                throw new IllegalStateException("File " + classpath + " is not found on the classpath.");
            }

            responseText = IOUtils.toString(is, "UTF-8");
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

    public static <T> T convertValue(Object o, Class<T> clazz) {
      return MAPPER.convertValue(o, clazz);
    }

}
