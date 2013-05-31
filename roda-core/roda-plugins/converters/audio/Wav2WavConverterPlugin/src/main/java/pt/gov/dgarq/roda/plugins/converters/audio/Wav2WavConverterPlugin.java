package pt.gov.dgarq.roda.plugins.converters.audio;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.plugins.converters.common.AbstractRodaMigratorPlugin;
import pt.gov.dgarq.roda.plugins.converters.common.RepresentationConverterException;

/**
 * @author Rui Castro
 */
public class Wav2WavConverterPlugin extends AbstractRodaMigratorPlugin {
	private static Logger logger = Logger
			.getLogger(Wav2WavConverterPlugin.class);

	private final String name = "Converter/WAV to WAV";
	private final float version = 1.0f;
	private final String description = "Audio WAV representations normalizer plugin. "
			+ "Convert Audio representations from WAV to WAV using RODA Migrator service Audio2Wav.";

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
		return "wav2wav-converter.properties";
	}

	/**
	 * 
	 * @see AbstractRodaMigratorPlugin#getRepresentationType ()
	 */
	@Override
	protected String getRepresentationType() {
		return RepresentationObject.AUDIO;
	}

	/**
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
