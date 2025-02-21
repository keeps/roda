package org.roda.wui.client.browse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.wui.client.browse.tabs.Tabs;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class DescriptiveMetadataPanel extends Composite {
    private static final ClientMessages messages = GWT.create(ClientMessages.class);
    private static DescriptiveMetadataPanel.MyUiBinder uiBinder = GWT.create(DescriptiveMetadataPanel.MyUiBinder.class);

    @UiField
    Tabs metadataTabs;

    public DescriptiveMetadataPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }


    interface MyUiBinder extends UiBinder<Widget, DescriptiveMetadataPanel> {
    }
}
