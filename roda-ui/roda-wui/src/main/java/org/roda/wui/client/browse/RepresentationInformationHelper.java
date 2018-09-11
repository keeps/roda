/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.Pair;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.planning.RepresentationInformationAssociations;
import org.roda.wui.client.planning.ShowRepresentationInformation;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.StringUtils;

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
    final FlowPanel fieldPanel, boolean createIcon, final String iconCssClass) {
    InlineHTML idHtml = new InlineHTML();
    idHtml.setHTML(message);
    fieldPanel.add(idHtml);

    if (createIcon) {
      final Anchor icon = new Anchor();
      icon.setHTML(
        SafeHtmlUtils.fromSafeConstant("<i class='fa fa-info-circle " + iconCssClass + "' aria-hidden='true'></i>"));
      icon.addStyleName("icon-left-padding");

      if (StringUtils.isBlank(iconCssClass)) {
        icon.addStyleName("representationInformationPresent");
      }

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
            icon.removeStyleName("representationInformationMissing");

            if (StringUtils.isBlank(iconCssClass)) {
              icon.addStyleName("representationInformationPresent");
            }

            if (pair.getSecond() == 1) {
              icon.setHref(HistoryUtils.createHistoryHashLink(ShowRepresentationInformation.RESOLVER, pair.getFirst()));
            } else if (pair.getSecond() > 1) {
              icon.setHref(HistoryUtils.createHistoryHashLink(RepresentationInformationAssociations.RESOLVER,
                RodaConstants.REPRESENTATION_INFORMATION_FILTERS, filter));
            } else {
              icon.addStyleName("representationInformationMissing");
              icon.removeStyleName("representationInformationPresent");
              icon.setHref(HistoryUtils.createHistoryHashLink(RepresentationInformationAssociations.RESOLVER,
                RodaConstants.REPRESENTATION_INFORMATION_FILTERS, filter));
            }
          }
        });

      fieldPanel.add(icon);
    }
  }
}
