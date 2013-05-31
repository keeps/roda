package pt.gov.dgarq.roda.plugins.converters.video;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.plugins.converters.common.AbstractRodaMigratorPlugin;
import pt.gov.dgarq.roda.plugins.converters.common.RepresentationConverterException;

/**
 * @author Rui Castro
 */
public class Mpeg22Mpeg2ConverterPlugin extends AbstractRodaMigratorPlugin {
	private static Logger logger = Logger
			.getLogger(Mpeg22Mpeg2ConverterPlugin.class);

	private final String name = "Converter/MPEG2 to MPEG2";
	private final float version = 1.0f;
	private final String description = "Video MPEG2 representations normalizer plugin. "
			+ "Convert Video representations from MPEG2 to MPEG2 using RODA Migrator service Video2DVD.";

	/**
	 * 
	 * @see Plugin#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @see Plugin#getVersion()
	 */
	public float getVersion() {
		return version;
	}

	/**
	 * @see Plugin#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	@Override
	protected String getConfigurationFile() {
		return "mpeg22mpeg2-converter.properties";
	}

	/**
	 * 
	 * @see AbstractRodaMigratorPlugin#getRepresentationType ()
	 */
	@Override
	protected String getRepresentationType() {
		return RepresentationObject.VIDEO;
	}

	/**
	 * 
	 * @see AbstractRodaMigratorPlugin#isNormalization()
	 */
	@Override
	protected boolean isNormalization() {
		return true;
	}

	/**
	 * @see AbstractRodaMigratorPlugin#isRepresentationConverted(RepresentationObject)
	 */
	@Override
	public boolean isRepresentationConverted(RepresentationObject object)
			throws RepresentationConverterException {
		return true;
	}

}
