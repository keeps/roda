/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.tabs;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class DescriptiveMetadataTabs extends Tabs {
  public void init(TabContentBuilder htmlTabBuilder, TabContentBuilder xmlTabBuilder) {

    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.descriptiveMetadataPreviewView()), htmlTabBuilder);

    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.descriptiveMetadataPreviewCode()), xmlTabBuilder);
  }

  public void init(TabContentBuilder xmlTabBuilder) {
    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.descriptiveMetadataPreviewCode()), xmlTabBuilder);
  }
}
