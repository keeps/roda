/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import java.util.List;
import java.util.MissingResourceException;

import org.gwtwidgets.client.ui.PNGImage;

import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.AuthenticatedUser;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.images.BrowseImageBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.BrowseConstants;
import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria
 * 
 */
public class RepresentationsPanel extends Composite {

	private static BrowseImageBundle browseImageBundle = (BrowseImageBundle) GWT
			.create(BrowseImageBundle.class);

	private static BrowseConstants constants = (BrowseConstants) GWT
			.create(BrowseConstants.class);

	private static BrowseMessages messages = (BrowseMessages) GWT
			.create(BrowseMessages.class);

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private final DescriptionObject obj;

	private final ScrollPanel scroll;

	private final VerticalPanel layout;

	private final FlowPanel disseminations;

	private final Label title;

	private boolean initialized;

	/**
	 * Create a new representations panel
	 * 
	 * @param obj
	 */
	public RepresentationsPanel(DescriptionObject obj) {
		this.obj = obj;
		disseminations = new FlowPanel();
		layout = new VerticalPanel();
		scroll = new ScrollPanel(layout);

		title = new Label();
		layout.add(title);
		layout.add(disseminations);

		layout.addStyleName("wui-browse-representations");
		scroll.addStyleName("wui-browse-representations-scroll");
		title.addStyleName("wui-browse-representations-title");
		disseminations
				.addStyleName("wui-browse-representations-disseminations");

		initWidget(scroll);

		initialized = false;
	}

	/**
	 * Initialize representations panel
	 */
	public void init() {
		if (!initialized) {
			initialized = true;
			BrowserService.Util.getInstance().getRepresentationsInfo(
					obj.getPid(),
					new AsyncCallback<List<RepresentationInfo>>() {

						public void onFailure(Throwable caught) {
							logger.error("Error getting representations info",
									caught);

						}

						public void onSuccess(
								final List<RepresentationInfo> reps) {
							if (reps.size() > 0) {
								title.setText(messages.representationsTitle(obj
										.getId(), obj.getTitle()));
							} else {
								title.setText(messages
										.noRepresentationsTitle(obj.getId()));
							}
							UserLogin.getInstance().getAuthenticatedUser(
									new AsyncCallback<AuthenticatedUser>() {

										public void onFailure(Throwable caught) {
											logger.error(
													"Error getting auth user",
													caught);

										}

										public void onSuccess(
												AuthenticatedUser user) {
											for (RepresentationInfo rep : reps) {
												addRepresentationPanel(rep,
														user);
											}
										}

									});

						}

					});
		}
	}

	protected void addRepresentationPanel(RepresentationInfo rep,
			AuthenticatedUser user) {

		for (DisseminationInfo dissemination : rep.getDisseminations()) {
			disseminations.add(createDisseminationPanel(rep, dissemination,
					user));
		}

		disseminations.addStyleName("representation");
	}

	private Widget createDisseminationPanel(RepresentationInfo repInfo,
			DisseminationInfo dissemination, AuthenticatedUser user) {
		FocusPanel focus = new FocusPanel();
		VerticalPanel layout = new VerticalPanel();
		focus.setWidget(layout);

		Image icon;
		if (dissemination.getIconURL() != null) {
			String iconURL = dissemination.getIconURL().replaceAll("\\$PID",
					repInfo.getRepresentationObjectPID());
			if (GWT.isScript()) {
				icon = new PNGImage(iconURL, 128, 128);
			} else {
				icon = new Image(iconURL);
			}
		} else {
			icon = browseImageBundle.disseminationDefaultIcon().createImage();
		}

		layout.add(icon);
		Label description = new Label();
		layout.add(description);

		if (dissemination.isDownloadDisseminator()) {
			focus.setTitle(messages.representationDownloadTooltip(repInfo
					.getFormat(), repInfo.getNumberOfFiles(), repInfo
					.getSizeOfFiles()));
			description.setText(translateFormat(repInfo.getFormat()));

			if (repInfo.isOriginal() || repInfo.isNormalized()) {
				Label originalDisseminatorLabel = new Label(constants
						.disseminationOfOriginal());
				if (repInfo.isOriginal() && repInfo.isNormalized()) {
					originalDisseminatorLabel.setText(constants
							.disseminationOfOriginalAndNormalized());
				} else if (repInfo.isOriginal()) {
					originalDisseminatorLabel.setText(constants
							.disseminationOfOriginal());
				} else /* if(repInfo.isNormalized()) */{
					originalDisseminatorLabel.setText(constants
							.disseminationOfNormalized());
				}

				layout.add(originalDisseminatorLabel);
				layout.setCellHorizontalAlignment(originalDisseminatorLabel,
						HasAlignment.ALIGN_CENTER);
				originalDisseminatorLabel
						.addStyleName("dissemination-original-label");
			}

		} else {
			description.setText(getDisseminationDescription(dissemination));
		}

		focus.addClickListener(getDisseminationClickListener(repInfo.getRepresentationObjectPID(),
				dissemination));

		layout.setCellHorizontalAlignment(icon, HasAlignment.ALIGN_CENTER);
		layout.setCellHorizontalAlignment(description,
				HasAlignment.ALIGN_CENTER);

		focus.addStyleName("dissemination");
		layout.addStyleName("dissemination-layout");
		icon.addStyleName("dissemination-icon");
		description.addStyleName("dissemination-description");

		return focus;
	}

	private String translateFormat(String format) {
		String ret;
		try {
			if (format != null) {
				ret = constants.getString("representation_format_"
						+ format.replaceAll("[/+-\\., ]", "_"));
			} else {
				ret = constants.representation_format_unknown();
			}
		} catch (MissingResourceException e) {
			ret = format;
		}
		return ret;
	}

	protected String getDisseminationDescription(DisseminationInfo dissemination) {
		String ret;
		try {
			ret = constants.getString("dissemination_" + dissemination.getId());
		} catch (MissingResourceException e) {
			ret = dissemination.getId();
		}
		return ret;
	}

	protected ClickListener getDisseminationClickListener(final String repPid,
			final DisseminationInfo dissemination) {
		return new ClickListener() {

			public void onClick(Widget sender) {
				UserLogin.getInstance().getAuthenticatedUser(
						new AsyncCallback<AuthenticatedUser>() {

							public void onFailure(Throwable caught) {
								logger.error("Error getting current user",
										caught);
							}

							public void onSuccess(AuthenticatedUser user) {
								String url = dissemination.getUrl().replaceAll(
										"\\$PID", repPid);
								Window.open(url, dissemination.getWindowName(),
										dissemination.getWindowFeatures());

							}

						});

			}

		};
	}
}
