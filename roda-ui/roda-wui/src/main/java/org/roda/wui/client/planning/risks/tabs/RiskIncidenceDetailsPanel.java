package org.roda.wui.client.planning.risks.tabs;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.aip.MembersLookupRequest;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.planning.RiskRegister;
import org.roda.wui.client.planning.risks.ShowRisk;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

import java.util.Collections;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RiskIncidenceDetailsPanel extends GenericMetadataCardPanel<RiskIncidence> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final String VERIFY_PERMISSIONS = "org.roda.wui.api.controllers.Browser.verifyPermissions";

  public RiskIncidenceDetailsPanel(RiskIncidence incidence) {
    setData(incidence);
  }

  @Override
  protected FlowPanel createHeaderWidget(RiskIncidence incidence) {
    return null;
  }

  @Override
  protected void buildFields(RiskIncidence incidence) {
    // 1. Identifiers
    buildField(messages.riskIncidenceIdentifier()).withValue(incidence.getId()).build();
    buildField(messages.riskIncidenceInstanceIdentifier()).withValue(incidence.getInstanceId()).build();

    // 2. Affected Object Link
    // HtmlSnippetUtils dynamically resolves whether the target is an AIP,
    // Representation, File, etc.
    Label objectLabel = new Label();
    Anchor objectLink = new Anchor();
    HtmlSnippetUtils.addRiskIncidenceObjectLinks(incidence, objectLabel, objectLink);
    if (StringUtils.isNotBlank(objectLabel.getText())) {
      buildField(objectLabel.getText()).withWidget(objectLink).build();
    }

    // 3. Risk ID Link
    String riskId = incidence.getRiskId();
    if (StringUtils.isNotBlank(riskId)) {
      Anchor riskLink = new Anchor(riskId);
      riskLink.setHref(
        HistoryUtils.createHistoryHashLink(RiskRegister.RESOLVER, ShowRisk.RESOLVER.getHistoryToken(), riskId));
      buildField(messages.riskIncidenceRisk()).withWidget(riskLink).build();
    }

    // 4. Description
    // FieldBuilder automatically ignores null or empty strings during .build()
    buildField(messages.riskIncidenceDescription()).withValue(incidence.getDescription()).build();

    // 5. Status & Severity
    if (incidence.getStatus() != null) {
      buildField(messages.riskIncidenceStatus()).withHtml(HtmlSnippetUtils.getStatusDefinition(incidence.getStatus()))
        .build();
    }
    if (incidence.getSeverity() != null) {
      buildField(messages.riskIncidenceSeverity())
        .withHtml(HtmlSnippetUtils.getSeverityDefinition(incidence.getSeverity())).build();
    }

    // 6. Detection Details
    if (incidence.getDetectedOn() != null) {
      String detectedOnFormatted = DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT)
        .format(incidence.getDetectedOn());
      buildField(messages.riskIncidenceDetectedOn()).withValue(detectedOnFormatted).build();
    }
    buildUserField(messages.riskIncidenceDetectedBy(), incidence.getDetectedBy());

    // 7. Mitigation Details
    if (incidence.getMitigatedOn() != null) {
      addSeparator("Mitigation details");

      String mitigatedOnFormatted = DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT)
        .format(incidence.getMitigatedOn());
      buildField(messages.riskIncidenceMitigatedOn()).withValue(mitigatedOnFormatted).build();
    }
    buildUserField(messages.riskIncidenceMitigatedBy(), incidence.getMitigatedBy());
    buildField(messages.riskIncidenceMitigatedDescription()).withValue(incidence.getMitigatedDescription()).build();
  }

  /**
   * Builds a user field that asynchronously fetches and formats the user's full
   * name if the current user has the appropriate permissions.
   */
  private void buildUserField(String label, String username) {
    if (StringUtils.isBlank(username)) {
      return;
    }

    // 1. Create a Label with the username fallback to reserve the DOM order
    Label userLabel = new Label(username);
    buildField(label).withWidget(userLabel).build();

    // 2. Fetch the user asynchronously via Services using CompletableFuture
    Services services = new Services("Retrieve user display name", "get");
    MembersLookupRequest request = new MembersLookupRequest(Collections.singleton(username), Collections.emptySet());

    services.membersResource(s -> s.getMembersDisplayNames(request)).whenComplete((response, error) -> {
      if (error == null && response != null) {
        String displayName = response.getUserDisplayName(username);
        if (StringUtils.isNotBlank(displayName) && !displayName.equals(username)) {
          // 3. Update the label text directly to the resolved display name
          userLabel.setText(displayName);
        }
      }
    });
    // } else {
    // buildField(label).withValue(username).build();
    // }
  }
}