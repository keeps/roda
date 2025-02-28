package org.roda.wui.client.common.cards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.browse.BrowseRepresentation;
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
public class AIPRepresentationCardList extends ThumbnailCardList<IndexedRepresentation> {
  public AIPRepresentationCardList(String aipId) {
    super(messages.someOfAObject(IndexedRepresentation.class.getName()),
      ConfigurationManager.getString(RodaConstants.UI_ICONS_CLASS, IndexedRepresentation.class.getSimpleName()),
      IndexedRepresentation.class, new Filter(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aipId)),
      new CardBuilder<IndexedRepresentation>() {
        @Override
        public ThumbnailCard constructCard(ClientMessages messages, IndexedRepresentation representation) {
          // Title
          String title = representation.getType();

          // Thumbnail
          HTML iconThumbnailHTML = new HTML(
            DescriptionLevelUtils.getRepresentationTypeIcon(representation.getType(), false));

          // Tags
          List<Tag> tags = new ArrayList<>();
          for (String state : representation.getRepresentationStates()) {
            tags.add(Tag.fromText(messages.statusLabel(state), Tag.TagStyle.SUCCESS));
          }

          // Attributes
          Map<String, String> attributes = new HashMap<>();
          attributes.put(messages.fileSize(), Humanize.readableFileSize(representation.getSizeInBytes()));
          attributes.put(messages.representationFiles(), Long.toString(representation.getNumberOfDataFiles()));
          attributes.put(messages.representationFolders(), Long.toString(representation.getNumberOfDataFolders()));
          if (representation.getCreatedOn() != null) {
            attributes.put(messages.objectCreatedDateShort(), Humanize.formatDate(representation.getCreatedOn()));
          }
          if (representation.getUpdatedOn() != null) {
            attributes.put(messages.objectLastModifiedShort(), Humanize.formatDate(representation.getUpdatedOn()));
          }

          ClickHandler thumbnailClickHandler = event -> {
            HistoryUtils.newHistory(BrowseRepresentation.RESOLVER, aipId, representation.getId());
          };

          return new ThumbnailCard(title, iconThumbnailHTML, tags, attributes, thumbnailClickHandler);
        }
      });
  }
}
