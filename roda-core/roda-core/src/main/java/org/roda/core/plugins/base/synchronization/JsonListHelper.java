package org.roda.core.plugins.base.synchronization;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

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
   *           if some i/o error occur
   */
  public void init() throws IOException {
    if (!Files.exists(path)) {
      Files.createFile(path);
    }

    if (path != null) {
      outputStream = new BufferedOutputStream(new FileOutputStream(path.toFile()));
      final JsonFactory jsonFactory = new JsonFactory();
      jsonGenerator = jsonFactory.createGenerator(outputStream, JsonEncoding.UTF8).useDefaultPrettyPrinter();
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
   *          the value.
   * @throws IOException
   *           if some i/o error occur
   */
  public void writeString(final String value) throws IOException {
    jsonGenerator.writeString(value);
  }

  /**
   * Close the {@link JsonGenerator} and the {@link OutputStream}.
   *
   * @throws IOException
   *           if some i/o error occur
   */
  public void close() throws IOException {
    if (jsonGenerator != null) {
      jsonGenerator.writeEndArray();
      jsonGenerator.close();
    }
    if (outputStream != null) {
      outputStream.close();
    }
  }
}
