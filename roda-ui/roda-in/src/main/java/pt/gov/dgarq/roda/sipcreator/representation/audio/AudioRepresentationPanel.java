package pt.gov.dgarq.roda.sipcreator.representation.audio;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import player.PlayerControl;
import player.PlayerControlPanel;
import player.PlayerControlPanelFactory;
import player.PlayerFactory;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.ingest.siputility.builders.AudioRepresentationBuilder;
import pt.gov.dgarq.roda.ingest.siputility.builders.RepresentationBuilder;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;
import pt.gov.dgarq.roda.sipcreator.Messages;
import pt.gov.dgarq.roda.sipcreator.SIPCreatorConfig;
import pt.gov.dgarq.roda.sipcreator.representation.FileNameExtensionFilter;
import pt.gov.dgarq.roda.sipcreator.representation.InvalidRepresentationException;
import pt.gov.dgarq.roda.sipcreator.representation.SingleFileRepresentationPanel;
import pt.gov.dgarq.roda.sipcreator.representation.SubTypeOption;

/**
 * @author Rui Castro
 * @author Luis Faria
 */
public class AudioRepresentationPanel extends SingleFileRepresentationPanel {
	private static final long serialVersionUID = 2561921981349152973L;

	private static final Logger logger = Logger
			.getLogger(AudioRepresentationPanel.class);

	/**
	 * Constructs a new {@link AudioRepresentationPanel}.s
	 * 
	 * @param rObject
	 * 
	 * @throws InvalidRepresentationException
	 */
	public AudioRepresentationPanel(SIPRepresentationObject rObject)
			throws InvalidRepresentationException {
		super(rObject);
	}

	/**
	 * @see SingleFileRepresentationPanel#getRepresentationFileFilter()
	 */
	public FileFilter getRepresentationFileFilter() {
		return new FileNameExtensionFilter(Messages
				.getString("Creator.audio.filedialog.FILTER"), "wav", "mp3",
				"mp4", "flac", "ogg", "aif", "aiff");
	}

	/**
	 * @see SingleFileRepresentationPanel#getTitle()
	 */
	@Override
	public String getTitle() {
		return Messages.getString("Creator.audio.TITLE");
	}

	@Override
	protected List<SubTypeOption> getSubTypeOptions() {
		List<SubTypeOption> options = new ArrayList<SubTypeOption>();
		options.add(new SubTypeOption("audio/wav", Messages
				.getString("Creator.audio.subtype.WAV")));
		options.add(new SubTypeOption("audio/mpeg", Messages
				.getString("Creator.audio.subtype.MP3")));
		options.add(new SubTypeOption("audio/mp4", Messages
				.getString("Creator.audio.subtype.MP4")));
		options.add(new SubTypeOption("audio/flac", Messages
				.getString("Creator.audio.subtype.FLAC")));
		options.add(new SubTypeOption("audio/ogg", Messages
				.getString("Creator.audio.subtype.OGG")));
		options.add(new SubTypeOption("audio/aiff", Messages
				.getString("Creator.audio.subtype.AIFF")));

		return options;
	}

	@Override
	protected Component getPreview() {
		Component ret = null;
		final RepresentationFile rootRepFile = getRepresentationObject()
				.getRootFile();
		if (rootRepFile == null) {
			return null;
		}
		String subtype = getRepresentationObject().getSubType();
		if (subtype.equals("audio/mpeg")) {
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(getMP4ControlPanel(), BorderLayout.CENTER);

			ret = panel;
		}
		return ret;
	}

	private PlayerControl mp4playerControl = null;

	private PlayerControl getMP4PlayerControl() {
		if (mp4playerControl == null) {
			mp4playerControl = PlayerFactory.createMPEG4Player();
			mp4playerControl.setAutoSize(true);
		}
		return mp4playerControl;
	}

	private PlayerControlPanel mp4playerControlPanel = null;

	private PlayerControlPanel getMP4ControlPanel() {
		if (mp4playerControlPanel == null) {
			mp4playerControlPanel = PlayerControlPanelFactory
					.createPlayerControlPanel(PlayerControlPanelFactory
							.getDefaultPanelName(), getMP4PlayerControl());
		}
		return mp4playerControlPanel;
	}

	protected void startPreview() {

		logger.debug("Starting audio preview");
		final RepresentationFile rootRepFile = getRepresentationObject()
				.getRootFile();
		if (rootRepFile == null) {
			return;
		}
		String subtype = getRepresentationObject().getSubType();
		if (subtype.equals("audio/mpeg")) {
			try {
				getMP4PlayerControl().close();
				getMP4PlayerControl().open(
						fileNameExtensionWorkarround(rootRepFile));
				getMP4ControlPanel().attachToPlayer();
				getMP4PlayerControl().start();

			} catch (IllegalStateException e) {
				logger.error("Could not resume player", e);
			} catch (IOException e) {
				logger.error("Could not resume player", e);
			}
		}

	}

	protected void stopPreview() {

		logger.debug("Stopping audio preview");
		String subtype = getRepresentationObject().getSubType();
		if (subtype == null) {
			return;
		} else if (subtype.equals("audio/mpeg")) {
			try {
				getMP4PlayerControl().stop();
				logger.debug("Player stopped");
			} catch (IllegalStateException e) {
				logger.error("Could not pause player", e);
			} catch (IOException e) {
				logger.error("Could not pause player", e);
			}
		}
	}

	/**
	 * The player library needs the original file name extension to work
	 * 
	 * @throws IOException
	 */
	private String fileNameExtensionWorkarround(RepresentationFile rootRepFile)
			throws IOException {
		File ret;
		File rootFile = new File(URI.create(rootRepFile.getAccessURL()));
		if (rootFile.getName().indexOf('.') == -1) {
			File tmpFile = File.createTempFile("video", rootRepFile
					.getOriginalName(), SIPCreatorConfig.getInstance()
					.getTmpDir());
			FileUtils.copyFile(rootFile, tmpFile);
			tmpFile.deleteOnExit();
			ret = tmpFile;

		} else {
			ret = rootFile;
		}

		return ret.getAbsolutePath();
	}

	@Override
	protected RepresentationBuilder getRepresentationBuilder() {
		return new AudioRepresentationBuilder();
	}

	@Override
	protected Component getMetadata() {
		return null;
	}

}
