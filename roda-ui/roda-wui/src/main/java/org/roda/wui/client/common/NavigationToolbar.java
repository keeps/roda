/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils.ProcessRelativeItem;
import org.roda.wui.client.common.model.BrowseDIPResponse;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class NavigationToolbar<T extends IsIndexed> extends Composite implements HasHandlers {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  @UiField
  AccessibleFocusPanel keyboardFocus;
  @UiField
  BreadcrumbPanel breadcrumb;
  @UiField
  HTML pageInformation;

  // breadcrumb on left side
  @UiField
  AccessibleFocusPanel nextButton;
  @UiField
  AccessibleFocusPanel previousButton;
  ProcessRelativeItem<T> processor;

  // buttons on the right side
  @UiField
  AccessibleFocusPanel infoSidebarButton;
  @UiField
  FlowPanel toolbarPanel;
  private boolean requireControlKeyModifier = true;
  private boolean requireShiftKeyModifier = false;
  private boolean requireAltKeyModifier = false;
  private boolean skipButtonSetup = false;
  private T currentObject = null;
  private Permissions permissions = null;
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

  public NavigationToolbar<T> withModifierKeys(boolean requireControlKeyModifier, boolean requireShiftKeyModifier,
    boolean requireAltKeyModifier) {
    this.requireControlKeyModifier = requireControlKeyModifier;
    this.requireShiftKeyModifier = requireShiftKeyModifier;
    this.requireAltKeyModifier = requireAltKeyModifier;
    return this;
  }

  private void hideButtons() {
    previousButton.setVisible(false);
    nextButton.setVisible(false);
    pageInformation.setVisible(false);
    infoSidebarButton.setVisible(false);
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

      int index = ListSelectionUtils.getIndex(currentObject.getClass().getName()) + 1;
      long total = ListSelectionUtils.getTotal(currentObject.getClass().getName());
      pageInformation.setHTML("<span>" + NumberFormat.getDecimalFormat().format(index) + "</span> / <span>"
        + NumberFormat.getDecimalFormat().format(total) + "</span>");

      setNavigationButtonTitles();
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

  public NavigationToolbar<T> withActionImpactHandler(Actionable.ActionImpact actionImpact, Runnable handler) {
    this.handlers.put(actionImpact, handler);
    return this;
  }

  public void clearBreadcrumb() {
    breadcrumb.clear();
  }

  // Breadcrumb management

  public void updateBreadcrumb(IndexedAIP aip, List<IndexedAIP> ancestors) {
    breadcrumb.updatePath(BreadcrumbUtils.getAipBreadcrumbs(ancestors, aip));

  }

  public void updateBreadcrumb(List<IndexedAIP> ancestors, IndexedAIP indexedAIP,
    IndexedRepresentation indexedRepresentation) {
    breadcrumb.updatePath(BreadcrumbUtils.getRepresentationBreadcrumbs(ancestors, indexedAIP, indexedRepresentation));
  }

  public void updateBreadcrumb(IndexedAIP indexedAIP, IndexedRepresentation indexedRepresentation,
    IndexedFile indexedFile) {
    breadcrumb.updatePath(BreadcrumbUtils.getFileBreadcrumbs(indexedAIP, indexedRepresentation, indexedFile));
  }

  public void updateBreadcrumb(IndexedDIP dip, DIPFile dipFile, List<DIPFile> ancestors) {
    breadcrumb.updatePath(BreadcrumbUtils.getDipBreadcrumbs(dip, dipFile, ancestors));
  }

  public void updateBreadcrumb(BrowseDIPResponse response) {
    if (response.getReferred() instanceof IndexedFile) {
      breadcrumb.updatePath(BreadcrumbUtils.getFileBreadcrumbs(response.getIndexedAIP(),
        response.getIndexedRepresentation(), response.getIndexedFile()));
    } else if (response.getReferred() instanceof IndexedRepresentation) {
      breadcrumb.updatePath(
        BreadcrumbUtils.getRepresentationBreadcrumbs(response.getIndexedAIP(), response.getIndexedRepresentation()));
    } else if (response.getReferred() instanceof IndexedAIP) {
      breadcrumb.updatePath(BreadcrumbUtils.getAipBreadcrumbs(response.getIndexedAIP()));
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

  interface MyUiBinder extends UiBinder<Widget, NavigationToolbar> {
  }
}
