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
