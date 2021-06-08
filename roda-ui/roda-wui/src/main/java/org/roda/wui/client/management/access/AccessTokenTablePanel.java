package org.roda.wui.client.management.access;

import java.util.List;

import org.roda.core.data.v2.AccessToken.AccessToken;
import org.roda.core.data.v2.AccessToken.AccessTokens;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
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
public class AccessTokenTablePanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, AccessTokenTablePanel> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FlowPanel contentFlowPanel;

  public AccessTokenTablePanel(String username) {
    initWidget(uiBinder.createAndBindUi(this));
    BrowserService.Util.getInstance().listAccessTokenByUser(username, new NoAsyncCallback<AccessTokens>() {
      @Override
      public void onSuccess(AccessTokens accessTokens) {
        contentFlowPanel.clear();
        contentFlowPanel.add(createTable(accessTokens));
      }
    });
  }

  public ScrollPanel createTable(AccessTokens accessTokens) {
    ScrollPanel scrollPanel = new ScrollPanel();
    scrollPanel.addStyleName("basicTable-border");
    scrollPanel.addStyleName("basicTable");

    if (accessTokens.getObjects().isEmpty()) {
      String someOfAObject = messages.someOfAObject(accessTokens.getClass().getName());
      Label label = new HTML(SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters(someOfAObject)));
      label.addStyleName("basicTableEmpty");
      scrollPanel.add(label);
    } else {
      FlowPanel accessTokenPanel = new FlowPanel();
      BasicTablePanel<AccessToken> table = getBasicTableForAccessToken(accessTokens);
      table.getSelectionModel().addSelectionChangeHandler(event -> {
        AccessToken selectedObject = table.getSelectionModel().getSelectedObject();
        if (selectedObject != null) {
          table.getSelectionModel().clear();
          List<String> path = HistoryUtils.getHistory(ShowAccessToken.RESOLVER.getHistoryPath(),
            selectedObject.getId());
          HistoryUtils.newHistory(path);
        }
      });

      accessTokenPanel.add(table);
      scrollPanel.add(accessTokenPanel);
    }

    return scrollPanel;
  }

  private BasicTablePanel<AccessToken> getBasicTableForAccessToken(AccessTokens accessTokens) {
    if (accessTokens.getObjects().isEmpty()) {
      return new BasicTablePanel<>(messages.noItemsToDisplay(messages.distributedInstancesLabel()));
    } else {
      return new BasicTablePanel<AccessToken>(accessTokens.getObjects().iterator(),
        new BasicTablePanel.ColumnInfo<AccessToken>(messages.accessTokenNameLabel(), 15, new TextColumn<AccessToken>() {
          @Override
          public String getValue(AccessToken accessToken) {
            return accessToken.getName();
          }
        }), new BasicTablePanel.ColumnInfo<AccessToken>(messages.accessTokenLastUsageDateLabel(), 15,
          new TextColumn<AccessToken>() {
            @Override
            public String getValue(AccessToken accessToken) {
              return accessToken.getLastUsageDate() != null ? Humanize.formatDate(accessToken.getLastUsageDate())
                : messages.accessTokenNeverUsedLabel();
            }
          }),
        new BasicTablePanel.ColumnInfo<AccessToken>(messages.accessTokenExpirationDateLabel(), 15,
          new TextColumn<AccessToken>() {
            @Override
            public String getValue(AccessToken accessToken) {
              return accessToken.getExpirationDate() != null ? Humanize.formatDate(accessToken.getExpirationDate())
                : messages.accessTokenNotFoundLabel();
            }
          }),
        new BasicTablePanel.ColumnInfo<AccessToken>(messages.accessTokenStatusLabel(), 15,
          new Column<AccessToken, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(AccessToken accessToken) {
              return HtmlSnippetUtils.getAccessTokenStateHtml(accessToken);
            }
          }));
    }
  }
}
