/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package config.i18n.client;

import com.google.gwt.i18n.client.Messages;

/**
 * @author Luis Faria
 *
 */
public interface IngestListMessages extends Messages {
  @DefaultMessage("{0} packets")
  String total(int total);

  @DefaultMessage("Reject packet {0}")
  String rejectMessageWindowTitle(String originalFilename);

  @DefaultMessage("Accept packet {0}")
  String acceptMessageWindowTitle(String originalFilename);
}
