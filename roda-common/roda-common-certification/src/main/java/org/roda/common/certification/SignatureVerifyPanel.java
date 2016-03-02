package org.roda.common.certification;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.bouncycastle.cms.CMSException;

/**
 * Panel which allow a signature to be verified
 * 
 * @author Luis Faria
 * 
 */
public class SignatureVerifyPanel extends JPanel {
  private static final long serialVersionUID = 2729277492782817659L;

  private File file = null;
  private File signature = null;

  private JPanel selectFormLayout;

  private JButton selectFileButton = null;
  private JButton selectSignatureButton = null;
  private JFileChooser fileChooser = null;
  private JFileChooser signatureChooser = null;
  private JLabel selectedFileLabel = null;
  private JLabel selectedSignatureLabel = null;

  private JLabel resultLabel = null;

  private SignatureUtility signatureUtility = null;

  /**
   * Create a new signature verify panel
   */
  public SignatureVerifyPanel() {
    super();
    try {
      signatureUtility = new SignatureUtility();
    } catch (KeyStoreException e) {
      JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    } catch (NoSuchAlgorithmException e) {
      JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    } catch (NoSuchProviderException e) {
      JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    if (signatureUtility == null) {
      System.exit(ERROR);
    } else {
      initComponents();
    }

  }

  protected void initComponents() {
    setLayout(new BorderLayout());
    add(getSelectFormLayout(), BorderLayout.NORTH);
    add(getResultLabel(), BorderLayout.CENTER);
  }

  protected JPanel getSelectFormLayout() {
    if (selectFormLayout == null) {
      selectFormLayout = new JPanel(new SpringLayout());
      selectFormLayout.add(getSelectFileButton());
      selectFormLayout.add(getSelectFileLabel());
      selectFormLayout.add(getSelectSignatureButton());
      selectFormLayout.add(getSelectSignatureLabel());

      SpringUtilities.makeCompactGrid(selectFormLayout, 2, 2, 5, 5, 5, 5);
    }
    return selectFormLayout;
  }

  protected JButton getSelectFileButton() {
    if (selectFileButton == null) {
      selectFileButton = new JButton("Select file...");
      selectFileButton.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          chooseFile();
        }

      });
    }
    return selectFileButton;
  }

  protected JButton getSelectSignatureButton() {
    if (selectSignatureButton == null) {
      selectSignatureButton = new JButton("Select signature...");
      selectSignatureButton.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          chooseSignature();
        }

      });
    }
    return selectSignatureButton;
  }

  protected JLabel getSelectFileLabel() {
    if (selectedFileLabel == null) {
      selectedFileLabel = new JLabel();
    }
    return selectedFileLabel;
  }

  protected JLabel getSelectSignatureLabel() {
    if (selectedSignatureLabel == null) {
      selectedSignatureLabel = new JLabel();
    }
    return selectedSignatureLabel;
  }

  protected JFileChooser getFileChooser() {
    if (fileChooser == null) {
      fileChooser = new JFileChooser();
    }
    return fileChooser;
  }

  protected JFileChooser getSignatureChooser() {
    if (signatureChooser == null) {
      signatureChooser = new JFileChooser();
    }
    return signatureChooser;
  }

  protected void chooseFile() {
    int status = getFileChooser().showOpenDialog(this);

    if (status == JFileChooser.APPROVE_OPTION) {
      file = getFileChooser().getSelectedFile();
      getSelectFileLabel().setText(file.getPath());
    } else {
      file = null;
      getSelectFileLabel().setText("");
    }

    updateResult();

  }

  protected void chooseSignature() {
    int status = getSignatureChooser().showOpenDialog(this);

    if (status == JFileChooser.APPROVE_OPTION) {
      signature = getSignatureChooser().getSelectedFile();
      getSelectSignatureLabel().setText(signature.getPath());
    } else {
      signature = null;
      getSelectSignatureLabel().setText("");
    }

    updateResult();

  }

  protected JLabel getResultLabel() {
    if (resultLabel == null) {
      resultLabel = new JLabel();
      updateResult();
    }
    return resultLabel;
  }

  protected void updateResult() {
    if (file == null) {
      resultLabel.setText("Please select file");
    } else if (signature == null) {
      resultLabel.setText("Please select signature");
    } else {
      try {
        boolean valid = signatureUtility.verify(file, signature);
        if (valid) {
          resultLabel.setText("VALID");
        } else {
          resultLabel.setText("NOT VALID");
        }
      } catch (CertificateExpiredException e) {
        resultLabel.setText("NOT VALID, certificate expired: " + e.getMessage());
      } catch (CertificateNotYetValidException e) {
        resultLabel.setText("NOT VALID, certificate not yet valid: " + e.getMessage());
      } catch (NoSuchAlgorithmException e) {
        resultLabel.setText("NOT VALID, internal error: " + e.getMessage());
      } catch (NoSuchProviderException e) {
        resultLabel.setText("NOT VALID, internal error: " + e.getMessage());
      } catch (CertStoreException e) {
        resultLabel.setText("NOT VALID, internal error: " + e.getMessage());
      } catch (FileNotFoundException e) {
        resultLabel.setText("NOT VALID, internal error: " + e.getMessage());
      } catch (CMSException e) {
        resultLabel.setText("NOT VALID, internal error: " + e.getMessage());
      } catch (IOException e) {
        resultLabel.setText("NOT VALID, internal error: " + e.getMessage());
      }
    }

  }

}
