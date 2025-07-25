/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public interface BinaryVersion extends Serializable {

  Binary getBinary();

  String getId();

  Map<String, String> getProperties();

  Date getCreatedDate();

}
