/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator.upload;

import java.awt.Component;
import java.awt.Dimension;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.sipcreator.FondsPanel;
import pt.gov.dgarq.roda.sipcreator.SIP;
import pt.gov.dgarq.roda.sipcreator.SIPCreator;
import pt.gov.dgarq.roda.sipcreator.Tools;

/**
 * @author Luis Faria
 * 
 */
public class SIPListPanel {

	private static final Logger logger = Logger.getLogger(SIPListPanel.class);

	private JPanel layout = null;
	private JScrollPane scroll = null;

	private final List<CheckedSIP> sips;

	/**
	 * Create new SIP List panel
	 */
	public SIPListPanel() {
		sips = new ArrayList<CheckedSIP>();
	}

	/**
	 * Get component
	 * 
	 * @return sip list panel component
	 */
	public Component getComponent() {
		return getScroll();
	}

	private Component getScroll() {
		if (scroll == null) {
			scroll = new JScrollPane(getLayout());
			scroll.setPreferredSize(new Dimension(500, 300));
		}
		return scroll;
	}

	private Component getLayout() {
		if (layout == null) {
			layout = new JPanel();
			layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
			update();
		}
		return layout;
	}

	private static final URL validSipIconSrc = SIPListPanel.class
			.getResource("/pt/gov/dgarq/roda/sipcreator/sip_valid.png");
	private static final URL sentSipIconSrc = SIPListPanel.class
			.getResource("/pt/gov/dgarq/roda/sipcreator/sip_commit.png");

	private void update() {
		layout.removeAll();
		updateSipList();
		for (final CheckedSIP chSip : sips) {
			String label = chSip.getSip().getCompleteReference();
			logger.debug("images: " + sentSipIconSrc + " " + validSipIconSrc);
			final JCheckBox checkbox = new JCheckBox(
					String
							.format(
									"<html><span><img src='%1$s'>&nbsp;%2$s</span></html>",
									chSip.getSip().wasSent() ? sentSipIconSrc
											: validSipIconSrc, label), chSip
							.isChecked());
			checkbox.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					chSip.setChecked(checkbox.isSelected());
				}

			});
			layout.add(checkbox);
		}

	}

	private void updateSipList() {
		sips.clear();
		for (SIP sip : getValidDraftSIPs()) {
			sips.add(new CheckedSIP(sip));
		}

	}

	private List<SIP> getValidDraftSIPs() {
		List<SIP> ret = new Vector<SIP>();
		FondsPanel fondsPanel = SIPCreator.getInstance().getMainFrame()
				.getMainPanel().getFondsPanel();
		fondsPanel.saveAllSIP();
		Set<SIP> sipSet = fondsPanel.getSIPSet();

		for (SIP sip : sipSet) {
			if (!sip.wasSent() && sip.isIngestValid()) {
				ret.add(sip);
			}
		}
		Tools.sortSipList(ret);

		return ret;
	}

	/**
	 * Get the user selected SIP list
	 * 
	 * @return the list of SIPs that the user left checked
	 */
	public List<SIP> getCheckedSIPList() {
		List<SIP> checked = new ArrayList<SIP>();
		for (CheckedSIP chSip : sips) {
			if (chSip.isChecked()) {
				checked.add(chSip.getSip());
			}
		}
		return checked;
	}

}
