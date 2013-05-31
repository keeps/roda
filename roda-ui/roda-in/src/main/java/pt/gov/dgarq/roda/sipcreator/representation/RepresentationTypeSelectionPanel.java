/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator.representation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;

import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.sipcreator.Messages;
import pt.gov.dgarq.roda.sipcreator.Tools;

/**
 * @author Luis Faria
 * 
 */
public class RepresentationTypeSelectionPanel extends JPanel {
	private static final long serialVersionUID = 1815965515328293090L;

	/**
	 * Representation type selection listener
	 * 
	 * @author Luis Faria
	 * 
	 */
	public interface RepresentationTypeSelectionListener {
		/**
		 * On representation type selected
		 * 
		 * @param type
		 *            the base representation type
		 * @param subtype
		 *            the sub type, or MIME type
		 */
		public void onRepresentationTypeSelected(String type, String subtype);
	}

	private JScrollPane representationTypesScroll = null;
	private JList representationTypes = null;
	private List<RepresentationTypeSelectionListener> listeners;

	/**
	 * Create a new representation type selection panel
	 */
	public RepresentationTypeSelectionPanel() {
		setLayout(new BorderLayout());
		add(getRepresentationTypesScroll(), BorderLayout.CENTER);
		listeners = new ArrayList<RepresentationTypeSelectionListener>();
		setPreferredSize(new Dimension(500, 680));
	}

	private JScrollPane getRepresentationTypesScroll() {
		if (representationTypesScroll == null) {
			representationTypesScroll = new JScrollPane(
					getRepresentationTypes());
		}
		return representationTypesScroll;
	}

	private RepresentationTypeInfo getInfo(String repType) {
		return new RepresentationTypeInfo(
				repType,
				null,
				repType + ".png",
				Messages.getString("Representation." + repType + ".TITLE"),
				Messages
						.getString("Representation." + repType + ".DESCRIPTION"));
	}

	private JList getRepresentationTypes() {
		if (representationTypes == null) {
			Vector<RepresentationTypeInfo> types = new Vector<RepresentationTypeInfo>();
			types.add(getInfo(RepresentationObject.STRUCTURED_TEXT));
			types.add(getInfo(RepresentationObject.DIGITALIZED_WORK));
			types.add(getInfo(RepresentationObject.AUDIO));
			types.add(getInfo(RepresentationObject.VIDEO));
			types.add(getInfo(RepresentationObject.RELATIONAL_DATABASE));
			types.add(getInfo(RepresentationObject.UNKNOWN));

			representationTypes = new JList(types);
			representationTypes.setCellRenderer(new ListCellRenderer() {

				public Component getListCellRendererComponent(JList list,
						Object value, int index, boolean isSelected,
						boolean cellHasFocus) {
					Component ret = null;
					if (value instanceof RepresentationTypeInfo) {
						ret = getRepresentationTypePanel((RepresentationTypeInfo) value);

					}

					return ret;
				}

			});

			representationTypes.addMouseListener(new MouseListener() {

				public void mouseClicked(MouseEvent e) {
					RepresentationTypeInfo selected = (RepresentationTypeInfo) representationTypes
							.getSelectedValue();
					onRepresentationTypeSelected(selected.getType(), selected
							.getSubtype());

				}

				public void mouseEntered(MouseEvent e) {
					representationTypes.setSelectedIndex(representationTypes
							.locationToIndex(e.getPoint()));
				}

				public void mouseExited(MouseEvent e) {
					representationTypes.clearSelection();
				}

				public void mousePressed(MouseEvent e) {
					// nothing to do
				}

				public void mouseReleased(MouseEvent e) {
					// nothing to do

				}

			});

			representationTypes
					.addMouseMotionListener(new MouseMotionListener() {

						public void mouseDragged(MouseEvent e) {
							representationTypes
									.setSelectedIndex(representationTypes
											.locationToIndex(e.getPoint()));

						}

						public void mouseMoved(MouseEvent e) {
							representationTypes
									.setSelectedIndex(representationTypes
											.locationToIndex(e.getPoint()));

						}

					});
		}
		return representationTypes;
	}

	/**
	 * Representation type information container class
	 * 
	 * @author Luis Faria
	 * 
	 */
	public class RepresentationTypeInfo {
		private String type;
		private String subtype;
		private String iconPath;
		private String title;
		private String description;

		/**
		 * Create a new representation type information class
		 * 
		 * @param type
		 * 
		 * @param subtype
		 * @param iconPath
		 * @param title
		 * @param description
		 */
		public RepresentationTypeInfo(String type, String subtype,
				String iconPath, String title, String description) {
			this.type = type;
			this.subtype = subtype;
			this.iconPath = iconPath;
			this.title = title;
			this.description = description;
		}

		/**
		 * Get type
		 * 
		 * @return the base type
		 */
		public String getType() {
			return type;
		}

		/**
		 * Set type
		 * 
		 * @param type
		 */
		public void setType(String type) {
			this.type = type;
		}

		/**
		 * Get sub type, or MIME type
		 * 
		 * @return the sub type
		 */
		public String getSubtype() {
			return subtype;
		}

		/**
		 * Set sub type, or MIME type
		 * 
		 * @param subtype
		 */
		public void setSubtype(String subtype) {
			this.subtype = subtype;
		}

		/**
		 * Get icon path
		 * 
		 * @return the icon path
		 */
		public String getIconPath() {
			return iconPath;
		}

		/**
		 * Set icon path
		 * 
		 * @param iconPath
		 */
		public void setIconPath(String iconPath) {
			this.iconPath = iconPath;
		}

		/**
		 * Get title
		 * 
		 * @return the title
		 */
		public String getTitle() {
			return title;
		}

		/**
		 * Set title
		 * 
		 * @param title
		 */
		public void setTitle(String title) {
			this.title = title;
		}

		/**
		 * Get description
		 * 
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * Set description
		 * 
		 * @param description
		 */
		public void setDescription(String description) {
			this.description = description;
		}

	}

	protected JPanel getRepresentationTypePanel(RepresentationTypeInfo info) {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel icon = new JLabel();
		icon
				.setIcon(Tools
						.createImageIcon("/pt/gov/dgarq/roda/sipcreator/representationType/"
								+ info.getIconPath()));
		JLabel title = new JLabel(String.format("<html><h3>%1$s</h3></html>",
				info.getTitle()));
		JTextArea description = new JTextArea(info.getDescription());
		Tools.makeTextAreaLookLikeLable(description);

		panel.add(title, BorderLayout.NORTH);
		panel.add(icon, BorderLayout.WEST);
		panel.add(description, BorderLayout.CENTER);

		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		description.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		return panel;
	}

	/**
	 * Add representation type selection listener
	 * 
	 * @param listener
	 */
	public void addRepresentationTypeSelectionListener(
			RepresentationTypeSelectionListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove representation type selection listener
	 * 
	 * @param listener
	 */
	public void removeRepresentationTypeSelectionListener(
			RepresentationTypeSelectionListener listener) {
		listeners.remove(listener);
	}

	protected void onRepresentationTypeSelected(String type, String subtype) {
		for (RepresentationTypeSelectionListener listener : listeners) {
			listener.onRepresentationTypeSelected(type, subtype);
		}
	}

}
