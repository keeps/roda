/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator;

import javax.swing.JOptionPane;

import org.apache.log4j.net.SMTPAppender;
import org.apache.log4j.spi.TriggeringEventEvaluator;

/**
 * @author Luis Faria
 * 
 */
public class RodaSMTPAppender extends SMTPAppender {

	/**
	 * From super class
	 */
	public RodaSMTPAppender() {
		super();
	}

	/**
	 * From super class
	 * 
	 * @param arg0
	 */
	public RodaSMTPAppender(TriggeringEventEvaluator arg0) {
		super(arg0);
	}

	@Override
	protected boolean checkEntryConditions() {
		boolean ret = super.checkEntryConditions();
		if (ret) {
			int confirm = JOptionPane.showConfirmDialog(SIPCreator
					.getInstance().getMainFrame(), Messages
					.getString("ErrorReport.MESSAGE"), Messages
					.getString("ErrorReport.TITLE"), JOptionPane.YES_NO_OPTION);
			ret &= (confirm == JOptionPane.YES_OPTION);

		}
		return ret;
	}

}
