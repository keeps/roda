/**
 *
 */
package pt.gov.dgarq.roda.sipcreator.representation.unknown;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import org.apache.log4j.Logger;
import pt.gov.dgarq.roda.common.FormatUtility;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.ingest.siputility.builders.RepresentationBuilder;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;
import pt.gov.dgarq.roda.sipcreator.Messages;
import pt.gov.dgarq.roda.sipcreator.Tools;
import pt.gov.dgarq.roda.sipcreator.representation.DragNDropHelper;
import pt.gov.dgarq.roda.sipcreator.representation.InvalidRepresentationException;
import pt.gov.dgarq.roda.sipcreator.representation.MetadataPanel;
import pt.gov.dgarq.roda.sipcreator.representation.PreviewPanel;
import pt.gov.dgarq.roda.sipcreator.representation.RepresentationObjectHelper;
import pt.gov.dgarq.roda.sipcreator.representation.image.DWRepresentationPanel;
import pt.gov.dgarq.roda.sipcreator.upload.SIPListPanel;

/**
 * @author Luis Faria
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 *
 */
public class UnknownRepresentationPanel extends JPanel implements
        DragGestureListener, DropTargetListener, DragSourceListener {

    private static final long serialVersionUID = -4883803056855882590L;
    private static final Logger logger = Logger
            .getLogger(UnknownRepresentationPanel.class);
    private static final String IMAGE_BASE = "/pt/gov/dgarq/roda/sipcreator/unknown/";
    private JLabel labelHeader = null;
    private JPanel panelFiles = null;
    private JLabel labelTop = null;
    private JScrollPane scrollPaneList = null;
    private JPanel fileList = null;
    private ButtonGroup filesGroup = null;
    private List<File> files = null;
    private int rootIndex = 0;
    private JToolBar toolbar = null;
    private Action actionAddFile = null;
    private Action actionRemoveFile = null;
    private Action actionSetRoot = null;
    private JTabbedPane tabbedpanePreviewMetadata = null;
    private MetadataPanel panelMetadata = null;
    private PreviewPanel panelPreview = null;
    private SIPRepresentationObject representationObject = null;
    private JFileChooser fileChooser = null;
    private RepresentationFile selectedFile = null;
    /**
     * Variables needed for DnD
     */
    private DragSource dragSource = null;

    /**
     * Constructs a new {@link DWRepresentationPanel}.
     *
     * @param rObject
     *
     * @throws InvalidRepresentationException
     */
    public UnknownRepresentationPanel(SIPRepresentationObject rObject)
            throws InvalidRepresentationException {

        files = new ArrayList<File>();

        setRepresentationObject(rObject);
        initComponents();

        dragSource = DragSource.getDefaultDragSource();

        DragGestureRecognizer dgr = dragSource
                .createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_COPY_OR_MOVE, this);
        dgr.setSourceActions(dgr.getSourceActions() & ~InputEvent.BUTTON3_MASK);
        new DropTarget(this, this);
    }

    /**
     * @return the representationObject
     */
    public SIPRepresentationObject getRepresentationObject() {
        return representationObject;
    }

    /**
     * @param rObject the representationObject to set
     *
     * @throws InvalidRepresentationException
     */
    public void setRepresentationObject(SIPRepresentationObject rObject)
            throws InvalidRepresentationException {
        if (rObject == null) {
            throw new InvalidRepresentationException("Representation is null");
        } else if (!RepresentationObject.UNKNOWN.equalsIgnoreCase(rObject
                .getType())) {
            throw new InvalidRepresentationException(
                    "Invalid representation type - " + rObject.getType());
        } else {
            this.representationObject = rObject;

            try {
                if (rObject.getRootFile() != null) {
                    files.add(new File(new URL(rObject.getRootFile()
                            .getAccessURL()).toURI()));
                }
                for (RepresentationFile file : rObject.getPartFiles()) {
                    files.add(new File(new URL(file.getAccessURL()).toURI()));
                }

            } catch (MalformedURLException e) {
                logger.error("Error setting representation object", e);
            } catch (URISyntaxException e) {
                logger.error("Error setting representation object", e);
            }

        }

    }

    /**
     * Add files to the file list
     *
     * @param fileList
     */
    public void addFiles(List<File> fileList) {
        addFilesImpl(fileList);
        updateRepresentation();
    }

    private void addFilesImpl(List<File> fileList) {
        for (File file : fileList) {
            if (file.isDirectory()) {
                addFilesImpl(Arrays.asList(file.listFiles()));
            } else if (DragNDropHelper.isFileAllowed(file)) {
                files.add(file);
            }
        }

        updateRepresentation();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        add(getHeaderPanel(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        splitPane.add(getFilesPanel());
        splitPane.add(getPreviewMetadataPanel());

        add(splitPane, BorderLayout.CENTER);

    }

    private JLabel getHeaderPanel() {
        if (this.labelHeader == null) {
            this.labelHeader = new JLabel(
                    String
                    .format(
                    "<html><p style='font-size: 16; font-weight: bold'>%1$s</p><html>",
                    Messages.getString("Creator.unknown.TITLE")));
            this.labelHeader.setBorder(BorderFactory.createEmptyBorder(5, 5, 5,
                    5));
        }
        return this.labelHeader;
    }

    private JTabbedPane getPreviewMetadataPanel() {
        if (this.tabbedpanePreviewMetadata == null) {
            this.tabbedpanePreviewMetadata = new JTabbedPane();

            this.tabbedpanePreviewMetadata.addTab(getPreviewPanel().getTitle(),
                    getPreviewPanel());
            this.tabbedpanePreviewMetadata.addTab(
                    getMetadataPanel().getTitle(), getMetadataPanel());

            this.tabbedpanePreviewMetadata.setBorder(BorderFactory
                    .createEmptyBorder(0, 1, 0, 1));
        }
        return this.tabbedpanePreviewMetadata;
    }

    private MetadataPanel getMetadataPanel() {
        if (this.panelMetadata == null) {
            this.panelMetadata = new MetadataPanel();
        }
        return this.panelMetadata;
    }

    private PreviewPanel getPreviewPanel() {
        if (this.panelPreview == null) {
            this.panelPreview = new PreviewPanel();
        }
        return this.panelPreview;
    }

    private JPanel getFilesPanel() {
        if (this.panelFiles == null) {
            this.panelFiles = new JPanel(new BorderLayout());

            this.panelFiles.add(getTopLabel(), BorderLayout.NORTH);
            this.panelFiles.add(getScrollList(), BorderLayout.CENTER);
            this.panelFiles.add(getToolbar(), BorderLayout.EAST);

            this.panelFiles.setBorder(BorderFactory.createEmptyBorder(5, 5, 5,
                    5));

        }
        return this.panelFiles;
    }

    private JLabel getTopLabel() {
        if (this.labelTop == null) {
            this.labelTop = new JLabel(Messages
                    .getString("Creator.unknown.SELECT_FILES"));
            this.labelTop
                    .setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        }
        return this.labelTop;
    }

    private JScrollPane getScrollList() {
        if (this.scrollPaneList == null) {
            this.scrollPaneList = new JScrollPane();
            this.scrollPaneList.getViewport().add(getFileList());
            scrollPaneList.setPreferredSize(new Dimension(400, 600));
        }
        return this.scrollPaneList;
    }

    private JPanel getFileList() {
        if (fileList == null) {
            fileList = new JPanel();
            fileList.setLayout(new BoxLayout(fileList, BoxLayout.Y_AXIS));
            update();
        }
        return fileList;
    }

    private JToolBar getToolbar() {
        if (this.toolbar == null) {
            this.toolbar = new JToolBar(JToolBar.VERTICAL);
            this.toolbar.setFloatable(false);

            this.toolbar.add(getAddFileAction());
            this.toolbar.add(getRemoveFileAction());

            this.toolbar.add(new JToolBar.Separator());

            this.toolbar.add(getSetRootAction());

        }
        return this.toolbar;
    }

    private Action getAddFileAction() {
        if (this.actionAddFile == null) {
            this.actionAddFile = new AbstractAction(Messages
                    .getString("Creator.digitalized_work.action.ADD_FILES"),
                    Tools.createImageIcon(IMAGE_BASE + "file_add.png")) {
                private static final long serialVersionUID = 1L;

                public void actionPerformed(ActionEvent e) {
                    addFiles(Arrays.asList(selectFiles()));
                }
            };

            this.actionAddFile.putValue(AbstractAction.SHORT_DESCRIPTION,
                    Messages.getString("Creator.unknown.action.ADD_FILES"));
        }
        return this.actionAddFile;
    }

    private Action getRemoveFileAction() {
        if (this.actionRemoveFile == null) {
            this.actionRemoveFile = new AbstractAction(Messages
                    .getString("Creator.unknown.action.REMOVE_FILES"), Tools
                    .createImageIcon(IMAGE_BASE + "file_delete.png")) {
                private static final long serialVersionUID = 1L;

                public void actionPerformed(ActionEvent e) {
                    int index;
                    try {
                        index = getRepresentationFileIndex(selectedFile);
                        if (index >= 0) {
                            files.remove(index);
                            updateRepresentation();
                        }
                    } catch (MalformedURLException e1) {
                        logger.error("Error removing file", e1);
                    } catch (URISyntaxException e1) {
                        logger.error("Error removing file", e1);
                    }

                }
            };
            this.actionRemoveFile.setEnabled(false);
            this.actionRemoveFile.putValue(AbstractAction.SHORT_DESCRIPTION,
                    Messages.getString("Creator.unknown.action.REMOVE_FILES"));
        }
        return this.actionRemoveFile;
    }

    private Action getSetRootAction() {
        if (this.actionSetRoot == null) {
            this.actionSetRoot = new AbstractAction(Messages
                    .getString("Creator.digitalized_work.action.SET_ROOT"),
                    Tools.createImageIcon(IMAGE_BASE + "set_root.png")) {
                private static final long serialVersionUID = 1L;

                public void actionPerformed(ActionEvent e) {
                    if (selectedFile != null) {
                        try {
                            setRoot(selectedFile);
                        } catch (MalformedURLException e1) {
                            logger.error("Error setting root", e1);
                        } catch (URISyntaxException e1) {
                            logger.error("Error setting root", e1);
                        }
                    }
                }
            };
            this.actionSetRoot.setEnabled(false);

            this.actionSetRoot.putValue(AbstractAction.SHORT_DESCRIPTION,
                    Messages.getString("Creator.unknown.action.SET_ROOT"));
        }
        return this.actionSetRoot;
    }

    protected File[] selectFiles() {
        File[] selectedFiles = null;
        int result = getFileChooser().showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFiles = getFileChooser().getSelectedFiles();
        }

        return selectedFiles;
    }

    protected JFileChooser getFileChooser() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
        }
        return fileChooser;
    }

    private void updateRepresentation() {
        try {
            // add root
            rootIndex = rootIndex >= files.size() ? files.size() - 1
                    : rootIndex;
            File rootFile = files.get(rootIndex);
            RepresentationFile root = new RepresentationFile("F0", rootFile
                    .getName(), rootFile
                    .length(), rootFile.toURI().toURL().toString(), FormatUtility.getFileFormat(rootFile, rootFile.getName()));

            // add part files
            List<RepresentationFile> partFiles = new ArrayList<RepresentationFile>();
            for (int i = 0; i < files.size(); i++) {
                if (i != rootIndex) {
                    File partFile = files.get(i);
                    String filePartId = RepresentationObjectHelper
                            .getNextFilePartId(partFiles);
                    RepresentationFile part = new RepresentationFile(
                            filePartId, partFile.getName(),
                            partFile.length(), partFile.toURI().toURL()
                            .toString(),
                            FormatUtility.getFileFormat(partFile, partFile.getName()));
                    partFiles.add(part);

                }
            }

            getRepresentationObject().setRootFile(root);
            getRepresentationObject().setPartFiles(
                    partFiles.toArray(new RepresentationFile[]{}));
            update();
        } catch (MalformedURLException e) {
            logger.error("Error updating reprensetation", e);
        }

    }

    private ButtonGroup getFilesGroup() {
        if (filesGroup == null) {
            filesGroup = new ButtonGroup();
        }
        return filesGroup;
    }
    private static final URL rootFileIconSrc = SIPListPanel.class
            .getResource(IMAGE_BASE + "file_root.png");
    private static final URL partFileIconSrc = SIPListPanel.class
            .getResource(IMAGE_BASE + "file.png");

    private void update() {
        fileList.removeAll();
        // add root
        if (getRepresentationObject().getRootFile() != null) {
            fileList.add(getRadioButton(
                    getRepresentationObject().getRootFile(), true));
        }

        List<RepresentationFile> partFiles = new ArrayList<RepresentationFile>(
                Arrays.asList(getRepresentationObject().getPartFiles()));

        Tools.sortPartFiles(partFiles);

        // add part files
        for (RepresentationFile file : partFiles) {
            fileList.add(getRadioButton(file, false));
        }

        repaint();
        revalidate();

    }

    private JRadioButton getRadioButton(final RepresentationFile file,
            boolean root) {
        String path = file.getAccessURL();
        try {
            File realFile = new File(new URL(file.getAccessURL()).toURI());
            path = realFile.getAbsolutePath();
        } catch (MalformedURLException e) {
            logger.error("Error getting representation file path", e);
        } catch (URISyntaxException e) {
            logger.error("Error getting representation file path", e);
        }

        JRadioButton button = new JRadioButton(String.format(
                "<html><span><img src='%1$s'>&nbsp;%2$s</span></html>",
                root ? rootFileIconSrc : partFileIconSrc, path));
        getFilesGroup().add(button);

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectedFile = file;
                updateVisibles();
            }
        });

        return button;
    }

    /**
     * Get representation file index
     *
     * @param file
     * @return the representation file index in the files list, or -1 if not
     * found
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    private int getRepresentationFileIndex(RepresentationFile file)
            throws MalformedURLException, URISyntaxException {
        int index = -1;
        File realFile = new File(new URL(file.getAccessURL()).toURI());
        for (int i = 0; i < files.size(); i++) {
            if (realFile.equals(files.get(i))) {
                index = i;
                break;
            }
        }
        return index;
    }

    private void setRoot(RepresentationFile file) throws MalformedURLException,
            URISyntaxException {
        int index = getRepresentationFileIndex(file);
        if (index >= 0) {
            setRootIndex(index);
        }
    }

    /**
     * Set representation root with the index
     *
     * @param rootIndex
     */
    public void setRootIndex(int rootIndex) {
        if (this.rootIndex != rootIndex) {
            this.rootIndex = rootIndex;
            updateRepresentation();
        }
    }

    private void updateVisibles() {
        actionRemoveFile.setEnabled(selectedFile != null);
        actionSetRoot.setEnabled(selectedFile != null
                && selectedFile != getRepresentationObject().getRootFile());
    }

    /**
     * @see
     * java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.DragGestureEvent)
     */
    public void dragGestureRecognized(DragGestureEvent dge) {
        // nothing to do
    }

    /**
     * @see
     * java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
     */
    public void dragEnter(DropTargetDragEvent dtde) {
        // nothing to do
    }

    /**
     * @see
     * java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
     */
    public void dragExit(DropTargetEvent dte) {
        // nothing to do
    }

    /**
     * @see
     * java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
     */
    public void dragOver(DropTargetDragEvent dtde) {
        // nothing to do
    }

    /**
     * @see
     * java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
     */
    @SuppressWarnings("unchecked")
    public void drop(DropTargetDropEvent e) {
        Transferable tr = e.getTransferable();
        try {
            // is it a file list?
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                logger.debug("java file list flavor");
                e.acceptDrop(DnDConstants.ACTION_COPY);
                List<File> files = (List<File>) tr
                        .getTransferData(DataFlavor.javaFileListFlavor);
                addFiles(files);

            } else {
                // Is it a file list in Linux (KDE/Gnome)?
                DataFlavor[] flavors = tr.getTransferDataFlavors();
                File[] files = null;
                for (int zz = 0; zz < flavors.length; zz++) {
                    if (flavors[zz].isRepresentationClassReader()) {
                        e.acceptDrop(DnDConstants.ACTION_COPY);
                        Reader reader = flavors[zz].getReaderForText(tr);
                        BufferedReader br = new BufferedReader(reader);

                        files = Tools.createFileArray(br);

                        // Mark that drop is completed.
                        e.getDropTargetContext().dropComplete(true);
                        break;
                    }
                }

                if (files != null) {
                    logger.debug("java file list in Linux (KDE/Gnome)");
                    e.acceptDrop(DnDConstants.ACTION_COPY);
                    addFiles(Arrays.asList(files));
                }
            }
        } catch (IOException e1) {
            logger.error("Error in Drag'n'Drop", e1);
        } catch (UnsupportedFlavorException e1) {
            logger.error("Error in Drag'n'Drop", e1);
        }

    }

    /**
     * @see
     * java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
     */
    public void dropActionChanged(DropTargetDragEvent dtde) {
        // nothing to do
    }

    /**
     * @see
     * java.awt.dnd.DragSourceListener#dragDropEnd(java.awt.dnd.DragSourceDropEvent)
     */
    public void dragDropEnd(DragSourceDropEvent dsde) {
        // nothing to do
    }

    /**
     * @see
     * java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.DragSourceDragEvent)
     */
    public void dragEnter(DragSourceDragEvent dsde) {
        // nothing to do
    }

    /**
     * @see
     * java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
     */
    public void dragExit(DragSourceEvent dse) {
        // nothing to do
    }

    /**
     * @see
     * java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent)
     */
    public void dragOver(DragSourceDragEvent dsde) {
        // nothing to do
    }

    /**
     * @see
     * java.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.DragSourceDragEvent)
     */
    public void dropActionChanged(DragSourceDragEvent dsde) {
        // nothing to do
    }
}
