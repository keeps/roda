/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management.access;

import java.util.List;

import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.accessKey.AccessKeys;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class AccessKeyTablePanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, AccessKeyTablePanel> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FlowPanel contentFlowPanel;

  public AccessKeyTablePanel(String username) {
    initWidget(uiBinder.createAndBindUi(this));
    Services services = new Services("Get user access keys", "get");
    services.membersResource(s -> s.getAccessKeysByUser(username)).whenComplete((accessKeys, error) -> {
      if (accessKeys != null) {
        contentFlowPanel.clear();
        contentFlowPanel.add(createTable(accessKeys));
      }
    });
  }

  public ScrollPanel createTable(AccessKeys accessKeys) {
    ScrollPanel scrollPanel = new ScrollPanel();
    scrollPanel.addStyleName("basicTable-border");
    scrollPanel.addStyleName("basicTable");

    if (accessKeys.getObjects().isEmpty()) {
      String someOfAObject = messages.someOfAObject(accessKeys.getClass().getName());
      Label label = new HTML(SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters(someOfAObject)));
      label.addStyleName("basicTableEmpty");
      scrollPanel.add(label);
    } else {
      FlowPanel accessKeyPanel = new FlowPanel();
      BasicTablePanel<AccessKey> table = getBasicTableForAccessKey(accessKeys);
      table.getSelectionModel().addSelectionChangeHandler(event -> {
        AccessKey selectedObject = table.getSelectionModel().getSelectedObject();
        if (selectedObject != null) {
          table.getSelectionModel().clear();
          List<String> path = HistoryUtils.getHistory(ShowAccessKey.RESOLVER.getHistoryPath(), selectedObject.getId());
          HistoryUtils.newHistory(path);
        }
      });

      accessKeyPanel.add(table);
      scrollPanel.add(accessKeyPanel);
    }

    return scrollPanel;
  }

  private BasicTablePanel<AccessKey> getBasicTableForAccessKey(AccessKeys accessKeys) {
    if (accessKeys.getObjects().isEmpty()) {
      return new BasicTablePanel<>(messages.noItemsToDisplay(messages.distributedInstancesLabel()));
    } else {
      return new BasicTablePanel<AccessKey>(accessKeys.getObjects().iterator(),
        new BasicTablePanel.ColumnInfo<AccessKey>(messages.accessKeyNameLabel(), 15, new TextColumn<AccessKey>() {
          @Override
          public String getValue(AccessKey accessKey) {
            return accessKey.getName();
          }
        }), new BasicTablePanel.ColumnInfo<AccessKey>(messages.accessKeyLastUsageDateLabel(), 15,
          new TextColumn<AccessKey>() {
            @Override
            public String getValue(AccessKey accessKey) {
              return accessKey.getLastUsageDate() != null ? Humanize.formatDate(accessKey.getLastUsageDate())
                : messages.accessKeyNeverUsedLabel();
            }
          }),
        new BasicTablePanel.ColumnInfo<AccessKey>(messages.accessKeyExpirationDateLabel(), 15,
          new TextColumn<AccessKey>() {
            @Override
            public String getValue(AccessKey accessKey) {
              return accessKey.getExpirationDate() != null ? Humanize.formatDate(accessKey.getExpirationDate())
                : messages.accessKeyNotFoundLabel();
            }
          }),
        new BasicTablePanel.ColumnInfo<AccessKey>(messages.accessKeyStatusLabel(), 15,
          new Column<AccessKey, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(AccessKey accessKey) {
              return HtmlSnippetUtils.getAccessKeyStateHtml(accessKey);
            }
          }));
    }
  }
}
