/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.cards.utils;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.wui.client.common.cards.ThumbnailCard;

import config.i18n.client.ClientMessages;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public abstract class CardBuilder<T extends IsIndexed> {
  protected CardBuilder() {
    // do nothing
  }

  public abstract ThumbnailCard constructCard(ClientMessages clientMessages, T object);
}
