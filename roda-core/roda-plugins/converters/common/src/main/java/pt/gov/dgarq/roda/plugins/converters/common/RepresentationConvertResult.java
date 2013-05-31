package pt.gov.dgarq.roda.plugins.converters.common;

import pt.gov.dgarq.roda.core.data.RepresentationObject;

/**
 * This interface defines the result of a {@link RepresentationConverterPlugin}.
 * 
 * @author Rui Castro
 */
public interface RepresentationConvertResult {

	/**
	 * Returns the converted {@link RepresentationObject}.
	 * 
	 * @return a {@link RepresentationObject}.
	 */
	public RepresentationObject getRepresentation();

	/**
	 * Returns a {@link String} with the details about the conversion.
	 * 
	 * @return a {@link String}.
	 */
	public String getOutcomeDetail();

}
