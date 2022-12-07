/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.common.Pair;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class RodaEntityResolver implements EntityResolver {

  private static CacheLoader<Pair<String, String>, byte[]> loader = new CacheLoader<Pair<String, String>, byte[]>() {

    @Override
    public byte[] load(Pair<String, String> pair) throws Exception {
      String systemId = pair.getSecond();

      String filename = FilenameUtils.normalize(Paths.get(URI.create(systemId)).getFileName().toString());
      String configurationFile = RodaConstants.CORE_SCHEMAS_FOLDER + "/" + filename;

      try (InputStream in = RodaCoreFactory.getConfigurationFileAsStream(configurationFile);
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        if (in != null) {
          IOUtils.copy(in, out);
          return out.toByteArray();
        } else
          throw new NotFoundException(configurationFile);
      }
    }

  };

  private static LoadingCache<Pair<String, String>, byte[]> cache = CacheBuilder.newBuilder()
    .expireAfterWrite(1, TimeUnit.MINUTES).build(loader);

  @Override
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
    try {
      byte[] in = cache.get(Pair.of(publicId, systemId));
      return new InputSource(new ByteArrayInputStream(in));
    } catch (ExecutionException e) {
      if (systemId.endsWith(".dtd")) {
        // ignore DTDs that are not in configuration
        return new InputSource(new StringReader(""));
      } else {
        throw new SAXException("Could not resolve entity", e);
      }
    }
  }
}
