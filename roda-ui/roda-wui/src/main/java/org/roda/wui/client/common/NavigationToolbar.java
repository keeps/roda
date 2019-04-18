/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.browse.bundle.BrowseDipBundle;
import org.roda.wui.client.browse.bundle.BrowseFileBundle;
import org.roda.wui.client.browse.bundle.BrowseRepresentationBundle;
import org.roda.wui.client.browse.bundle.Bundle;
import org.roda.wui.client.common.actions.AbstractActionable;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.AipActions;
import org.roda.wui.client.common.actions.DisseminationActions;
import org.roda.wui.client.common.actions.DisseminationFileActions;
import org.roda.wui.client.common.actions.FileActions;
import org.roda.wui.client.common.actions.RepresentationActions;
import org.roda.wui.client.common.actions.TransferredResourceActions;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils.ProcessRelativeItem;
import org.roda.wui.client.common.popup.CalloutPopup;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.search.Search;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class NavigationToolbar<T extends IsIndexed> extends Composite implements HasHandlers {
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private boolean requireControlKeyModifier = true;
  private boolean requireShiftKeyModifier = false;
  private boolean requireAltKeyModifier = false;
  private boolean skipButtonSetup = false;

  interface MyUiBinder extends UiBinder<Widget, NavigationToolbar> {
  }

  @UiField
  AccessibleFocusPanel keyboardFocus;

  // breadcrumb on left side

  @UiField
  Label navigationToolbarHeader;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  HTML aipState;

  @UiField
  HTML pageInformation;

  // buttons on the right side

  @UiField
  AccessibleFocusPanel disseminationsButton;

  @UiField
  AccessibleFocusPanel searchButton;

  @UiField
  AccessibleFocusPanel nextButton, previousButton;
  ProcessRelativeItem<T> processor;

  @UiField
  AccessibleFocusPanel infoSidebarButton;

  @UiField
  AccessibleFocusPanel actionsButton;

  @UiField
  FlowPanel toolbarPanel;

  private T currentObject = null;
  private Permissions permissions = null;
  private HandlerRegistration searchPopupClickHandler = null;

  private Map<Actionable.ActionImpact, Runnable> handlers = new EnumMap<>(Actionable.ActionImpact.class);

  private AsyncCallback<Actionable.ActionImpact> handler = new NoAsyncCallback<Actionable.ActionImpact>() {
    @Override
    public void onSuccess(Actionable.ActionImpact result) {
      if (handlers.containsKey(result)) {
        handlers.get(result).run();
      }
    }
  };

  public NavigationToolbar() {
    initWidget(uiBinder.createAndBindUi(this));
    withModifierKeys(true, false, false);
    hideButtons();
  }

  public NavigationToolbar<T> withObject(T object) {
    this.currentObject = object;
    return this;
  }

  public NavigationToolbar<T> withoutButtons() {
    this.skipButtonSetup = true;
    return this;
  }

  public NavigationToolbar<T> withProcessor(ProcessRelativeItem<T> processor) {
    this.processor = processor;
    return this;
  }

  public NavigationToolbar<T> withPermissions(Permissions permissions) {
    this.permissions = permissions;
    return this;
  }

  public NavigationToolbar<T> withAlternativeStyle(boolean useAltStyle) {
    toolbarPanel.setStyleDependentName("alt", useAltStyle);
    return this;
  }

  public AccessibleFocusPanel getInfoSidebarButton() {
    infoSidebarButton.setVisible(true);
    return infoSidebarButton;
  }

  public AccessibleFocusPanel getDisseminationsButton() {
    disseminationsButton.setVisible(true);
    return disseminationsButton;
  }

  public NavigationToolbar<T> withModifierKeys(boolean requireControlKeyModifier, boolean requireShiftKeyModifier,
    boolean requireAltKeyModifier) {
    this.requireControlKeyModifier = requireControlKeyModifier;
    this.requireShiftKeyModifier = requireShiftKeyModifier;
    this.requireAltKeyModifier = requireAltKeyModifier;
    return this;
  }

  public void setHeader(String headerText) {
    navigationToolbarHeader.setText(headerText);
  }

  private void hideButtons() {
    aipState.setVisible(false);

    disseminationsButton.setVisible(false);
    searchButton.setVisible(false);
    previousButton.setVisible(false);
    nextButton.setVisible(false);
    pageInformation.setVisible(false);
    infoSidebarButton.setVisible(false);
    actionsButton.setVisible(false);
  }

  public void build() {
    hideButtons();
    if (!skipButtonSetup) {
      if (processor != null) {
        ListSelectionUtils.bindLayout(currentObject, previousButton, nextButton, pageInformation, keyboardFocus,
          requireControlKeyModifier, requireShiftKeyModifier, requireAltKeyModifier, processor);
      } else {
        ListSelectionUtils.bindLayout(currentObject, previousButton, nextButton, pageInformation, keyboardFocus,
          requireControlKeyModifier, requireShiftKeyModifier, requireAltKeyModifier);
      }

      int index = ListSelectionUtils.getIndex(IndexedAIP.class.getName()) + 1;
      long total = ListSelectionUtils.getTotal(IndexedAIP.class.getName());
      pageInformation.setHTML("<span>" + NumberFormat.getDecimalFormat().format(index) + "</span> " + messages.of()
        + " <span>" + NumberFormat.getDecimalFormat().format(total) + "</span>");

      setNavigationButtonTitles();

      clearSearchPopupHandlers();
      setupSearchPopup();
      setupActions();
    }
  }

  private void setNavigationButtonTitles() {
    StringBuilder modifiers = new StringBuilder();

    if (requireControlKeyModifier) {
      if (modifiers.length() > 0) {
        modifiers.append('+');
      }
      modifiers.append("CTRL");
    }

    if (requireShiftKeyModifier) {
      if (modifiers.length() > 0) {
        modifiers.append('+');
      }
      modifiers.append("Shift");
    }

    if (requireAltKeyModifier) {
      if (modifiers.length() > 0) {
        modifiers.append('+');
      }
      modifiers.append("Alt");
    }

    modifiers.append(' ');

    previousButton.setTitle(modifiers.toString() + '\u21E6');
    nextButton.setTitle(modifiers.toString() + '\u21E8');

    // TODO 2018-09-07 bferreira: after fixing shortcuts, remove code below
    previousButton.setTitle(messages.searchPrevious());
    nextButton.setTitle(messages.searchNext());
  }

  public void setSearchButtonVisibility(boolean visible) {
    searchButton.setVisible(visible);
  }

  public void setActionsButtonVisibility(boolean visible) {
    actionsButton.setVisible(visible);
  }

  public NavigationToolbar<T> withActionImpactHandler(Actionable.ActionImpact actionImpact, Runnable handler) {
    this.handlers.put(actionImpact, handler);
    return this;
  }

  private void setupSearchPopup() {
    if (currentObject instanceof IndexedAIP) {
      SearchPopup searchPopup = new SearchPopup((IndexedAIP) currentObject);
      searchPopup.addStyleName("ActionableStyleMenu");
      searchPopupClickHandler = searchButton
        .addClickHandler(event -> searchPopup.showRelativeTo(searchButton, CalloutPopup.CalloutPosition.TOP_RIGHT));
      searchButton.setVisible(true);
    }
  }

  private void setupActions() {
    CalloutPopup popup = new CalloutPopup();
    popup.addStyleName("ActionableStyleMenu");

    if (currentObject instanceof IndexedAIP) {
      AipActions actions;
      IndexedAIP aip = (IndexedAIP) this.currentObject;
      if (aip.getParentID() != null) {
        actions = AipActions.get(aip.getParentID(), aip.getState(), aip.getPermissions());
      } else {
        actions = AipActions.get();
      }

      popup.setWidget(new ActionableWidgetBuilder<>(actions).withActionCallback(handler)
        .buildListWithObjects(new ActionableObject<>(aip)));
      actionsButton.addClickHandler(event -> popup.showRelativeTo(actionsButton));
      actionsButton.setVisible(actions.hasAnyRoles());
    } else if (currentObject instanceof IndexedRepresentation) {
      IndexedRepresentation representation = (IndexedRepresentation) this.currentObject;
      RepresentationActions actions = RepresentationActions.get(representation.getAipId(), permissions);

      popup.setWidget(new ActionableWidgetBuilder<>(actions).withActionCallback(handler)
        .buildListWithObjects(new ActionableObject<>(representation)));
      actionsButton.addClickHandler(event -> popup.showRelativeTo(actionsButton));
      actionsButton.setVisible(actions.hasAnyRoles());
    } else if (currentObject instanceof IndexedFile) {
      infoSidebarButton.setTitle(messages.viewRepresentationInfoFileButton());

      IndexedFile file = (IndexedFile) this.currentObject;
      FileActions actions = FileActions.get(file.getAipId(), file.getRepresentationId(),
        file.isDirectory() ? file : null, permissions);

      popup.setWidget(new ActionableWidgetBuilder<>(actions).withActionCallback(handler)
        .buildListWithObjects(new ActionableObject<>(file)));
      actionsButton.addClickHandler(event -> popup.showRelativeTo(actionsButton));
      actionsButton.setVisible(actions.hasAnyRoles());
    } else if (currentObject instanceof IndexedDIP) {
      infoSidebarButton.setTitle(messages.dissemination());

      IndexedDIP dip = (IndexedDIP) this.currentObject;
      DisseminationActions actions = DisseminationActions.get(permissions);

      popup.setWidget(new ActionableWidgetBuilder<>(actions).withActionCallback(handler)
        .buildListWithObjects(new ActionableObject<>(dip)));
      actionsButton.addClickHandler(event -> popup.showRelativeTo(actionsButton));
      actionsButton.setVisible(actions.hasAnyRoles());
    } else if (currentObject instanceof DIPFile) {
      infoSidebarButton.setTitle(messages.disseminationFile());

      DIPFile dipFile = (DIPFile) this.currentObject;
      DisseminationFileActions actions = DisseminationFileActions.get(permissions);

      popup.setWidget(new ActionableWidgetBuilder<>(actions).withActionCallback(handler)
        .buildListWithObjects(new ActionableObject<>(dipFile)));
      actionsButton.addClickHandler(event -> popup.showRelativeTo(actionsButton));
      actionsButton.setVisible(actions.hasAnyRoles());
    } else if (currentObject instanceof TransferredResource) {
      infoSidebarButton.setTitle(messages.oneOfAObject(TransferredResource.class.getName()));

      TransferredResource transferredResource = (TransferredResource) this.currentObject;
      TransferredResourceActions actions = TransferredResourceActions.get(null);

      popup.setWidget(new ActionableWidgetBuilder<>(actions).withActionCallback(handler)
        .buildListWithObjects(new ActionableObject<>(transferredResource)));
      actionsButton.addClickHandler(event -> popup.showRelativeTo(actionsButton));
      actionsButton.setVisible(actions.hasAnyRoles());
    }
  }

  // Breadcrumb management

  public void clearBreadcrumb() {
    breadcrumb.clear();
  }

  public void updateBreadcrumb(BrowseAIPBundle bundle) {
    breadcrumb.updatePath(BreadcrumbUtils.getAipBreadcrumbs(bundle.getAIPAncestors(), bundle.getAip()));
  }

  public void updateBreadcrumb(BrowseRepresentationBundle bundle) {
    breadcrumb.updatePath(BreadcrumbUtils.getRepresentationBreadcrumbs(bundle));
  }

  public void updateBreadcrumb(BrowseFileBundle bundle) {
    breadcrumb.updatePath(BreadcrumbUtils.getFileBreadcrumbs(bundle));
    aipState.setHTML(HtmlSnippetUtils.getAIPStateHTML(bundle.getAip().getState()));
    aipState.setVisible(AIPState.ACTIVE != bundle.getAip().getState());
  }

  public void updateBreadcrumb(BrowseDipBundle bundle) {
    breadcrumb.updatePath(
      BreadcrumbUtils.getDipBreadcrumbs(bundle.getDip(), bundle.getDipFile(), bundle.getDipFileAncestors()));
  }

  public void updateBreadcrumb(Bundle dipReferrerBundle) {
    if (dipReferrerBundle instanceof BrowseFileBundle) {
      BrowseFileBundle bundle = (BrowseFileBundle) dipReferrerBundle;
      breadcrumb
        .updatePath(BreadcrumbUtils.getFileBreadcrumbs(bundle.getAip(), bundle.getRepresentation(), bundle.getFile()));
    } else if (dipReferrerBundle instanceof BrowseRepresentationBundle) {
      BrowseRepresentationBundle bundle = (BrowseRepresentationBundle) dipReferrerBundle;
      breadcrumb.updatePath(BreadcrumbUtils.getRepresentationBreadcrumbs(bundle.getAip(), bundle.getRepresentation()));
    } else if (dipReferrerBundle instanceof BrowseAIPBundle) {
      BrowseAIPBundle bundle = (BrowseAIPBundle) dipReferrerBundle;
      breadcrumb.updatePath(BreadcrumbUtils.getAipBreadcrumbs(bundle.getAip()));
    }
  }

  public void updateBreadcrumb(TransferredResource transferredResource) {
    breadcrumb.updatePath(BreadcrumbUtils.getTransferredResourceBreadcrumbs(transferredResource));
  }

  public void updateBreadcrumbPath(BreadcrumbItem... items) {
    updateBreadcrumbPath(Arrays.asList(items));
  }

  public void updateBreadcrumbPath(List<BreadcrumbItem> items) {
    breadcrumb.updatePath(items);
  }

  private void clearSearchPopupHandlers() {
    if (searchPopupClickHandler != null) {
      searchPopupClickHandler.removeHandler();
    }
    searchPopupClickHandler = null;
  }

  private static class SearchPopup extends CalloutPopup {
    public SearchPopup(IndexedAIP aip) {
      super();
      setWidget(
        new ActionableWidgetBuilder<>(SearchAipActions.get()).buildListWithObjects(new ActionableObject<>(aip)));
    }
  }

  // TODO 2018-08-21 bferreira: change this to use button/action whitelist
  private static class SearchAipActions extends AbstractActionable<IndexedAIP> {
    private static final SearchAipActions INSTANCE = new SearchAipActions();

    private static final Set<SearchAipAction> POSSIBLE_ACTIONS = new HashSet<>(Arrays.asList(SearchAipAction.values()));

    private SearchAipActions() {
      // do nothing
    }

    public enum SearchAipAction implements Action<IndexedAIP> {
      SEARCH_DESCENDANTS(), SEARCH_PACKAGE();

      private List<String> methods;

      SearchAipAction(String... methods) {
        this.methods = Arrays.asList(methods);
      }

      @Override
      public List<String> getMethods() {
        return this.methods;
      }
    }

    @Override
    public SearchAipAction[] getActions() {
      return SearchAipAction.values();
    }

    @Override
    public Action<IndexedAIP> actionForName(String name) {
      return SearchAipAction.valueOf(name);
    }

    public static SearchAipActions get() {
      return INSTANCE;
    }

    @Override
    public boolean canAct(Action<IndexedAIP> action, IndexedAIP object) {
      return POSSIBLE_ACTIONS.contains(action);
    }

    @Override
    public void act(Action<IndexedAIP> action, IndexedAIP object, AsyncCallback<ActionImpact> callback) {
      if (action.equals(SearchAipAction.SEARCH_DESCENDANTS)) {
        List<String> searchFilters = new ArrayList<>();

        searchFilters.add(SearchFilters.classesToHistoryTokens(IndexedAIP.class));
        searchFilters.add(RodaConstants.AIP_ANCESTORS);
        searchFilters.add(object.getId());

        searchFilters.add(SearchFilters.classesToHistoryTokens(IndexedRepresentation.class));
        searchFilters.add(RodaConstants.REPRESENTATION_ANCESTORS);
        searchFilters.add(object.getId());

        searchFilters.add(SearchFilters.classesToHistoryTokens(IndexedFile.class));
        searchFilters.add(RodaConstants.FILE_ANCESTORS);
        searchFilters.add(object.getId());

        HistoryUtils.newHistory(Search.RESOLVER, searchFilters);
      } else if (action.equals(SearchAipAction.SEARCH_PACKAGE)) {
        List<String> searchFilters = new ArrayList<>();

        searchFilters.add(SearchFilters.classesToHistoryTokens(IndexedRepresentation.class));
        searchFilters.add(RodaConstants.REPRESENTATION_AIP_ID);
        searchFilters.add(object.getId());

        searchFilters.add(SearchFilters.classesToHistoryTokens(IndexedFile.class));
        searchFilters.add(RodaConstants.FILE_AIP_ID);
        searchFilters.add(object.getId());

        HistoryUtils.newHistory(Search.RESOLVER, searchFilters);
      } else {
        unsupportedAction(action, callback);
      }
    }

    @Override
    public ActionableBundle<IndexedAIP> createActionsBundle() {
      ActionableBundle<IndexedAIP> actionableBundle = new ActionableBundle<>();

      // SEARCH
      ActionableGroup<IndexedAIP> searchGroup = new ActionableGroup<>();
      searchGroup.addButton(messages.searchContext(), SearchAipAction.SEARCH_DESCENDANTS, ActionImpact.NONE,
        "btn-sitemap");
      searchGroup.addButton(messages.searchAIP(), SearchAipAction.SEARCH_PACKAGE, ActionImpact.NONE, "btn-archive");

      actionableBundle.addGroup(searchGroup);
      return actionableBundle;
    }
  }
}
