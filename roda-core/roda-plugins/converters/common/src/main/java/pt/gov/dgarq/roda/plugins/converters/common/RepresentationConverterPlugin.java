package pt.gov.dgarq.roda.plugins.converters.common;

import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.plugins.AbstractPlugin;
import pt.gov.dgarq.roda.migrator.common.data.ConversionResult;

/**
 * This is the base class for all RODA representation converters.
 * 
 * @author Rui Castro
 * @author Luis Faria
 */
public abstract class RepresentationConverterPlugin extends AbstractPlugin {

	/**
	 * Converts a {@link RepresentationObject}.
	 * 
	 * @param rObject
	 *            the representation to convert.
	 * 
	 * @return a {@link RepresentationConvertResult}
	 * 
	 * @throws RepresentationConverterException
	 */
	public abstract ConversionResult convert(RepresentationObject rObject)
			throws RepresentationConverterException;

	/**
	 * Verifies if the specified representation is already in the converted
	 * format.
	 * 
	 * @param rObject
	 *            the {@link RepresentationObject} to verify.
	 * @return
	 *         <code>true</core> if the representation is already converted, <code>false</core> otherwise.
	 */
	public abstract boolean isRepresentationConverted(
			RepresentationObject rObject)
			throws RepresentationConverterException;
}
