/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.sipcreator.description.DescriptionPanel;

/**
 * @author Luis Faria
 * 
 */
public class DOPanel extends JPanel {

	private static final long serialVersionUID = 458654748140989775L;
	private static final Logger logger = Logger.getLogger(DOPanel.class);

	// data
	private DescriptionObject dObj;

	// UI
	private JPanel header = null;
	private JLabel title = null;

	private DescriptionPanel descriptionPanel;

	/**
	 * Description Object Panel
	 * 
	 * @param dObj
	 */
	public DOPanel(DescriptionObject dObj) {
		this.dObj = dObj;

		init();
	}

	private JPanel getHeader() {
		if (header == null) {
			header = new JPanel();
			header.add(getTitle());
		}
		return header;
	}

	private Component getTitle() {
		if (title == null) {
			title = new JLabel();
			try {
				title.setText(String.format(
						"<html><p style='font-size: 20'>%1$s</p></html>",
						MyClassificationPlanHelper.getInstance()
								.getCompleteReference(dObj)));
			} catch (Exception e) {
				logger.error("Error getting DO complete reference", e);
				title.setText(String.format(
						"<html><p style='font-size: 20'>%1$s</p></html>", dObj
								.getId()));
			}
		}
		return title;
	}

	private DescriptionPanel getDescriptionPanel() {
		if (descriptionPanel == null) {
			descriptionPanel = new DescriptionPanel();
			descriptionPanel.setDescriptionObject(dObj);
			descriptionPanel.setReadonly(true);
		}
		return descriptionPanel;
	}

	private void init() {
		setLayout(new BorderLayout());

		add(getHeader(), BorderLayout.NORTH);
		add(getDescriptionPanel(), BorderLayout.CENTER);

	}

}
