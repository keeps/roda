package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.jobs.CertificateInfo;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class CertificateDialogs {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static void showCertificateDialog(CertificateInfo certificateInfo, final AsyncCallback<Boolean> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText("Certificates");

    final Button closeButton = new Button(messages.closeButton());
    final FlowPanel layout = new FlowPanel();
    layout.addStyleName("wui-dialog-layout");
    for (CertificateInfo.Certificate certificate : certificateInfo.getCertificates()) {
      HTML issuer = new HTML(HtmlSnippetUtils
        .getCertificateInfoHTML("Issuer: " + certificate.getOrganizationName(certificate.getIssuerDN())));
      issuer.addStyleName("certificatesHTML");
      layout.add(issuer);
      HTML subject = new HTML(HtmlSnippetUtils
        .getCertificateInfoHTML("Subject: " + certificate.getOrganizationName(certificate.getSubjectDN())));
      subject.addStyleName("certificatesHTML");
      layout.add(subject);
    }
    layout.add(closeButton);

    dialogBox.addStyleName("wui-dialog-information");
    dialogBox.setWidget(layout);
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    closeButton.addClickHandler(clickEvent -> {
      dialogBox.hide();
      callback.onSuccess(null);
    });

    dialogBox.center();
    dialogBox.show();
  }

}
