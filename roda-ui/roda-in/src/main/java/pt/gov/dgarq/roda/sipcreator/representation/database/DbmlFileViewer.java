package pt.gov.dgarq.roda.sipcreator.representation.database;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;
import pt.gov.dgarq.roda.sipcreator.Messages;

/**
 * DBML files viewer
 * 
 * @author Luis Faria
 * 
 */
public class DbmlFileViewer extends JPanel {

	private static final long serialVersionUID = 6250761888718047321L;

	private final SIPRepresentationObject sipRepObj;

	private JLabel title = null;
	private JList fileList = null;
	private JScrollPane fileListScroll = null;

	/**
	 * Create a new DBML file viewer
	 * 
	 * @param sipRepObj
	 */
	public DbmlFileViewer(SIPRepresentationObject sipRepObj) {
		this.sipRepObj = sipRepObj;
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		initContents();
	}

	private void initContents() {
		add(getTitle(), BorderLayout.NORTH);
		add(getFileListScroll(), BorderLayout.CENTER);
	}

	private JLabel getTitle() {
		if (title == null) {
			title = new JLabel(Messages
					.getString("Creator.relational_database.viewer.TITLE"));
			title.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		}
		return title;
	}

	private JList getFileList() {
		if (fileList == null) {
			List<RepresentationFile> files = new ArrayList<RepresentationFile>();
			files.add(sipRepObj.getRootFile());
			if (sipRepObj.getPartFiles() != null) {
				files.addAll(Arrays.asList(sipRepObj.getPartFiles()));
			}
			List<String> filenames = new ArrayList<String>(files.size());
			for (RepresentationFile file : files) {
				filenames.add(file.getOriginalName());
			}

			fileList = new JList(filenames.toArray());
		}
		return fileList;
	}

	private JScrollPane getFileListScroll() {
		if (fileListScroll == null) {
			fileListScroll = new JScrollPane(getFileList());
		}
		return fileListScroll;
	}

}
