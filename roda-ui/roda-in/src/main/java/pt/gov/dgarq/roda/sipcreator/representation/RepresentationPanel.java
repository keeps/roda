package pt.gov.dgarq.roda.sipcreator.representation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.ingest.siputility.data.DataChangeListener;
import pt.gov.dgarq.roda.ingest.siputility.data.DataChangedEvent;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;
import pt.gov.dgarq.roda.sipcreator.representation.audio.AudioRepresentationPanel;
import pt.gov.dgarq.roda.sipcreator.representation.database.RdbRepresentationPanel;
import pt.gov.dgarq.roda.sipcreator.representation.email.EmailRepresentationPanel;
import pt.gov.dgarq.roda.sipcreator.representation.image.DWRepresentationPanel;
import pt.gov.dgarq.roda.sipcreator.representation.presentation.PresentationRepresentationPanel;
import pt.gov.dgarq.roda.sipcreator.representation.spreadsheet.SpreadsheetRepresentationPanel;
import pt.gov.dgarq.roda.sipcreator.representation.text.STRepresentationPanel;
import pt.gov.dgarq.roda.sipcreator.representation.unknown.UnknownRepresentationPanel;
import pt.gov.dgarq.roda.sipcreator.representation.vector.VectorGraphicRepresentationPanel;
import pt.gov.dgarq.roda.sipcreator.representation.video.VideoRepresentationPanel;

/**
 * @author Rui Castro
 * @author Luis Faria
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public class RepresentationPanel extends JPanel implements DataChangeListener {

    private static final long serialVersionUID = 3181608791108334033L;
    private static final Logger logger = Logger
            .getLogger(RepresentationPanel.class);
    private Component currentRepresentationPanel = null;
    private SIPRepresentationObject representationObject = null;

    /**
     * Constructs a new {@link RepresentationPanel}.
     *
     * @param representationObject
     */
    public RepresentationPanel(SIPRepresentationObject representationObject) {
        this.representationObject = representationObject;
        initComponents();
    }

    /**
     * @return the representationObject
     */
    public SIPRepresentationObject getRepresentationObject() {
        return representationObject;
    }

    /**
     * @param rObject the representationObject to set
     */
    public void setRepresentationObject(SIPRepresentationObject rObject) {

        if (this.representationObject != null
                && this.representationObject != rObject) {
            this.representationObject.removeChangeListener(this);
        }

        this.representationObject = rObject;

        if (this.representationObject != null) {

            this.representationObject.addChangeListener(this);

        } else {
            logger.debug("RepresentationObject is null");
        }

        updateRepresentationPanel();

    }

    /**
     * @see DataChangeListener#dataChanged(DataChangedEvent)
     */
    public void dataChanged(DataChangedEvent evtDataChanged) {
        updateRepresentationPanel();
    }

    /**
     * Save all data
     */
    public void save() {
        if (representationObject != null) {
            if (SIPRepresentationObject.DIGITALIZED_WORK
                    .equalsIgnoreCase(representationObject.getType())) {
                DWRepresentationPanel dwRepPanel = (DWRepresentationPanel) currentRepresentationPanel;
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                dwRepPanel.save();
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }
    private static RepresentationPanel playingPanel = null;

    /**
     * Call start preview on current representation panel
     */
    public void startPreview() {
        if (playingPanel != null && playingPanel != this) {
            playingPanel.stopPreview();
        }

        if (playingPanel != this
                && currentRepresentationPanel instanceof AbstractRepresentationPanel) {
            logger.debug("Start representation panel preview");
            AbstractRepresentationPanel repPanel = (AbstractRepresentationPanel) currentRepresentationPanel;
            repPanel.startPreview();
        }

        playingPanel = this;
    }

    /**
     * Call stop preview on current representation panel
     */
    public void stopPreview() {
        if (playingPanel == this) {
            playingPanel = null;
        }

        if (currentRepresentationPanel instanceof AbstractRepresentationPanel) {
            logger.debug("Stop representation panel preview");
            AbstractRepresentationPanel repPanel = (AbstractRepresentationPanel) currentRepresentationPanel;
            repPanel.stopPreview();
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        updateRepresentationPanel();
    }

    private void setRepresentationPanel(Component representationPanel) {

        if (this.currentRepresentationPanel != representationPanel) {

            if (this.currentRepresentationPanel != null) {
                remove(this.currentRepresentationPanel);
            }

            this.currentRepresentationPanel = representationPanel;

            if (this.currentRepresentationPanel != null) {
                add(this.currentRepresentationPanel, BorderLayout.CENTER);
            }

            revalidate();
            repaint();
        }

    }

    private void updateRepresentationPanel() {

        logger.debug("Updating representation panel with "
                + representationObject);

        Component representationPanel = null;

        if (this.representationObject != null) {
            try {

                if (SIPRepresentationObject.EMAIL
                        .equalsIgnoreCase(this.representationObject.getType())) {

                    representationPanel = getEmailRepresentationPanel();

                } else if (SIPRepresentationObject.DIGITALIZED_WORK
                        .equalsIgnoreCase(this.representationObject.getType())) {

                    representationPanel = getDWRepresentationPanel();

                } else if (SIPRepresentationObject.STRUCTURED_TEXT
                        .equalsIgnoreCase(this.representationObject.getType())) {

                    representationPanel = getSTRepresentationPanel();

                } else if (SIPRepresentationObject.PRESENTATION
                        .equalsIgnoreCase(this.representationObject.getType())) {

                    representationPanel = getPresentationRepresentationPanel();

                } else if (SIPRepresentationObject.SPREADSHEET
                        .equalsIgnoreCase(this.representationObject.getType())) {

                    representationPanel = getSpreadsheetRepresentationPanel();

                } else if (SIPRepresentationObject.VECTOR_GRAPHIC
                        .equalsIgnoreCase(this.representationObject.getType())) {

                    representationPanel = getVectorGraphicRepresentationPanel();

                } else if (SIPRepresentationObject.RELATIONAL_DATABASE
                        .equalsIgnoreCase(this.representationObject.getType())) {

                    representationPanel = getRDBRepresentationPanel();

                } else if (SIPRepresentationObject.AUDIO
                        .equalsIgnoreCase(this.representationObject.getType())) {

                    representationPanel = getAudioRepresentationPanel();

                } else if (SIPRepresentationObject.VIDEO
                        .equalsIgnoreCase(this.representationObject.getType())) {

                    representationPanel = getVideoRepresentationPanel();

                } else if (SIPRepresentationObject.UNKNOWN
                        .equalsIgnoreCase(this.representationObject.getType())) {

                    representationPanel = getUnknownRepresentationPanel();

                } else {
                    throw new InvalidRepresentationException(
                            "Unsupported type "
                            + representationObject.getType());
                }

                logger.debug("Setting representation panel "
                        + representationPanel.getClass().getSimpleName());
                setRepresentationPanel(representationPanel);
            } catch (InvalidRepresentationException e) {
                logger.error("Error updating representation panel", e);
            }
        }

    }

    private JPanel getUnknownRepresentationPanel()
            throws InvalidRepresentationException {
        return new UnknownRepresentationPanel(this.representationObject);
    }

    private VideoRepresentationPanel getVideoRepresentationPanel()
            throws InvalidRepresentationException {
        return new VideoRepresentationPanel(this.representationObject);
    }

    private AudioRepresentationPanel getAudioRepresentationPanel()
            throws InvalidRepresentationException {
        return new AudioRepresentationPanel(this.representationObject);
    }

    private JPanel getRDBRepresentationPanel()
            throws InvalidRepresentationException {
        return new RdbRepresentationPanel(this.representationObject);
    }

    private STRepresentationPanel getSTRepresentationPanel()
            throws InvalidRepresentationException {
        STRepresentationPanel ret;
        if (currentRepresentationPanel instanceof STRepresentationPanel) {
            STRepresentationPanel currentST = (STRepresentationPanel) currentRepresentationPanel;
            if (currentST.getRepresentationObject() == getRepresentationObject()) {
                ret = currentST;
            } else {
                ret = new STRepresentationPanel(this.representationObject);
            }
        } else {
            ret = new STRepresentationPanel(this.representationObject);
        }
        return ret;
    }

    private DWRepresentationPanel getDWRepresentationPanel()
            throws InvalidRepresentationException {
        DWRepresentationPanel ret;
        if (currentRepresentationPanel instanceof DWRepresentationPanel) {
            DWRepresentationPanel currentDW = (DWRepresentationPanel) currentRepresentationPanel;
            if (currentDW.getRepresentationObject() == getRepresentationObject()) {
                ret = currentDW;
            } else {
                ret = new DWRepresentationPanel(this.representationObject);
            }
        } else {
            ret = new DWRepresentationPanel(this.representationObject);
        }
        return ret;
    }
    // private JPanel getRepresentationTypeSelectionPanel() {
    // if (repTypeSelectionPanel == null) {
    // repTypeSelectionPanel = new RepresentationTypeSelectionPanel();
    // repTypeSelectionPanel
    // .addRepresentationTypeSelectionListener(new
    // RepresentationTypeSelectionListener() {
    //
    // public void onRepresentationTypeSelected(String type,
    // String subtype) {
    // representationObject.setType(type);
    // representationObject.setSubType(subtype);
    //
    // updateRepresentationPanel();
    //
    // }
    //
    // });
    // }
    // return repTypeSelectionPanel;
    // }

     private Component getEmailRepresentationPanel() throws InvalidRepresentationException {
        EmailRepresentationPanel ret;
        if (currentRepresentationPanel instanceof EmailRepresentationPanel) {
            EmailRepresentationPanel currentEmail = (EmailRepresentationPanel) currentRepresentationPanel;
            if (currentEmail.getRepresentationObject() == getRepresentationObject()) {
                ret = currentEmail;
            } else {
                ret = new EmailRepresentationPanel(this.representationObject);
            }
        } else {
            ret = new EmailRepresentationPanel(this.representationObject);
        }
        return ret;
    }
    
    private Component getPresentationRepresentationPanel() throws InvalidRepresentationException {
        PresentationRepresentationPanel ret;
        if (currentRepresentationPanel instanceof PresentationRepresentationPanel) {
            PresentationRepresentationPanel currentPresentation = (PresentationRepresentationPanel) currentRepresentationPanel;
            if (currentPresentation.getRepresentationObject() == getRepresentationObject()) {
                ret = currentPresentation;
            } else {
                ret = new PresentationRepresentationPanel(this.representationObject);
            }
        } else {
            ret = new PresentationRepresentationPanel(this.representationObject);
        }
        return ret;
    }

    private Component getSpreadsheetRepresentationPanel() throws InvalidRepresentationException {
        SpreadsheetRepresentationPanel ret;
        if (currentRepresentationPanel instanceof SpreadsheetRepresentationPanel) {
            SpreadsheetRepresentationPanel currentSpreadsheet = (SpreadsheetRepresentationPanel) currentRepresentationPanel;
            if (currentSpreadsheet.getRepresentationObject() == getRepresentationObject()) {
                ret = currentSpreadsheet;
            } else {
                ret = new SpreadsheetRepresentationPanel(this.representationObject);
            }
        } else {
            ret = new SpreadsheetRepresentationPanel(this.representationObject);
        }
        return ret;
    }

    private Component getVectorGraphicRepresentationPanel() throws InvalidRepresentationException {
        VectorGraphicRepresentationPanel ret;
        if (currentRepresentationPanel instanceof VectorGraphicRepresentationPanel) {
            VectorGraphicRepresentationPanel currentVectorGraphic = (VectorGraphicRepresentationPanel) currentRepresentationPanel;
            if (currentVectorGraphic.getRepresentationObject() == getRepresentationObject()) {
                ret = currentVectorGraphic;
            } else {
                ret = new VectorGraphicRepresentationPanel(this.representationObject);
            }
        } else {
            ret = new VectorGraphicRepresentationPanel(this.representationObject);
        }
        return ret;
    }
}
