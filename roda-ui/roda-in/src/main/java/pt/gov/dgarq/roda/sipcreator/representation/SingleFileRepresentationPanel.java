package pt.gov.dgarq.roda.sipcreator.representation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
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
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.FormatUtility;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.ingest.siputility.builders.RepresentationBuilder;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;
import pt.gov.dgarq.roda.sipcreator.Messages;
import pt.gov.dgarq.roda.sipcreator.Tools;

/**
 * @author Rui Castro
 * @author Luis Faria
 */
public abstract class SingleFileRepresentationPanel extends
		AbstractRepresentationPanel implements DragGestureListener,
		DropTargetListener, DragSourceListener {
	private static final long serialVersionUID = 2903042299411379578L;

	private static final Logger logger = Logger
			.getLogger(SingleFileRepresentationPanel.class);

	private JLabel labelHeader = null;

	private JPanel panelFile = null;
	private JTextField textfieldFile = null;
	private Action actionSelectFile = null;
	private JPanel subTypePanel = null;
	private List<JRadioButton> optionButtons = null;

	/** Variables needed for DnD */
	private DragSource dragSource = null;

	/**
	 * Constructs a new {@link SingleFileRepresentationPanel}.
	 * 
	 * @param rObject
	 * 
	 * @throws InvalidRepresentationException
	 */
	public SingleFileRepresentationPanel(SIPRepresentationObject rObject)
			throws InvalidRepresentationException {
		super(rObject);
		initComponents();
		dragSource = DragSource.getDefaultDragSource();
		DragGestureRecognizer dgr = dragSource
				.createDefaultDragGestureRecognizer(this,
						DnDConstants.ACTION_COPY_OR_MOVE, this);
		dgr.setSourceActions(dgr.getSourceActions() & ~InputEvent.BUTTON3_MASK);
		new DropTarget(this, this);
	}

	/**
	 * @return a {@link String} with the title.
	 */
	public abstract String getTitle();

	/**
	 * @return a {@link FileFilter} to filter the files that can be added to the
	 *         {@link RepresentationObject}.
	 */
	public abstract FileFilter getRepresentationFileFilter();

	private void initComponents() {
		setLayout(new BorderLayout());

		add(getHeaderPanel(), BorderLayout.NORTH);

		JPanel panel = new JPanel(new BorderLayout());
		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
		top.add(getFilePanel());
		top.add(getSubTypePanel());
		panel.add(top, BorderLayout.NORTH);
		panel.add(getPreviewMetadataPanel(), BorderLayout.CENTER);

		add(panel, BorderLayout.CENTER);
	}

	private JLabel getHeaderPanel() {
		if (this.labelHeader == null) {
			this.labelHeader = new JLabel(
					String
							.format(
									"<html><p style='font-size: 16; font-weight: bold'>%1$s</p><html>",
									getTitle()));
			this.labelHeader.setBorder(BorderFactory.createEmptyBorder(5, 5, 5,
					5));
		}
		return this.labelHeader;
	}

	private JPanel getFilePanel() {
		if (this.panelFile == null) {
			this.panelFile = new JPanel();

			this.panelFile.setLayout(new BoxLayout(this.panelFile,
					BoxLayout.LINE_AXIS));
			this.panelFile.add(new JLabel(Messages
					.getString("Representation.single.FILE")));
			this.panelFile.add(getTextFieldFile());
			JButton selectButton = new JButton(getSelectFileAction());
			this.panelFile.add(selectButton);
			this.panelFile.setBorder(BorderFactory.createEmptyBorder(2, 15, 2,
					15));
		}
		return this.panelFile;
	}

	private JTextField getTextFieldFile() {
		if (this.textfieldFile == null) {
			this.textfieldFile = new JTextField();
			this.textfieldFile.setEditable(false);
			if (getRepresentationObject().getRootFile() != null) {
				textfieldFile.setText(getRepresentationObject().getRootFile()
						.getOriginalName());
			}
		}
		return this.textfieldFile;
	}

	private Action getSelectFileAction() {
		if (this.actionSelectFile == null) {

			this.actionSelectFile = new AbstractAction(Messages
					.getString("Representation.single.SELECT_FILE")) {
				private static final long serialVersionUID = 5642593882066731894L;

				public void actionPerformed(ActionEvent e) {
					chooseFile();
				}

			};
		}
		return this.actionSelectFile;
	}

	private void chooseFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(getRepresentationFileFilter());
		fileChooser.setMultiSelectionEnabled(false);
		int result = fileChooser.showOpenDialog(this);
		File selectedFile = fileChooser.getSelectedFile();

		if (result == JFileChooser.APPROVE_OPTION) {
			selectFile(selectedFile);
		}
	}

	private void selectFile(File selectedFile) {
		getPreviewPanel().loadingPreview();
		Component viewer = null;
		// try {
		logger.debug("Setting text field");
		getTextFieldFile().setText(selectedFile.getAbsolutePath());
		logger.debug("Setting subType");
		getRepresentationObject().setSubType(getSubType(selectedFile));
		logger.debug("Setting root");
		setRootFile(selectedFile);
		viewer = getPreview();
		/*
		 * } catch (SIPException e) {
		 * logger.error("Error setting representation root file", e);
		 * JOptionPane.showMessageDialog(this, e.getMessage(), Messages
		 * .getString("common.ERROR"), JOptionPane.ERROR_MESSAGE); }
		 */
		if (viewer != null) {
			getPreviewPanel().setPreview(viewer);
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					logger.debug("Single file selected, starting preview");
					startPreview();
				}

			});
		} else {
			getPreviewPanel().noPreview();
		}

		updateSubType();
	}

	protected abstract RepresentationBuilder getRepresentationBuilder();

	protected void setRootFile(File file) {
		RepresentationFile rootFile;
		try {
			rootFile = new RepresentationFile("F0", file.getName(),
					RepresentationBuilder.getFileMimetype(file), file.length(),
					file.toURI().toURL().toString());
			getRepresentationObject().setRootFile(rootFile);
		} catch (MalformedURLException e) {
			logger.error("Error setting root file", e);
		}

	}

	private JPanel getSubTypePanel() {
		if (subTypePanel == null) {
			subTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			// Group the radio buttons.
			ButtonGroup group = new ButtonGroup();
			optionButtons = new ArrayList<JRadioButton>();

			for (final SubTypeOption option : getSubTypeOptions()) {
				JRadioButton subTypeOption = new JRadioButton(option.getLabel());
				subTypeOption.setActionCommand(option.getSubType());
				group.add(subTypeOption);
				subTypePanel.add(subTypeOption);
				optionButtons.add(subTypeOption);
				subTypeOption.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						getRepresentationObject().setSubType(
								option.getSubType());
					}

				});
			}

			subTypePanel.setBorder(BorderFactory
					.createEmptyBorder(0, 20, 0, 10));

			updateSubType();

		}
		return subTypePanel;
	}

	private String getSubType(File selectedFile) {
		return FormatUtility.getMimetype(selectedFile);
	}

	private void updateSubType() {
		String subType = getRepresentationObject().getSubType();
		if (subType != null) {
			for (JRadioButton radioButton : optionButtons) {
				if (radioButton.getActionCommand().equals(subType)) {
					radioButton.setSelected(true);
				} else {
					radioButton.setSelected(false);
				}
			}
		}
	}

	protected abstract List<SubTypeOption> getSubTypeOptions();

	/**
	 * Drag gesture recognized listener action
	 * 
	 * @param dge
	 */
	public void dragGestureRecognized(DragGestureEvent dge) {
		// do nothing
	}

	/**
	 * Drag enter listener action
	 * 
	 * @param e
	 */
	public void dragEnter(DropTargetDragEvent e) {
		// nothing to do
	}

	/**
	 * Drag exit listener action
	 * 
	 * @param e
	 */
	public void dragExit(DropTargetEvent e) {
	}

	/**
	 * Drag over listener action
	 * 
	 * @param e
	 */
	public void dragOver(DropTargetDragEvent e) {
		Transferable transferable = e.getTransferable();
		if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			e.acceptDrag(DnDConstants.ACTION_MOVE);
		}
	}

	/**
	 * Drop listener action
	 * 
	 * @param e
	 */
	@SuppressWarnings("unchecked")
	public void drop(DropTargetDropEvent e) {
		Transferable tr = e.getTransferable();
		try {
			if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				logger.debug("java file list flavor");
				e.acceptDrop(DnDConstants.ACTION_COPY);
				List<File> files = (List<File>) tr
						.getTransferData(DataFlavor.javaFileListFlavor);
				if (files.size() > 0) {
					selectFile(files.get(0));
				}
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
					if (files.length > 0) {
						selectFile(files[0]);
					}
				}
			}
		} catch (IOException e1) {
			e.rejectDrop();
		} catch (UnsupportedFlavorException e1) {
			e.rejectDrop();
		}
	}

	/**
	 * Drop Action Changed listener action
	 * 
	 * @param arg0
	 */
	public void dropActionChanged(DropTargetDragEvent arg0) {
		// nothing to do

	}

	/**
	 * Drag Drop End listener action
	 * 
	 * @param arg0
	 */
	public void dragDropEnd(DragSourceDropEvent arg0) {
		// nothing to do
	}

	/**
	 * Drag Enter listener action
	 * 
	 * @param arg0
	 */
	public void dragEnter(DragSourceDragEvent arg0) {
		// nothing to do

	}

	/**
	 * Drag Exit listener action
	 * 
	 * @param arg0
	 */
	public void dragExit(DragSourceEvent arg0) {
		// nothing to do

	}

	/**
	 * Drag Over listener action
	 * 
	 * @param arg0
	 */
	public void dragOver(DragSourceDragEvent arg0) {
		// nothing to do

	}

	/**
	 * Drop Action Changed listener action
	 * 
	 * @param arg0
	 */
	public void dropActionChanged(DragSourceDragEvent arg0) {
		// nothing to do

	}
}
