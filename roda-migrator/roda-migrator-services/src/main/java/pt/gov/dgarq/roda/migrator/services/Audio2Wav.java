package pt.gov.dgarq.roda.migrator.services;

import pt.gov.dgarq.roda.core.common.RODAServiceException;

/**
 * @author Luis Faria
 * 
 */
public class Audio2Wav extends SoundConverter {

	/**
	 * Create a new servlet that converts any gstreamer supported audio to WAV
	 * 
	 * @throws RODAServiceException
	 */
	public Audio2Wav() throws RODAServiceException {
		super();
		soundconverterFormat = "audio/x-wav";
		formatExtension = ".wav";
	}

}
