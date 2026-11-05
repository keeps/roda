/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.notifications;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates webhook URLs against an admin-configured list of allowed
 * destinations (compared by scheme, host and port) and rejects any URL whose
 * host resolves to a non-public address.
 */
public final class WebhookUrlValidator {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebhookUrlValidator.class);

  /**
   * Resolver that refuses to return non-public addresses, so the address used
   * to connect is the one that was checked (prevents DNS rebinding between
   * validation and delivery).
   */
  public static final DnsResolver PUBLIC_ONLY_DNS_RESOLVER = new DnsResolver() {
    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
      InetAddress[] addresses = SystemDefaultDnsResolver.INSTANCE.resolve(host);
      for (InetAddress address : addresses) {
        if (!isPublicAddress(address)) {
          throw new UnknownHostException("Host " + host + " resolves to a non-public address");
        }
      }
      return addresses;
    }

    @Override
    public String resolveCanonicalHostname(String host) throws UnknownHostException {
      return SystemDefaultDnsResolver.INSTANCE.resolveCanonicalHostname(host);
    }
  };

  private WebhookUrlValidator() {
    // do nothing
  }

  public static void validate(String url) throws RequestNotValidException {
    validate(url, RodaCoreFactory.getRodaConfigurationAsList(RodaConstants.NOTIFICATION_WEBHOOK_ALLOWED_DESTINATIONS));
  }

  public static void validate(String url, List<String> allowedDestinations) throws RequestNotValidException {
    Origin origin = parseOrigin(url);
    if (origin == null) {
      throw new RequestNotValidException("Webhook URL is not a valid http(s) URL without user information: " + url);
    }

    boolean allowed = allowedDestinations.stream().map(WebhookUrlValidator::parseOrigin).anyMatch(origin::equals);
    if (!allowed) {
      throw new RequestNotValidException("Webhook URL is not on the list of allowed destinations: " + url);
    }

    try {
      for (InetAddress address : InetAddress.getAllByName(origin.host())) {
        if (!isPublicAddress(address)) {
          throw new RequestNotValidException("Webhook URL resolves to a non-public address: " + url);
        }
      }
    } catch (UnknownHostException e) {
      throw new RequestNotValidException("Webhook URL host could not be resolved: " + url);
    }
  }

  public static boolean isPublicAddress(InetAddress address) {
    if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
      || address.isSiteLocalAddress() || address.isMulticastAddress()) {
      return false;
    }

    byte[] bytes = address.getAddress();
    if (address instanceof Inet4Address) {
      return isPublicIPv4(bytes);
    }

    // IPv6 unique local addresses (fc00::/7)
    if ((bytes[0] & 0xFE) == 0xFC) {
      return false;
    }

    // addresses embedding an IPv4 address: IPv4-compatible (::/96) and NAT64
    // (64:ff9b::/96); IPv4-mapped addresses are already returned by Java as
    // Inet4Address
    boolean compatible = true;
    for (int i = 0; compatible && i < 12; i++) {
      compatible = bytes[i] == 0;
    }
    boolean nat64 = (bytes[0] & 0xFF) == 0x00 && (bytes[1] & 0xFF) == 0x64 && (bytes[2] & 0xFF) == 0xFF
      && (bytes[3] & 0xFF) == 0x9B;
    for (int i = 4; nat64 && i < 12; i++) {
      nat64 = bytes[i] == 0;
    }
    if (compatible || nat64) {
      byte[] ipv4 = new byte[] {bytes[12], bytes[13], bytes[14], bytes[15]};
      try {
        return isPublicAddress(InetAddress.getByAddress(ipv4));
      } catch (UnknownHostException e) {
        return false;
      }
    }

    return true;
  }

  private static boolean isPublicIPv4(byte[] bytes) {
    int b0 = bytes[0] & 0xFF;
    int b1 = bytes[1] & 0xFF;
    int b2 = bytes[2] & 0xFF;
    return b0 != 0 // 0.0.0.0/8 "this network"
      && !(b0 == 100 && (b1 & 0xC0) == 64) // 100.64.0.0/10 carrier-grade NAT
      && !(b0 == 192 && b1 == 0 && b2 == 0) // 192.0.0.0/24 protocol assignments
      && !(b0 == 198 && (b1 & 0xFE) == 18) // 198.18.0.0/15 benchmarking
      && b0 < 240; // 240.0.0.0/4 reserved and broadcast
  }

  private static Origin parseOrigin(String url) {
    if (StringUtils.isBlank(url)) {
      return null;
    }
    try {
      URI uri = new URI(url.trim());
      String scheme = uri.getScheme() == null ? null : uri.getScheme().toLowerCase(Locale.ROOT);
      if (!("http".equals(scheme) || "https".equals(scheme)) || uri.getRawUserInfo() != null
        || StringUtils.isBlank(uri.getHost())) {
        return null;
      }
      String host = uri.getHost().toLowerCase(Locale.ROOT);
      if (host.startsWith("[") && host.endsWith("]")) {
        host = host.substring(1, host.length() - 1);
      }
      int port = uri.getPort() != -1 ? uri.getPort() : ("https".equals(scheme) ? 443 : 80);
      return new Origin(scheme, host, port);
    } catch (URISyntaxException e) {
      LOGGER.debug("Invalid webhook URL: {}", url);
      return null;
    }
  }

  private record Origin(String scheme, String host, int port) {
  }
}
