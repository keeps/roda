package org.roda.wui.client.browse;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.Pair;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.planning.RepresentationInformationAssociations;
import org.roda.wui.client.planning.ShowRepresentationInformation;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;

public class RepresentationInformationHelper {

  private RepresentationInformationHelper() {
    // do nothing
  }

  public static void addFieldWithRepresentationInformationIcon(SafeHtml message, String filter,
    final FlowPanel fieldPanel, boolean createIcon) {
    addFieldWithRepresentationInformationIcon(message, filter, fieldPanel, createIcon, "");
  }

  public static void addFieldWithRepresentationInformationIcon(SafeHtml message, String filter,
    final FlowPanel fieldPanel, boolean createIcon, String iconCssClass) {
    InlineHTML idHtml = new InlineHTML();
    idHtml.setHTML(message);
    fieldPanel.add(idHtml);

    if (createIcon) {
      final Anchor icon = new Anchor();
      icon.setHTML(
        SafeHtmlUtils.fromSafeConstant("<i class='fa fa-info-circle " + iconCssClass + "' aria-hidden='true'></i>"));
      icon.addStyleName("icon-left-padding");

      BrowserService.Util.getInstance().retrieveRepresentationInformationWithFilter(filter,
        new AsyncCallback<Pair<String, Integer>>() {

          @Override
          public void onFailure(Throwable caught) {
            AsyncCallbackUtils.defaultFailureTreatment(caught);
          }

          @Override
          public void onSuccess(Pair<String, Integer> pair) {
            LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
            selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
            icon.removeStyleName("browseIconRed");

            if (pair.getSecond() == 1) {
              icon.setHref(HistoryUtils.createHistoryHashLink(ShowRepresentationInformation.RESOLVER, pair.getFirst()));
            } else if (pair.getSecond() > 1) {
              icon.setHref(HistoryUtils.createHistoryHashLink(RepresentationInformationAssociations.RESOLVER,
                RodaConstants.REPRESENTATION_INFORMATION_FILTERS, filter));
            } else {
              icon.addStyleName("browseIconRed");
              icon.setHref(HistoryUtils.createHistoryHashLink(RepresentationInformationAssociations.RESOLVER,
                RodaConstants.REPRESENTATION_INFORMATION_FILTERS, filter));
            }
          }
        });

      fieldPanel.add(icon);
    }
  }
}
