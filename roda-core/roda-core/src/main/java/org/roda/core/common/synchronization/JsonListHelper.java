/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.synchronization;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonEncoding;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class JsonListHelper {

  private OutputStream outputStream = null;
  private JsonGenerator jsonGenerator = null;
  private Path path;

  private boolean isInitialized;

  public JsonListHelper(Path path) {
    this.path = path;
    this.isInitialized = false;
  }

  /**
   * Init the {@link OutputStream} and the {@link JsonGenerator} and init the json
   * array.
   *
   * @throws IOException
   * if some i/o error occur
   * @throws JacksonException
   * if a Jackson processing error occurs
   */
  public void init() throws IOException, JacksonException {
    if (!Files.exists(path)) {
      Files.createFile(path);
    }

    if (path != null) {
      outputStream = new BufferedOutputStream(Files.newOutputStream(path.toFile().toPath()));
      JsonMapper mapper = JsonMapper.builder().build();
      jsonGenerator = mapper.writerWithDefaultPrettyPrinter().createGenerator(outputStream, JsonEncoding.UTF8);
    }

    if (!isInitialized) {
      jsonGenerator.writeStartArray();
      isInitialized = true;
    }
  }

  /**
   * Write a json string.
   *
   * @param value
   * the value.
   * @throws JacksonException
   * if a Jackson processing error occurs
   */
  public void writeString(final String value) throws JacksonException {
    jsonGenerator.writeString(value);
  }

  /**
   * Close the {@link JsonGenerator} and the {@link OutputStream}.
   *
   * @throws IOException
   * if some i/o error occur
   * @throws JacksonException
   * if a Jackson processing error occurs
   */
  public void close() throws IOException, JacksonException {
    if (jsonGenerator != null) {
      jsonGenerator.writeEndArray();
      jsonGenerator.close();
    }
    if (outputStream != null) {
      outputStream.close();
    }
  }
}