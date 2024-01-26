package org.roda.core.plugins.certificate;

import org.roda.core.data.exceptions.RODAException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class PluginCertificateException  extends RODAException {

  public PluginCertificateException() {
    super();
  }

  public PluginCertificateException(String message) {
    super(message);
  }

  public PluginCertificateException(String message, Throwable cause) {
    super(message, cause);
  }

  public PluginCertificateException(Throwable cause) {
    super(cause);
  }
}
