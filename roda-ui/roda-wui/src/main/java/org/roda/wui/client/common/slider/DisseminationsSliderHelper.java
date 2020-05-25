/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.slider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.DisseminationActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.popup.CalloutPopup;
import org.roda.wui.client.common.popup.CalloutPopup.CalloutPosition;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.IndexedDIPUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.UIObject;

import config.i18n.client.ClientMessages;

public class DisseminationsSliderHelper {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private DisseminationsSliderHelper() {
    // do nothing
  }

  private static void updateDisseminationsSliderPanel(final IndexedAIP aip,
    final SliderPanel disseminationsSliderPanel) {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_AIP_UUIDS, aip.getUUID()));
    updateDisseminations(filter, disseminationsSliderPanel);
  }

  private static void updateDisseminationsSliderPanel(IndexedRepresentation representation,
    final SliderPanel disseminationsSliderPanel) {
    Filter filter = new Filter(
      new SimpleFilterParameter(RodaConstants.DIP_REPRESENTATION_UUIDS, representation.getUUID()));
    updateDisseminations(filter, disseminationsSliderPanel);
  }

  private static void updateDisseminationsSliderPanel(IndexedFile file, final SliderPanel disseminationsSliderPanel) {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_FILE_UUIDS, file.getUUID()));
    updateDisseminations(filter, disseminationsSliderPanel);
  }

  private static void updateDisseminations(Filter filter, final SliderPanel disseminationsSliderPanel) {
    Sorter sorter = new Sorter(new SortParameter(RodaConstants.DIP_DATE_CREATED, true));
    Sublist sublist = new Sublist(0, 100);
    Facets facets = Facets.NONE;
    String localeString = LocaleInfo.getCurrentLocale().getLocaleName();

    List<String> dipFields = new ArrayList<>(RodaConstants.DIP_PERMISSIONS_FIELDS_TO_RETURN);
    dipFields.addAll(Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.DIP_ID, RodaConstants.DIP_TITLE,
      RodaConstants.DIP_DESCRIPTION, RodaConstants.DIP_DELETE_EXTERNAL_URL, RodaConstants.DIP_OPEN_EXTERNAL_URL));

    BrowserService.Util.getInstance().find(IndexedDIP.class.getName(), filter, sorter, sublist, facets, localeString,
      true, dipFields, new AsyncCallback<IndexResult<IndexedDIP>>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(IndexResult<IndexedDIP> result) {
          updateDisseminationsSliderPanel(result.getResults(), disseminationsSliderPanel);
        }
      });
  }

  private static void updateDisseminationsSliderPanel(List<IndexedDIP> dips, SliderPanel disseminationsSliderPanel) {
    disseminationsSliderPanel.clear();
    disseminationsSliderPanel.addTitle(new Label(messages.viewRepresentationFileDisseminationTitle()));

    if (dips.isEmpty()) {
      Label dipEmpty = new Label(messages.browseFileDipEmpty());
      disseminationsSliderPanel.addContent(dipEmpty);
      dipEmpty.addStyleName("dip-empty");
    } else {
      for (final IndexedDIP dip : dips) {
        disseminationsSliderPanel.addContent(createDisseminationPanel(dip));
      }
    }
  }

  private static FlowPanel createDisseminationPanel(final IndexedDIP dip) {
    FlowPanel layout = new FlowPanel();

    // open layout
    FlowPanel leftLayout = new FlowPanel();
    Label titleLabel = new Label(dip.getTitle());
    Label descriptionLabel = new Label(dip.getDescription());

    leftLayout.add(titleLabel);
    leftLayout.add(descriptionLabel);

    FocusPanel openFocus = new FocusPanel(leftLayout);
    layout.add(openFocus);

    // options
    HTML optionsIcon = new HTML(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-ellipsis-v'></i>"));
    final FocusPanel optionsButton = new FocusPanel(optionsIcon);

    optionsButton.addStyleName("lightbtn");
    optionsIcon.addStyleName("lightbtn-icon");
    optionsButton.setTitle(messages.browseFileDipDelete());

    optionsButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        showActions(dip, optionsButton);
      }
    });

    layout.add(optionsButton);

    titleLabel.addStyleName("dipTitle");
    descriptionLabel.addStyleName("dipDescription");
    layout.addStyleName("dip");
    leftLayout.addStyleName("dip-left");
    openFocus.addStyleName("dip-focus");
    optionsButton.addStyleName("dip-options");

    openFocus.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        HistoryUtils.openBrowse(dip);
      }

    });

    return layout;
  }

  protected static <T extends IsIndexed> void updateDisseminationsObjectSliderPanel(final T object,
    final SliderPanel disseminationsSliderPanel) {
    if (object instanceof IndexedAIP) {
      updateDisseminationsSliderPanel((IndexedAIP) object, disseminationsSliderPanel);
    } else if (object instanceof IndexedRepresentation) {
      updateDisseminationsSliderPanel((IndexedRepresentation) object, disseminationsSliderPanel);
    } else if (object instanceof IndexedFile) {
      updateDisseminationsSliderPanel((IndexedFile) object, disseminationsSliderPanel);
    } else {
      // do nothing
    }
  }

  protected static void showActions(final IndexedDIP dip, final UIObject actionsButton) {
    final CalloutPopup actionsPopup = new CalloutPopup();
    actionsPopup.addStyleName("ActionableStyleMenu");

    if (actionsPopup.isShowing()) {
      actionsPopup.hide();
    } else {
      AsyncCallback<Actionable.ActionImpact> callback = new AsyncCallback<Actionable.ActionImpact>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(Actionable.ActionImpact impact) {
          if (!Actionable.ActionImpact.NONE.equals(impact)) {
            // update
          }
          actionsPopup.hide();
        }
      };

      actionsPopup.setWidget(new ActionableWidgetBuilder<>(DisseminationActions.get(dip.getPermissions()))
        .withActionCallback(callback).buildListWithObjects(new ActionableObject<>(dip)));
      actionsPopup.showRelativeTo(actionsButton, CalloutPosition.TOP_RIGHT);
    }

  }

}
