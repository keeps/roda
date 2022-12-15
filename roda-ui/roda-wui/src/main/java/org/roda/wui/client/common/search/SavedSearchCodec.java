/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.search;

import org.fusesource.restygwt.client.JsonEncoderDecoder;
import org.roda.core.data.v2.common.SavedSearch;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public interface SavedSearchCodec extends JsonEncoderDecoder<SavedSearch> {
}