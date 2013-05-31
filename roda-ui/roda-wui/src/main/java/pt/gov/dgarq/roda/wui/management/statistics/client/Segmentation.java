package pt.gov.dgarq.roda.wui.management.statistics.client;

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