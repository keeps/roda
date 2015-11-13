/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.client.browse;

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
