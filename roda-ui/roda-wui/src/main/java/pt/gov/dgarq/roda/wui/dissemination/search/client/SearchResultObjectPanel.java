/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.search.client;

import pt.gov.dgarq.roda.core.data.SearchResultObject;
import pt.gov.dgarq.roda.wui.common.client.tools.DescriptionLevelUtils;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.Browse;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.ElementPathPanel;
import pt.gov.dgarq.roda.wui.dissemination.client.DescriptiveMetadataPanel;
import pt.gov.dgarq.roda.wui.dissemination.client.Dissemination;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DisclosureEvent;
import com.google.gwt.user.client.ui.DisclosureHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.SearchConstants;

/**
 * @author Luis Faria
 * 
 */
public class SearchResultObjectPanel extends SimplePanel {

	// private GWTLogger logger = new GWTLogger(GWT.getTypeName(this));

	private static SearchConstants constants = (SearchConstants) GWT.create(SearchConstants.class);

	private final SearchResultObject object;

	private final DisclosurePanel disclosure;

	private final HorizontalPanel headerLayout;

	private final Image icon;

	private final SimplePanel idContainer;

	private final HTML id;

	private final SimplePanel titleContainer;

	private final HTML title;

	private final Label dateInitial;

	private final Label dateFinal;

	private final ScorePanel score;

	/**
	 * Create new search result object
	 * 
	 * @param object
	 */
	public SearchResultObjectPanel(SearchResultObject object) {
		this.object = object;
		this.disclosure = new DisclosurePanel();
		this.disclosure.setAnimationEnabled(true);
		this.headerLayout = new HorizontalPanel();
		this.icon = DescriptionLevelUtils.getElementLevelIconImage(object.getDescriptionObject().getLevel().getLevel());
		this.idContainer = new SimplePanel();
		this.id = new HTML(object.getDescriptionObject().getId());
		this.titleContainer = new SimplePanel();
		this.title = new HTML(object.getDescriptionObject().getTitle());
		this.dateInitial = new Label(object.getDescriptionObject().getDateInitial());
		this.dateFinal = new Label(object.getDescriptionObject().getDateFinal());
		this.score = new ScorePanel(object.getScore());
		headerLayout.add(icon);
		idContainer.setWidget(id);
		headerLayout.add(idContainer);
		titleContainer.setWidget(title);
		headerLayout.add(titleContainer);
		headerLayout.add(dateInitial);
		headerLayout.add(dateFinal);
		headerLayout.add(score);

		disclosure.setHeader(headerLayout);
		disclosure.setContent(new Label(constants.searchResultLoading()));
		disclosure.addEventHandler(new DisclosureHandler() {
			private boolean firstOpen = true;

			public void onClose(DisclosureEvent event) {

			}

			public void onOpen(DisclosureEvent event) {
				if (firstOpen) {
					firstOpen = false;
					final String pid = SearchResultObjectPanel.this.object.getDescriptionObject().getPid();
					VerticalPanel contentLayout = new VerticalPanel();
					DescriptiveMetadataPanel descriptiveMetadata = new DescriptiveMetadataPanel(
							SearchResultObjectPanel.this.object.getDescriptionObject(), true);
					WUIButton browseResult = new WUIButton(constants.browseResult(), WUIButton.Left.ROUND,
							WUIButton.Right.ARROW_FORWARD);
					browseResult.addClickListener(new ClickListener() {

						public void onClick(Widget sender) {
							Browse.getInstance().view(pid);
						}

					});

					SimplePanel elementPathWrapper = new SimplePanel();
					ElementPathPanel elementPath = new ElementPathPanel(pid);

					elementPathWrapper.setWidget(elementPath);

					contentLayout.add(descriptiveMetadata);
					contentLayout.add(browseResult);
					contentLayout.add(elementPathWrapper);

					disclosure.setContent(contentLayout);

					contentLayout.setCellHorizontalAlignment(browseResult, VerticalPanel.ALIGN_RIGHT);
					contentLayout.addStyleName("content-layout");
					browseResult.addStyleName("content-browseItem");
					elementPathWrapper.addStyleName("content-elementPath");
				}
			}

		});

		this.setWidget(disclosure);

		this.headerLayout.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		this.headerLayout.setCellWidth(titleContainer, "100%");

		this.addStyleName("wui-search-result");
		this.disclosure.setStylePrimaryName("disclosure");
		this.headerLayout.addStyleName("header-layout");
		this.icon.addStyleName("header-icon");
		this.id.addStyleName("header-id");
		this.title.addStyleName("header-title");
		this.dateInitial.addStyleName("header-date");
		this.dateFinal.addStyleName("header-date");
		this.idContainer.addStyleName("header-id-container");
		this.titleContainer.addStyleName("header-title-container");
	}

	/**
	 * @see DisclosurePanel#addEventHandler(DisclosureHandler)
	 * @param handler
	 */
	public void addEventHandler(DisclosureHandler handler) {
		disclosure.addEventHandler(handler);
	}

	/**
	 * @see DisclosurePanel#removeEventHandler(DisclosureHandler)
	 * @param handler
	 */
	public void removeEventHandler(DisclosureHandler handler) {
		disclosure.removeEventHandler(handler);
	}

	/**
	 * @see DisclosurePanel#isOpen()
	 * @return true if open
	 */
	public boolean isOpen() {
		return disclosure.isOpen();
	}

	/**
	 * @see DisclosurePanel#setOpen(boolean)
	 * @param isOpen
	 */
	public void setOpen(boolean isOpen) {
		disclosure.setOpen(isOpen);
	}

	/**
	 * Score graphic panel
	 * 
	 */
	public class ScorePanel extends SimplePanel {
		private final HTML bar;

		private float score;

		/**
		 * Create a new score panel
		 * 
		 * @param score
		 *            the score, between 0 and 1
		 */
		public ScorePanel(float score) {
			bar = new HTML("&nbsp;");
			this.setWidget(bar);
			this.score = score;

			this.addStyleName("wui-score");
			bar.addStyleName("bar");
		}

		protected void onLoad() {
			super.onLoad();
			setScore(score);
		}

		/**
		 * Get current score
		 * 
		 * @return the score, between 0 and 1
		 */
		public float getScore() {
			return score;
		}

		/**
		 * Set the score
		 * 
		 * @param score
		 *            the score, between 0 and 1
		 */
		public void setScore(float score) {
			this.score = score;
			int barWidth = (int) ((this.getOffsetWidth() - 4) * score);
			bar.setWidth(barWidth + "px");
		}

	}

}
