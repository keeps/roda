/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.management.editor.client;

import org.roda.core.data.eadc.EadCValue;

import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 *
 */
public interface MetadataElementEditor extends SourcesChangeEvents {

  public EadCValue getValue();

  public void setValue(EadCValue value);

  public Widget getWidget();

  public boolean isEmpty();

  public boolean isValid();
}
