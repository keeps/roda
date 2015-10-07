/**
 * 
 */
package org.roda.wui.dissemination.browse.client;

/**
 * @author Luis Faria
 * 
 */
public interface SourcesSliderEvents {

  /**
   * Add slider event listener
   * 
   * @param listener
   */
  public void addSliderEventListener(SliderEventListener listener);

  /**
   * Remove slider event listener
   * 
   * @param listener
   */
  public void removeSliderEventListener(SliderEventListener listener);

}
