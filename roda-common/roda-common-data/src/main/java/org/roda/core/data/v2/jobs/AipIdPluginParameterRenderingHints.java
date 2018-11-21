/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

import org.roda.core.data.v2.index.filter.Filter;

/**
 * Renderinghints for plugin to customize gwt dialog.
 */
public class AipIdPluginParameterRenderingHints implements RenderingHints {
  private static final long serialVersionUID = -8331898217589161894L;

  /**
   * Returns the search filter.
   */
  private Filter filter;
  /**
   * Determines if just active aips is to be shown in result list
   * determines if export csv button is visible
   */
  private boolean justActive, exportCsvVisible;
  /**
   * Returns customized button label if plugin sets one, otherwise
   * returns null and in that case the gwt dialog will show default label.
   */
  private String customizedButtonLabel = null;
  /**
   * If set to true gwt dialog will work as a preview and doesn't have the possibility to select items in result list.
   */
  private boolean disableSelection = false;

  /**
   * Necessary for GWT serialization.
   */
  public AipIdPluginParameterRenderingHints() {

  }

  /**
   * @return the filter
   */
  public Filter getFilter() {
    return filter;
  }

  /**
   * @param filter
   *          the filter to set
   */
  public void setFilter(final Filter filter) {
    this.filter = filter;
  }

  /**
   * @return the justActive
   */
  public boolean isJustActive() {
    return justActive;
  }

  /**
   * @param justActive
   *          the justActive to set
   */
  public void setJustActive(final boolean justActive) {
    this.justActive = justActive;
  }

  /**
   * @return the exportCsvVisible
   */
  public boolean isExportCsvVisible() {
    return exportCsvVisible;
  }

  /**
   * @param exportCsvVisible
   *          the exportCsvVisible to set
   */
  public void setExportCsvVisible(final boolean exportCsvVisible) {
    this.exportCsvVisible = exportCsvVisible;
  }

  public String getCustomizedButtonLabel() {
    return customizedButtonLabel;
  }

  /**
   * @param customizedButtonLabel
   *          add customized buttonLabel for plugin
   */
  public void setCustomizedButtonLabel(final String customizedButtonLabel) {
    this.customizedButtonLabel = customizedButtonLabel;
  }

  public boolean isDisableSelection() {
    return disableSelection;
  }

  /**
   * @param disableSelection
   *          true if dialog is used only for preview
   */
  public void setDisableSelection(final boolean disableSelection) {
    this.disableSelection = disableSelection;
  }
}
