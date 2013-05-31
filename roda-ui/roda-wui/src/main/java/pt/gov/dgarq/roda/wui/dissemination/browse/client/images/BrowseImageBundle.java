/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client.images;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

/**
 * @author Luis Faria
 * 
 */
public interface BrowseImageBundle extends ImageBundle {

	public AbstractImagePrototype elementPathPanelSeparator();

	@Resource("application_side_tree.png")
	public AbstractImagePrototype browseViewPanel();

	@Resource("application_double.png")
	public AbstractImagePrototype browseViewWindow();

	public AbstractImagePrototype browseCreateFonds();

	@Resource("resultset_next.png")
	public AbstractImagePrototype collectionsTreeNextResults();

	@Resource("resultset_previous.png")
	public AbstractImagePrototype collectionsTreePreviousResults();

	public AbstractImagePrototype disseminationDefaultIcon();
	
	public AbstractImagePrototype refresh();

}
