package org.roda.wui.client.browse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.RestErrorOverlayType;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class RepresentationPanel extends Composite {

  interface MyUiBinder extends UiBinder<Widget, RepresentationPanel> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FocusPanel representationPanelFocus;

  @UiField
  SimplePanel representationIconPanel;

  @UiField
  Label representationType;

  @UiField
  Label representationInformation;

  @UiField
  Label representationId;

  @UiField
  FlowPanel representationMetadata;

  @UiField
  TabPanel itemMetadata;

  @UiField
  Button newDescriptiveMetadata;

  private HTMLPanel representationIcon;

  private List<HandlerRegistration> handlers;
  private String aipId;
  private String repId;
  private List<DescriptiveMetadataViewBundle> representationDescriptiveMetadata;

  public RepresentationPanel(final String aipId, final IndexedRepresentation representation,
    List<DescriptiveMetadataViewBundle> representationDescriptiveMetadata) {
    this.aipId = aipId;
    this.repId = representation.getUUID();
    this.representationDescriptiveMetadata = representationDescriptiveMetadata;

    handlers = new ArrayList<HandlerRegistration>();

    initWidget(uiBinder.createAndBindUi(this));

    representationType.setText(representation.getType());
    representationInformation.setText("Has " + representation.getNumberOfDataFiles() + " files, "
      + Humanize.readableFileSize(representation.getSizeInBytes()) + ", originally submitted representation");
    representationId.setText(representation.getUUID());

    representationIcon = representationIcon(representation.getType());

    representationIconPanel.setWidget(representationIcon);
    representationIcon.addStyleName("representationIcon");

    representationMetadata.setVisible(false);
    itemMetadata.setVisible(false);
    newDescriptiveMetadata.setVisible(false);

    representationPanelFocus.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        Tools.newHistory(ViewRepresentation.RESOLVER, RepresentationPanel.this.aipId, RepresentationPanel.this.repId);
      }
    });

    // removeButton.addClickHandler(new ClickHandler() {
    //
    // @Override
    // public void onClick(ClickEvent event) {
    // Dialogs.showConfirmDialog(messages.viewRepresentationRemoveFileTitle(),
    // messages.viewRepresentationRemoveFileMessage(), messages.dialogCancel(),
    // messages.dialogYes(),
    // new AsyncCallback<Boolean>() {
    //
    // @Override
    // public void onSuccess(Boolean confirmed) {
    // if (confirmed) {
    // RepresentationPanel.this.removeFromParent();
    // }
    // }
    //
    // @Override
    // public void onFailure(Throwable caught) {
    // // nothing to do
    // }
    // });
    // }
    // });

    final List<Pair<String, HTML>> descriptiveMetadataContainers = new ArrayList<Pair<String, HTML>>();
    final Map<String, DescriptiveMetadataViewBundle> bundles = new HashMap<>();
    for (DescriptiveMetadataViewBundle bundle : representationDescriptiveMetadata) {
      String title = bundle.getLabel() != null ? bundle.getLabel() : bundle.getId();
      HTML container = new HTML();
      container.addStyleName("metadataContent");
      itemMetadata.add(container, title);
      descriptiveMetadataContainers.add(Pair.create(bundle.getId(), container));
      bundles.put(bundle.getId(), bundle);
    }

    HandlerRegistration tabHandler = itemMetadata.addSelectionHandler(new SelectionHandler<Integer>() {

      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        if (event.getSelectedItem() < descriptiveMetadataContainers.size()) {
          Pair<String, HTML> pair = descriptiveMetadataContainers.get(event.getSelectedItem());
          String descId = pair.getFirst();
          final HTML html = pair.getSecond();
          final DescriptiveMetadataViewBundle bundle = bundles.get(descId);
          if (html.getText().length() == 0) {
            getDescriptiveMetadataHTML(descId, bundle, new AsyncCallback<SafeHtml>() {

              @Override
              public void onFailure(Throwable caught) {
                if (!AsyncCallbackUtils.treatCommonFailures(caught)) {
                  Toast.showError(messages.errorLoadingDescriptiveMetadata(caught.getMessage()));
                }
              }

              @Override
              public void onSuccess(SafeHtml result) {
                html.setHTML(result);
              }
            });
          }
        }
      }
    });

    final int addTabIndex = itemMetadata.getWidgetCount();
    FlowPanel addTab = new FlowPanel();
    addTab.add(new HTML(SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-plus-circle\"></i>")));
    itemMetadata.add(new Label(), addTab);
    HandlerRegistration addTabHandler = itemMetadata.addSelectionHandler(new SelectionHandler<Integer>() {
      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        if (event.getSelectedItem() == addTabIndex) {
          Tools.newHistory(CreateDescriptiveMetadata.RESOLVER, "representation", RepresentationPanel.this.aipId,
            RepresentationPanel.this.repId);
        }
      }
    });
    addTab.addStyleName("addTab");
    addTab.getParent().addStyleName("addTabWrapper");

    handlers.add(tabHandler);
    handlers.add(addTabHandler);

    if (!representationDescriptiveMetadata.isEmpty()) {
      itemMetadata.selectTab(0);
    }
    // if (!representationDescriptiveMetadata.isEmpty()) {
    // itemMetadata.setVisible(true);
    // itemMetadata.selectTab(0);
    // } else {
    // newDescriptiveMetadata.setVisible(true);
    // }
  }

  private HTMLPanel representationIcon(String type) {
    StringBuilder b = new StringBuilder();

    b.append("<i class='");
    if (type.equals("MIXED")) {
      b.append("fa fa-files-o");
    } else {
      b.append("fa fa-file");
    }
    b.append("'>");
    b.append("</i>");

    return new HTMLPanel(SafeHtmlUtils.fromSafeConstant(b.toString()));
  }

  private void getDescriptiveMetadataHTML(final String descId, final DescriptiveMetadataViewBundle bundle,
    final AsyncCallback<SafeHtml> callback) {
    SafeUri uri = RestUtils.createRepresentationDescriptiveMetadataHTMLUri(repId, descId);
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uri.asString());
    requestBuilder.setHeader("Authorization", "Custom");
    try {
      requestBuilder.sendRequest(null, new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (200 == response.getStatusCode()) {
            String html = response.getText();

            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataLinks'>"));

            if (bundle.hasHistory()) {
              // History link
              String historyLink = Tools.createHistoryHashLink(DescriptiveMetadataHistory.RESOLVER, aipId, repId,
                descId);
              String historyLinkHtml = "<a href='" + historyLink
                + "' class='toolbarLink'><i class='fa fa-history'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(historyLinkHtml));
            }
            // Edit link
            String editLink = Tools.createHistoryHashLink(EditDescriptiveMetadata.RESOLVER, aipId, repId, descId);
            String editLinkHtml = "<a href='" + editLink + "' class='toolbarLink'><i class='fa fa-edit'></i></a>";
            b.append(SafeHtmlUtils.fromSafeConstant(editLinkHtml));

            // Download link
            SafeUri downloadUri = RestUtils.createRepresentationDescriptiveMetadataDownloadUri(repId, descId);
            String downloadLinkHtml = "<a href='" + downloadUri.asString()
              + "' class='toolbarLink'><i class='fa fa-download'></i></a>";
            b.append(SafeHtmlUtils.fromSafeConstant(downloadLinkHtml));

            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));

            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataHTML'>"));
            b.append(SafeHtmlUtils.fromTrustedString(html));
            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
            SafeHtml safeHtml = b.toSafeHtml();

            callback.onSuccess(safeHtml);
          } else {
            String text = response.getText();
            String message;
            try {
              RestErrorOverlayType error = (RestErrorOverlayType) JsonUtils.safeEval(text);
              message = error.getMessage();
            } catch (IllegalArgumentException e) {
              message = text;
            }

            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataLinks'>"));

            if (bundle.hasHistory()) {
              // History link
              String historyLink = Tools.createHistoryHashLink(DescriptiveMetadataHistory.RESOLVER, aipId, repId,
                descId);
              String historyLinkHtml = "<a href='" + historyLink
                + "' class='toolbarLink'><i class='fa fa-history'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(historyLinkHtml));
            }

            // Edit link
            String editLink = Tools.createHistoryHashLink(EditDescriptiveMetadata.RESOLVER, aipId, repId, descId);
            String editLinkHtml = "<a href='" + editLink + "' class='toolbarLink'><i class='fa fa-edit'></i></a>";
            b.append(SafeHtmlUtils.fromSafeConstant(editLinkHtml));

            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));

            // error message
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='error'>"));
            b.append(messages.descriptiveMetadataTranformToHTMLError());
            b.append(SafeHtmlUtils.fromSafeConstant("<pre><code>"));
            b.append(SafeHtmlUtils.fromString(message));
            b.append(SafeHtmlUtils.fromSafeConstant("</core></pre>"));
            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));

            callback.onSuccess(b.toSafeHtml());
          }
        }

        @Override
        public void onError(Request request, Throwable exception) {
          callback.onFailure(exception);
        }
      });
    } catch (RequestException e) {
      callback.onFailure(e);
    }
  }

  @UiHandler("newDescriptiveMetadata")
  void buttonNewDescriptiveMetadataEventsHandler(ClickEvent e) {
    Tools.newHistory(CreateDescriptiveMetadata.RESOLVER, "representation", aipId, repId);
  }

  @UiHandler("downloadRepresentationButton")
  void buttonDownloadRepresentationHandler(ClickEvent e) {

  }

  @UiHandler("removeRepresentationButton")
  void buttonRemoveRepresentationHandler(ClickEvent e) {
  }

  @UiHandler("infoRepresentationButton")
  void buttonInfoRepresentationHandler(ClickEvent e) {
    representationMetadata.setVisible(!representationMetadata.isVisible());
    representationMetadata.isVisible();
    if (!representationDescriptiveMetadata.isEmpty()) {
      itemMetadata.setVisible(true);
    } else {
      newDescriptiveMetadata.setVisible(true);
    }
  }
}
