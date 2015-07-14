/**
 * 
 */
package pt.gov.dgarq.roda.wui.ingest.list.client;

import java.util.HashSet;
import java.util.Set;

import org.roda.legacy.aip.metadata.descriptive.SimpleDescriptionObject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.IngestListConstants;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIWindow;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.CollectionsTree;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.CollectionsTreeItem;

/**
 * @author Luis Faria
 * 
 */
public class SelectDescriptionObjectWindow extends WUIWindow {

	public interface SelectDescriptionObjectListener {
		public void onSelect(SimpleDescriptionObject sdo);
	}

	private static IngestListConstants constants = (IngestListConstants) GWT.create(IngestListConstants.class);
	// private ClientLogger logger = new ClientLogger(getClass().getName());

	private final CollectionsTree tree;
	private final WUIButton close;
	private final Set<SelectDescriptionObjectListener> listeners;

	public SelectDescriptionObjectWindow(SimpleDescriptionObject sdo) {
		super(constants.selectDescriptionObjectWindowTitle(), 850, 400);

		this.tree = new CollectionsTree(sdo, null, null, 10, true);
		this.close = new WUIButton(constants.selectDescriptionObjectWindowClose(), WUIButton.Left.ROUND,
				WUIButton.Right.CROSS);
		this.listeners = new HashSet<SelectDescriptionObjectListener>();

		this.setWidget(tree);
		this.addToBottom(close);

		tree.addTreeListener(new TreeListener() {

			public void onTreeItemSelected(TreeItem item) {
				if (item instanceof CollectionsTreeItem) {
					onSelect(((CollectionsTreeItem) item).getSDO());
				}

			}

			public void onTreeItemStateChanged(TreeItem item) {
				// nothing to do

			}

		});

		close.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				onCancel();
				hide();
			}

		});

		tree.getRootItem().setState(true);

		tree.addStyleName("select-do-window-tree");
	}

	public void addSelectDescriptionObjectListener(SelectDescriptionObjectListener listener) {
		listeners.add(listener);
	}

	public void removeSelectDescriptionObjectListener(SelectDescriptionObjectListener listener) {
		listeners.remove(listener);
	}

	private void onSelect(SimpleDescriptionObject sdo) {
		for (SelectDescriptionObjectListener listener : listeners) {
			listener.onSelect(sdo);
		}
		onSuccess();
	}
}
