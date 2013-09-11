package pt.gov.dgarq.roda.ingest.siputility.builders;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.w3c.util.DateParser;
import pt.gov.dgarq.roda.common.FormatUtility;
import pt.gov.dgarq.roda.core.data.FileFormat;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.ingest.siputility.SIPException;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;
import pt.gov.dgarq.roda.ingest.siputility.data.StreamRepresentationFile;
import pt.gov.dgarq.roda.ingest.siputility.data.StreamRepresentationObject;

/**
 * This is the base for all representation builders. It contains methods common
 * to all builders.
 *
 * @author Rui Castro
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public abstract class RepresentationBuilder {

    /**
     * Creates a new representation identifier. The created ID is the ISO
     * representation of the current date/time.
     *
     * @return a {@link String} with a representation identifier.
     */
    public static String createNewRepresentationID() {

        String isoDate = DateParser
                .getIsoDate(Calendar.getInstance().getTime());
        isoDate = isoDate.replaceAll(":", ".");

        String representationID = "R" + isoDate; //$NON-NLS-1$

        return representationID;
    }

    /**
     * Constructs a new {@link SIPRepresentationObject} with the given type and
     * files.
     *
     * @param type the type of the representation. Must be one of
     * {@link SIPRepresentationObject#TYPES}.
     * @param filenames the names of the files
     * @param streams the {@link InputStream}s for the files.
     *
     * @return a {@link StreamRepresentationObject}.
     *
     * @throws SIPException
     * @throws IllegalArgumentException
     */
    public static StreamRepresentationObject createRepresentation(String type,
            List<String> filenames, List<InputStream> streams)
            throws SIPException {

        if (filenames == null || streams == null
                || filenames.size() != streams.size() || filenames.isEmpty()) {
            throw new IllegalArgumentException(
                    "filenames and streams cannot be null or empty");
        }

        RepresentationBuilder rBuilder = null;

        if (SIPRepresentationObject.DIGITALIZED_WORK.equals(type)) {
            rBuilder = new DigitalizedWorkRepresentationBuilder();
        } else if (SIPRepresentationObject.STRUCTURED_TEXT.equals(type)) {
            rBuilder = new StructuredTextRepresentationBuilder();
        } else if (SIPRepresentationObject.RELATIONAL_DATABASE.equals(type)) {
            rBuilder = new RelationalDatabaseRepresentationBuilder();
        } else if (SIPRepresentationObject.VIDEO.equals(type)) {
            rBuilder = new VideoRepresentationBuilder();
        } else if (SIPRepresentationObject.AUDIO.equals(type)) {
            rBuilder = new AudioRepresentationBuilder();
        } else if (SIPRepresentationObject.EMAIL.equals(type)) {
            rBuilder = new EmailRepresentationBuilder();
        } else if (SIPRepresentationObject.PRESENTATION.equals(type)) {
            rBuilder = new PresentationRepresentationBuilder();
        } else if (SIPRepresentationObject.SPREADSHEET.equals(type)) {
            rBuilder = new SpreadsheetRepresentationBuilder();
        } else if (SIPRepresentationObject.VECTOR_GRAPHIC.equals(type)) {
            rBuilder = new VectorGraphicRepresentationBuilder();
        } else if (SIPRepresentationObject.UNKNOWN.equals(type)) {
            rBuilder = new UnknownRepresentationBuilder();
        }

        return rBuilder.createRepresentation(filenames, streams);
    }

    /**
     * Returns the mimetype of the given {@link File}.
     *
     * @param file the {@link File}
     *
     * @return a {@link String} with the mimetype of the given {@link File}.
     */
    public static FileFormat getFileFormat(File file) {
        return FormatUtility.getFileFormat(file, file.getName());
    }

    /**
     * Returns the subtype of the given {@link RepresentationObject}.
     *
     * @param rObject the {@link RepresentationObject}. It must have a type
     * defined.
     *
     * @return a {@link String} with the subtype of the given
     * {@link RepresentationObject} or <code>null</code> if the subtype couldn't
     * be determined.
     */
    public static String getRepresentationSubtype(RepresentationObject rObject) {

        String subType = null;

        if (SIPRepresentationObject.DIGITALIZED_WORK.equals(rObject.getType())) {
            subType = DigitalizedWorkRepresentationBuilder
                    .getRepresentationSubtype(rObject);
        } else if (SIPRepresentationObject.STRUCTURED_TEXT.equals(rObject
                .getType())) {
            subType = StructuredTextRepresentationBuilder
                    .getRepresentationSubtype(rObject);
        } else if (SIPRepresentationObject.RELATIONAL_DATABASE.equals(rObject
                .getType())) {
            subType = RelationalDatabaseRepresentationBuilder
                    .getRepresentationSubtype(rObject);
        } else if (SIPRepresentationObject.VIDEO.equals(rObject.getType())) {
            subType = VideoRepresentationBuilder
                    .getRepresentationSubtype(rObject);
        } else if (SIPRepresentationObject.AUDIO.equals(rObject.getType())) {
            subType = AudioRepresentationBuilder
                    .getRepresentationSubtype(rObject);
        } else if (SIPRepresentationObject.UNKNOWN.equals(rObject.getType())) {
            subType = UnknownRepresentationBuilder
                    .getRepresentationSubtype(rObject);
        } else if (SIPRepresentationObject.EMAIL.equals(rObject.getType())) {
            subType = EmailRepresentationBuilder
                    .getRepresentationSubtype(rObject);
        } else if (SIPRepresentationObject.PRESENTATION.equals(rObject.getType())) {
            subType = PresentationRepresentationBuilder
                    .getRepresentationSubtype(rObject);
        } else if (SIPRepresentationObject.SPREADSHEET.equals(rObject.getType())) {
            subType = SpreadsheetRepresentationBuilder
                    .getRepresentationSubtype(rObject);
        } else if (SIPRepresentationObject.VECTOR_GRAPHIC.equals(rObject.getType())) {
            subType = VectorGraphicRepresentationBuilder
                    .getRepresentationSubtype(rObject);
        }

        return subType;
    }

    /**
     * Creates a {@link StreamRepresentationObject} with the given filenames and
     * streams.
     *
     * @param filenames
     * @param streams
     *
     * @return a {@link StreamRepresentationObject}.
     *
     * @throws SIPException if some problem occurs creating the representation.
     */
    public abstract StreamRepresentationObject createRepresentation(
            List<String> filenames, List<InputStream> streams)
            throws SIPException;

    /**
     *
     */
    protected StreamRepresentationObject createRepresentation(String type,
            String id, String rootFilename, InputStream rootStream,
            List<String> partFilenames, List<InputStream> partStreams) {

        return createRepresentation(type, null, id,
                new String[]{RepresentationObject.STATUS_ORIGINAL}, null,
                rootFilename, rootStream, partFilenames, partStreams);
    }

    /**
     *
     */
    protected StreamRepresentationObject createRepresentation(String type,
            String subType, String id, String rootFilename,
            InputStream rootStream, List<String> partFilenames,
            List<InputStream> partStreams) {

        return createRepresentation(type, subType, id,
                new String[]{RepresentationObject.STATUS_ORIGINAL}, null,
                rootFilename, rootStream, partFilenames, partStreams);
    }

    /**
     *
     */
    protected StreamRepresentationObject createRepresentation(String type,
            String subType, String id, String[] statuses,
            String descriptionObjectPID, String rootFilename,
            InputStream rootStream, List<String> partFilenames,
            List<InputStream> partStreams) {

        StreamRepresentationObject streamRObject = new StreamRepresentationObject();
        streamRObject.setId(id);
        streamRObject.setType(type);
        streamRObject.setSubType(subType);
        streamRObject.setStatuses(statuses);
        streamRObject.setDescriptionObjectPID(descriptionObjectPID);

        int currentId = 0;
        StreamRepresentationFile rootRepStream = createStreamRepresentationFile(
                rootFilename, rootStream);
        rootRepStream.setId("F" + Integer.toString(currentId));
        currentId++;
        streamRObject.setRootStream(rootRepStream);

        List<StreamRepresentationFile> listPartStreams = new ArrayList<StreamRepresentationFile>();
        for (int i = 0; partStreams != null && i < partStreams.size(); i++) {

            String partFilename = partFilenames.get(i);
            InputStream partStream = partStreams.get(i);

            StreamRepresentationFile partRepStream = createStreamRepresentationFile(
                    partFilename, partStream);
            partRepStream.setId("F" + Integer.toString(currentId));
            currentId++;
            listPartStreams.add(partRepStream);
        }
        streamRObject.setPartStreams(listPartStreams);

        return streamRObject;
    }

    protected StreamRepresentationFile createStreamRepresentationFile(
            String filename, InputStream inputStream) {
        StreamRepresentationFile representationStream = new StreamRepresentationFile();
        representationStream.setOriginalName(filename);
        representationStream.importFileFormat(FormatUtility.getFileFormat(inputStream, filename));
        representationStream.setInputStream(inputStream);
        return representationStream;
    }
}
