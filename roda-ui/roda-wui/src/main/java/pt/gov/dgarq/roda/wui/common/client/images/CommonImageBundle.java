/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client.images;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

/**
 * @author Luis Faria
 * 
 */
@SuppressWarnings("deprecation")
public interface CommonImageBundle extends ImageBundle {

  public AbstractImagePrototype forwardLight();

  public AbstractImagePrototype crossLight();

  @Resource("error_48x48.png")
  public AbstractImagePrototype bigRedCross();

  public AbstractImagePrototype report();

  public AbstractImagePrototype info();

  public AbstractImagePrototype plus();

  public AbstractImagePrototype plusLight();

  public AbstractImagePrototype minus();

  public AbstractImagePrototype minusLight();

  public AbstractImagePrototype listSortDirection();

  public AbstractImagePrototype listAscending();

  public AbstractImagePrototype listDescending();

  public AbstractImagePrototype printPDF();

  public AbstractImagePrototype printCSV();

  public AbstractImagePrototype chart();

  public AbstractImagePrototype date_edit();

  public AbstractImagePrototype login();

  public AbstractImagePrototype user();
}
