/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client.widgets;

import java.util.List;
import java.util.Vector;

import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.SourcesSuccessEvents;
import pt.gov.dgarq.roda.wui.common.client.SuccessListener;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class WUIWindow implements SourcesSuccessEvents {

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private final MyLightBox lightbox;

	private final PopupPanel popup;

	private final Grid shadowedDock;

	private final DockPanel dock;

	private final Label title;

	private boolean tabmode;

	private final TabPanel tabPanel;

	private Widget widget;

	private final HorizontalPanel bottom;

	private final List<SuccessListener> successListeners;

	private int maxWidth;

	private int maxHeight;

	/**
	 * Create a new WUI Window
	 * 
	 * @param maxWidth
	 * @param maxHeight
	 */
	public WUIWindow(int maxWidth, int maxHeight) {
		this.dock = new DockPanel();
		this.shadowedDock = new Grid(3, 3);
		this.popup = new PopupPanel(false, true);
		this.lightbox = new MyLightBox(popup);
		this.shadowedDock.setWidget(1, 1, dock);
		this.popup.setWidget(shadowedDock);

		this.title = new Label();
		this.tabmode = true;
		this.tabPanel = new TabPanel();
		this.widget = null;
		this.bottom = new HorizontalPanel();

		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;

		this.dock.add(title, DockPanel.NORTH);
		this.dock.add(tabPanel, DockPanel.CENTER);
		this.dock.add(bottom, DockPanel.SOUTH);

		shadowedDock.addStyleName("office-window-shadow");
		shadowedDock.getCellFormatter().addStyleName(0, 0, "shadow-up-left");
		shadowedDock.getCellFormatter().addStyleName(0, 1, "shadow-up");
		shadowedDock.getCellFormatter().addStyleName(0, 2, "shadow-up-right");
		shadowedDock.getCellFormatter().addStyleName(1, 0, "shadow-left");
		shadowedDock.getCellFormatter().addStyleName(1, 2, "shadow-right");
		shadowedDock.getCellFormatter()
				.addStyleName(2, 0, "shadow-bottom-left");
		shadowedDock.getCellFormatter().addStyleName(2, 1, "shadow-bottom");
		shadowedDock.getCellFormatter().addStyleName(2, 2,
				"shadow-bottom-right");
		shadowedDock.setBorderWidth(0);
		shadowedDock.setCellPadding(0);
		shadowedDock.setCellSpacing(0);

		this.dock.addStyleName("office-window-dock");
		this.title.addStyleName("office-window-title");
		this.bottom.addStyleName("office-window-bottom");
		this.dock.setCellHorizontalAlignment(bottom, DockPanel.ALIGN_RIGHT);

		this.successListeners = new Vector<SuccessListener>();

		Window.addWindowResizeListener(new WindowResizeListener() {

			public void onWindowResized(int width, int height) {
				updatePopupSize(width, height);
				lightbox.center();
			}

		});
	}

	protected void updatePopupSize() {
		int clientWidth = Window.getClientWidth();
		int clientHeight = Window.getClientHeight();
		updatePopupSize(clientWidth, clientHeight);
	}

	protected void updatePopupSize(int clientWidth, int clientHeight) {
		int newDeckWidth = Math.min(clientWidth, maxWidth);
		int newDeckHeight = Math.min(clientHeight, maxHeight);

		logger.debug("Resizing OfficeWindow Deck [" + maxWidth + ", "
				+ maxHeight + "] > [" + newDeckWidth + ", " + newDeckHeight
				+ "]");

		if (tabmode) {
			tabPanel.getDeckPanel().setSize(newDeckWidth + "px",
					newDeckHeight + "px");
		} else if (widget != null) {
			widget.setSize(newDeckWidth + "px", newDeckHeight + "px");
		}

	}

	/**
	 * Create a new WUI window
	 * 
	 * @param text
	 * @param maxWidth
	 * @param maxHeigth
	 */
	public WUIWindow(String text, int maxWidth, int maxHeigth) {
		this(maxWidth, maxHeigth);
		this.setTitle(text);
	}

	/**
	 * Set window title
	 * 
	 * @param text
	 */
	public void setTitle(String text) {
		this.title.setText(text);
	}

	/**
	 * Add widget to window bottom
	 * 
	 * @param widget
	 */
	public void addToBottom(Widget widget) {
		this.bottom.add(widget);
	}

	/**
	 * Remove widget to window bottom
	 * 
	 * @param widget
	 * @return
	 */
	public boolean removeFromBottom(Widget widget) {
		return this.bottom.remove(widget);
	}

	/**
	 * Add tab to window
	 * 
	 * @param widget
	 * @param tabText
	 */
	public void addTab(Widget widget, String tabText) {
		ScrollPanel scroll = new ScrollPanel(widget);
		scroll.addStyleName("tabScroll");
		tabPanel.add(scroll, tabText);
		if (!tabmode && this.widget != null) {
			dock.remove(this.widget);
			dock.add(tabPanel, DockPanel.CENTER);
			tabmode = true;
		}
	}

	/**
	 * Set window widget, removing tab panel
	 * 
	 * @param widget
	 */
	public void setWidget(Widget widget) {
		if (tabmode) {
			dock.remove(tabPanel);
			tabmode = false;
		} else if (this.widget != null) {
			dock.remove(this.widget);
		}

		this.widget = widget;
		dock.add(widget, DockPanel.CENTER);
	}

	/**
	 * Remove a tab
	 * @param index
	 */
	public void removeTab(int index) {
		tabPanel.remove(index);
	}

	/**
	 * Select a tab
	 * @param index
	 */
	public void selectTab(int index) {
		tabPanel.selectTab(index);
	}

	/**
	 * Show window
	 */
	public void show() {
		updatePopupSize();
		lightbox.show();
	}

	/**
	 * Hide window
	 */
	public void hide() {
		lightbox.hide();
	}

	public void addSuccessListener(SuccessListener listener) {
		this.successListeners.add(listener);
	}

	public void removeSuccessListener(SuccessListener listener) {
		this.successListeners.remove(listener);
	}

	protected void onSuccess() {
		for(SuccessListener listener: successListeners) {
			listener.onSuccess();
		}
	}

	protected void onCancel() {
		for(SuccessListener listener: successListeners) {
			listener.onCancel();
		}
	}

	/**
	 * Set window popup position
	 * @param left
	 * @param top
	 */
	public void setPopupPosition(int left, int top) {
		popup.setPopupPosition(left, top);
	}

	/**
	 * Get window tab panel
	 * @return
	 */
	public TabPanel getTabPanel() {
		return tabPanel;
	}

}
