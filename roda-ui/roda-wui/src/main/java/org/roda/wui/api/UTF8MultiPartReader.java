/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartProperties;
import org.glassfish.jersey.media.multipart.internal.LocalizationMessages;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.jvnet.mimepull.Header;
import org.jvnet.mimepull.MIMEConfig;
import org.jvnet.mimepull.MIMEMessage;
import org.jvnet.mimepull.MIMEParsingException;
import org.jvnet.mimepull.MIMEPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.Providers;

@Provider
@Consumes("multipart/*")
public class UTF8MultiPartReader implements MessageBodyReader<MultiPart> {
  private static final Logger LOGGER = LoggerFactory.getLogger(UTF8MultiPartReader.class);

  private Providers providers;
  private final MIMEConfig mimeConfig;

  public UTF8MultiPartReader(@Context Providers providers) {
    final ContextResolver<MultiPartProperties> contextResolver = providers.getContextResolver(MultiPartProperties.class,
      MediaType.WILDCARD_TYPE);
    this.providers = providers;

    MultiPartProperties properties = null;
    if (contextResolver != null) {
      properties = contextResolver.getContext(this.getClass());
    }
    if (properties == null) {
      properties = new MultiPartProperties();
    }

    mimeConfig = createMimeConfig(properties);
  }

  private MIMEConfig createMimeConfig(final MultiPartProperties properties) {
    final MIMEConfig mimeConfig = new MIMEConfig();

    // Set values defined by user.
    mimeConfig.setMemoryThreshold(properties.getBufferThreshold());

    final String tempDir = FilenameUtils.normalize(properties.getTempDir());
    if (tempDir != null) {
      mimeConfig.setDir(tempDir);
    }

    if (properties.getBufferThreshold() != MultiPartProperties.BUFFER_THRESHOLD_MEMORY_ONLY) {
      // Validate - this checks whether it's possible to create temp files in
      // currently set temp directory.
      try {
        // noinspection ResultOfMethodCallIgnored
        File.createTempFile("MIME", null, tempDir != null ? new File(tempDir) : null).delete();
      } catch (final IOException ioe) {
        LOGGER.error("", ioe);
      }
    }

    return mimeConfig;
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return MultiPart.class.isAssignableFrom(type);
  }

  @Override
  public MultiPart readFrom(Class<MultiPart> type, Type genericType, Annotation[] annotations, MediaType mediaType,
    MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
    try {
      return readMultiPart(genericType, annotations, mediaType, httpHeaders, entityStream);
    } catch (final MIMEParsingException mpe) {
      if (mpe.getCause() instanceof IOException) {
        throw (IOException) mpe.getCause();
      } else {
        throw new BadRequestException(mpe);
      }
    }
  }

  protected MultiPart readMultiPart(final Type genericType, final Annotation[] annotations, MediaType mediaType,
    final MultivaluedMap<String, String> headers, final InputStream stream) throws IOException, MIMEParsingException {
    MediaType media = unquoteMediaTypeParameters(mediaType, "boundary");

    final MIMEMessage mimeMessage = new MIMEMessage(stream, media.getParameters().get("boundary"), mimeConfig);

    final boolean formData = MediaTypes.typeEqual(media, MediaType.MULTIPART_FORM_DATA_TYPE);
    final MultiPart multiPart = formData ? new FormDataMultiPart() : new MultiPart();

    multiPart.setProviders(providers);

    final MultivaluedMap<String, String> multiPartHeaders = multiPart.getHeaders();
    for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
      final List<String> values = entry.getValue();

      for (final String value : values) {
        multiPartHeaders.add(entry.getKey(), value);
      }
    }

    final boolean fileNameFix;
    if (!formData) {
      multiPart.setMediaType(media);
      fileNameFix = false;
    } else {
      // see if the User-Agent header corresponds to some version of MS Internet
      // Explorer
      // if so, need to set fileNameFix to true to handle issue
      // http://java.net/jira/browse/JERSEY-759
      final String userAgent = headers.getFirst(HttpHeaders.USER_AGENT);
      fileNameFix = userAgent != null && userAgent.contains(" MSIE ");
    }

    for (final MIMEPart mimePart : getMimeParts(mimeMessage)) {
      final BodyPart bodyPart = formData ? new FormDataBodyPart(fileNameFix) : new BodyPart();

      // Configure providers.
      bodyPart.setProviders(providers);

      // Copy headers.
      for (final Header header : mimePart.getAllHeaders()) {
        bodyPart.getHeaders().add(header.getName(), getFixedHeaderValue(header));
      }

      try {
        final String contentType = bodyPart.getHeaders().getFirst("Content-Type");
        if (contentType != null) {
          bodyPart.setMediaType(MediaType.valueOf(contentType));
        }

        bodyPart.getContentDisposition();
      } catch (final IllegalArgumentException ex) {
        throw new BadRequestException(ex);
      }

      // Copy data into a BodyPartEntity structure.
      bodyPart.setEntity(new BodyPartEntity(mimePart));

      // Add this BodyPart to our MultiPart.
      multiPart.getBodyParts().add(bodyPart);
    }

    return multiPart;
  }

  private String getFixedHeaderValue(Header h) throws UnsupportedEncodingException {
    String result = h.getValue();

    if ("Content-Disposition".equals(h.getName()) && (result.contains("filename="))) {
        result = new String(result.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    return result;
  }

  private List<MIMEPart> getMimeParts(final MIMEMessage message) {
    try {
      return message.getAttachments();
    } catch (final MIMEParsingException obtainPartsError) {
      LOGGER.error(LocalizationMessages.PARSING_ERROR(), obtainPartsError);
      message.close();

      // Re-throw the exception.
      throw obtainPartsError;
    }
  }

  protected static MediaType unquoteMediaTypeParameters(final MediaType mediaType, final String... parameters) {
    if (parameters == null || parameters.length == 0) {
      return mediaType;
    }

    final Map<String, String> unquotedParams = new HashMap<>(mediaType.getParameters());
    for (final String parameter : parameters) {
      String value = mediaType.getParameters().get(parameter);

      if (value != null && value.startsWith("\"")) {
        value = value.substring(1, value.length() - 1);
        unquotedParams.put(parameter, value);
      }
    }

    return new MediaType(mediaType.getType(), mediaType.getSubtype(), unquotedParams);
  }
}
