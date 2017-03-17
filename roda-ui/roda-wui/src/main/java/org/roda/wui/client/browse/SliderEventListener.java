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
@FunctionalInterface
public interface SliderEventListener {

  /**
   * Slider moved
   * 
   * @param value
   *          the new value
   * @param size
   *          the slider max size
   */
  public void onSliderMove(int value, int size);

}
