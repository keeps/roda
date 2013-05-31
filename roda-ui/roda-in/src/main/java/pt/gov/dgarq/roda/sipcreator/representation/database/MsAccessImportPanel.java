/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator.representation.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.convert.db.model.exception.ModuleException;
import pt.gov.dgarq.roda.common.convert.db.modules.DatabaseImportModule;
import pt.gov.dgarq.roda.common.convert.db.modules.msAccess.in.MsAccessImportModule;
import pt.gov.dgarq.roda.sipcreator.Messages;
import pt.gov.dgarq.roda.sipcreator.Tools;
import pt.gov.dgarq.roda.sipcreator.representation.FileNameExtensionFilter;

/**
 * @author Luis Faria
 * 
 */
public class MsAccessImportPanel extends JPanel implements DbmsImportPanel {

	private static final long serialVersionUID = -4274646621385563502L;

	private static final Logger logger = Logger
			.getLogger(MsAccessImportPanel.class);

	private JTextField msAccessFileLocation = null;
	private JButton browseButton = null;
	private JFileChooser fileChooser = null;
	private File msAccessFile = null;

	private JLabel info = null;

	/**
	 * Create new Microsoft Access import panel
	 */
	public MsAccessImportPanel() {
		super();
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		add(getMsAccessFileLocation(), BorderLayout.CENTER);
		add(getBrowseButton(), BorderLayout.EAST);
		add(getInfo(), BorderLayout.SOUTH);

	}

	private JTextField getMsAccessFileLocation() {
		if (msAccessFileLocation == null) {
			msAccessFileLocation = new JTextField(10);
			msAccessFileLocation.setEditable(false);
		}
		return msAccessFileLocation;
	}

	private JButton getBrowseButton() {
		if (browseButton == null) {
			browseButton = new JButton(
					Messages
							.getString("Creator.relational_database.import.access.action.BROWSE"));
			browseButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					int result = getFileChooser().showOpenDialog(
							MsAccessImportPanel.this);
					File selectedFile = fileChooser.getSelectedFile();

					if (result == JFileChooser.APPROVE_OPTION) {
						msAccessFile = selectedFile;
						msAccessFileLocation.setText(msAccessFile.getName());
					}
				}

			});
		}
		return browseButton;
	}

	private JFileChooser getFileChooser() {
		if (fileChooser == null) {
			fileChooser = new JFileChooser();
			fileChooser
					.setDialogTitle(Messages
							.getString("Creator.relational_database.import.access.filedialog.TITLE"));
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setCurrentDirectory(new File("."));
			fileChooser
					.setFileFilter(new FileNameExtensionFilter(
							Messages
									.getString("Creator.relational_database.import.access.filedialog.FILTER"),
							"mdb"));

		}
		return fileChooser;
	}

	private JLabel getInfo() {
		if (info == null) {
			info = new JLabel(
					Messages
							.getString("Creator.relational_database.import.access.info"));
			info.setIcon(Tools.createImageIcon("/pt/gov/dgarq/roda/sipcreator/info.png"));
			info.addMouseListener(new MouseListener() {

				public void mouseClicked(MouseEvent e) {
					JDialog dialog = new JDialog();
					JEditorPane editorPane = new JEditorPane();
					URL resource = getClass()
							.getResource(
									"/pt/gov/dgarq/roda/sipcreator/MSAccessManual/index.html");
					try {
						editorPane.setPage(resource);
					} catch (IOException e1) {
						logger.error("Error showing MS Access info page", e1);
					}
					editorPane.setEditable(false);
					JScrollPane scroll = new JScrollPane(editorPane);
					scroll.setPreferredSize(new Dimension(900, 750));
					dialog.add(scroll);
					dialog.pack();
					dialog.setLocationRelativeTo(null);
					dialog.setVisible(true);
				}

				public void mouseEntered(MouseEvent e) {
					// nothing to do

				}

				public void mouseExited(MouseEvent e) {
					// nothing to do

				}

				public void mousePressed(MouseEvent e) {
					// nothing to do

				}

				public void mouseReleased(MouseEvent e) {
					// nothing to do

				}

			});
		}
		return info;
	}

	/**
	 * Get database import module
	 * 
	 * @return the database import module
	 * @throws ModuleException
	 */
	public DatabaseImportModule getDatabaseImportModule()
			throws ModuleException {
		DatabaseImportModule importModule = null;
		if (isInfoValid()) {
			importModule = new MsAccessImportModule(msAccessFile);
		}

		return importModule;
	}

	/**
	 * Get panel
	 * 
	 * @return the panel
	 */
	public JPanel getPanel() {
		return this;
	}

	/**
	 * Is information valid
	 * 
	 * @return true if valid
	 */
	public boolean isInfoValid() {
		return msAccessFile != null;
	}

	/**
	 * Title that will appear in selection combo box
	 * 
	 * @return the title
	 */
	public String toString() {
		return Messages
				.getString("Creator.relational_database.import.access.TITLE");
	}

}
