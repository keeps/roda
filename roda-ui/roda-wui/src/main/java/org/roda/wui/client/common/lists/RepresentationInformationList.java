/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.NamedIndexedModel;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ProvidesKey;

import config.i18n.client.ClientMessages;

public class RepresentationInformationList extends AsyncTableCell<RepresentationInformation> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private Column<RepresentationInformation, SafeHtml> nameColumn;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.REPRESENTATION_INFORMATION_ID, RodaConstants.REPRESENTATION_INFORMATION_NAME,
    RodaConstants.REPRESENTATION_INFORMATION_TAGS, RodaConstants.REPRESENTATION_INFORMATION_SUPPORT,
    RodaConstants.REPRESENTATION_INFORMATION_FAMILY);

  @Override
  protected void adjustOptions(AsyncTableCellOptions<RepresentationInformation> options) {
    options.withFieldsToReturn(fieldsToReturn);
  }

  @Override
  protected void configureDisplay(CellTable<RepresentationInformation> display) {

    nameColumn = new Column<RepresentationInformation, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(RepresentationInformation ri) {
        SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
        safeHtmlBuilder.append(SafeHtmlUtils.fromString(ri.getName()));
        for (String tag : ri.getTags()) {
          safeHtmlBuilder
            .append(SafeHtmlUtils.fromTrustedString("<span class='label label-info btn-separator-left ri-category'>"))
            .append(
              SafeHtmlUtils.fromString(messages.representationInformationListItems(SafeHtmlUtils.htmlEscape(tag))))
            .append(SafeHtmlUtils.fromTrustedString("</span>"));
        }

        return safeHtmlBuilder.toSafeHtml();
      }
    };

    TextColumn<RepresentationInformation> supportColumn = new TextColumn<RepresentationInformation>() {
      @Override
      public String getValue(RepresentationInformation ri) {
        return messages.representationInformationSupportValue(ri.getSupport().toString());
      }
    };

    TextColumn<RepresentationInformation> familyColumn = new TextColumn<RepresentationInformation>() {
      @Override
      public String getValue(RepresentationInformation ri) {
        return ri != null ? ri.getFamilyI18n() : null;
      }
    };

    nameColumn.setSortable(true);

    addColumn(nameColumn, messages.representationInformationName(), false, false);
    addColumn(supportColumn, messages.representationInformationSupport(), false, false, 8.5);
    addColumn(familyColumn, messages.representationInformationFamily(), false, false, 7);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(nameColumn, true));
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<RepresentationInformation, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    columnSortingKeyMap.put(nameColumn, Arrays.asList(RodaConstants.REPRESENTATION_INFORMATION_NAME_SORT));
    return createSorter(columnSortList, columnSortingKeyMap);
  }

  @Override
  protected ProvidesKey<RepresentationInformation> getKeyProvider() {
    return NamedIndexedModel::getId;
  }
}
