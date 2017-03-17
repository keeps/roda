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
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class RodaURIFileResolver implements URIResolver {

  private static CacheLoader<String, byte[]> loader = new CacheLoader<String, byte[]>() {

    @Override
    public byte[] load(String href) throws Exception {
      String filePath = RodaConstants.CROSSWALKS_DISSEMINATION_OTHER_PATH + href;

      try (InputStream in = RodaCoreFactory.getConfigurationFileAsStream(filePath);
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        if (in == null) {
          throw new NotFoundException(filePath);
        }

        IOUtils.copy(in, out);
        return out.toByteArray();
      }
    }

  };
  private static LoadingCache<String, byte[]> cache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES)
    .build(loader);

  @Override
  public Source resolve(String href, String base) throws TransformerException {
    try {
      byte[] in = cache.get(href);
      return new StreamSource(new ByteArrayInputStream(in));
    } catch (ExecutionException e) {
      throw new TransformerException("Could not load URI: " + href, e);
    }
  }
}