/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.editor.client;

import pt.gov.dgarq.roda.core.data.eadc.EadCValue;

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
