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

import java.util.Date;
import java.util.List;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.DateRangeFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.SearchPanel;
import org.roda.wui.client.common.Dialogs;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.AsyncTableCell.CheckboxSelectionListener;
import org.roda.wui.client.common.lists.FormatList;
import org.roda.wui.client.common.lists.SelectedItemsUtils;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.BrowseMessages;
import config.i18n.client.FormatMessages;

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
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {FormatRegister.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(Planning.RESOLVER.getHistoryPath(), getHistoryToken());
    }

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

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static final BrowseMessages defaultMessages = GWT.create(BrowseMessages.class);
  private static final FormatMessages messages = GWT.create(FormatMessages.class);

  @UiField
  FlowPanel formatRegisterDescription;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField(provided = true)
  FormatList formatList;

  @UiField
  DateBox inputDateInitial;

  @UiField
  DateBox inputDateFinal;

  @UiField
  Button buttonAdd;

  @UiField
  Button buttonRemove;

  @UiField
  Button startProcess;

  private static final Filter DEFAULT_FILTER = new Filter(new BasicSearchFilterParameter(RodaConstants.FORMAT_SEARCH,
    "*"));

  /**
   * Create a format register page
   *
   * @param user
   */
  public FormatRegister() {
    Filter filter = null;
    Facets facets = null;

    formatList = new FormatList(filter, facets, "Formats", true);

    searchPanel = new SearchPanel(DEFAULT_FILTER, RodaConstants.FORMAT_SEARCH,
      messages.formatRegisterSearchPlaceHolder(), false, false);
    searchPanel.setList(formatList);

    formatList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        Format selected = formatList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          Tools.newHistory(RESOLVER, ShowFormat.RESOLVER.getHistoryToken(), selected.getId());
        }
      }
    });

    formatList.addCheckboxSelectionListener(new CheckboxSelectionListener() {

      @Override
      public void onSelectionChange(SelectedItems selected) {
        boolean empty = SelectedItemsUtils.isEmpty(selected);
        if (empty) {
          buttonRemove.setEnabled(false);
        } else {
          buttonRemove.setEnabled(true);
        }
      }

    });

    initWidget(uiBinder.createAndBindUi(this));

    formatRegisterDescription.add(new HTMLWidgetWrapper("FormatRegisterDescription.html"));

    buttonRemove.setEnabled(false);

    DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
    ValueChangeHandler<Date> valueChangeHandler = new ValueChangeHandler<Date>() {

      @Override
      public void onValueChange(ValueChangeEvent<Date> event) {
        updateDateFilter();
      }

    };

    inputDateInitial.setFormat(dateFormat);
    inputDateInitial.getDatePicker().setYearArrowsVisible(true);
    inputDateInitial.setFireNullValues(true);
    inputDateInitial.addValueChangeHandler(valueChangeHandler);

    inputDateFinal.setFormat(dateFormat);
    inputDateFinal.getDatePicker().setYearArrowsVisible(true);
    inputDateFinal.setFireNullValues(true);
    inputDateFinal.addValueChangeHandler(valueChangeHandler);

    inputDateInitial.getElement().setPropertyString("placeholder", defaultMessages.sidebarFilterFromDatePlaceHolder());
    inputDateFinal.getElement().setPropertyString("placeholder", defaultMessages.sidebarFilterToDatePlaceHolder());
  }

  private void updateDateFilter() {
    Date dateInitial = inputDateInitial.getDatePicker().getValue();
    Date dateFinal = inputDateFinal.getDatePicker().getValue();

    DateRangeFilterParameter filterParameter = new DateRangeFilterParameter(RodaConstants.FORMAT_INITIAL_RELEASE,
      dateInitial, dateFinal, RodaConstants.DateGranularity.DAY);

    formatList.setFilter(new Filter(filterParameter));
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      formatList.refresh();
      callback.onSuccess(this);
    } else if (historyTokens.size() == 2 && historyTokens.get(0).equals(ShowFormat.RESOLVER.getHistoryToken())) {
      ShowFormat.RESOLVER.resolve(Tools.tail(historyTokens), callback);
    } else if (historyTokens.size() == 1 && historyTokens.get(0).equals(CreateFormat.RESOLVER.getHistoryToken())) {
      CreateFormat.RESOLVER.resolve(Tools.tail(historyTokens), callback);
    } else if (historyTokens.size() == 2 && historyTokens.get(0).equals(EditFormat.RESOLVER.getHistoryToken())) {
      EditFormat.RESOLVER.resolve(Tools.tail(historyTokens), callback);
    } else {
      Tools.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

  @UiHandler("buttonAdd")
  void buttonAddFormatHandler(ClickEvent e) {
    Tools.newHistory(RESOLVER, CreateFormat.RESOLVER.getHistoryToken());
  }

  @UiHandler("buttonRemove")
  void buttonRemoveFormatHandler(ClickEvent e) {

    final SelectedItems selected = formatList.getSelected();

    SelectedItemsUtils.size(TransferredResource.class, selected, new AsyncCallback<Long>() {

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
                BrowserService.Util.getInstance().removeFormat(selected, new AsyncCallback<Void>() {

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
}
