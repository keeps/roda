/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.notifications;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPNotificationProcessor implements NotificationProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(HTTPNotificationProcessor.class);

  public static String JOB_KEY = "job";

  private String endpoint;
  private Map<String, Object> scope;

  public HTTPNotificationProcessor(String endpoint, Map<String, Object> scope) {
    this.endpoint = endpoint;
    this.scope = scope;
  }

  @Override
  public Notification processNotification(ModelService model, Notification notification) throws RODAException {
    try {
      if (scope.containsKey(JOB_KEY)) {
        Job job = (Job) scope.get(JOB_KEY);
        String content = createNotificationContent(job);
        notification.setBody(content);
        if (endpoint != null) {
          LOGGER.debug("Sending notification via HTTP ...");
          post(endpoint, content);
          LOGGER.debug("Notification sent");
        } else {
          LOGGER.warn("No endpoint, cannot send notification.");
        }
      }
    } catch (IOException e) {
      throw new GenericException(e.getMessage(), e);
    }
    return notification;
  }

  private String createNotificationContent(Job job) {
    // TODO: create content (XML?) from Job
    return JsonUtils.getJsonFromObject(job);
  }

  private void post(String endpoint, String content) throws ClientProtocolException, IOException {
    HttpClient httpclient = HttpClients.createDefault();
    HttpPost httppost = new HttpPost(endpoint);

    httppost.setEntity(new StringEntity(content));

    HttpResponse response = httpclient.execute(httppost);
    HttpEntity entity = response.getEntity();
    String responseTxt = processEntity(entity);
    LOGGER.debug("HTTP response: {}", responseTxt);
  }

  // FIXME 20160905 hsilva: is this method really needed? and, is this the best
  // way to implement it (30 lines)?
  private String processEntity(HttpEntity entity) {
    String responseTxt = null;
    if (entity != null) {
      InputStream is = null;
      try {
        is = entity.getContent();
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

          br = new BufferedReader(new InputStreamReader(is));
          while ((line = br.readLine()) != null) {
            sb.append(line);
          }

        } catch (IOException e) {
          // do nothing
        } finally {
          IOUtils.closeQuietly(br);
        }
        responseTxt = sb.toString();
      } catch (UnsupportedOperationException | IOException e1) {
        // do nothing
      } finally {
        IOUtils.closeQuietly(is);
      }
    }
    return responseTxt;
  }
}
