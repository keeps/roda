/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.util.List;

import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.common.Dialogs;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class Representations extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        final String aipId = historyTokens.get(0);

        BrowserService.Util.getInstance().getItemBundle(aipId, LocaleInfo.getCurrentLocale().getLocaleName(),
          new AsyncCallback<BrowseItemBundle>() {

          @Override
          public void onFailure(Throwable caught) {
            Tools.newHistory(Browse.RESOLVER);
            callback.onSuccess(null);
          }

          @Override
          public void onSuccess(BrowseItemBundle itemBundle) {
            Representations representations = new Representations(itemBundle);
            callback.onSuccess(representations);
          }
        });
      } else {
        Tools.newHistory(Browse.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {Representations.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(Browse.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "representations";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, Representations> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static ClientMessages messages = GWT.create(ClientMessages.class);

  private BrowseItemBundle itemBundle;

  @UiField
  FlowPanel representationsPanel;

  public Representations(BrowseItemBundle itemBundle) {
    this.itemBundle = itemBundle;

    initWidget(uiBinder.createAndBindUi(this));

    createRepresentationsPanel();
  }

  private void createRepresentationsPanel() {
    List<IndexedRepresentation> representations = itemBundle.getRepresentations();

    for (IndexedRepresentation rep : representations) {
      representationsPanel.add(new RepresentationPanel(rep));
    }
  }

  @UiHandler("buttonAdd")
  void buttonAddHandler(ClickEvent e) {
    representationsPanel.add(new RepresentationPanel(new IndexedRepresentation()));
  }

  @UiHandler("buttonClose")
  void buttonCancelHandler(ClickEvent e) {
    close();
  }

  public void close() {
    Tools.newHistory(Browse.RESOLVER, itemBundle.getAip().getId());
  }

  public class RepresentationPanel extends Composite {
    private FlowPanel panel;
    private FlowPanel panelBody;
    private FlowPanel representationPanel;
    private SimplePanel representationIconPanel;
    private VerticalPanel representationDataPanel;
    private HTMLPanel representationIcon;
    private Label representationType;
    private Label representationInformation;
    private Label representationId;
    private FlowPanel rightPanel;
    private Button viewButton;
    private Button removeButton;

    private IndexedRepresentation representation;

    public RepresentationPanel(IndexedRepresentation representation) {
      this.representation = representation;

      panel = new FlowPanel();
      panelBody = new FlowPanel();
      rightPanel = new FlowPanel();
      
      representationPanel = new FlowPanel();
      representationIconPanel = new SimplePanel();
      representationDataPanel = new VerticalPanel();

      representationIcon = representationIcon(representation.getType());
      representationType = new Label(representation.getType());
      representationInformation = new Label("Has " + representation.getNumberOfDataFiles() + " files, "
        + Humanize.readableFileSize(representation.getSizeInBytes()) + ", originally submitted representation");
      representationId = new Label(representation.getUUID());

      representationIconPanel.setWidget(representationIcon);
      representationDataPanel.add(representationType);
      representationDataPanel.add(representationInformation);
      representationDataPanel.add(representationId);

      representationPanel.add(representationIconPanel);
      representationPanel.add(representationDataPanel);

      viewButton = new Button("VER");
      removeButton = new Button(messages.removeButton());

      rightPanel.add(viewButton);
      rightPanel.add(removeButton);
      
      panelBody.add(rightPanel);   
      panelBody.add(representationPanel);
      panel.add(panelBody);
      
      initWidget(panel);

      panel.addStyleName("panel representation");
      panelBody.addStyleName("panel-body");
      rightPanel.addStyleName("pull-right");
      removeButton.addStyleName("btn btn-danger btn-ban");
      viewButton.addStyleName("btn btn-view");    
      representationPanel.addStyleName("representationPanel");
      representationIconPanel.addStyleName("representationIconPanel");
      representationDataPanel.addStyleName("representationDataPanel");
      representationIcon.addStyleName("representationIcon");
      representationType.addStyleName("representationType");
      representationInformation.addStyleName("representationInformation");
      representationId.addStyleName("representationId");

      removeButton.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          Dialogs.showConfirmDialog(messages.viewRepresentationRemoveFileTitle(),
            messages.viewRepresentationRemoveFileMessage(), messages.dialogCancel(), messages.dialogYes(),
            new AsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                RepresentationPanel.this.removeFromParent();
              }
            }

            @Override
            public void onFailure(Throwable caught) {
              // nothing to do
            }
          });
        }
      });

      ClickHandler clickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          Tools.newHistory(ViewRepresentation.RESOLVER, RepresentationPanel.this.representation.getAipId(),
            RepresentationPanel.this.representation.getUUID());
        }
      };
      
      representationType.addClickHandler(clickHandler);
      viewButton.addClickHandler(clickHandler);
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
  }
}
