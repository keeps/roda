package uk.gov.nationalarchives.droid.core;

import uk.gov.nationalarchives.droid.core.signature.droid6.FFSignatureFile;

/**
 * Extended BinarySignatureIdentifier for getting SigFile. SigFile can be used
 * for getting metadata about containers like DOCX from PUID id
 * Thanks "Frederic Bregier" for this workaround
 *
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public class RodaBinarySignatureIdentifie extends uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier {

    /**
     * Default constructor.
     */
    public RodaBinarySignatureIdentifie() {
    }

    /**
     * @return the sigFile
     */
    public FFSignatureFile getSigFile() {
        return super.getSigFile();
    }
}
