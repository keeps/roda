package org.roda.wui.client.management;

import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import org.roda.core.data.v2.institution.Institution;
import org.roda.core.data.v2.institution.Institutions;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.SidebarUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class InstitutionManagement extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {InstitutionManagement.RESOLVER}, false, callback);
    }

    @Override
    public String getHistoryToken() {
      return "institutions";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Management.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };
  private static InstitutionManagement instance = null;

  interface MyUiBinder extends UiBinder<Widget, InstitutionManagement> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static InstitutionManagement getInstance() {
    if (instance == null) {
      instance = new InstitutionManagement();
    } else {
      instance.refresh();
    }
    return instance;
  }

  @UiField
  FlowPanel institutionManagementDescription;

  @UiField
  FlowPanel contentFlowPanel;

  @UiField
  ScrollPanel institutionManagementTablePanel;

  @UiField
  FlowPanel sidebarFlowPanel;

  @UiField
  FlowPanel sidebarButtonsPanel;

  public InstitutionManagement() {
    initWidget(uiBinder.createAndBindUi(this));
    institutionManagementDescription.add(new HTMLWidgetWrapper(("DisposalPolicyDescription.html")));
    BrowserService.Util.getInstance().listInstitutions(new NoAsyncCallback<Institutions>() {
      @Override
      public void onSuccess(Institutions institutions) {
        init(institutions);
      }
    });
    initSidebar();
  }

  private void initSidebar() {
    SidebarUtils.showSidebar(contentFlowPanel, sidebarFlowPanel);
    Button createInstitutionBtn = new Button();
    createInstitutionBtn.addStyleName("btn btn-block btn-plus");
    createInstitutionBtn.setText(messages.newButton());
    createInstitutionBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        HistoryUtils.newHistory(CreateInstitution.RESOLVER);
      }
    });

    sidebarButtonsPanel.add(createInstitutionBtn);
  }

  private void init(Institutions institutions) {
    createInstitutionListPanel(institutions);
  }

  private void createInstitutionListPanel(Institutions institutions) {
    institutionManagementTablePanel.clear();
    institutionManagementTablePanel.addStyleName("basicTable-border");
    institutionManagementTablePanel.addStyleName("basicTable");

    if (institutions.getObjects().isEmpty()) {
      String someOfAObject = messages.someOfAObject(institutions.getClass().getName());
      Label label = new HTML(SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters(someOfAObject)));
      label.addStyleName("basicTableEmpty");
      institutionManagementTablePanel.add(label);
    } else {
      FlowPanel institutionsPanel = new FlowPanel();
      BasicTablePanel<Institution> tableInstitutions = getBasicTableForInstitutions(institutions);
      tableInstitutions.getSelectionModel().addSelectionChangeHandler(event -> {
        Institution selectedObject = tableInstitutions.getSelectionModel().getSelectedObject();
        if (selectedObject != null) {
          tableInstitutions.getSelectionModel().clear();
          List<String> path = HistoryUtils.getHistory(ShowInstitution.RESOLVER.getHistoryPath(),
            selectedObject.getId());
          HistoryUtils.newHistory(path);
        }
      });

      institutionsPanel.add(tableInstitutions);
      institutionManagementTablePanel.add(institutionsPanel);
    }
  }

  private BasicTablePanel<Institution> getBasicTableForInstitutions(Institutions institutions) {
    if (institutions.getObjects().isEmpty()) {
      return new BasicTablePanel<>(messages.noItemsToDisplay(messages.institutionsLabel()));
    } else {
      return new BasicTablePanel<Institution>(institutions.getObjects().iterator(),
        new BasicTablePanel.ColumnInfo<Institution>(messages.institutionNameLabel(), 15, new TextColumn<Institution>() {
          @Override
          public String getValue(Institution institution) {
            return institution.getName();
          }
        }), new BasicTablePanel.ColumnInfo<Institution>(messages.institutionStatusLabel(), 15,
          new Column<Institution, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(Institution institution) {
              return HtmlSnippetUtils.getInstitutionStateHtml(institution);
            }
          }),
        new BasicTablePanel.ColumnInfo<Institution>(messages.institutionLastSyncDateLabel(), 15,
          new TextColumn<Institution>() {
            @Override
            public String getValue(Institution institution) {
              return institution.getLastSyncDate() != null ? institution.getLastSyncDate().toString() : "None";
            }
          }),
        new BasicTablePanel.ColumnInfo<Institution>(messages.institutionAccessKeyLabel(), 15,
          new TextColumn<Institution>() {
            @Override
            public String getValue(Institution institution) {
              return institution.getAccessKey();
            }
          }));
    }
  }

  private void refresh() {
    GWT.log("Refresh");
    BrowserService.Util.getInstance().listInstitutions(new NoAsyncCallback<Institutions>() {
      @Override
      public void onSuccess(Institutions institutions) {
        GWT.log("init");
        init(institutions);
      }
    });
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      callback.onSuccess(this);
    } else if (historyTokens.get(0).equals(CreateInstitution.RESOLVER.getHistoryToken())) {
      CreateInstitution.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(ShowInstitution.RESOLVER.getHistoryToken())) {
      ShowInstitution.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(EditInstitution.RESOLVER.getHistoryToken())) {
      EditInstitution.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else {
      HistoryUtils.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }
}
