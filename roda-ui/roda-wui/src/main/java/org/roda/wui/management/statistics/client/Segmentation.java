/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.management.statistics.client;

import java.io.Serializable;

/**
 * Time segmentation where to aggregate values
 * 
 * @author Luis Faria
 * 
 */
public enum Segmentation implements Serializable {
  /**
   * Per year segmentation
   */
  YEAR,
  /**
   * Per month segmentation
   */
  MONTH,
  /**
   * Per day segmentation
   */
  DAY
}