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
