/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package config.i18n.client;

import com.google.gwt.i18n.client.Messages;

public interface ClientGeneratedMessages extends Messages {

  String nationalityList(@Select int index);

  String countryList(@Select int index);
}