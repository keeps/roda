/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client;

/**
 * @author Luis Faria
 * 
 */
public interface SliderEventListener {

	/**
	 * Slider moved
	 * 
	 * @param value
	 *            the new value
	 * @param size
	 *            the slider max size
	 */
	public void onSliderMove(int value, int size);

}
