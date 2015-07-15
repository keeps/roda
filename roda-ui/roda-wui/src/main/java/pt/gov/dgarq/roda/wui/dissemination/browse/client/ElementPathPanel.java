/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.BrowseMessages;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.images.BrowseImageBundle;
import pt.gov.dgarq.roda.wui.dissemination.client.Dissemination;

/**
 * @author Luis Faria
 * 
 */
public class ElementPathPanel extends HorizontalPanel {

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private static BrowseMessages messages = (BrowseMessages) GWT.create(BrowseMessages.class);

	private static BrowseImageBundle browseImageBundle = (BrowseImageBundle) GWT.create(BrowseImageBundle.class);

	private final String pid;

	private ViewWindow viewWindow;

	/**
	 * Create a new element path panel
	 * 
	 * @param pid
	 */
	public ElementPathPanel(String pid) {
		this.pid = pid;

		DeferredCommand.addCommand(new Command() {

			public void execute() {

				BrowserService.Util.getInstance().getAncestors(ElementPathPanel.this.pid,
						new AsyncCallback<String[]>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting ancestors of" + ElementPathPanel.this.pid, caught);
					}

					public void onSuccess(String[] ancestors) {
						createPanel(ancestors);

					}

				});
			}

		});

		this.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		this.addStyleName("wui-elementPath");
	}

	protected void addSeparator() {
		Image sep = browseImageBundle.elementPathPanelSeparator().createImage();
		sep.addStyleName("separator");
		add(sep);
		setCellVerticalAlignment(sep, ALIGN_MIDDLE);
	}

	protected void createPanel(String[] ancestors) {
		final SimpleDescriptionObject[] sdos = new SimpleDescriptionObject[ancestors.length];
		for (int i = 0; i < ancestors.length; i++) {
			final String elementPid = ancestors[i];
			final int index = i;
			BrowserService.Util.getInstance().getSimpleDescriptionObject(elementPid,
					new AsyncCallback<SimpleDescriptionObject>() {

						public void onFailure(Throwable caught) {
							logger.error("Error getting SimpleDescriptionObject of" + elementPid, caught);
						}

						public void onSuccess(SimpleDescriptionObject sdo) {
							sdos[index] = sdo;
							boolean last = true;
							for (int i = 0; i < sdos.length; i++) {
								last &= (sdos[i] != null);
							}
							if (last) {
								createPanel(sdos);
							}
						}

					});
		}
	}

	protected void createPanel(final SimpleDescriptionObject[] sdos) {
		for (int i = 0; i < sdos.length; i++) {
			final int index = i;
			final SimpleDescriptionObject sdo = sdos[index];
			HorizontalPanel itemPanel = new HorizontalPanel();
			Image icon = Dissemination.getInstance().getElementLevelIcon(sdo.getLevel());
			Label id = new Label(sdo.getId());

			ClickListener onItemClicked = new ClickListener() {

				public void onClick(Widget sender) {
					onElementClick(sdo);
				}

			};
			icon.addClickListener(onItemClicked);
			id.addClickListener(onItemClicked);

			itemPanel.add(icon);
			itemPanel.add(id);

			if (index > 0) {
				addSeparator();
			}
			add(itemPanel);

			if (index == sdos.length - 1) {
				itemPanel.addStyleName("elementPathItem-final-layout");
			}

			ElementPathPanel.this.setCellVerticalAlignment(itemPanel, ALIGN_MIDDLE);
			itemPanel.setCellVerticalAlignment(icon, HorizontalPanel.ALIGN_MIDDLE);
			itemPanel.setCellVerticalAlignment(id, HorizontalPanel.ALIGN_MIDDLE);
			itemPanel.addStyleName("elementPathItem-layout");
			icon.addStyleName("elementPathItem-icon");
			id.addStyleName("elementPathItem-id");
		}
	}

	protected void onElementClick(SimpleDescriptionObject sdo) {
		viewWindow = new ViewWindow(sdo.getId(), new AsyncCallback<DescriptionObject>() {

			public void onFailure(Throwable caught) {
				if (caught instanceof NoSuchRODAObjectException) {
					Window.alert(messages.noSuchRODAObject(pid));
				} else {
					logger.error("Error creating view window", caught);
				}
			}

			public void onSuccess(DescriptionObject obj) {
				viewWindow.show();
			}

		});

	}

}
