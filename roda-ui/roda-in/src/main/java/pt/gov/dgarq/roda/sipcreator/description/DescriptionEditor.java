/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator.description;

import java.awt.Component;

import pt.gov.dgarq.roda.core.data.eadc.EadCValue;
import pt.gov.dgarq.roda.ingest.siputility.data.DataChangeListener;

/**
 * @author Luis Faria
 * 
 */
public interface DescriptionEditor {

	/**
	 * Get value defined by editor
	 * 
	 * @return the EAD component value
	 */
	public EadCValue getValue();

	/**
	 * Set value defined by editor
	 * 
	 * @param value
	 */
	public void setValue(EadCValue value);

	/**
	 * Add data changed listener
	 * 
	 * @param listener
	 */
	public void addDataChangedListener(DataChangeListener listener);

	/**
	 * Remove data changed listener
	 * 
	 * @param listener
	 */
	public void removeDataChangedListener(DataChangeListener listener);

	/**
	 * Get editor component
	 * 
	 * @return the visual component
	 */
	public Component getComponent();

	/**
	 * Check if edited value is valid
	 * 
	 * @return true if valid
	 */
	public boolean isValid();

	/**
	 * Check if editor is read only
	 * 
	 * @return true if read only
	 */
	public boolean isReadonly();

	/**
	 * Set editor read only
	 * 
	 * @param readonly
	 */
	public void setReadonly(boolean readonly);

}
