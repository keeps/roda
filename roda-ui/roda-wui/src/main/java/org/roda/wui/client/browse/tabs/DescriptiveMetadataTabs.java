package org.roda.wui.client.browse.tabs;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class DescriptiveMetadataTabs extends Tabs {
  public void init(TabContentBuilder htmlTabBuilder, TabContentBuilder xmlTabBuilder) {
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.descriptiveMetadataPreviewView()), htmlTabBuilder);
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.descriptiveMetadataPreviewCode()), xmlTabBuilder);
  }
}
