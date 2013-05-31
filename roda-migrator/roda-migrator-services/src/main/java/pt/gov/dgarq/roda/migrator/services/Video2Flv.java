/**
 * 
 */
package pt.gov.dgarq.roda.migrator.services;

import java.util.Arrays;
import java.util.List;

import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.migrator.common.ConverterException;
import pt.gov.dgarq.roda.util.CommandException;
import pt.gov.dgarq.roda.util.CommandUtility;

/**
 * Convert video to Flash Video
 * 
 * Converting video with codec FLV and sound to MP3, resizing to 320x240,
 * modifying audio and video bitrate.
 * 
 * mencoder <source> -o <target>.flv -of lavf -ovc lavc -oac lavc -lavcopts
 * vcodec=flv:vbitrate=250:autoaspect:mbd=2:mv0:trell:v4mv:cbp:last_pred=3:
 * predia=2:dia=2:precmp=2:cmp=2:subcmp=2:preme=2:turbo:acodec=libmp3lame:
 * abitrate=56 -vf scale=320:240 -srate 22050 -af lavcresample=22050 -endpos
 * 02:00
 * 
 * @author Luis Faria
 * 
 */
public class Video2Flv extends FFmpegConverter {

	/**
	 * Create a new video2flv convertion service
	 * 
	 * @throws RODAServiceException
	 */
	public Video2Flv() throws RODAServiceException {
		targetExtension = ".flv";
	}

	@Override
	protected List<String> getOptions(RepresentationObject representation) {
		return Arrays.asList(new String[] { "-deinterlace", "-ar", "44100",
				"-r", "25", "-qmin", "3", "-qmax", "6", "-s", "320x240", "-y",
				"-v", "0" });
	}

	@Override
	protected String getMimeType() {
		return "video/x-flv";
	}

}
