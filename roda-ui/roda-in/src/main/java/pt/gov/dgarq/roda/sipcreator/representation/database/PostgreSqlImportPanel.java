package pt.gov.dgarq.roda.sipcreator.representation.database;

import java.awt.Checkbox;
import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import pt.gov.dgarq.roda.common.convert.db.modules.DatabaseImportModule;
import pt.gov.dgarq.roda.common.convert.db.modules.postgreSql.in.PostgreSQLJDBCImportModule;
import pt.gov.dgarq.roda.sipcreator.Messages;
import pt.gov.dgarq.roda.sipcreator.SpringUtilities;

/**
 * PostgreSQL database import panel
 * 
 * @author Luis Faria
 * 
 */
public class PostgreSqlImportPanel extends JPanel implements DbmsImportPanel {

	private static final long serialVersionUID = 2868963613025722491L;

	private JPanel formPanel = null;
	private JLabel serverNameLabel = null;
	private JTextField serverNameValue = null;
	private JLabel portLabel = null;
	private JTextField portValue = null;
	private JLabel databaseLabel = null;
	private JTextField databaseValue = null;
	private JLabel usernameLabel = null;
	private JTextField usernameValue = null;
	private JLabel passwordLabel = null;
	private JPasswordField passwordValue = null;

	private JPanel options = null;
	private Checkbox encrypt = null;

	/**
	 * Create a new SQL Server import panel
	 */
	public PostgreSqlImportPanel() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		formPanel = new JPanel(new SpringLayout());

		serverNameLabel = new JLabel(
				Messages
						.getString("Creator.relational_database.import.postgresql.HOST"),
				JLabel.TRAILING);
		portLabel = new JLabel(
				Messages
						.getString("Creator.relational_database.import.postgresql.PORT"),
				JLabel.TRAILING);
		databaseLabel = new JLabel(
				Messages
						.getString("Creator.relational_database.import.postgresql.DATABASE"),
				JLabel.TRAILING);
		usernameLabel = new JLabel(
				Messages
						.getString("Creator.relational_database.import.postgresql.USERNAME"),
				JLabel.TRAILING);
		passwordLabel = new JLabel(
				Messages
						.getString("Creator.relational_database.import.postgresql.PASSWORD"),
				JLabel.TRAILING);

		serverNameValue = new JTextField("localhost", 15);
		portValue = new JTextField("5432", 15);
		databaseValue = new JTextField(15);
		usernameValue = new JTextField("root", 15);
		passwordValue = new JPasswordField(15);

		serverNameLabel.setLabelFor(serverNameValue);
		databaseLabel.setLabelFor(databaseValue);
		usernameLabel.setLabelFor(usernameValue);
		passwordLabel.setLabelFor(passwordValue);

		formPanel.add(serverNameLabel);
		formPanel.add(serverNameValue);
		formPanel.add(portLabel);
		formPanel.add(portValue);
		formPanel.add(databaseLabel);
		formPanel.add(databaseValue);
		formPanel.add(usernameLabel);
		formPanel.add(usernameValue);
		formPanel.add(passwordLabel);
		formPanel.add(passwordValue);

		SpringUtilities.makeCompactGrid(formPanel, 5, 2, 5, 5, 5, 5);

		options = new JPanel();

		options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
		encrypt = new Checkbox(
				Messages
						.getString("Creator.relational_database.import.postgresql.USE_ENCRYPTION"));

		options.add(encrypt);

		options.setAlignmentX(Component.RIGHT_ALIGNMENT);

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
			String hostname = serverNameValue.getText();
			int port = Integer.valueOf(portValue.getText());
			String database = databaseValue.getText();
			String username = usernameValue.getText();
			String password = new String(passwordValue.getPassword());
			boolean useEncryption = encrypt.getState();
			ret = new PostgreSQLJDBCImportModule(hostname, port, database,
					username, password, useEncryption);

		}
		return ret;
	}

	/**
	 * @return the panel
	 * 
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
		String port = portValue.getText();
		String database = databaseValue.getText();
		String username = usernameValue.getText();
		String password = new String(passwordValue.getPassword());

		valid &= serverName.length() > 0;
		valid &= database.length() > 0;
		valid &= username.length() > 0;
		valid &= password.length() > 0;

		try {
			int portInt = Integer.valueOf(port);
			valid &= portInt >= 0 && portInt <= 65535;
		} catch (NumberFormatException e) {
			valid = false;
		}

		return valid;
	}

	/**
	 * Get title, used in DBMS selection combo box
	 * 
	 * @return the title
	 */
	@Override
	public String toString() {
		return Messages
				.getString("Creator.relational_database.import.postgresql.TITLE");
	}

}
