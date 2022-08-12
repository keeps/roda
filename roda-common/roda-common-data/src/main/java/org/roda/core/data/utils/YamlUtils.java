/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.IOUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class YamlUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(YamlUtils.class);
  private static final String YAML_ERROR_MESSAGE = "Error while parsing YAML";

  private YamlUtils() {

  }

  public static <T> T readObjectFromFile(Path yamlFile, Class<T> objectClass) throws GenericException {
    try (InputStream stream = Files.newInputStream(yamlFile)) {
      return getObjectFromYaml(stream, objectClass);
    } catch (IOException e) {
      throw new GenericException(e);
    }
  }

  public static void writeObjectToFile(Object object, Path file) throws GenericException {
    try {
      String yaml = getYamlFromObject(object);
      if (yaml != null) {
        Files.write(file, yaml.getBytes(), StandardOpenOption.CREATE);
      }
    } catch (IOException e) {
      throw new GenericException("Error writing object, as yaml, to file", e);
    }
  }

  private static String getYamlFromObject(Object object) {
    return getYamlFromObject(object, null);
  }

  private static String getYamlFromObject(Object object, Class<?> mixin) {
    String ret = null;
    try {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      mapper = addMixinsToMapper(mapper, object, mixin);
      ret = mapper.writeValueAsString(object);
    } catch (IOException e) {
      LOGGER.error("Error transforming object '{}' to yaml string", object, e);
    }
    return ret;
  }

  private static ObjectMapper addMixinsToMapper(ObjectMapper mapper, Object object, Class<?> mixin) {
    if (mixin != null) {
      mapper.addMixIn(object.getClass(), mixin);
    }

    return mapper;
  }

  public static <T> T getObjectFromYaml(InputStream yaml, Class<T> objectClass) throws GenericException {
    T ret;
    try {
      String yamlString = IOUtils.toString(yaml, RodaConstants.DEFAULT_ENCODING);
      ret = getObjectFromYaml(yamlString, objectClass);
    } catch (IOException e) {
      throw new GenericException(e);
    } finally {
      IOUtils.closeQuietly(yaml);
    }
    return ret;
  }

  public static <T> T getObjectFromYaml(String yaml, Class<T> objectClass) throws GenericException {
    try {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
      return mapper.readValue(yaml, objectClass);
    } catch (IOException e) {
      throw new GenericException(YAML_ERROR_MESSAGE, e);
    }
  }
}
