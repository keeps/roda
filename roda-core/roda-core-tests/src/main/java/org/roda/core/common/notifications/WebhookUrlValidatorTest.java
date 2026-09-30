/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.notifications;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class WebhookUrlValidatorTest {
  // public IP literals are used so tests do not depend on external DNS
  private static final String PUBLIC_IP = "93.184.216.34";
  private static final List<String> ALLOWED = List.of("https://" + PUBLIC_IP, "http://" + PUBLIC_IP + ":8080");

  @Test
  public void testAllowedDestinationIsAccepted() throws RequestNotValidException {
    WebhookUrlValidator.validate("https://" + PUBLIC_IP + "/callback?job=1", ALLOWED);
    WebhookUrlValidator.validate("https://" + PUBLIC_IP + ":443/callback", ALLOWED);
    WebhookUrlValidator.validate("HTTP://" + PUBLIC_IP + ":8080/", ALLOWED);
  }

  @DataProvider
  public Object[][] rejectedUrls() {
    return new Object[][] {{null}, {""}, {"not a url"}, {"ftp://" + PUBLIC_IP + "/"},
      {"https://user:pass@" + PUBLIC_IP + "/"}, {"http://" + PUBLIC_IP + "/"}, {"https://" + PUBLIC_IP + ":8443/"},
      {"https://" + PUBLIC_IP + ".evil.example/"}, {"https://localhost/"}};
  }

  @Test(dataProvider = "rejectedUrls", expectedExceptions = RequestNotValidException.class)
  public void testUrlsNotOnAllowedListAreRejected(String url) throws RequestNotValidException {
    WebhookUrlValidator.validate(url, ALLOWED);
  }

  @Test(expectedExceptions = RequestNotValidException.class)
  public void testEmptyAllowedListRejectsEverything() throws RequestNotValidException {
    WebhookUrlValidator.validate("https://" + PUBLIC_IP + "/", Collections.emptyList());
  }

  @DataProvider
  public Object[][] internalHosts() {
    return new Object[][] {{"127.0.0.1"}, {"10.1.2.3"}, {"172.16.0.1"}, {"192.168.1.1"}, {"169.254.169.254"},
      {"100.64.0.1"}, {"0.0.0.0"}, {"[::1]"}, {"[fd00::1]"}, {"[fe80::1]"}, {"[::ffff:127.0.0.1]"},
      {"[64:ff9b::a9fe:a9fe]"}};
  }

  @Test(dataProvider = "internalHosts", expectedExceptions = RequestNotValidException.class)
  public void testAllowedButInternalDestinationIsRejected(String host) throws RequestNotValidException {
    String url = "https://" + host + "/";
    WebhookUrlValidator.validate(url, List.of(url));
  }

  @Test
  public void testPublicOnlyDnsResolver() throws UnknownHostException {
    Assert.assertEquals(WebhookUrlValidator.PUBLIC_ONLY_DNS_RESOLVER.resolve(PUBLIC_IP)[0],
      InetAddress.getByName(PUBLIC_IP));
    Assert.assertThrows(UnknownHostException.class,
      () -> WebhookUrlValidator.PUBLIC_ONLY_DNS_RESOLVER.resolve("localhost"));
    Assert.assertThrows(UnknownHostException.class,
      () -> WebhookUrlValidator.PUBLIC_ONLY_DNS_RESOLVER.resolve("169.254.169.254"));
  }
}
