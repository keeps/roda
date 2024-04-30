/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.planning.RepresentationInformationAssociations;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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

      Services services = new Services("Count representation information", "count");
      CountRequest request = new CountRequest(RepresentationInformation.class.getName(),
        new Filter(new SimpleFilterParameter(RodaConstants.REPRESENTATION_INFORMATION_FILTERS, filter)), true);
      services.representationInformationResource(s -> s.count(request)).whenComplete((longResponse, throwable) -> {
        if (throwable == null) {
          LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
          selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
          icon.removeStyleName("representationInformationMissing");

          if (StringUtils.isBlank(iconCssClass)) {
            icon.addStyleName("representationInformationPresent");
          }

          if (longResponse.getResult() > 0) {
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
