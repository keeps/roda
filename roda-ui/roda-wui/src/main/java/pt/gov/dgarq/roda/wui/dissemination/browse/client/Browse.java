/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.CommonConstants;
import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.Representation;
import pt.gov.dgarq.roda.core.data.v2.RepresentationState;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.AuthenticatedUser;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.tools.DescriptionLevelUtils;
import pt.gov.dgarq.roda.wui.common.client.tools.RestUtils;
import pt.gov.dgarq.roda.wui.common.client.widgets.AIPList;
import pt.gov.dgarq.roda.wui.main.client.BreadcrumbItem;
import pt.gov.dgarq.roda.wui.main.client.BreadcrumbPanel;

/**
 * @author Luis Faria
 * 
 */
public class Browse extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public String getHistoryToken() {
      return "browse";
    }

    @Override
    public String getHistoryPath() {
      return getHistoryToken();
    }
  };

  public static final String getViewItemHistoryToken(String id) {
    return RESOLVER.getHistoryPath() + "." + id;
  }

  interface MyUiBinder extends UiBinder<Widget, Browse> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static Browse instance = null;

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static Browse getInstance() {
    if (instance == null) {
      instance = new Browse();
    }
    return instance;
  }

  private static CommonConstants constants = (CommonConstants) GWT.create(CommonConstants.class);

  private static Filter COLLECTIONS_FILTER = new Filter(new EmptyKeyFilterParameter(RodaConstants.AIP_PARENT_ID));

  // private static BrowseConstants constants = (BrowseConstants)
  // GWT.create(BrowseConstants.class);

  // private static BrowseMessages messages = (BrowseMessages)
  // GWT.create(BrowseMessages.class);
  //
  // private static BrowseImageBundle browseImageBundle = (BrowseImageBundle)
  // GWT.create(BrowseImageBundle.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  // private SimplePanel viewPanelContainer;

  @UiField(provided = true)
  BreadcrumbPanel breadcrumb;

  @UiField
  SimplePanel itemIcon;

  @UiField
  Label itemTitle;

  @UiField
  Label itemDates;

  @UiField
  HTML itemDescriptiveMetadata;

  @UiField
  Label fondsPanelTitle;

  @UiField(provided = true)
  AIPList fondsPanel;

  @UiField
  FlowPanel sidebarGroupDownloads;

  @UiField
  FlowPanel downloadList;

  @UiField
  Button createItem;

  private boolean viewingTop;

  private Browse() {
    viewingTop = true;
    breadcrumb = new BreadcrumbPanel();
    fondsPanel = new AIPList();
    initWidget(uiBinder.createAndBindUi(this));

    fondsPanel.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        SimpleDescriptionObject sdo = fondsPanel.getSelectionModel().getSelectedObject();
        if (sdo != null) {
          view(sdo.getId());
        }
      }
    });

    fondsPanel.addValueChangeHandler(new ValueChangeHandler<IndexResult<SimpleDescriptionObject>>() {

      @Override
      public void onValueChange(ValueChangeEvent<IndexResult<SimpleDescriptionObject>> event) {
        fondsPanelTitle.setVisible(!viewingTop && event.getValue().getTotalCount() > 0);
        fondsPanel.setVisible(event.getValue().getTotalCount() > 0);
      }
    });
  }

  protected void onPermissionsUpdate(AuthenticatedUser user) {
    if (user.hasRole(RodaConstants.REPOSITORY_PERMISSIONS_METADATA_EDITOR)) {
      createItem.setVisible(true);
      // refresh.setVisible(true);
    } else {
      createItem.setVisible(false);
      // refresh.setVisible(false);
    }
  }

  public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.length == 0) {
      viewAction();
      callback.onSuccess(this);
    } else if (historyTokens.length == 1) {
      viewAction(historyTokens[0]);
      callback.onSuccess(this);
    } else {
      History.newItem(RESOLVER.getHistoryPath());
      callback.onSuccess(null);
    }
  }

  /**
   * Call the view action by the history token
   * 
   * @param id
   *          the pid of the object to view. if pid is null, then the base state
   *          will be called
   */
  public void view(final String id) {
    boolean historyUpdated = updateHistory(id);

    if (!historyUpdated) {
      viewAction(id);
    }
  }

  protected void viewAction(final String id) {
    if (id == null) {
      viewAction();
    } else {
      BrowserService.Util.getInstance().getItemBundle(id, constants.locale(), new AsyncCallback<BrowseItemBundle>() {

        @Override
        public void onFailure(Throwable caught) {
          logger.error("Could not view id=" + id, caught);
        }

        @Override
        public void onSuccess(BrowseItemBundle itemBundle) {
          viewAction(itemBundle);
        }
      });
    }
  }

  protected void viewAction(BrowseItemBundle itemBundle) {
    if (itemBundle != null) {
      SimpleDescriptionObject sdo = itemBundle.getSdo();
      List<DescriptiveMetadataBundle> descMetadata = itemBundle.getDescriptiveMetadata();
      List<Representation> representations = itemBundle.getRepresentations();

      breadcrumb.updatePath(getBreadcrumbsFromAncestors(itemBundle.getSdoAncestors(), sdo));
      breadcrumb.setVisible(true);
      HTMLPanel itemIconHtmlPanel = DescriptionLevelUtils.getElementLevelIconHTMLPanel(sdo.getLevel());
      itemIconHtmlPanel.addStyleName("browseItemIcon-other");
      itemIcon.setWidget(itemIconHtmlPanel);
      itemTitle.setText(sdo.getTitle());
      itemTitle.removeStyleName("browseTitle-allCollections");
      itemDates.setText(getDatesText(sdo));
      SafeHtml html = getDescriptiveMetadataPanelHTML(descMetadata);
      itemDescriptiveMetadata.setHTML(html);
      itemDescriptiveMetadata.setVisible(true);

      viewingTop = false;
      fondsPanelTitle.setVisible(true);
      Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, sdo.getId()));
      fondsPanel.setFilter(filter);

      downloadList.clear();
      sidebarGroupDownloads.setVisible(true);

      for (Representation rep : representations) {
        downloadList.add(createRepresentationDownloadPanel(rep));
      }

      downloadList.add(createDescriptiveMetadataDownloadPanel(sdo.getId(), descMetadata));

    } else {
      viewAction();
    }
  }

  protected void viewAction() {
    HTMLPanel topIcon = new HTMLPanel(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-circle-o'></i>"));
    topIcon.addStyleName("browseItemIcon-all");
    itemIcon.setWidget(topIcon);

    breadcrumb.updatePath(Arrays.asList(
      new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-circle-o'></i>"), RESOLVER.getHistoryPath())));
    breadcrumb.setVisible(false);
    itemTitle.setText("All collections");
    itemTitle.addStyleName("browseTitle-allCollections");
    itemDates.setText("");
    itemDescriptiveMetadata.setText("");
    itemDescriptiveMetadata.setVisible(false);
    viewingTop = true;
    fondsPanelTitle.setVisible(false);
    fondsPanel.setFilter(COLLECTIONS_FILTER);

    sidebarGroupDownloads.setVisible(false);
    downloadList.clear();
  }

  private List<BreadcrumbItem> getBreadcrumbsFromAncestors(List<SimpleDescriptionObject> sdoAncestors,
    SimpleDescriptionObject sdo) {
    List<BreadcrumbItem> ret = new ArrayList<>();
    ret.add(
      new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-circle-o'></i>"), RESOLVER.getHistoryPath()));
    for (SimpleDescriptionObject ancestor : sdoAncestors) {
      SafeHtml breadcrumbLabel = getBreadcrumbLabel(ancestor);
      BreadcrumbItem ancestorBreadcrumb = new BreadcrumbItem(breadcrumbLabel,
        getViewItemHistoryToken(ancestor.getId()));
      ret.add(1, ancestorBreadcrumb);
    }

    ret.add(new BreadcrumbItem(getBreadcrumbLabel(sdo), getViewItemHistoryToken(sdo.getId())));
    return ret;
  }

  private SafeHtml getBreadcrumbLabel(SimpleDescriptionObject ancestor) {
    SafeHtml elementLevelIconSafeHtml = DescriptionLevelUtils.getElementLevelIconSafeHtml(ancestor.getLevel());
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    builder.append(elementLevelIconSafeHtml).append(SafeHtmlUtils.fromString(ancestor.getTitle()));
    SafeHtml breadcrumbLabel = builder.toSafeHtml();
    return breadcrumbLabel;
  }

  private Widget createRepresentationDownloadPanel(Representation rep) {
    FlowPanel downloadPanel = new FlowPanel();
    HTML icon = new HTML(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-download'></i>"));

    SafeHtml labelText;
    Set<RepresentationState> statuses = rep.getStatuses();
    if (statuses.containsAll(Arrays.asList(RepresentationState.ORIGINAL, RepresentationState.NORMALIZED))) {
      labelText = SafeHtmlUtils.fromString("Original and normalized document");
    } else if (statuses.contains(RepresentationState.ORIGINAL)) {
      labelText = SafeHtmlUtils.fromString("Original document");
    } else if (statuses.contains(RepresentationState.NORMALIZED)) {
      labelText = SafeHtmlUtils.fromString("Normalized document");
    } else {
      labelText = SafeHtmlUtils.fromString("Document");
    }

    FlowPanel labelsPanel = new FlowPanel();

    Anchor label = new Anchor(labelText, RestUtils.createRepresentationDownloadUri(rep.getAipId(), rep.getId()));
    Label subLabel = new Label(rep.getFileIds().size() + " files, " + readableFileSize(rep.getSizeInBytes()));

    labelsPanel.add(label);
    labelsPanel.add(subLabel);
    downloadPanel.add(icon);
    downloadPanel.add(labelsPanel);

    downloadPanel.addStyleName("browseDownload");
    icon.addStyleName("browseDownloadIcon");
    labelsPanel.addStyleName("browseDownloadLabels");
    label.addStyleName("browseDownloadLabel");
    subLabel.addStyleName("browseDownloadSublabel");
    return downloadPanel;
  }

  /**
   * TODO move this to Utils
   * 
   * @param size
   * @return
   */
  public static String readableFileSize(long size) {
    if (size <= 0)
      return "0";
    final String[] units = new String[] {"B", "KB", "MB", "GB", "TB"};
    int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
    return NumberFormat.getFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
  }

  private Widget createDescriptiveMetadataDownloadPanel(String aipId, List<DescriptiveMetadataBundle> descMetadata) {
    FlowPanel downloadPanel = new FlowPanel();
    HTML icon = new HTML(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-download'></i>"));
    FlowPanel labelsPanel = new FlowPanel();

    int files = descMetadata.size();
    long sizeInBytes = 0;
    for (DescriptiveMetadataBundle desc : descMetadata) {
      sizeInBytes += desc.getSizeInBytes();
    }

    Anchor label = new Anchor(SafeHtmlUtils.fromSafeConstant("Descriptive metadata"),
      RestUtils.createDescriptiveMetadataDownloadUri(aipId));
    Label subLabel = new Label(files + " files, " + readableFileSize(sizeInBytes));

    labelsPanel.add(label);
    labelsPanel.add(subLabel);
    downloadPanel.add(icon);
    downloadPanel.add(labelsPanel);

    downloadPanel.addStyleName("browseDownload");
    icon.addStyleName("browseDownloadIcon");
    labelsPanel.addStyleName("browseDownloadLabels");
    label.addStyleName("browseDownloadLabel");
    subLabel.addStyleName("browseDownloadSublabel");
    return downloadPanel;
  }

  private String getDatesText(SimpleDescriptionObject sdo) {
    String ret;
    DateTimeFormat formatter = DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM);

    Date dateInitial = sdo.getDateInitial();
    Date dateFinal = sdo.getDateFinal();

    if (dateInitial == null && dateFinal == null) {
      ret = "";
    } else if (dateInitial != null && dateFinal == null) {
      ret = "From " + formatter.format(sdo.getDateInitial());
    } else if (dateInitial == null && dateFinal != null) {
      ret = "Up to " + formatter.format(sdo.getDateFinal());
    } else {
      ret = formatter.format(sdo.getDateInitial()) + " to " + formatter.format(sdo.getDateFinal());
    }

    return ret;
  }

  private SafeHtml getDescriptiveMetadataPanelHTML(List<DescriptiveMetadataBundle> descriptiveMetadata) {
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    for (DescriptiveMetadataBundle bundle : descriptiveMetadata) {
      builder.append(SafeHtmlUtils.fromTrustedString(bundle.getHtml()));
    }
    return builder.toSafeHtml();
  }

  private boolean updateHistory(String id) {
    boolean historyUpdated;
    String token;
    if (id == null) {
      token = RESOLVER.getHistoryPath();
    } else {
      token = getViewItemHistoryToken(id);
    }

    if (token.equals(History.getToken())) {
      historyUpdated = false;
    } else {
      logger.debug("calling new history token");
      History.newItem(token);
      historyUpdated = true;
    }
    return historyUpdated;
  }

  protected void onMove(final String targetPid, final String oldParentPid, final String newParentPid) {
    // FIXME
    // update(oldParentPid, false, true, new
    // AsyncCallback<CollectionsTreeItem>() {
    //
    // public void onFailure(Throwable caught) {
    // logger.error("Error on move event", caught);
    // }
    //
    // public void onSuccess(CollectionsTreeItem treeItem) {
    // update(newParentPid, false, true, new
    // AsyncCallback<CollectionsTreeItem>() {
    //
    // public void onFailure(Throwable caught) {
    // logger.error("Error on move event", caught);
    // }
    //
    // public void onSuccess(CollectionsTreeItem result) {
    // // fondsPanel.setSelected(null);
    // // fondsPanel.setSelected(targetPid);
    // }
    //
    // });
    //
    // }

    // });
  }

  protected void onClone(final String clonePID) {
    // FIXME
    // BrowserService.Util.getInstance().getParent(clonePID, new
    // AsyncCallback<String>() {
    //
    // public void onFailure(Throwable caught) {
    // logger.error("Error on cloning event", caught);
    // }
    //
    // public void onSuccess(final String parentPID) {
    // update(parentPID, false, true, new
    // AsyncCallback<CollectionsTreeItem>() {
    //
    // public void onFailure(Throwable caught) {
    // logger.error("Error on cloning event", caught);
    // }
    //
    // public void onSuccess(CollectionsTreeItem treeItem) {
    // ViewPanel.setEditMode(true);
    // view(clonePID);
    //
    // }
    //
    // });
    // }

    // });

  }

  protected void onRemove(final String parentPID) {
    // FIXME
    // update(parentPID, false, true, new
    // AsyncCallback<CollectionsTreeItem>() {
    //
    // public void onFailure(Throwable caught) {
    // logger.error("Error on remove event", caught);
    // }
    //
    // public void onSuccess(CollectionsTreeItem treeItem) {
    // view(null);
    // }
    //
    // });
  }
}
