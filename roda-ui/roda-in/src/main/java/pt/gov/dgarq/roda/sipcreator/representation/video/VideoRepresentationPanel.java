package pt.gov.dgarq.roda.sipcreator.representation.video;

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
import pt.gov.dgarq.roda.ingest.siputility.builders.RepresentationBuilder;
import pt.gov.dgarq.roda.ingest.siputility.builders.VideoRepresentationBuilder;
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
public class VideoRepresentationPanel extends SingleFileRepresentationPanel {
	private static final long serialVersionUID = -3091038272905382637L;

	private static final Logger logger = Logger
			.getLogger(VideoRepresentationPanel.class);

	/**
	 * Constructs a new {@link VideoRepresentationPanel}.s
	 * 
	 * @param rObject
	 * 
	 * @throws InvalidRepresentationException
	 */
	public VideoRepresentationPanel(SIPRepresentationObject rObject)
			throws InvalidRepresentationException {
		super(rObject);
	}

	/**
	 * @see SingleFileRepresentationPanel#getRepresentationFileFilter()
	 */
	@Override
	public FileFilter getRepresentationFileFilter() {
		return new FileNameExtensionFilter(Messages
				.getString("Creator.video.filedialog.FILTER"), "mpg", "mpeg",
				"vob", "mpv2", "mp2v", "mp4", "avi", "mov", "qt");
	}

	/**
	 * @see SingleFileRepresentationPanel#getTitle()
	 */
	@Override
	public String getTitle() {
		return Messages.getString("Creator.video.TITLE");
	}

	@Override
	protected List<SubTypeOption> getSubTypeOptions() {
		List<SubTypeOption> options = new ArrayList<SubTypeOption>();
		options.add(new SubTypeOption("video/mpeg", Messages
				.getString("Creator.video.subtype.MPEG")));
		options.add(new SubTypeOption("video/mpeg2", Messages
				.getString("Creator.video.subtype.MPEG2")));
		options.add(new SubTypeOption("video/mp4", Messages
				.getString("Creator.video.subtype.MPEG4")));
		options.add(new SubTypeOption("video/avi", Messages
				.getString("Creator.video.subtype.AVI")));
		options.add(new SubTypeOption("video/x-ms-wmv", Messages
				.getString("Creator.video.subtype.WMV")));
		options.add(new SubTypeOption("video/quicktime", Messages
				.getString("Creator.video.subtype.QT")));

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
		if (subtype.equals("video/avi") || subtype.equals("video/mpeg")
				|| subtype.equals("video/mpeg2") || subtype.equals("video/mp4")) {
			if (rootRepFile.getSize() < 100 * 1024 * 1024) {
				JPanel panel = new JPanel(new BorderLayout());
				panel.add(getMP4PlayerControl().getRendererComponent(),
						BorderLayout.CENTER);
				panel.add(getMP4ControlPanel(), BorderLayout.SOUTH);

				ret = panel;
			} else {
				getPreviewPanel().preview(
						Messages.getString(
								"Creator.video.VIDEO_TOO_BIG_TO_PREVIEW",
								FileUtils.byteCountToDisplaySize(rootRepFile
										.getSize())));
			}

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
		final RepresentationFile rootRepFile = getRepresentationObject()
				.getRootFile();
		if (rootRepFile == null) {
			return;
		}
		String subtype = getRepresentationObject().getSubType();
		if (subtype.equals("video/avi") || subtype.equals("video/mpeg")
				|| subtype.equals("video/mpeg2") || subtype.equals("video/mp4")) {
			if (rootRepFile.getSize() < 100 * 1024 * 1024) {
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

	}

	protected void stopPreview() {
		String subtype = getRepresentationObject().getSubType();
		if (subtype != null
				&& (subtype.equals("video/avi") || subtype.equals("video/mpeg")
						|| subtype.equals("video/mpeg2") || subtype
						.equals("video/mp4"))) {
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
		return new VideoRepresentationBuilder();
	}

	@Override
	protected Component getMetadata() {
		return null;
	}

}
