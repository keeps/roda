/**
 * 
 */
package pt.gov.dgarq.roda.common.certification;

import java.awt.BorderLayout;

import javax.swing.JFrame;

/**
 * @author Luis Faria
 * 
 */
public class SignatureVerifyFrame extends JFrame {
	private static final long serialVersionUID = -511828264271668378L;

	private SignatureVerifyPanel signatureVerifyPanel = null;

	/**
	 * Create a new signature verify frame
	 */
	public SignatureVerifyFrame() {
		initComponents();
	}

	private void initComponents() {
		setTitle("RODA: Signature Verification Tool");
		setLayout(new BorderLayout());
		add(getSignatureVerifyPanel(), BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	protected SignatureVerifyPanel getSignatureVerifyPanel() {
		if (signatureVerifyPanel == null) {
			signatureVerifyPanel = new SignatureVerifyPanel();
		}
		return signatureVerifyPanel;
	}

	/**
	 * Show a new frame
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SignatureVerifyFrame instance = new SignatureVerifyFrame();
		// TODO check arguments for file and signature path
		instance.setVisible(true);
	}

}
