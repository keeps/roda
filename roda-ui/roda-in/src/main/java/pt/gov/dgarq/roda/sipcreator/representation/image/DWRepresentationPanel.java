package pt.gov.dgarq.roda.sipcreator.representation.image;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.JAI;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import jpl.mipl.jade.JViewportImage;
import jpl.mipl.jade.JadeDisplay;
import jpl.mipl.jade.MouseScroller;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.metadata.mets.DigitalizedWorkMetsHelper;
import pt.gov.dgarq.roda.core.metadata.mets.MetsMetadataException;
import pt.gov.dgarq.roda.ingest.siputility.builders.DigitalizedWorkRepresentationBuilder;
import pt.gov.dgarq.roda.ingest.siputility.builders.RepresentationBuilder;
import pt.gov.dgarq.roda.ingest.siputility.data.DataChangeListener;
import pt.gov.dgarq.roda.ingest.siputility.data.DataChangedEvent;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;
import pt.gov.dgarq.roda.sipcreator.Messages;
import pt.gov.dgarq.roda.sipcreator.SIPCreatorConfig;
import pt.gov.dgarq.roda.sipcreator.Tools;
import pt.gov.dgarq.roda.sipcreator.representation.FileNameExtensionFilter;
import pt.gov.dgarq.roda.sipcreator.representation.InvalidRepresentationException;
import pt.gov.dgarq.roda.sipcreator.representation.MetadataPanel;
import pt.gov.dgarq.roda.sipcreator.representation.PreviewPanel;
import pt.gov.dgarq.roda.sipcreator.representation.SubTypeOption;

/**
 * @author Rui Castro
 * @author Luis Faria
 */
public class DWRepresentationPanel extends JPanel {
	private static final long serialVersionUID = 5789085196502424149L;
	private static final String IMAGE_BASE = "/pt/gov/dgarq/roda/sipcreator/digitalizedWork/";
	private static final String METS_FILE_NAME = "METS.xml";

	private static final Logger logger = Logger
			.getLogger(DWRepresentationPanel.class);

	private JPanel subTypePanel = null;
	private List<JRadioButton> optionButtons = null;

	private JLabel labelHeader = null;

	private JPanel panelFiles = null;

	private JLabel labelTop = null;

	private JScrollPane scrollPaneFileTree = null;
	private JTree treeFiles = null;
	private DWMetsTreeModel treeModelDWMets = null;

	private JToolBar toolbar = null;
	private Action actionAddSection = null;
	private Action actionAddFile = null;
	private Action actionRemoveFile = null;
	private Action actionRemoveSection = null;
	private Action actionMoveUp = null;
	private Action actionMoveDown = null;

	private JTabbedPane tabbedpanePreviewMetadata = null;
	private MetadataPanel panelMetadata = null;
	private PreviewPanel panelPreview = null;

	private SIPRepresentationObject representationObject = null;

	private JFileChooser imageFileChooser = null;

	/**
	 * Constructs a new {@link DWRepresentationPanel}.
	 * 
	 * @param rObject
	 * 
	 * @throws InvalidRepresentationException
	 */
	public DWRepresentationPanel(SIPRepresentationObject rObject)
			throws InvalidRepresentationException {

		setRepresentationObject(rObject);
		initComponents();

		rObject.addChangeListener(new DataChangeListener() {

			public void dataChanged(DataChangedEvent evtDataChanged) {
				updateSubType();
			}

		});

		addAncestorListener(new AncestorListener() {

			public void ancestorAdded(AncestorEvent event) {
				// nothing to do

			}

			public void ancestorMoved(AncestorEvent event) {
				// nothing to do

			}

			public void ancestorRemoved(AncestorEvent event) {
				save();
			}

		});
	}

	/**
	 * @return the representationObject
	 */
	public SIPRepresentationObject getRepresentationObject() {
		return representationObject;
	}

	/**
	 * @param rObject
	 *            the representationObject to set
	 * 
	 * @throws InvalidRepresentationException
	 */
	public void setRepresentationObject(SIPRepresentationObject rObject)
			throws InvalidRepresentationException {

		if (rObject == null) {
			throw new InvalidRepresentationException("Representation is null");
		} else if (!RepresentationObject.DIGITALIZED_WORK
				.equalsIgnoreCase(rObject.getType())) {
			throw new InvalidRepresentationException(
					"Invalid representation type - " + rObject.getType());
		} else {
			this.representationObject = rObject;
		}

	}

	private void initComponents() {
		setLayout(new BorderLayout());

		add(getHeaderPanel(), BorderLayout.NORTH);
		add(getSubTypePanel(), BorderLayout.NORTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		splitPane.add(getFilesPanel());
		splitPane.add(getPreviewMetadataPanel());

		add(splitPane, BorderLayout.CENTER);
	}

	private JPanel getSubTypePanel() {
		if (subTypePanel == null) {
			subTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			// Group the radio buttons.
			ButtonGroup group = new ButtonGroup();
			optionButtons = new ArrayList<JRadioButton>();

			for (SubTypeOption option : getSubTypeOptions()) {
				JRadioButton subTypeOption = new JRadioButton(option.getLabel());
				subTypeOption.setActionCommand(option.getSubType());
				group.add(subTypeOption);
				subTypePanel.add(subTypeOption);
				optionButtons.add(subTypeOption);
			}

			subTypePanel.setBorder(BorderFactory
					.createEmptyBorder(0, 20, 0, 10));

			updateSubType();

		}
		return subTypePanel;
	}

	private List<SubTypeOption> getSubTypeOptions() {
		List<SubTypeOption> options = new ArrayList<SubTypeOption>();

		options.add(getSubTypeOption("image/mets+tiff"));
		options.add(getSubTypeOption("image/mets+jpeg"));
		options.add(getSubTypeOption("image/mets+png"));
		options.add(getSubTypeOption("image/mets+bmp"));
		options.add(getSubTypeOption("image/mets+gif"));
		options.add(getSubTypeOption("image/mets+ico"));
		options.add(getSubTypeOption("image/mets+xpm"));
		options.add(getSubTypeOption("image/mets+tga"));
		options.add(getSubTypeOption("image/mets+misc"));

		return options;
	}

	private SubTypeOption getSubTypeOption(String subtype) {
		return new SubTypeOption(subtype, Messages
				.getString("Creator.digitalized_work.subtype." + subtype));
	}

	private void updateSubType() {
		String subType = getRepresentationObject().getSubType();
		logger.debug("Updating subtype with " + subType);
		if (subType != null) {
			for (JRadioButton radioButton : optionButtons) {
				if (radioButton.getActionCommand().equals(subType)) {
					radioButton.setSelected(true);
					break;
				}
			}
		} else {
			for (JRadioButton radioButton : optionButtons) {
				radioButton.setSelected(false);
			}
		}
	}

	protected String getSuggestedSubType() {
		return DigitalizedWorkRepresentationBuilder
				.getRepresentationSubtype(getFilesTreeModel().getPartFiles());
	}

	private JLabel getHeaderPanel() {
		if (this.labelHeader == null) {
			this.labelHeader = new JLabel(
					String
							.format(
									"<html><p style='font-size: 16; font-weight: bold'>%1$s</p><html>",
									Messages
											.getString("Creator.digitalized_work.TITLE")));
			this.labelHeader.setBorder(BorderFactory.createEmptyBorder(5, 5, 5,
					5));
		}
		return this.labelHeader;
	}

	private JPanel getFilesPanel() {
		if (this.panelFiles == null) {
			this.panelFiles = new JPanel(new BorderLayout());

			this.panelFiles.add(getTopLabel(), BorderLayout.NORTH);
			this.panelFiles.add(getScrollTree(), BorderLayout.CENTER);
			this.panelFiles.add(getToolbar(), BorderLayout.EAST);

			this.panelFiles.setBorder(BorderFactory.createEmptyBorder(5, 5, 5,
					5));

		}
		return this.panelFiles;
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

	private JLabel getTopLabel() {
		if (this.labelTop == null) {
			this.labelTop = new JLabel(Messages
					.getString("Creator.digitalized_work.SELECT_IMAGES"));
			this.labelTop
					.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		}
		return this.labelTop;
	}

	private JScrollPane getScrollTree() {
		if (this.scrollPaneFileTree == null) {
			this.scrollPaneFileTree = new JScrollPane();

			this.scrollPaneFileTree.getViewport().add(getFileTree());
		}
		return this.scrollPaneFileTree;
	}

	private JTree getFileTree() {
		if (this.treeFiles == null) {
			this.treeFiles = new DWMetsTree(getFilesTreeModel(), this);

			treeFiles.addTreeSelectionListener(new TreeSelectionListener() {

				public void valueChanged(TreeSelectionEvent e) {
					updateToolbar();

					Object selected = getSelectedNode();
					getPreviewPanel().loadingPreview();
					getMetadataPanel().loadingMetadata();

					if (selected == null) {
						getPreviewPanel().noPreview();
					} else if (selected instanceof DWMetsFptrTreeNode) {
						DWMetsFptrTreeNode fptr = (DWMetsFptrTreeNode) selected;
						RenderedImage image;

						File imageFile = null;
						if (fptr.getFileUrl() != null) {
							URI imageURI = URI.create(fptr.getFileUrl());
							imageFile = new File(imageURI);
						}
						if (imageFile != null
								&& imageFile.length() < 8 * 1024 * 1024) {
							try {
								ParameterBlock pb = new ParameterBlock();
								pb.add(imageFile.getPath());
								image = JAI.create("fileload", pb);
								JadeDisplay img_panel = new JadeDisplay(image,
										new Point(0, 0),
										JadeDisplay.REPAINT_IMMEDIATE, false);
								JScrollPane sp = new JScrollPane(img_panel);
								sp.setViewport(new JViewportImage());
								sp.setViewportView(img_panel);
								new MouseScroller(sp.getViewport());
								getPreviewPanel().setPreview(sp);

								Timer timer = new Timer(1000,
										new ActionListener() {

											public void actionPerformed(
													ActionEvent e) {
												getPreviewPanel().repaint();
												getPreviewPanel().revalidate();

											}

										});
								timer.setRepeats(false);
								timer.start();
							} catch (Exception e1) {
								getPreviewPanel().preview(e1.getMessage());
							}

						} else if (imageFile != null) {
							logger.debug("Image too big to preview: "
									+ imageFile.length() + "B");
							getPreviewPanel()
									.preview(
											Messages
													.getString(
															"Creator.digitalized_work.IMAGE_TOO_BIG_TO_PREVIEW",
															FileUtils
																	.byteCountToDisplaySize(imageFile
																			.length())));
						} else {
							logger
									.error("Fptr file URL is null. fptr: "
											+ fptr);
						}

						getMetadataPanel().noMetadata();

					} else if (selected instanceof DWMetsDivTreeNode) {
						getPreviewPanel().noPreview();
						String metadata;
						if (getFilesTreeModel().getRoot().equals(selected)) {
							metadata = getFilesTreeModel().getRoot()
									.getHelper().getMets().toString();

						} else {
							DWMetsDivTreeNode divNode = (DWMetsDivTreeNode) selected;
							metadata = divNode.getDiv().toString();
						}
						// XmlTextPane metadataArea = new XmlTextPane();
						JTextArea metadataArea = new JTextArea();
						metadataArea.setText(metadata);
						metadataArea.setEditable(false);
						JScrollPane metadataScroll = new JScrollPane(
								metadataArea);
						getMetadataPanel().setMetadata(metadataScroll);
					}
				}

			});

			updateToolbar();
		}
		return this.treeFiles;
	}

	protected void updateToolbar() {
		Object selected = getSelectedNode();

		if (selected == null) {
			getAddSectionAction().setEnabled(false);
			getRemoveSectionAction().setEnabled(false);
			getAddFileAction().setEnabled(false);
			getRemoveFileAction().setEnabled(false);
			getMoveUpAction().setEnabled(false);
			getMoveDownAction().setEnabled(false);
		} else {
			if (selected instanceof DWMetsTreeNode) {
				DWMetsTreeNode metsNode = (DWMetsTreeNode) selected;
				TreeNode parent = metsNode.getParent();

				if (parent != null) {
					int index = getFilesTreeModel().getIndexOfChild(parent,
							metsNode);
					getMoveUpAction().setEnabled(index > 0);
					getMoveDownAction().setEnabled(
							index < parent.getChildCount() - 1);

					if (selected instanceof DWMetsFptrTreeNode) {
						getAddSectionAction().setEnabled(false);
						getRemoveSectionAction().setEnabled(false);
						getAddFileAction().setEnabled(false);
						getRemoveFileAction().setEnabled(true);

					} else {
						getAddSectionAction().setEnabled(true);
						getRemoveSectionAction().setEnabled(true);
						getAddFileAction().setEnabled(true);
						getRemoveFileAction().setEnabled(false);
					}
				} else {
					// is the root node
					getAddSectionAction().setEnabled(true);
					getRemoveSectionAction().setEnabled(false);
					getAddFileAction().setEnabled(true);
					getRemoveFileAction().setEnabled(false);
					getMoveUpAction().setEnabled(false);
					getMoveDownAction().setEnabled(false);
				}
			}

		}

	}

	private DWMetsTreeModel getFilesTreeModel() {
		if (this.treeModelDWMets == null) {
			this.treeModelDWMets = new DWMetsTreeModel(
					getRepresentationObject(), getDWMetsHelper());
		}
		return this.treeModelDWMets;
	}

	private JToolBar getToolbar() {
		if (this.toolbar == null) {
			this.toolbar = new JToolBar(JToolBar.VERTICAL);
			this.toolbar.setFloatable(false);

			this.toolbar.add(getAddSectionAction());
			this.toolbar.add(getRemoveSectionAction());

			this.toolbar.add(new JToolBar.Separator());

			this.toolbar.add(getAddFileAction());
			this.toolbar.add(getRemoveFileAction());

			this.toolbar.add(new JToolBar.Separator());

			this.toolbar.add(getMoveUpAction());
			this.toolbar.add(getMoveDownAction());

		}
		return this.toolbar;
	}

	private Action getAddSectionAction() {
		if (this.actionAddSection == null) {
			this.actionAddSection = new AbstractAction(Messages
					.getString("Creator.digitalized_work.action.ADD_SECTION"),
					Tools.createImageIcon(IMAGE_BASE + "folder_add.png")) {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					Object selected = getSelectedNode();
					if (selected instanceof DWMetsDivTreeNode) {
						DWMetsDivTreeNode node = (DWMetsDivTreeNode) selected;
						DWMetsDivTreeNode addedSection = getFilesTreeModel()
								.addSection(
										node,
										Messages
												.getString("Creator.digitalized_work.NEW_SECTION"));

						treeFiles.expandPath(new TreePath(getFilesTreeModel()
								.getPathToRoot(addedSection)));
					}

				}
			};
			this.actionAddSection.putValue(Action.SHORT_DESCRIPTION, Messages
					.getString("Creator.digitalized_work.action.ADD_SECTION"));
			this.actionAddSection.setEnabled(false);
		}
		return this.actionAddSection;
	}

	private Action getRemoveSectionAction() {
		if (this.actionRemoveSection == null) {
			this.actionRemoveSection = new AbstractAction(
					Messages
							.getString("Creator.digitalized_work.action.REMOVE_SECTION"),
					Tools.createImageIcon(IMAGE_BASE + "folder_delete.png")) {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					Object selected = getSelectedNode();
					if (selected instanceof DWMetsDivTreeNode) {
						DWMetsDivTreeNode divNode = (DWMetsDivTreeNode) selected;
						int confirm = JOptionPane.OK_OPTION;
						if (divNode.getChildCount() > 0) {
							confirm = JOptionPane
									.showConfirmDialog(
											DWRepresentationPanel.this,
											Messages
													.getString(
															"Creator.digitalized_work.warning.CASCADE_SECTION_REMOVE",
															divNode
																	.getChildCount()),
											Messages
													.getString("common.WARNING"),
											JOptionPane.OK_CANCEL_OPTION,
											JOptionPane.WARNING_MESSAGE);
						}
						if (confirm == JOptionPane.OK_OPTION) {
							getFilesTreeModel().remove(divNode, true);
						}
					}
				}
			};
			this.actionRemoveSection
					.putValue(
							Action.SHORT_DESCRIPTION,
							Messages
									.getString("Creator.digitalized_work.action.REMOVE_SECTION"));
			this.actionRemoveSection.setEnabled(false);
		}
		return this.actionRemoveSection;
	}

	private RepresentationBuilder repBuilder = null;

	/**
	 * Get representation builder
	 * 
	 * @return the representation builder
	 */
	public RepresentationBuilder getRepresentationBuilder() {
		if (repBuilder == null) {
			repBuilder = new DigitalizedWorkRepresentationBuilder();
		}
		return repBuilder;
	}

	private Action getAddFileAction() {
		if (this.actionAddFile == null) {
			this.actionAddFile = new AbstractAction(Messages
					.getString("Creator.digitalized_work.action.ADD_IMAGES"),
					Tools.createImageIcon(IMAGE_BASE + "picture_add.png")) {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					Object selected = getSelectedNode();
					if (selected instanceof DWMetsDivTreeNode) {
						DWMetsDivTreeNode divNode = (DWMetsDivTreeNode) selected;
						File[] images = selectImageFiles();
						if (images != null) {
							for (File image : images) {
								getFilesTreeModel().addFile(divNode, image);
							}
						}
						representationObject.setSubType(getSuggestedSubType());
					}
				}
			};
			this.actionAddFile.putValue(Action.SHORT_DESCRIPTION, Messages
					.getString("Creator.digitalized_work.action.ADD_IMAGES"));
			this.actionAddFile.setEnabled(false);
		}
		return this.actionAddFile;
	}

	private Action getRemoveFileAction() {
		if (this.actionRemoveFile == null) {
			this.actionRemoveFile = new AbstractAction(
					Messages
							.getString("Creator.digitalized_work.action.REMOVE_IMAGES"),
					Tools.createImageIcon(IMAGE_BASE + "picture_delete.png")) {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					Object selected = getSelectedNode();
					if (selected instanceof DWMetsFptrTreeNode) {
						DWMetsFptrTreeNode node = (DWMetsFptrTreeNode) selected;
						getFilesTreeModel().remove(node, true);

						representationObject.setSubType(getSuggestedSubType());
					}
				}
			};
			this.actionRemoveFile
					.putValue(
							Action.SHORT_DESCRIPTION,
							Messages
									.getString("Creator.digitalized_work.action.REMOVE_IMAGES"));
			this.actionRemoveFile.setEnabled(false);
		}
		return this.actionRemoveFile;
	}

	private Action getMoveUpAction() {
		if (this.actionMoveUp == null) {
			this.actionMoveUp = new AbstractAction(Messages
					.getString("Creator.digitalized_work.action.MOVE_UP"),
					Tools.createImageIcon(IMAGE_BASE + "move_up.png")) {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					Object selected = getSelectedNode();
					if (selected instanceof DWMetsTreeNode) {
						DWMetsTreeNode node = (DWMetsTreeNode) selected;
						DWMetsTreeNode newNode = getFilesTreeModel().moveUp(
								node);
						setSelectedNode(newNode);
					}
				}
			};
			this.actionMoveUp.putValue(Action.SHORT_DESCRIPTION, Messages
					.getString("Creator.digitalized_work.action.MOVE_UP"));
			this.actionMoveUp.setEnabled(false);
		}
		return this.actionMoveUp;
	}

	private Action getMoveDownAction() {
		if (this.actionMoveDown == null) {
			this.actionMoveDown = new AbstractAction(Messages
					.getString("Creator.digitalized_work.action.MOVE_DOWN"),
					Tools.createImageIcon(IMAGE_BASE + "move_down.png")) {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					Object selected = getSelectedNode();
					if (selected instanceof DWMetsTreeNode) {
						DWMetsTreeNode node = (DWMetsTreeNode) selected;
						DWMetsTreeNode newNode = getFilesTreeModel().moveDown(
								node);
						setSelectedNode(newNode);
					}
				}
			};
			this.actionMoveDown.putValue(Action.SHORT_DESCRIPTION, Messages
					.getString("Creator.digitalized_work.action.MOVE_DOWN"));
			this.actionMoveDown.setEnabled(false);
		}
		return this.actionMoveDown;
	}

	private Object getSelectedNode() {
		return treeFiles.getLastSelectedPathComponent();
	}

	private void setSelectedNode(DWMetsTreeNode node) {
		getFileTree().setSelectionPath(
				new TreePath(getFilesTreeModel().getPathToRoot(node)));
	}

	private DigitalizedWorkMetsHelper dwMetsHelper = null;

	private DigitalizedWorkMetsHelper getDWMetsHelper() {
		if (dwMetsHelper == null) {

			if (getRepresentationObject().getRootFile() != null) {

				String accessURL = getRepresentationObject().getRootFile()
						.getAccessURL();
				try {
					logger.debug("Creating METS helper on " + accessURL);
					dwMetsHelper = DigitalizedWorkMetsHelper
							.newInstance(new File(URI.create(accessURL)));

					logger.debug("Existing METS " + dwMetsHelper.getMets());

				} catch (MetsMetadataException e) {
					logger.error("Error getting Mets Helper", e);
				} catch (IOException e) {
					logger.error("Error getting Mets Helper", e);
				}

			} else {
				logger
						.debug("Representation doesn't have a METS file. Creating a new one.");

				dwMetsHelper = new DigitalizedWorkMetsHelper();
				dwMetsHelper.createRepresentation(getRepresentationObject()
						.getLabel());

				logger.debug("Created METS " + dwMetsHelper.getMets());
			}
		}

		return dwMetsHelper;
	}

	protected File[] selectImageFiles() {
		File[] selectedFiles = null;
		int result = getImageFileChooser().showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			selectedFiles = getImageFileChooser().getSelectedFiles();
		}

		return selectedFiles;
	}

	protected JFileChooser getImageFileChooser() {
		if (imageFileChooser == null) {
			imageFileChooser = new JFileChooser();
			imageFileChooser
					.setFileFilter(new FileNameExtensionFilter(
							Messages
									.getString("Creator.digitalized_work.filedialog.FILTER"),
							Messages
									.getStringArray("Creator.digitalized_work.filedialog.FILE_NAME_EXTENSIONS")));
			imageFileChooser.setMultiSelectionEnabled(true);
		}
		return imageFileChooser;
	}

	/**
	 * Save current representation
	 */
	public void save() {
		logger.debug("Saving digitalized work");
		if (getFilesTreeModel().isChanged()) {
			try {
				RepresentationFile rootFile = getRepresentationObject()
						.getRootFile();
				if (rootFile == null) {
					File metsFile = File.createTempFile("METS", null,
							SIPCreatorConfig.getInstance().getTmpDir());
					metsFile.deleteOnExit();
					rootFile = new RepresentationFile("F0", METS_FILE_NAME,
							RepresentationBuilder.getFileMimetype(metsFile),
							metsFile.length(), metsFile.toURI().toURL()
									.toString());

				}

				File accessRootFile = new File(URI.create(rootFile
						.getAccessURL()));
				logger.debug("Saving mets: " + getDWMetsHelper().getMets());
				getDWMetsHelper().saveToFile(accessRootFile);

				if (getRepresentationObject().getRootFile() == null) {
					getRepresentationObject().setRootFile(rootFile);
				}

				getRepresentationObject().setPartFiles(
						getFilesTreeModel().getPartFiles().toArray(
								new RepresentationFile[getFilesTreeModel()
										.getPartFiles().size()]));
				getFilesTreeModel().setChanged(false);
			} catch (IOException e) {
				logger.error("Error updating METS file", e);
			} catch (MetsMetadataException e) {
				logger.error("Error updating METS file", e);
			}
		}

	}
}
