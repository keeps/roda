/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.InvalidDescriptionObjectException;
import pt.gov.dgarq.roda.ingest.siputility.SIPException;
import pt.gov.dgarq.roda.ingest.siputility.SIPUtility;
import pt.gov.dgarq.roda.ingest.siputility.data.DataChangeListener;
import pt.gov.dgarq.roda.ingest.siputility.data.DataChangedEvent;

/**
 * @author Luis Faria
 * 
 */
public class SIP extends pt.gov.dgarq.roda.ingest.siputility.data.SIP implements
		Transferable {

	private static final Logger logger = Logger.getLogger(SIP.class);

	private static final ImageIcon invalidSipIcon = Tools
			.createImageIcon("sip_invalid.png");
	private static final ImageIcon validSipIcon = Tools
			.createImageIcon("sip_valid.png");
	private static final ImageIcon sentSipIcon = Tools
			.createImageIcon("sip_commit.png");

	/**
	 * SIP data flavor
	 */
	public static final DataFlavor SIP_FLAVOR = new DataFlavor(SIP.class, "SIP");

	private boolean sent;

	private boolean changed;

	/**
	 * Extension of SIP, adding the 'was sent' flag
	 */
	public SIP() {
		super();
		sent = false;
		changed = true;
	}

	/**
	 * Create a new SIP, wrapping the argument SIP
	 * 
	 * @param sip
	 */
	public SIP(SIP sip) {
		super(sip);
		this.sent = sip.wasSent();
		this.changed = sip.isChanged();

	}

	/**
	 * Create a new SIP, wrapping the argument SIP
	 * 
	 * @param sip
	 * @param sent
	 */
	public SIP(pt.gov.dgarq.roda.ingest.siputility.data.SIP sip, boolean sent) {
		super(sip);
		this.sent = sent;
		addChangeListener(new DataChangeListener() {

			public void dataChanged(DataChangedEvent evtDataChanged) {
				changed = true;
				try {
					confirmChange();
				} catch (IOException e) {
					logger.error("Error confirming SIP change", e);
				} catch (SIPException e) {
					logger.error("Error confirming SIP change", e);
				}
			}

		});
	}

	/**
	 * Was this SIP already sent to RODA
	 * 
	 * @return true if it was sent, false otherwise
	 */
	public boolean wasSent() {
		return sent;
	}

	/**
	 * Mark this SIP was already sent to RODA
	 * 
	 * @param sent
	 * @throws IOException
	 */
	public void setSent(boolean sent) throws IOException {
		logger.debug("!!!!Setting SIP as sent=" + sent);
		if (!this.sent && sent) {
			logger.debug("!!!this.sent=" + this.sent + " sent=" + sent);
			logger.debug("!!!directory:" + getDirectory());
			File sipSentDir = SIPCreatorConfig.getInstance().getSipSentDir();
			File newSipDir = new File(sipSentDir, getDirectory().getName());
			FileUtils.moveDirectory(getDirectory(), newSipDir);
			if (getDirectory().exists()) {
				logger.warn("!!!Original directory still exists! "
						+ getDirectory().getPath());
				boolean quietly = FileUtils.deleteQuietly(getDirectory());
				if (!quietly) {
					logger.warn("!!!Still directory exists!!!!");
				}
			}
			setDirectory(newSipDir);
			changed = false;
		} else if (this.sent && !sent) {
			logger.debug("!!!this.sent=" + this.sent + " sent=" + sent);
			File sipDraftDir = SIPCreatorConfig.getInstance().getSipDraftDir();
			File newSipDir = new File(sipDraftDir, getDirectory().getName());
			FileUtils.moveDirectory(getDirectory(), newSipDir);
			if (getDirectory().exists()) {
				logger.warn("!!!Original directory still exists! "
						+ getDirectory().getPath());
				boolean quietly = FileUtils.deleteQuietly(getDirectory());
				if (!quietly) {
					logger.warn("!!!Still directory exists!!!!");
				}
			}
			setDirectory(newSipDir);
		}
		this.sent = sent;
	}

	/**
	 * Get transfer data
	 * 
	 * @param flavor
	 * @return the data on the defined flavor
	 * @throws UnsupportedFlavorException
	 * @throws IOException
	 */
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (flavor.equals(SIP_FLAVOR)) {
			return this;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	/**
	 * Is data flavor supported
	 * 
	 * @param flavor
	 * @return true if supported
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(SIP_FLAVOR);
	}

	/**
	 * Get transfer data flavors
	 * 
	 * @return the flavors
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { SIP_FLAVOR };
	}

	/**
	 * Get SIP icon
	 * 
	 * @return the icon for this SIP
	 */
	public ImageIcon getIcon() {
		ImageIcon ret;
		if (wasSent()) {
			ret = sentSipIcon;
		} else if (isIngestValid()) {
			ret = validSipIcon;
		} else {
			ret = invalidSipIcon;
		}
		return ret;
	}

	/**
	 * Is this SIP ready for save
	 * 
	 * @return true if it is ready
	 */
	public boolean isSaveValid() {
		boolean ret;
		try {
			SIPUtility.validateForSaving(this);
			ret = true;
		} catch (SIPException e) {
			ret = false;
		} catch (InvalidDescriptionObjectException e) {
			ret = false;
		}
		return ret;
	}

	/**
	 * Is this SIP ready for ingest
	 * 
	 * @return true if it is ready
	 */
	public boolean isIngestValid() {
		boolean ret;
		try {
			SIPUtility.validateForIngest(this);
			ret = true;
		} catch (SIPException e) {
			ret = false;
		} catch (InvalidDescriptionObjectException e) {
			ret = false;
		}
		return ret;
	}

	/**
	 * Confirm change when SIP was already sent
	 * 
	 * @return true if can change was confirmed
	 * @throws IOException
	 * @throws SIPException
	 */
	public boolean confirmChange() throws IOException, SIPException {
		boolean ret = true;
		if (wasSent()) {
			int showConfirmDialog = JOptionPane.showConfirmDialog(SIPCreator
					.getInstance().getMainFrame(), Messages.getString(
					"SIP.warning.MODIFYING_SENT_SIP", getCompleteReference()),
					Messages.getString("common.WARNING"),
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (showConfirmDialog == JOptionPane.NO_OPTION) {
				ret = false;
			} else {
				setSent(false);
				save();
			}
		}
		return ret;
	}

	/**
	 * Save SIP
	 * 
	 * @return true if save was successful
	 * @throws IOException
	 * @throws SIPException
	 */
	public boolean save() throws IOException, SIPException {
		File sipDir = null;
		boolean success = false;
		logger.debug("SIP save: " + this);
		logger.debug("SIP save: checking for changes after sent");
		if (!confirmChange()) {
			logger
					.debug("SIP save: exiting because SIP was sent and change was denied");
			return false;
		}

		logger.debug("SIP save: checking if it is changed");
		if (!isChanged()) {
			logger.debug("SIP save: exiting because SIP did not change");
			return true;
		}

		if (getDirectory() == null) {
			logger.debug("SIP save: creating SIP directory");
			sipDir = createSipDirectory(SIPCreatorConfig.getInstance()
					.getSipDraftDir());
		} else {
			logger.debug("SIP save: using existing SIP directory");
			sipDir = getDirectory();

		}

		if (sipDir != null) {
			logger.debug("SIP save: writing SIP");
			SIPUtility.writeSIP(this, sipDir);
			logger.debug("SIP save: setting new directory");
			setDirectory(sipDir);
			logger.debug("SIP save: setting not sent");
			setSent(false);
			logger.debug("SIP save: finished");
			success = true;
			changed = false;
		}

		logger.debug("SIP save: exiting with success = " + success);

		return success;
	}

	private File createSipDirectory(File baseDir) {
		String reference = getCompleteReference().replace('/', '_');
		File ret = new File(baseDir, reference);

		int index = 0;
		while (ret.exists()) {
			index++;
			ret = new File(baseDir, reference + "_(" + index + ")");
		}

		return ret;
	}

	/**
	 * Get complete reference
	 * 
	 * @return the complete reference
	 */
	public String getCompleteReference() {
		return MyClassificationPlanHelper.getInstance().getCompleteReference(
				this.getDescriptionObject(), this);
	}

	/**
	 * Was SIP changed after last save
	 * 
	 * @return true if it was changed
	 */
	public boolean isChanged() {
		return changed;
	}

}
