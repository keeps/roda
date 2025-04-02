package org.roda.wui.client.common.cards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.wui.client.browse.BrowseDIP;
import org.roda.wui.client.common.cards.utils.CardBuilder;
import org.roda.wui.client.common.labels.Tag;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;

import config.i18n.client.ClientMessages;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class FileDisseminationCardList extends ThumbnailCardList<IndexedDIP> {
  public FileDisseminationCardList(String aipId, String representationId, String fileId, String fileUUID) {
    super(messages.someOfAObject(IndexedDIP.class.getName()),
      ConfigurationManager.getString(RodaConstants.UI_ICONS_CLASS, IndexedDIP.class.getSimpleName()), IndexedDIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.DIP_FILE_UUIDS, fileUUID)), new CardBuilder<IndexedDIP>() {
        @Override
        public ThumbnailCard constructCard(ClientMessages messages, IndexedDIP dip) {
          // Title
          String title = dip.getTitle();

          // Thumbnail
          HTML iconThumbnailHTML = new HTML(DescriptionLevelUtils.getRepresentationTypeIcon(dip.getType(), false));

          // Tags
          List<Tag> tags = new ArrayList<>();

          // Attributes
          Map<String, String> attributes = new HashMap<>();
          attributes.put(messages.disseminationFiles(), Long.toString(dip.getFileIds().size()));
          if (dip.getDateCreated() != null) {
            attributes.put(messages.objectCreatedDateShort(), Humanize.formatDate(dip.getDateCreated()));
          }
          if (dip.getLastModified() != null) {
            attributes.put(messages.objectLastModifiedShort(), Humanize.formatDate(dip.getLastModified()));
          }

          ClickHandler thumbnailClickHandler = event -> {
            HistoryUtils.newHistory(BrowseDIP.RESOLVER, aipId, representationId, fileId, dip.getId());
          };

          return new ThumbnailCard(title, iconThumbnailHTML, tags, attributes, thumbnailClickHandler);
        }
      });
  }
}
