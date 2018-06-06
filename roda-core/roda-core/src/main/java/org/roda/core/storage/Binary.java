/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.util.Map;

public interface Binary extends Resource {

  /**
   * Retrieve the payload for the data of the binary file.
   * 
   * @return
   */
  ContentPayload getContent();

  /**
   * The total number of bytes of content of this resource.
   * 
   * @return
   */
  Long getSizeInBytes();

  /**
   * The binary is a reference to the real content, which is managed externally.
   * 
   * @return
   */
  boolean isReference();

  /**
   * Get the checksums of the binary content.
   * 
   * @return A map with all the checksums where the key is the checksum algorithm
   *         and the value is the value of the checksum for that algorithm.
   * 
   *         Example: {("md5", "1234abc..."), ("sha1", "1234567890abc...")}
   * 
   */
  Map<String, String> getContentDigest();

}
