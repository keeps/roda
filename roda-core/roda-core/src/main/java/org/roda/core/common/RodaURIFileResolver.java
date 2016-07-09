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

import org.apache.commons.compress.utils.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

class RodaURIResolver implements URIResolver {

  private static CacheLoader<String, ByteArrayInputStream> loader = new CacheLoader<String, ByteArrayInputStream>() {

    @Override
    public ByteArrayInputStream load(String href) throws Exception {
      InputStream in = RodaCoreFactory
        .getConfigurationFileAsStream(RodaConstants.CROSSWALKS_DISSEMINATION_OTHER_PATH + "/" + href);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOUtils.copy(in, out);

      return new ByteArrayInputStream(out.toByteArray());
    }

  };
  private static LoadingCache<String, ByteArrayInputStream> cache = CacheBuilder.newBuilder()
    .expireAfterWrite(1, TimeUnit.MINUTES).build(loader);

  @Override
  public Source resolve(String href, String base) throws TransformerException {   
    try {
      ByteArrayInputStream in = cache.get(href);
      return new StreamSource(in);
    } catch (ExecutionException e) {
      throw new TransformerException("Could not load URI: "+href, e);
    }
  }
}