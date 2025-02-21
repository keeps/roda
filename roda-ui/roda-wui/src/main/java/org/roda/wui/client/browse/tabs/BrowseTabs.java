package org.roda.wui.client.browse.tabs;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.wui.client.common.search.SearchWrapper;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class BrowseTabs extends Tabs {
    public void init(IndexedAIP aip, DescriptiveMetadataInfos descriptiveMetadataInfos) {
        if (descriptiveMetadataInfos != null) {
            AIPDescriptiveMetadataTabs aipDescriptiveMetadataTabs = new AIPDescriptiveMetadataTabs();
            aipDescriptiveMetadataTabs.init(aip, descriptiveMetadataInfos);
            aipDescriptiveMetadataTabs.setStyleName("descriptiveMetadataTabs");
            createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.descriptiveMetadataTab()), aipDescriptiveMetadataTabs);
        }

    }
}