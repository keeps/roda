/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.SecureString;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.accessToken.AccessToken;

/**
 * 20160824 hsilva: I think someone, outside RODA code base, is using this
 * method via maven dependency
 *
 * @author Hélder Silva <hsilva@keep.pt>
 */
public final class RESTClientUtility {

  public static final String AUTHORIZATION = "Authorization";
  public static final String BEARER = "Bearer ";

  /** Private empty constructor */
  private RESTClientUtility() {
    // do nothing
  }

  public static <T extends Serializable> T sendPostRequestWithoutBodyHttp5(Class<T> elementClass, String url,
    String resource, AccessToken accessToken) throws GenericException {
    try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
      HttpPost httpPost = new HttpPost(buildSafeUri(url, resource));
      httpPost.addHeader(AUTHORIZATION, BEARER + accessToken.getToken());
      httpPost.addHeader("content-type", RodaConstants.MEDIA_TYPE_APPLICATION_JSON);
      httpPost.addHeader("Accept", RodaConstants.MEDIA_TYPE_APPLICATION_JSON);

      Result result = httpclient.execute(httpPost, response -> new Result(response.getCode(),
        EntityUtils.toString(response.getEntity(), Charset.defaultCharset())));

      if (result.statusCode == 201) {
        if (elementClass != null) {
          return JsonUtils.getObjectFromJson(result.content, elementClass);
        } else {
          return null;
        }
      } else {
        throw new GenericException("POST request response status code: " + result.statusCode);
      }
    } catch (IOException e) {
      throw new GenericException("Error sending POST request", e);
    }
  }

  public static <T extends Serializable> T sendPostRequestHttpClient5(Object element, Class<T> elementClass, String url,
    String resource, AccessToken accessToken) throws GenericException {
    try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
      HttpPost httpPost = new HttpPost(buildSafeUri(url, resource));
      httpPost.addHeader(AUTHORIZATION, BEARER + accessToken.getToken());
      httpPost.addHeader("content-type", RodaConstants.MEDIA_TYPE_APPLICATION_JSON);
      httpPost.addHeader("Accept", RodaConstants.MEDIA_TYPE_APPLICATION_JSON);

      httpPost.setEntity(new StringEntity(JsonUtils.getJsonFromObject(element)));

      Result result = httpclient.execute(httpPost, response -> new Result(response.getCode(),
        EntityUtils.toString(response.getEntity(), Charset.defaultCharset())));

      if (result.statusCode == 201) {
        if (elementClass != null) {
          return JsonUtils.getObjectFromJson(result.content, elementClass);
        } else {
          return null;
        }
      } else {
        throw new GenericException("POST request response status code: " + result.statusCode);
      }
    } catch (IOException e) {
      throw new GenericException("Error sending POST request", e);
    }
  }

  public static int sendPostRequestWithCompressedFileHttp5(String url, String resource, Path path,
    AccessToken accessToken) throws RODAException {
    try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
      HttpPost httpPost = new HttpPost(buildSafeUri(url, resource));
      httpPost.addHeader(AUTHORIZATION, BEARER + accessToken.getToken());

      InputStream inputStream = Files.newInputStream(path.toFile().toPath());
      MultipartEntityBuilder builder = MultipartEntityBuilder.create();
      builder.setMode(HttpMultipartMode.STRICT);
      builder.addBinaryBody(RodaConstants.API_QUERY_KEY_FILE, inputStream, ContentType.create("application/zip"),
        path.getFileName().toString());
      httpPost.setEntity(builder.build());

      Result result = httpclient.execute(httpPost, response -> new Result(response.getCode(), null));

      return result.statusCode;
    } catch (IOException e) {
      throw new RODAException("Error sending POST request", e);
    }
  }

  public static int sendPostRequestWithFileHttp5(String url, String resource, String username, SecureString password,
    Path file) throws RODAException {
    try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
      HttpPost httpPost = new HttpPost(buildSafeUri(url, resource));
      try (
        SecureString basicAuth = new SecureString(
          ArrayUtils.addAll((username + ":").toCharArray(), password.getChars()));
        SecureString basicAuthToken = new SecureString(
          Base64.encode(StandardCharsets.UTF_8.encode(CharBuffer.wrap(basicAuth)).array()))) {

        httpPost.setHeader(AUTHORIZATION, "Basic " + basicAuthToken);
        File fileToUpload = new File(FilenameUtils.normalize(file.toString()));
        InputStream inputStream = new FileInputStream(fileToUpload);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody(RodaConstants.API_QUERY_KEY_FILE, inputStream, ContentType.DEFAULT_BINARY,
          fileToUpload.getName());
        httpPost.setEntity(builder.build());

        Result result = httpclient.execute(httpPost, response -> new Result(response.getCode(), null));

        return result.statusCode;
      }
    } catch (IOException e) {
      throw new RODAException("Error sending POST request", e);
    }
  }

  private static URI buildSafeUri(String baseUrl, String resource) throws GenericException {
    if (baseUrl == null) {
      throw new GenericException("Base URL must not be null");
    }
    try {
      URI base = new URI(baseUrl.trim());
      String scheme = base.getScheme();
      if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
        throw new GenericException("Unsupported URL scheme for central instance: " + scheme);
      }
      if (base.getHost() == null) {
        throw new GenericException("Central instance URL must include a host");
      }
      if (base.getUserInfo() != null) {
        throw new GenericException("User info is not allowed in central instance URL");
      }

      // Optional but safer: prevent requests to loopback or private addresses
      try {
        InetAddress addr = InetAddress.getByName(base.getHost());
        if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()) {
          throw new GenericException("Central instance URL points to a disallowed address");
        }
      } catch (UnknownHostException e) {
        throw new GenericException("Cannot resolve central instance host", e);
      }

      String basePath = base.getPath() == null ? "" : base.getPath();
      String resPath = resource == null ? "" : resource;
      // Ensure exactly one slash between base path and resource
      if (!basePath.endsWith("/") && !resPath.startsWith("/")) {
        resPath = RodaConstants.API_SEP + resPath;
      } else if (basePath.endsWith("/") && resPath.startsWith("/")) {
        resPath = resPath.substring(1);
      }
      return new URI(base.getScheme(), base.getUserInfo(), base.getHost(), base.getPort(), basePath + resPath, null,
        null);
    } catch (URISyntaxException e) {
      throw new GenericException("Invalid central instance URL", e);
    }
  }

  private record Result(int statusCode, String content) {
  }
}
