/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator.representation.database;

import java.awt.Checkbox;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import pt.gov.dgarq.roda.common.convert.db.modules.DatabaseImportModule;
import pt.gov.dgarq.roda.common.convert.db.modules.sqlServer.in.SQLServerJDBCImportModule;
import pt.gov.dgarq.roda.sipcreator.Messages;
import pt.gov.dgarq.roda.sipcreator.SpringUtilities;

/**
 * @author Luis Faria
 * 
 */
public class SqlServerImportPanel extends JPanel implements DbmsImportPanel {

	private static final long serialVersionUID = 5926335813897041196L;

	private JPanel formPanel = null;
	private JLabel serverNameLabel = null;
	private JTextField serverNameValue = null;
	private JLabel instanceLabel = null;
	private JTextField instanceValue = null;
	private JLabel databaseLabel = null;
	private JTextField databaseValue = null;
	private JLabel usernameLabel = null;
	private JTextField usernameValue = null;
	private JLabel passwordLabel = null;
	private JPasswordField passwordValue = null;

	private JPanel options = null;
	private Checkbox integratedSecurity = null;
	private Checkbox encrypt = null;

	/**
	 * Create a new SQL Server import panel
	 */
	public SqlServerImportPanel() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		formPanel = new JPanel(new SpringLayout());

		serverNameLabel = new JLabel(
				Messages
						.getString("Creator.relational_database.import.sqlserver.HOST"),
				JLabel.TRAILING);
		instanceLabel = new JLabel(
				Messages
						.getString("Creator.relational_database.import.sqlserver.INSTANCE"),
				JLabel.TRAILING);
		databaseLabel = new JLabel(
				Messages
						.getString("Creator.relational_database.import.sqlserver.DATABASE"),
				JLabel.TRAILING);
		usernameLabel = new JLabel(
				Messages
						.getString("Creator.relational_database.import.sqlserver.USERNAME"),
				JLabel.TRAILING);
		passwordLabel = new JLabel(
				Messages
						.getString("Creator.relational_database.import.sqlserver.PASSWORD"),
				JLabel.TRAILING);

		serverNameValue = new JTextField("localhost", 15);
		instanceValue = new JTextField(15);
		databaseValue = new JTextField(15);
		usernameValue = new JTextField("sa", 15);
		passwordValue = new JPasswordField(15);

		serverNameLabel.setLabelFor(serverNameValue);
		instanceLabel.setLabelFor(instanceValue);
		databaseLabel.setLabelFor(databaseValue);
		usernameLabel.setLabelFor(usernameValue);
		passwordLabel.setLabelFor(passwordValue);

		formPanel.add(serverNameLabel);
		formPanel.add(serverNameValue);
		formPanel.add(instanceLabel);
		formPanel.add(instanceValue);
		formPanel.add(databaseLabel);
		formPanel.add(databaseValue);
		formPanel.add(usernameLabel);
		formPanel.add(usernameValue);
		formPanel.add(passwordLabel);
		formPanel.add(passwordValue);

		SpringUtilities.makeCompactGrid(formPanel, 5, 2, 5, 5, 5, 5);

		options = new JPanel();

		options.setLayout(new SpringLayout());
		integratedSecurity = new Checkbox(
				Messages
						.getString("Creator.relational_database.import.sqlserver.INTEGRATED_SECURITY"));
		encrypt = new Checkbox(
				Messages
						.getString("Creator.relational_database.import.sqlserver.USE_ENCRYPTION"));

		options.add(integratedSecurity);
		options.add(encrypt);

		SpringUtilities.makeCompactGrid(options, 2, 1, 5, 5, 5, 5);

		add(formPanel);
		add(options);

	}

	/**
	 * Get database import module
	 * 
	 * @return the database import module
	 */
	public DatabaseImportModule getDatabaseImportModule() {
		DatabaseImportModule ret = null;
		if (isInfoValid()) {
			String serverName = serverNameValue.getText();
			String instanceName = instanceValue.getText();
			String database = databaseValue.getText();
			String username = usernameValue.getText();
			String password = new String(passwordValue.getPassword());
			boolean integratedSecurity = this.integratedSecurity.getState();
			boolean encrypt = this.encrypt.getState();
			ret = new SQLServerJDBCImportModule(serverName, instanceName,
					database, username, password, integratedSecurity, encrypt);
		}
		return ret;
	}

	/**
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
		boolean valid = true;
		String serverName = serverNameValue.getText();
		String instanceName = instanceValue.getText();
		String database = databaseValue.getText();
		String username = usernameValue.getText();
		String password = new String(passwordValue.getPassword());

		valid &= serverName.length() > 0;
		valid &= instanceName.length() > 0;
		valid &= database.length() > 0;
		valid &= username.length() > 0;
		valid &= password.length() > 0;

		return valid;
	}

	/**
	 * Title that will appear in DBMS selection combo box
	 * 
	 * @return the title
	 */
	public String toString() {
		return Messages
				.getString("Creator.relational_database.import.sqlserver.TITLE");
	}

}
