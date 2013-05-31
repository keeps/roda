package pt.gov.dgarq.roda.sipcreator.representation.text;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.rtf.RTFEditorKit;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.ingest.siputility.builders.RepresentationBuilder;
import pt.gov.dgarq.roda.ingest.siputility.builders.StructuredTextRepresentationBuilder;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;
import pt.gov.dgarq.roda.sipcreator.Messages;
import pt.gov.dgarq.roda.sipcreator.representation.FileNameExtensionFilter;
import pt.gov.dgarq.roda.sipcreator.representation.InvalidRepresentationException;
import pt.gov.dgarq.roda.sipcreator.representation.SingleFileRepresentationPanel;
import pt.gov.dgarq.roda.sipcreator.representation.SubTypeOption;

import com.adobe.acrobat.Viewer;
import com.adobe.acrobat.ViewerCommand;

/**
 * @author Rui Castro
 * @author Luis Faria
 */
public class STRepresentationPanel extends SingleFileRepresentationPanel {
	private static final long serialVersionUID = 9157864610260356575L;

	private static final Logger logger = Logger
			.getLogger(STRepresentationPanel.class);

	/**
	 * Constructs a new {@link STRepresentationPanel}.s
	 * 
	 * @param rObject
	 * 
	 * @throws InvalidRepresentationException
	 */
	public STRepresentationPanel(SIPRepresentationObject rObject)
			throws InvalidRepresentationException {
		super(rObject);
	}

	/**
	 * @see SingleFileRepresentationPanel#getRepresentationFileFilter()
	 */
	@Override
	public FileFilter getRepresentationFileFilter() {
		return new FileNameExtensionFilter(Messages
				.getString("Creator.structured_text.filedialog.FILTER"), "pdf",
				"doc", "docx", "odt", "rtf", "txt");
	}

	/**
	 * @see SingleFileRepresentationPanel#getTitle()
	 */
	@Override
	public String getTitle() {
		return Messages.getString("Creator.structured_text.TITLE");
	}

	@Override
	protected List<SubTypeOption> getSubTypeOptions() {
		List<SubTypeOption> options = new ArrayList<SubTypeOption>();
		options.add(new SubTypeOption("application/pdf", Messages
				.getString("Creator.structured_text.subtype.PDF")));
		options.add(new SubTypeOption("application/msword", Messages
				.getString("Creator.structured_text.subtype.MS_WORD")));

		options
				.add(new SubTypeOption(
						"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
						Messages
								.getString("Creator.structured_text.subtype.MS_OPEN_XML")));
		options.add(new SubTypeOption(
				"application/vnd.oasis.opendocument.text", Messages
						.getString("Creator.structured_text.subtype.OO_TEXT")));
		options.add(new SubTypeOption("application/rtf", Messages
				.getString("Creator.structured_text.subtype.RTF")));
		options.add(new SubTypeOption("text/plain", Messages
				.getString("Creator.structured_text.subtype.TXT")));

		return options;
	}

	@Override
	protected Component getPreview() {
		Component ret = null;

		String subtype = getRepresentationObject().getSubType();
		if (subtype == null) {
			ret = null;
		} else if (subtype.equals("application/pdf")) {
			ret = getAcrobatViewer();
		} else if (subtype.equals("application/rtf")) {
			ret = getRtfViewer();
		} else if (subtype.equals("text/plain")) {
			ret = getTxtViewer();
		}

		return ret;
	}

	private Viewer acrobat = null;

	protected Viewer getAcrobatViewer() {
		if (acrobat == null) {
			try {
				// Create acrobat fonts
				File acrobatFonts = new File(System.getProperty("user.home"),
						"AcrobatFonts");
				if (!acrobatFonts.exists()) {
					acrobatFonts.mkdir();
				}

				// Create acrobat viewer
				acrobat = new Viewer(new String[] { ViewerCommand.Open_K,
						ViewerCommand.OpenURL_K });
				acrobat.setProperty("Default_Zoom_Type", "FitWidth");
				acrobat.setProperty("Substitution_Fonts", "Sans&Serif");
			} catch (Exception e) {
				logger.error("Error getting acrobat", e);
			}
		}
		return acrobat;
	}

	private JEditorPane rtfPane = null;

	private JEditorPane getRtfPane() {
		if (rtfPane == null) {
			rtfPane = new JEditorPane();
			rtfPane.setEditorKit(new RTFEditorKit());
			rtfPane.setEditable(false);
		}
		return rtfPane;
	}

	private JScrollPane rtfScrollPane = null;

	protected JScrollPane getRtfViewer() {
		if (rtfScrollPane == null) {
			rtfScrollPane = new JScrollPane(getRtfPane());
		}
		return rtfScrollPane;
	}

	private JTextArea txtPane = null;

	private JTextArea getTxtPane() {
		if (txtPane == null) {
			txtPane = new JTextArea();
			txtPane.setEditable(false);
		}
		return txtPane;
	}

	private JScrollPane txtScrollPane = null;

	protected JScrollPane getTxtViewer() {
		if (txtScrollPane == null) {
			txtScrollPane = new JScrollPane(getTxtPane());
		}
		return txtScrollPane;
	}

	protected void startPreview() {
		logger.debug("Starting ST preview");
		RepresentationFile rootRepFile = getRepresentationObject()
				.getRootFile();
		if (rootRepFile == null) {
			logger.debug("Root rep file doesn't exist, aborting preview");
			return;
		}
		final File rootFile = new File(URI.create(rootRepFile.getAccessURL()));

		String subtype = getRepresentationObject().getSubType();
		if (subtype == null) {
			// do nothing
		} else if (subtype.equals("application/pdf")) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					try {
						logger.debug("Setting acrobat viewer input stream");
						getAcrobatViewer().setDocumentInputStream(
								new FileInputStream(rootFile));
						logger.debug("Activating acrobat viewer");
						getAcrobatViewer().activate();
					} catch (FileNotFoundException e) {
						logger.error("Error starting preview", e);
					} catch (Exception e) {
						logger.error("Error starting preview", e);
					}
				}

			});
		} else if (subtype.equals("application/rtf")) {
			try {
				getRtfPane().read(new FileInputStream(rootFile),
						rootRepFile.getOriginalName());
			} catch (FileNotFoundException e) {
				logger.error("Error starting preview", e);
			} catch (IOException e) {
				logger.error("Error starting preview", e);
			}
		} else if (subtype.equals("text/plain")) {
			try {
				StringBuffer fileData = new StringBuffer(1000);
				BufferedReader reader = new BufferedReader(new FileReader(
						rootFile));
				char[] buf = new char[1024];
				int numRead = 0;
				while ((numRead = reader.read(buf)) != -1) {
					String readData = String.valueOf(buf, 0, numRead);
					fileData.append(readData);
					buf = new char[1024];
				}
				reader.close();
				getTxtPane().setText(fileData.toString());
			} catch (FileNotFoundException e) {
				logger.error("Error starting preview", e);
			} catch (IOException e) {
				logger.error("Error starting preview", e);
			}
		}
	}

	protected void stopPreview() {
		logger.debug("Stoping preview");
		String subtype = getRepresentationObject().getSubType();
		if (subtype == null) {
			// nothing to do
		} else if (subtype.equals("application/pdf")) {
			getAcrobatViewer().deactivate();
		} else if (subtype.equals("application/rtf")) {
			// nothing to do (?)
		} else if (subtype.equals("text/plain")) {
			// nothing to do
		}
	}

	@Override
	protected RepresentationBuilder getRepresentationBuilder() {
		return new StructuredTextRepresentationBuilder();
	}

	@Override
	protected Component getMetadata() {
		return null;
	}

}
