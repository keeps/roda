package pt.gov.dgarq.roda.sipcreator.representation.database;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import pt.gov.dgarq.roda.common.convert.db.model.exception.ModuleException;
import pt.gov.dgarq.roda.common.convert.db.modules.DatabaseImportModule;
import pt.gov.dgarq.roda.common.convert.db.modules.odbc.in.ODBCImportModule;
import pt.gov.dgarq.roda.sipcreator.Messages;

/**
 * 
 * @author Luis Faria
 * 
 */
public class OdbcImportPanel extends JPanel implements DbmsImportPanel {

	private static final long serialVersionUID = 5066185199036122302L;

	private JPanel odbcSourcePanel = null;
	private JLabel odbcSourceLabel = null;
	private JTextField odbcSourceField = null;

	/**
	 * Create new ODBC import panel
	 */
	public OdbcImportPanel() {
		super();
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		add(getOdbcSourcePanel(), BorderLayout.CENTER);

	}

	private JPanel getOdbcSourcePanel() {
		if (odbcSourcePanel == null) {
			odbcSourcePanel = new JPanel();
			odbcSourcePanel.add(getOdbcSourceLabel());
			odbcSourcePanel.add(getOdbcSourceField());
		}
		return odbcSourcePanel;
	}

	private JLabel getOdbcSourceLabel() {
		if (odbcSourceLabel == null) {
			odbcSourceLabel = new JLabel(
					Messages
							.getString("Creator.relational_database.import.odbc.SOURCE"));
		}
		return odbcSourceLabel;
	}

	private JTextField getOdbcSourceField() {
		if (odbcSourceField == null) {
			odbcSourceField = new JTextField(15);
		}
		return odbcSourceField;
	}

	/**
	 * Get database import module
	 * 
	 * @return the database import
	 * @throws ModuleException
	 */
	public DatabaseImportModule getDatabaseImportModule()
			throws ModuleException {
		DatabaseImportModule importModule = null;
		if (isInfoValid()) {
			String source = getOdbcSourceField().getText();
			importModule = new ODBCImportModule(source);
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
		return getOdbcSourceField().getText().length() > 0;
	}

	/**
	 * Title that will appear in DBMS selection combo box
	 */
	@Override
	public String toString() {
		return Messages
				.getString("Creator.relational_database.import.odbc.TITLE");
	}

}
