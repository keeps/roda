/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator.representation.database;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import pt.gov.dgarq.roda.common.convert.db.model.exception.ModuleException;
import pt.gov.dgarq.roda.common.convert.db.modules.DatabaseImportModule;
import pt.gov.dgarq.roda.common.convert.db.modules.dbml.in.DBMLImportModule;
import pt.gov.dgarq.roda.sipcreator.Messages;

/**
 * @author Luis Faria
 * 
 */
public class DbmlImportPanel extends JPanel implements DbmsImportPanel {
	private static final long serialVersionUID = 4462230624968774761L;

	private JTextField dbmlFileLocation = null;
	private JButton browseButton = null;
	private JFileChooser fileChooser = null;
	private File dbmlDir = null;

	/**
	 * Create DBML Import panel
	 */
	public DbmlImportPanel() {
		super();
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		add(getDbmlFileLocation(), BorderLayout.CENTER);
		add(getBrowseButton(), BorderLayout.EAST);

	}

	private JTextField getDbmlFileLocation() {
		if (dbmlFileLocation == null) {
			dbmlFileLocation = new JTextField(10);
			dbmlFileLocation.setEditable(false);
		}
		return dbmlFileLocation;
	}

	private JButton getBrowseButton() {
		if (browseButton == null) {
			browseButton = new JButton(
					Messages
							.getString("Creator.relational_database.import.dbml.BROWSE"));
			browseButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					int result = getFileChooser().showOpenDialog(
							DbmlImportPanel.this);
					File selectedFile = fileChooser.getSelectedFile();

					if (result == JFileChooser.APPROVE_OPTION) {
						dbmlDir = selectedFile;
						dbmlFileLocation.setText(dbmlDir.getName());
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
							.getString("Creator.relational_database.import.dbml.filedialog.TITLE"));
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setCurrentDirectory(new File("."));

		}
		return fileChooser;
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
			importModule = new DBMLImportModule(dbmlDir);
		}

		return importModule;
	}

	/**
	 * @return the panel
	 * 
	 */
	public JPanel getPanel() {
		return this;
	}

	/**
	 * Check if information is valid
	 * 
	 * @return true if valid
	 * 
	 */
	public boolean isInfoValid() {
		return dbmlDir != null
				&& dbmlDir.list() != null
				&& dbmlDir.list().length > 0
				&& Arrays.asList(dbmlDir.list()).contains(
						DBMLImportModule.DBML_DEFAULT_FILE_NAME);
	}

	/**
	 * @return the string representation that will appear in the selection combo
	 *         box
	 * 
	 */
	@Override
	public String toString() {
		return Messages
				.getString("Creator.relational_database.import.dbml.TITLE");
	}

}
