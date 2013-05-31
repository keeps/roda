/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.editor.client;

import java.util.ArrayList;
import java.util.List;

import pt.gov.dgarq.roda.core.common.IllegalOperationException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.widgets.LoadingPopup;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIWindow;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.BrowserService;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.CollectionsTreeItem;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.CollectionsTreeVerticalScrollPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.BrowseMessages;
import config.i18n.client.MetadataEditorConstants;

/**
 * @author Luis Faria
 * 
 */
public class MoveChooseDestinationPanel extends WUIWindow {
	private static MetadataEditorConstants constants = (MetadataEditorConstants) GWT
			.create(MetadataEditorConstants.class);

	private static BrowseMessages messages = (BrowseMessages) GWT
			.create(BrowseMessages.class);

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private final SimpleDescriptionObject source;

	private final CollectionsTreeVerticalScrollPanel collectionsTree;

	private final WUIButton choose;

	private final WUIButton cancel;

	private final LoadingPopup loading;

	/**
	 * Listener for move events
	 * 
	 */
	public interface MoveListener {
		/**
		 * On object move
		 * 
		 * @param oldParent
		 *            PID of the parent object which the object is moving from
		 * 
		 * @param newParentPid
		 *            PID of the parent object under which the object is moving
		 *            to
		 */
		public void onMove(String oldParent, String newParentPid);

		/**
		 * On move cancellation
		 */
		public void onCancel();
	}

	private List<MoveListener> listeners;

	/**
	 * Create a new panel to choose the destination where to move the object
	 * 
	 * @param sdo
	 *            the simple description object to move
	 */
	public MoveChooseDestinationPanel(SimpleDescriptionObject sdo) {
		super(650, 550);
		this.source = sdo;
		setTitle(constants.moveChooseDestinationTitle());
		collectionsTree = new CollectionsTreeVerticalScrollPanel(false);
		setWidget(collectionsTree);

		collectionsTree.setSelected(source.getPid());

		listeners = new ArrayList<MoveListener>();

		choose = new WUIButton(constants.moveChooseDestinationChoose(),
				WUIButton.Left.ROUND,
				WUIButton.Right.ARROW_FORWARD);

		choose.setEnabled(false);

		choose.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				final CollectionsTreeItem selected = collectionsTree
						.getSelected();
				loading.show();
				moveTo(selected.getSDO(),
						new AsyncCallback<CollectionsTreeItem>() {

							public void onFailure(Throwable caught) {
								if (caught instanceof IllegalOperationException) {
									Window.alert(messages
											.moveIllegalOperation(caught
													.getMessage()));
								} else if (caught instanceof NoSuchRODAObjectException) {
									Window.alert(messages
											.moveNoSuchObject(caught
													.getMessage()));
								} else {
									logger.error("Error while moving "
											+ source.getPid() + " to "
											+ selected.getPid());
								}
								loading.hide();
								hide();
							}

							public void onSuccess(CollectionsTreeItem treeItem) {
								loading.hide();
								hide();
							}

						});

			}

		});

		cancel = new WUIButton(constants.moveChooseDestinationCancel(),
				WUIButton.Left.ROUND,
				WUIButton.Right.CROSS);

		cancel.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				hide();
				onCancel();
			}

		});

		addToBottom(choose);
		addToBottom(cancel);

		collectionsTree.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				final CollectionsTreeItem selected = collectionsTree
						.getSelected();
				choose.setEnabled(isMoveValid(source, selected.getSDO()));

			}

		});

		loading = new LoadingPopup(collectionsTree);

		collectionsTree.addStyleName("wui-edit-move-collections");

	}

	protected boolean isMoveValid(SimpleDescriptionObject source,
			SimpleDescriptionObject target) {
		return source.getLevel().compareTo(target.getLevel()) > 0;
	}

	protected void moveTo(final SimpleDescriptionObject newParent,
			final AsyncCallback<CollectionsTreeItem> callback) {
		BrowserService.Util.getInstance().getParent(source.getPid(),
				new AsyncCallback<String>() {

					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}

					public void onSuccess(final String oldParentPID) {
						EditorService.Util.getInstance().moveElement(
								source.getPid(), newParent.getPid(),
								new AsyncCallback() {

									public void onFailure(Throwable caught) {
										callback.onFailure(caught);
									}

									public void onSuccess(Object result) {
										onMove(oldParentPID, newParent.getPid());
										callback.onSuccess(null);
									}

								});

					}

				});

	}

	/**
	 * Add move listener
	 * 
	 * @param listener
	 */
	public void addMoveListener(MoveListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove move listener
	 * 
	 * @param listener
	 */
	public void removeMoveListener(MoveListener listener) {
		listeners.remove(listener);
	}

	protected void onMove(String oldParent, String newParent) {
		for (MoveListener listener : listeners) {
			listener.onMove(oldParent, newParent);
		}
	}

	protected void onCancel() {
		for (MoveListener listener : listeners) {
			listener.onCancel();
		}
	}
}
