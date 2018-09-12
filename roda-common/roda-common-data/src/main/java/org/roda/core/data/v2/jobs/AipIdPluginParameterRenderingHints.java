package org.roda.core.data.v2.jobs;

import org.roda.core.data.v2.index.filter.Filter;

public class AipIdPluginParameterRenderingHints implements RenderingHints {
  private static final long serialVersionUID = -8331898217589161894L;

  private Filter filter;
  private boolean justActive, exportCsvVisible;

  /**
   * Necessary for GWT serialization
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
  public void setFilter(Filter filter) {
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
  public void setJustActive(boolean justActive) {
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
  public void setExportCsvVisible(boolean exportCsvVisible) {
    this.exportCsvVisible = exportCsvVisible;
  }

}
