/**
 *
 */
package pt.gov.dgarq.roda.wui.main.client;

import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.MainConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.BrowserService;

/**
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 *
 */
public class DipDialog extends WUIWindow {

    private ClientLogger logger = new ClientLogger(getClass().getName());
    private static MainConstants constants = (MainConstants) GWT.create(MainConstants.class);
    private final VerticalPanel layout;
    private final VerticalPanel itemsLayout;
    private final WUIButton createDip;
    private final WUIButton cancel;
    private final ScrollPanel scrlPanel;
    private final List<String> selectedPids = new ArrayList<String>();

    public DipDialog() throws Exception {
        super("Creation of DIP ", 340, 300);
        scrlPanel = new ScrollPanel();
        layout = new VerticalPanel();
        itemsLayout = new VerticalPanel();
        scrlPanel.add(itemsLayout);
        layout.add(scrlPanel);
        createDip = new WUIButton("Create DIP", WUIButton.Left.ROUND, WUIButton.Right.ARROW_FORWARD);
        cancel = new WUIButton(constants.loginDialogCancel(), WUIButton.Left.ROUND, WUIButton.Right.CROSS);

        BrowserService.Util.getInstance().getDOPIDs(new AsyncCallback<String[]>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.error("Error on getting all DO PIDS", caught);
            }

            @Override
            public void onSuccess(String[] doPIDs) {
                Arrays.sort(doPIDs);
                for (String doPID : doPIDs) {
                    BrowserService.Util.getInstance().getSimpleDescriptionObject(doPID, new AsyncCallback<SimpleDescriptionObject>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            logger.error("Error on getting  SimpleDescriptionObject", caught);
                        }

                        @Override
                        public void onSuccess(SimpleDescriptionObject result) {
                            if (result.getLevel().equals(DescriptionLevel.ITEM)) {
                                CheckBox cbDO = new CheckBox(result.getPid() + " - " + result.getTitle());
                                cbDO.setFormValue(result.getPid());
                                itemsLayout.add(cbDO);
                                cbDO.addClickHandler(new ClickHandler() {
                                    @Override
                                    public void onClick(ClickEvent event) {
                                        CheckBox cb = (CheckBox) event.getSource();
                                        if (cb.getValue()) {
                                            selectedPids.add(cb.getFormValue());
                                        } else {
                                            selectedPids.remove(cb.getFormValue());
                                        }
                                    }
                                });
                            }
                        }
                    });
                }

                setWidget(layout);
                addToBottom(cancel);
                addToBottom(createDip);

                createDip.addClickListener(new ClickListener() {
                    public void onClick(Widget sender) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < selectedPids.size(); i++) {
                            String pid = selectedPids.get(i);
                            sb.append(pid);
                            if (i < (selectedPids.size() - 1)) {
                                sb.append("|");
                            }
                        }
                        Window.open(GWT.getModuleBaseURL()
                                + "DIPDownloadDEMO?pids=" + sb.toString(),
                                "_blank", "");
                        hide();
                    }
                });


                cancel.addClickListener(new ClickListener() {
                    public void onClick(Widget sender) {
                        hide();
                    }
                });

                layout.addStyleName("wui-login-dialog");
                createDip.addStyleName("login");
                cancel.addStyleName("cancel");
            }
        });
    }

    private native void reload() /*-{
     $wnd.location.reload();
     }-*/;
}
