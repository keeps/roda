package org.roda.wui.client.browse.tabs;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ConfigurableAsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import org.roda.wui.client.planning.tabs.RepresentationInformationDetailsPanel;

/**
 *
 * @author Eduardo Teixeira <eteixeira@keep.pt>
 */
public class BrowseRepresentationInformationTabs extends Tabs {
  public void init(RepresentationInformation ri) {

    List<FilterParameter> aipParams = new ArrayList<>();
    List<FilterParameter> representationParams = new ArrayList<>();
    List<FilterParameter> fileParams = new ArrayList<>();
    initEntityFilters(ri, aipParams, representationParams, fileParams);

    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.detailsTab()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new RepresentationInformationDetailsPanel(ri);
      }
    });

    createAndAddTab(SafeHtmlUtils.fromString(messages.representationInformationIntellectualEntitiesAssociations()),
      () -> buildAssociationsTab(IndexedAIP.class, aipParams, "Search_AIPs"));

    createAndAddTab(SafeHtmlUtils.fromString(messages.representationInformationRepresentationsAssociations()),
      () -> buildAssociationsTab(IndexedRepresentation.class, representationParams, "Search_representations"));

    createAndAddTab(SafeHtmlUtils.fromString(messages.representationInformationFilesAssociations()),
      () -> buildAssociationsTab(IndexedFile.class, fileParams, "Search_files"));
  }

  private void initEntityFilters(RepresentationInformation ri, List<FilterParameter> aipParams,
    List<FilterParameter> representationParams, List<FilterParameter> fileParams) {
    for (String filter : ri.getFilters()) {
      String[] parts = filter.split(RepresentationInformationUtils.REPRESENTATION_INFORMATION_FILTER_SEPARATOR);
      if (parts.length < 3) {
        continue;
      }

      if (RodaConstants.INDEX_AIP.equals(parts[0])) {
        aipParams.add(new SimpleFilterParameter(parts[1], parts[2]));
      } else if (RodaConstants.INDEX_REPRESENTATION.equals(parts[0])) {
        representationParams.add(new SimpleFilterParameter(parts[1], parts[2]));
      } else if (RodaConstants.INDEX_FILE.equals(parts[0])) {
        fileParams.add(new SimpleFilterParameter(parts[1], parts[2]));
      }
    }
  }

  private <T extends IsIndexed> Widget buildAssociationsTab(Class<T> clazz, List<FilterParameter> params,
    String listId) {

    if (params == null || params.isEmpty()) {
      if (IndexedAIP.class.equals(clazz)) {
        return buildEmptyAssociationsCardLikeTab(messages.representationInformationIntellectualEntities(0, ""));
      }
      if (IndexedRepresentation.class.equals(clazz)) {
        return buildEmptyAssociationsCardLikeTab(messages.representationInformationRepresentations(0, ""));
      }
      return buildEmptyAssociationsCardLikeTab(messages.representationInformationFiles(0, ""));
    }
    Filter filter = new Filter(new OrFiltersParameters(params));

    ListBuilder<T> listBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
      new AsyncTableCellOptions<>(clazz, listId).withFilter(filter).withJustActive(false).bindOpener());

    return new SearchWrapper(false).createListAndSearchPanel(listBuilder);
  }

  private Widget buildEmptyAssociationsCardLikeTab(String messageHtml) {
    FlowPanel card = new FlowPanel();
    card.addStyleName("roda6CardWithHeader");
    card.addStyleName("wrapper");
    card.addStyleName("skip_padding");

    FlowPanel body = new FlowPanel();
    body.addStyleName("cardBody");

    SimplePanel info = new SimplePanel();
    info.addStyleName("table-empty-inner");
    HTML label = new HTML(messageHtml);
    label.addStyleName("table-empty-inner-label");
    info.setWidget(label);

    body.add(info);
    card.add(body);
    return card;
  }
}
