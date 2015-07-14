/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import org.roda.legacy.aip.metadata.descriptive.SimpleDescriptionObject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.BrowseConstants;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIWindow;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.ViewPanel.ViewListener;

/**
 * @author Luis Faria
 * 
 */
public class ViewWindow extends WUIWindow {

	private static BrowseConstants constants = (BrowseConstants) GWT.create(BrowseConstants.class);

	private static ClientLogger logger = new ClientLogger(ViewWindow.class.getName());

	private final ViewPanel viewPanel;

	public ViewWindow(String pid) {
		this(pid, new AsyncCallback<DescriptionObject>() {

			public void onFailure(Throwable caught) {
				if (caught instanceof NoSuchRODAObjectException) {
					Window.alert(constants.viewWindowObjectNoLongerExists());
				} else {
					logger.error("Error creating view window", caught);
				}

			}

			public void onSuccess(DescriptionObject result) {
				// nothing to do

			}

		});
	}

	/**
	 * Create a new view window
	 * 
	 * @param pid
	 * @param callback
	 */
	public ViewWindow(String pid, final AsyncCallback<DescriptionObject> callback) {
		super(constants.viewWindowLoading(), 850, 400);
		viewPanel = new ViewPanel(pid, new AsyncCallback<DescriptionObject>() {

			public void onFailure(Throwable caught) {
				hide();
				callback.onFailure(caught);
			}

			public void onSuccess(DescriptionObject obj) {
				setWidget(viewPanel);
				init();
				callback.onSuccess(obj);
			}

		});

	}

	/**
	 * Create a new view window
	 * 
	 * @param viewPanel
	 */
	public ViewWindow(ViewPanel viewPanel) {
		super(constants.viewWindowLoading(), 850, 400);
		this.viewPanel = viewPanel;
		this.setWidget(viewPanel);
		init();
	}

	private void init() {
		viewPanel.addViewListener(new ViewListener() {

			public void onClose(String thisPid) {
				hide();
			}

			public void onEdit(String thisPid) {
				// nothing to do

			}

			public void onRemove(String thisPid, String parentPid) {
				hide();

			}

			public void onSave(DescriptionObject obj) {
				// TODO fix this
				// setTitle(obj);

			}

			public void onCancel(String thisPid) {
				hide();

			}

			public void onClone(String thisPid, String clonePid) {
				// nothing to do

			}

			public void onCreateChild(String thisPid, String childPid) {
				// nothing to do

			}

			public void onMove(String thisPid, String oldParentPid, String newParentPid) {
				// nothing to do

			}

		});

		viewPanel.getSDO(new AsyncCallback<SimpleDescriptionObject>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting SDO of ", caught);
			}

			public void onSuccess(SimpleDescriptionObject sdo) {
				ViewWindow.this.setTitle(sdo);
			}

		});

		viewPanel.addStyleName("wui-view-window");
	}

	/**
	 * Add view listener to inner view panel
	 * 
	 * @param listener
	 */
	public void addViewListener(ViewListener listener) {
		viewPanel.addViewListener(listener);
	}

	/**
	 * Remove view listener from inner view panel
	 * 
	 * @param listener
	 */
	public void removeViewListener(ViewListener listener) {
		viewPanel.removeViewListener(listener);
	}

	/**
	 * Get PID
	 * 
	 * @return the PID
	 */
	public String getPID() {
		return viewPanel.getPID();
	}

	/**
	 * Update window title with a simple description object
	 * 
	 * @param sdo
	 */
	public void setTitle(SimpleDescriptionObject sdo) {
		setTitle(sdo.getId());
	}

}
