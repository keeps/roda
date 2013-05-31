package pt.gov.dgarq.roda.sipcreator;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;

import pt.gov.dgarq.roda.core.common.InvalidDescriptionObjectException;
import pt.gov.dgarq.roda.ingest.siputility.SIPException;
import pt.gov.dgarq.roda.ingest.siputility.SIPUtility;
import pt.gov.dgarq.roda.ingest.siputility.data.DataChangeListener;
import pt.gov.dgarq.roda.ingest.siputility.data.DataChangedEvent;
import pt.gov.dgarq.roda.sipcreator.SIPActionHelper.SaveSipActionListener;

/**
 * 
 * @author Luis Faria
 * 
 */
public class SIPPanel extends JPanel {

	private static final long serialVersionUID = -3587310153921894656L;

	// data
	private final SIP sip;

	// UI
	private JPanel header = null;
	private JLabel title = null;
	private JPanel centerLayout = null;

	private JLabel pathLabel = null;
	private JLabel pathValue = null;

	private JLabel parentLabel = null;
	private JLabel parentValue = null;

	private JLabel validLabel = null;
	private JLabel validValue = null;

	private JLabel changedLabel = null;
	private JLabel changedValue = null;

	private JLabel sentLabel = null;
	private JLabel sentValue = null;

	private JToolBar toolbar = null;
	private JButton saveSIP = null;
	private JButton validateSIPForIngest = null;

	/**
	 * Create a new SIP panel
	 * 
	 * @param sip
	 */
	public SIPPanel(SIP sip) {
		super();
		this.sip = sip;
		sip.addChangeListener(new DataChangeListener() {

			public void dataChanged(DataChangedEvent evtDataChanged) {
				update();
			}

		});

		init();
		update();

	}

	private void init() {
		setLayout(new BorderLayout());

		header = new JPanel();
		title = new JLabel();
		title.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		header.add(title);

		centerLayout = new JPanel(new SpringLayout());
		centerLayout.setBorder(BorderFactory
				.createBevelBorder(BevelBorder.LOWERED));

		pathLabel = new JLabel(Messages.getString("SIPPanel.PATH"),
				JLabel.TRAILING);
		pathValue = new JLabel();

		parentLabel = new JLabel(Messages.getString("SIPPanel.PARENT"),
				JLabel.TRAILING);
		parentValue = new JLabel();

		validLabel = new JLabel(Messages.getString("SIPPanel.VALID"),
				JLabel.TRAILING);
		validValue = new JLabel();

		changedLabel = new JLabel(Messages.getString("SIPPanel.HAS_CHANGES"),
				JLabel.TRAILING);
		changedValue = new JLabel();

		sentLabel = new JLabel(Messages.getString("SIPPanel.SENT"),
				JLabel.TRAILING);
		sentValue = new JLabel();

		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		saveSIP = new JButton(SIPActionHelper.createSaveSipAction(this, sip,
				new SaveSipActionListener() {

					public void onAfterSipSave() {
						// nothing to do

					}

					public void onBeforeSipSave() {
						// nothing to do

					}

				}));
		validateSIPForIngest = new JButton(SIPActionHelper
				.createIngestValidateAction(this, sip));

		toolbar.add(saveSIP);
		toolbar.add(validateSIPForIngest);

		centerLayout.add(pathLabel);
		centerLayout.add(pathValue);
		centerLayout.add(parentLabel);
		centerLayout.add(parentValue);
		centerLayout.add(validLabel);
		centerLayout.add(validValue);
		centerLayout.add(changedLabel);
		centerLayout.add(changedValue);
		centerLayout.add(sentLabel);
		centerLayout.add(sentValue);

		Font font = pathLabel.getFont().deriveFont(
				pathLabel.getFont().getStyle() ^ Font.BOLD);

		pathLabel.setFont(font);
		parentLabel.setFont(font);
		validLabel.setFont(font);
		changedLabel.setFont(font);
		sentLabel.setFont(font);

		SpringUtilities.makeCompactGrid(centerLayout, 5, 2, 5, 5, 5, 5);

		add(header, BorderLayout.NORTH);
		add(centerLayout, BorderLayout.CENTER);
		add(toolbar, BorderLayout.SOUTH);

	}

	protected void update() {
		updateTitle();
		pathValue
				.setText(sip.getDirectory() != null ? sip.getDirectory()
						.getAbsolutePath() : Messages
						.getString("SIPPanel.SIP_NO_PATH"));
		parentValue.setText(sip.getParentPID());
		try {
			SIPUtility.validateForIngest(sip);
			validValue.setText(Messages.getString("SIPPanel.SIP_IS_VALID"));
		} catch (SIPException e) {
			validValue.setText(Messages.getString("SIPPanel.SIP_NOT_VALID", e
					.getMessage()));
		} catch (InvalidDescriptionObjectException e) {
			validValue.setText(Messages.getString("SIPPanel.SIP_NOT_VALID", e
					.getMessage()));
		}

		changedValue.setText(sip.isChanged() ? Messages
				.getString("SIPPanel.SIP_CHANGED_TRUE") : Messages
				.getString("SIPPanel.SIP_CHANGED_FALSE"));
		sentValue.setText(sip.wasSent() ? Messages
				.getString("SIPPanel.SIP_SENT_TRUE") : Messages
				.getString("SIPPanel.SIP_SENT_FALSE"));

	}

	protected void updateTitle() {
		if (sip.getDescriptionObject() != null
				&& sip.getDescriptionObject().getId() != null) {
			title.setText(String.format(
					"<html><p style='font-size: 20'>%1$s</p></html>",
					MyClassificationPlanHelper.getInstance()
							.getCompleteReference(sip.getDescriptionObject(),
									sip)));
		} else {
			title.setText(String.format(
					"<html><p style='font-size: 20'>%1$s</p></html>", Messages
							.getString("SIPPanel.NEW_SIP_TITLE")));
		}
	}

}
