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

/**
 * Only IP literals are used, so no DNS lookups are made.
 */
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class WebhookUrlValidatorTest {

  private static final List<String> ALLOWED = List.of("https://1.1.1.1", "http://8.8.8.8:8080",
    "https://[2606:4700:4700::1111]", "http://127.0.0.1:8080", "http://10.0.0.1");

  @DataProvider
  public Object[][] acceptedUrls() {
    return new Object[][] {{"https://1.1.1.1"}, {"https://1.1.1.1/hook"}, {"https://1.1.1.1/hook?a=b#c"},
      {"https://1.1.1.1:443/hook"}, {"HTTPS://1.1.1.1/hook"}, {"  https://1.1.1.1/hook  "},
      {"http://8.8.8.8:8080/hook"}, {"https://[2606:4700:4700::1111]/hook"}};
  }

  @Test(dataProvider = "acceptedUrls")
  public void testAllowedPublicDestinationIsAccepted(String url) throws RequestNotValidException {
    WebhookUrlValidator.validate(url, ALLOWED);
  }

  @DataProvider
  public Object[][] rejectedUrls() {
    return new Object[][] {
      // not a valid http(s) URL
      {null}, {""}, {"   "}, {"not a url"}, {"https://"}, {"ftp://1.1.1.1/hook"}, {"file:///etc/passwd"},
      {"//1.1.1.1/hook"}, {"https://user:pass@1.1.1.1/hook"}, {"https://user@1.1.1.1/hook"},
      // valid but not on the list: scheme, host or port mismatch
      {"http://1.1.1.1/hook"}, {"https://1.0.0.1/hook"}, {"https://1.1.1.1:8443/hook"},
      {"http://8.8.8.8/hook"}, {"https://8.8.8.8:8080/hook"},
      // on the list but internal
      {"http://127.0.0.1:8080/hook"}, {"http://10.0.0.1/hook"}};
  }

  @Test(dataProvider = "rejectedUrls", expectedExceptions = RequestNotValidException.class)
  public void testInvalidOrNotAllowedUrlIsRejected(String url) throws RequestNotValidException {
    WebhookUrlValidator.validate(url, ALLOWED);
  }

  @Test(expectedExceptions = RequestNotValidException.class)
  public void testEmptyAllowListRejectsEverything() throws RequestNotValidException {
    WebhookUrlValidator.validate("https://1.1.1.1/hook", Collections.emptyList());
  }

  @Test
  public void testAllowListEntryPathIsIgnored() throws RequestNotValidException {
    WebhookUrlValidator.validate("https://1.1.1.1/other", List.of("https://1.1.1.1/hook?x=y"));
  }

  @Test
  public void testInvalidAllowListEntriesAreIgnored() throws RequestNotValidException {
    List<String> allowed = List.of("", "not a url", "ftp://1.1.1.1", "https://user@1.1.1.1", "https://1.1.1.1");
    WebhookUrlValidator.validate("https://1.1.1.1/hook", allowed);
  }

  @Test(expectedExceptions = RequestNotValidException.class)
  public void testOnlyInvalidAllowListEntriesRejectEverything() throws RequestNotValidException {
    WebhookUrlValidator.validate("https://1.1.1.1/hook", List.of("", "not a url", "https://user@1.1.1.1"));
  }

  @DataProvider
  public Object[][] nonPublicAddresses() {
    return new Object[][] {
      // IPv4
      {"0.0.0.0"}, {"0.1.2.3"}, {"127.0.0.1"}, {"127.255.255.254"}, {"10.0.0.1"}, {"172.16.0.1"},
      {"172.31.255.255"}, {"192.168.1.1"}, {"169.254.169.254"}, {"100.64.0.1"}, {"100.127.255.255"},
      {"192.0.0.1"}, {"198.18.0.1"}, {"198.19.255.255"}, {"224.0.0.1"}, {"239.255.255.255"}, {"240.0.0.1"},
      {"255.255.255.255"},
      // IPv6
      {"::"}, {"::1"}, {"fe80::1"}, {"fc00::1"}, {"fd12:3456::1"}, {"ff02::1"},
      // IPv6 embedding an internal IPv4 address
      {"::ffff:127.0.0.1"}, {"::ffff:10.0.0.1"}, {"::127.0.0.1"}, {"::10.0.0.1"}, {"64:ff9b::7f00:1"},
      {"64:ff9b::a9fe:a9fe"}};
  }

  @Test(dataProvider = "nonPublicAddresses")
  public void testNonPublicAddress(String address) throws UnknownHostException {
    Assert.assertFalse(WebhookUrlValidator.isPublicAddress(InetAddress.getByName(address)), address);
  }

  @DataProvider
  public Object[][] publicAddresses() {
    return new Object[][] {
      // IPv4, including the neighbours of the reserved ranges
      {"1.1.1.1"}, {"8.8.8.8"}, {"100.63.255.255"}, {"100.128.0.1"}, {"172.15.255.255"}, {"172.32.0.1"},
      {"192.0.1.1"}, {"198.17.255.255"}, {"198.20.0.1"}, {"223.255.255.255"},
      // IPv6
      {"2606:4700:4700::1111"}, {"2001:4860:4860::8888"},
      // IPv6 embedding a public IPv4 address
      {"::ffff:8.8.8.8"}, {"64:ff9b::808:808"}};
  }

  @Test(dataProvider = "publicAddresses")
  public void testPublicAddress(String address) throws UnknownHostException {
    Assert.assertTrue(WebhookUrlValidator.isPublicAddress(InetAddress.getByName(address)), address);
  }

  @Test
  public void testPublicOnlyDnsResolverReturnsPublicAddress() throws UnknownHostException {
    InetAddress[] addresses = WebhookUrlValidator.PUBLIC_ONLY_DNS_RESOLVER.resolve("1.1.1.1");
    Assert.assertEquals(addresses.length, 1);
    Assert.assertEquals(addresses[0].getHostAddress(), "1.1.1.1");
  }

  @Test(dataProvider = "nonPublicAddresses", expectedExceptions = UnknownHostException.class)
  public void testPublicOnlyDnsResolverRefusesNonPublicAddress(String address) throws UnknownHostException {
    WebhookUrlValidator.PUBLIC_ONLY_DNS_RESOLVER.resolve(address);
  }
}
