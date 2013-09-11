/**
 *
 */
package pt.gov.dgarq.roda.sipcreator.representation.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import pt.gov.dgarq.roda.common.FormatUtility;
import pt.gov.dgarq.roda.common.convert.db.model.data.BinaryCell;
import pt.gov.dgarq.roda.common.convert.db.model.exception.InvalidDataException;
import pt.gov.dgarq.roda.common.convert.db.model.exception.ModuleException;
import pt.gov.dgarq.roda.common.convert.db.model.exception.UnknownTypeException;
import pt.gov.dgarq.roda.common.convert.db.model.structure.DatabaseStructure;
import pt.gov.dgarq.roda.common.convert.db.modules.DatabaseImportModule;
import pt.gov.dgarq.roda.common.convert.db.modules.dbml.in.DBMLImportModule;
import pt.gov.dgarq.roda.common.convert.db.modules.dbml.out.DBMLExportModule;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.ingest.siputility.builders.RepresentationBuilder;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;
import pt.gov.dgarq.roda.sipcreator.Loading;
import pt.gov.dgarq.roda.sipcreator.Messages;
import pt.gov.dgarq.roda.sipcreator.SIPCreatorConfig;
import pt.gov.dgarq.roda.util.TempDir;

/**
 * @author Luis Faria
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 *
 */
public class DbmsSelectPanel extends JPanel {

    private static final long serialVersionUID = -7412531495068611367L;
    private static final Logger logger = Logger
            .getLogger(DbmsSelectPanel.class);
    private final SIPRepresentationObject repObj;
    private JPanel dbmsSelectPanel = null;
    private boolean imported;
    private JLabel title = null;
    private JComboBox dbmsList = null;
    private JPanel deckPanel = null;
    private DbmsImportPanel currImportPanel = null;
    private JButton importButton = null;
    private JButton backButton = null;
    private DbmlFileViewer dbmlFileViewer = null;

    /**
     * Create a new Database Management System selection panel
     *
     * @param repObj
     */
    public DbmsSelectPanel(SIPRepresentationObject repObj) {
        this.repObj = repObj;
        imported = repObj.getRootFile() != null;
        initComponents();
    }

    private JPanel getDbmsSelectPanel() {
        if (dbmsSelectPanel == null) {
            dbmsSelectPanel = new JPanel(new FlowLayout());
            title = new JLabel(Messages
                    .getString("Creator.relational_database.SELECTION_LABEL"));

            dbmsSelectPanel.add(title);
            dbmsSelectPanel.add(getDbmsList());
        }
        return dbmsSelectPanel;
    }

    private JComboBox getDbmsList() {
        if (dbmsList == null) {
            dbmsList = new JComboBox();

            dbmsList.addItem(new SqlServerImportPanel());
            dbmsList.addItem(new MySqlImportPanel());
            dbmsList.addItem(new PostgreSqlImportPanel());
            dbmsList.addItem(new DbmlImportPanel());
            dbmsList.addItem(new MsAccessImportPanel());
            dbmsList.addItem(new OdbcImportPanel());

            dbmsList.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    update();

                }
            });
        }
        return dbmsList;
    }

    private JButton getImportButton() {
        if (importButton == null) {
            importButton = new JButton(Messages
                    .getString("Creator.relational_database.action.IMPORT"));
            importButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (currImportPanel.isInfoValid()) {
                        Loading
                                .run(
                                Messages
                                .getString("Creator.relational_database.IMPORT"),
                                new Runnable() {
                            public void run() {
                                doImport();
                            }
                        });

                    } else {
                        JOptionPane
                                .showMessageDialog(
                                DbmsSelectPanel.this,
                                Messages
                                .getString("Creator.relational_database.error.INVALID_INPUT"),
                                Messages.getString("common.ERROR"),
                                JOptionPane.ERROR_MESSAGE);
                    }

                }
            });
        }
        return importButton;
    }

    private class MyDBMLExportModule extends DBMLExportModule {

        private int table_count;
        private int table_index;

        public MyDBMLExportModule(File baseDir) throws FileNotFoundException,
                UnsupportedEncodingException {
            super(baseDir, DEFAULT_DBML_FILE_NAME, new DBMLBinaryCreate() {
                int i = 1;

                public File createBinaryFile(File baseDir, BinaryCell bin,
                        String path) {
                    return new File(baseDir, path);
                }

                public String createBinaryPath(BinaryCell bin) {
                    return "F" + (i++);
                }
            });
            table_index = 0;
        }

        @Override
        public void handleStructure(DatabaseStructure structure)
                throws ModuleException, UnknownTypeException {
            table_count = structure.getTables().size();
            Loading.setMessage(Messages.getString(
                    "Creator.relational_database.loading.IMPORT_STRUCTURE",
                    structure.getName(), table_count));
            super.handleStructure(structure);
        }

        @Override
        public void handleDataOpenTable(String tableId) throws ModuleException {
            Loading.setMessage(Messages.getString(
                    "Creator.relational_database.loading.IMPORT_TABLE_DATA",
                    tableId, ++table_index, table_count));
            super.handleDataOpenTable(tableId);
        }
    }

    private void doImport() {
        try {
            DatabaseImportModule databaseImportModule = currImportPanel
                    .getDatabaseImportModule();

            File dbmlDir = TempDir.createUniqueTemporaryDirectory("dbml",
                    SIPCreatorConfig.getInstance().getTmpDir());
            MyDBMLExportModule dbmlExport = new MyDBMLExportModule(dbmlDir);
            databaseImportModule.getDatabase(dbmlExport);
            updateRepresentation(dbmlDir);
            imported = true;
            update();

        } catch (FileNotFoundException e1) {
            logger.error("Error importing database", e1);
            JOptionPane.showMessageDialog(DbmsSelectPanel.this, Messages
                    .getString("Creator.relational_database.error.IMPORT", e1
                    .getMessage()), Messages.getString("common.ERROR"),
                    JOptionPane.ERROR_MESSAGE);
        } catch (UnsupportedEncodingException e1) {
            logger.error("Error importing database", e1);
            JOptionPane.showMessageDialog(DbmsSelectPanel.this, Messages
                    .getString("Creator.relational_database.error.IMPORT", e1
                    .getMessage()), Messages.getString("common.ERROR"),
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(DbmsSelectPanel.this, Messages
                    .getString("Creator.relational_database.error.IMPORT", e1
                    .getMessage()), Messages.getString("common.ERROR"),
                    JOptionPane.ERROR_MESSAGE);
        } catch (ModuleException e1) {
            String message = "";
            if (e1.getModuleErrors() != null) {
                for (Map.Entry<String, Throwable> entry : e1.getModuleErrors()
                        .entrySet()) {
                    message += entry.getKey() + ": "
                            + entry.getValue().getMessage() + "\n";
                    logger.error(entry.getKey(), entry.getValue());
                }
            } else {
                logger.error("Module error", e1);
                message = "Error converting database: " + e1.getMessage();
                if (e1.getCause() != null) {
                    message += ", " + e1.getCause().getMessage();

                    if (e1.getCause().getCause() != null) {
                        message += ", " + e1.getCause().getCause().getMessage();

                    }
                }
            }
            JOptionPane.showMessageDialog(DbmsSelectPanel.this, Messages
                    .getString("Creator.relational_database.error.IMPORT",
                    message), Messages.getString("common.ERROR"),
                    JOptionPane.ERROR_MESSAGE);

        } catch (UnknownTypeException e1) {
            logger.error("Error converting database", e1);
            JOptionPane
                    .showMessageDialog(
                    DbmsSelectPanel.this,
                    Messages
                    .getString(
                    "Creator.relational_database.error.UNKNOWN_COLUMN_TYPE",
                    e1.getMessage()), Messages
                    .getString("common.ERROR"),
                    JOptionPane.ERROR_MESSAGE);
        } catch (InvalidDataException e1) {
            logger.error("Error converting database", e1);
            JOptionPane.showMessageDialog(DbmsSelectPanel.this, Messages
                    .getString(
                    "Creator.relational_database.error.INVALID_DATA",
                    e1.getMessage()), Messages
                    .getString("common.ERROR"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton getBackButton() {
        if (backButton == null) {
            backButton = new JButton(Messages
                    .getString("Creator.relational_database.action.BACK"));
            backButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    imported = false;
                    update();

                }
            });
        }
        return backButton;
    }

    private JPanel getDeckPanel() {
        if (deckPanel == null) {
            deckPanel = new JPanel();
        }
        return deckPanel;
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
        setMinimumSize(new Dimension(270, 200));
        setPreferredSize(new Dimension(300, 400));
        update();
    }

    protected DbmlFileViewer getDbmlFileViewer() {
        if (dbmlFileViewer == null) {
            dbmlFileViewer = new DbmlFileViewer(repObj);
        }
        return dbmlFileViewer;
    }

    private void update() {
        if (imported) {
            remove(getDbmsSelectPanel());
            remove(getDeckPanel());
            remove(getImportButton());

            add(getDbmlFileViewer(), BorderLayout.CENTER);
            add(getBackButton(), BorderLayout.SOUTH);

        } else {
            if (dbmlFileViewer != null) {
                remove(dbmlFileViewer);
                dbmlFileViewer = null;
            }
            if (backButton != null) {
                remove(getBackButton());
            }

            add(getDbmsSelectPanel(), BorderLayout.NORTH);
            add(getDeckPanel(), BorderLayout.CENTER);
            add(getImportButton(), BorderLayout.SOUTH);

            DbmsImportPanel importPanel = (DbmsImportPanel) dbmsList
                    .getSelectedItem();
            deckPanel.removeAll();
            currImportPanel = importPanel;
            deckPanel.add(importPanel.getPanel());

        }

        this.revalidate();
        this.repaint();

    }

    private void updateRepresentation(File dbmlDir) {
        RepresentationFile rootFile = null;
        List<RepresentationFile> partFiles = new ArrayList<RepresentationFile>();
        for (File file : dbmlDir.listFiles()) {
            if (file.getName().equals(DBMLImportModule.DBML_DEFAULT_FILE_NAME)) {
                try {
                    rootFile = new RepresentationFile("F0", file.getName(), file.length(), file.toURI().toURL().toString(), FormatUtility.getFileFormat(file, file.getName()));
                } catch (MalformedURLException e) {
                    logger.error("Error adding root file", e);
                }
            } else {
                // String partFileId = RepresentationObjectHelper
                // .getNextFilePartId(partFiles);
                // Part files have already a standardized name to use as id
                try {
                    RepresentationFile partFile = new RepresentationFile(file.getName(), file.getName(), file.length(), file.toURI().toURL().toString(), FormatUtility.getFileFormat(file, file.getName()));
                    partFiles.add(partFile);
                } catch (MalformedURLException e) {
                    logger.error("Error adding part file", e);
                }
            }
        }

        repObj.setRootFile(rootFile);
        repObj.setPartFiles(partFiles.toArray(new RepresentationFile[partFiles
                .size()]));
        if (partFiles.size() == 0) {
            repObj.setSubType("application/dbml");
        } else {
            repObj.setSubType("application/dbml+octet-stream");
        }
    }
}
