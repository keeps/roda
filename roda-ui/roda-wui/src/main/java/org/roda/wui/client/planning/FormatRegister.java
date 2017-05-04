/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 *
 */
package org.roda.wui.client.planning;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.lists.FormatList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell.CheckboxSelectionListener;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class FormatRegister extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Planning.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "formatregister";
    }
  };

  private static FormatRegister instance = null;

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static FormatRegister getInstance() {
    if (instance == null) {
      instance = new FormatRegister();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, FormatRegister> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel formatRegisterDescription;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField(provided = true)
  FormatList formatList;

  @UiField
  Button buttonAdd;

  @UiField
  Button buttonRemove;

  @UiField
  Button startProcess;

  private static final Filter DEFAULT_FILTER = SearchFilters.defaultFilter(Format.class.getName());
  private static final String ALL_FILTER = SearchFilters.allFilter(Format.class.getName());

  /**
   * Create a format register page
   *
   * @param user
   */
  public FormatRegister() {
    Facets facets = null;
    formatList = new FormatList(Filter.NULL, facets, messages.formatsTitle(), true);

    searchPanel = new SearchPanel(DEFAULT_FILTER, ALL_FILTER, true, messages.formatRegisterSearchPlaceHolder(), false,
      false, false);
    searchPanel.setList(formatList);

    formatList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        Format selected = formatList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          HistoryUtils.newHistory(RESOLVER, ShowFormat.RESOLVER.getHistoryToken(), selected.getId());
        }
      }
    });

    formatList.addCheckboxSelectionListener(new CheckboxSelectionListener<Format>() {

      @Override
      public void onSelectionChange(SelectedItems<Format> selected) {
        boolean empty = ClientSelectedItemsUtils.isEmpty(selected);
        buttonRemove.setEnabled(!empty);
        startProcess.setEnabled(!empty);
      }

    });

    initWidget(uiBinder.createAndBindUi(this));

    formatRegisterDescription.add(new HTMLWidgetWrapper("FormatRegisterDescription.html"));
    buttonRemove.setEnabled(false);
    startProcess.setEnabled(false);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      formatList.refresh();
      formatList.setFilter(Filter.ALL);
      callback.onSuccess(this);
    } else if (historyTokens.size() == 2 && historyTokens.get(0).equals(ShowFormat.RESOLVER.getHistoryToken())) {
      ShowFormat.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() == 1 && historyTokens.get(0).equals(CreateFormat.RESOLVER.getHistoryToken())) {
      CreateFormat.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() == 2 && historyTokens.get(0).equals(EditFormat.RESOLVER.getHistoryToken())) {
      EditFormat.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else {
      HistoryUtils.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

  @UiHandler("buttonAdd")
  void buttonAddFormatHandler(ClickEvent e) {
    HistoryUtils.newHistory(RESOLVER, CreateFormat.RESOLVER.getHistoryToken());
  }

  @UiHandler("buttonRemove")
  void buttonRemoveFormatHandler(ClickEvent e) {
    final SelectedItems<Format> selected = formatList.getSelected();

    ClientSelectedItemsUtils.size(Format.class, selected, new AsyncCallback<Long>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.formatRemoveFolderConfirmDialogTitle(),
          messages.formatRemoveSelectedConfirmDialogMessage(size), messages.formatRemoveFolderConfirmDialogCancel(),
          messages.formatRemoveFolderConfirmDialogOk(), new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable caught) {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
            }

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                BrowserService.Util.getInstance().deleteFormat(selected, new AsyncCallback<Void>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    AsyncCallbackUtils.defaultFailureTreatment(caught);
                    formatList.refresh();
                  }

                  @Override
                  public void onSuccess(Void result) {
                    Toast.showInfo(messages.formatRemoveSuccessTitle(), messages.formatRemoveSuccessMessage(size));
                    formatList.refresh();
                  }
                });
              }
            }
          });
      }
    });
  }

  @UiHandler("startProcess")
  void handleButtonProcess(ClickEvent e) {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setSelectedItems(formatList.getSelected());
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
  }
}
