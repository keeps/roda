package pt.gov.dgarq.roda.migrator.services;

import pt.gov.dgarq.roda.core.common.RODAServiceException;

/**
 * @author Luis Faria
 * 
 */
public class Audio2Mp3 extends SoundConverter {
	/**
	 * Create a new servlet that converts any gstreamer supported audio to MP3
	 * 
	 * @throws RODAServiceException
	 */
	public Audio2Mp3() throws RODAServiceException {
		super();
		soundconverterFormat = "audio/mpeg";
		formatExtension = ".mp3";
	}
}
