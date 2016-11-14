/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.validation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class ResourceResolver implements LSResourceResolver {

  private static CacheLoader<String, byte[]> loader = new CacheLoader<String, byte[]>() {

    @Override
    public byte[] load(String href) throws Exception {
      InputStream in = null;
      ByteArrayOutputStream out = null;
      try {
        String filename = href;
        try {
          filename = Paths.get(URI.create(href)).getFileName().toString();
        } catch (IllegalArgumentException e) {
          try {
            filename = Paths.get(href).getFileName().toString();
          } catch (InvalidPathException e2) {
            // nothing to do
          }
        }
        String filePath = RodaConstants.CORE_SCHEMAS_FOLDER + "/" + filename;
        in = RodaCoreFactory.getConfigurationFileAsStream(filePath);
        if (in == null) {
          throw new NotFoundException(filePath);
        }
        out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);

        return out.toByteArray();
      } finally {
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
      }
    }

  };

  private static LoadingCache<String, byte[]> cache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES)
    .build(loader);

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
    InputStream resourceAsStream = null;
    try {
      byte[] in = cache.get(systemId);
      resourceAsStream = new ByteArrayInputStream(in);
    } catch (ExecutionException e) {
      logger.error("Error loading " + systemId, e);
      resourceAsStream = null;
    }
    return resourceAsStream == null ? null : new Input(publicId, systemId, resourceAsStream);

  }

}
