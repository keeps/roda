package pt.gov.dgarq.roda.services.client;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.w3c.util.DateParser;
import pt.gov.dgarq.roda.common.FormatUtility;
import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.Uploader;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.FileFormat;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.RepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.core.stubs.Editor;
import pt.gov.dgarq.roda.core.stubs.Ingest;

/**
 * Test class for Ingest service.
 *
 * @author Rui Castro
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public class IngestTest {

    /**
     * @param args
     */
    public static void main(String[] args) {

        List<String> createdObjectPIDs = new ArrayList<String>();

        try {

            RODAClient rodaClient = null;
            Uploader rodaUploader = null;

            String doParentPID = null;
            File representationRootFile = null;

            if (args.length == 5) {

                // http://localhost:8180/ user pass
                String hostUrl = args[0];
                String username = args[1];
                String password = args[2];
                doParentPID = args[3];
                representationRootFile = new File(args[4]);

                if (!representationRootFile.exists()) {
                    System.err.println("File " + representationRootFile
                            + " doesn't exist");
                    System.exit(1);
                }

                rodaClient = new RODAClient(new URL(hostUrl), username,
                        password);
                rodaUploader = new Uploader(new URL(hostUrl), username,
                        password);

            } else {
                System.err
                        .println(IngestTest.class.getSimpleName()
                        + " protocol://hostname:port/ username password DO_Parent_PID representationRootFile");
                System.exit(1);
            }

            Ingest ingestService = rodaClient.getIngestService();
            Browser browserService = rodaClient.getBrowserService();
            Editor editorService = rodaClient.getEditorService();

            try {

                DescriptionObject dObject = new DescriptionObject();
                dObject.setLevel(DescriptionLevel.ITEM);
                dObject.setId("testIngest");
                dObject.setCountryCode("PT");
                dObject.setRepositoryCode("DGARQ");
                dObject.setTitle("Test Item for Ingest Service");
                dObject.setOrigination("Rui Castro");
                dObject
                        .setScopecontent("This object is just for testing purposes");
                dObject.setParentPID(doParentPID);

                System.out.println("\n**************************************");
                System.out.println("Create DescriptionObject");
                System.out.println("**************************************");

                System.out.println("Creating " + dObject);

                String dObjectPID = ingestService
                        .createDescriptionObject(dObject);

                System.out.println("Created with pid " + dObjectPID);

                createdObjectPIDs.add(dObjectPID);

                System.out.println("\n**************************************");
                System.out.println("Possible levels for DO " + dObjectPID);
                System.out.println("**************************************");

                System.out.println(Arrays.asList(editorService
                        .getDOPossibleLevels(dObjectPID)));

                System.out.println("\n**************************************");
                System.out.println("Create RepresentationObject");
                System.out.println("**************************************");

                RepresentationFile rFile = new RepresentationFile("F0",
                        representationRootFile.getName(), representationRootFile.length(), representationRootFile.toURI().toURL().toExternalForm(),
                        FormatUtility.getFileFormat(representationRootFile, representationRootFile.getName()));
                RepresentationObject rObject = new RepresentationObject();
                rObject.setLabel(DateParser.getIsoDate(new Date()));
                rObject.setType(RepresentationObject.UNKNOWN);
                rObject.setStatuses(new String[]{RepresentationObject.STATUS_ORIGINAL});
                rObject.setDescriptionObjectPID(dObjectPID);
                rObject.setRootFile(rFile);
                rObject.setDescriptionObjectPID(dObjectPID);

                System.out.println("Creating " + rObject);

                String rObjectPID = ingestService
                        .createRepresentationObject(rObject);

                System.out.println("Created with pid " + rObjectPID);
                createdObjectPIDs.add(rObjectPID);

                System.out.println("Uploading representation file " + rFile);
                rodaUploader.uploadRepresentationFile(rObjectPID, rObject
                        .getRootFile());

                System.out.println("\n**************************************");
                System.out.println("Registering ingestion");
                System.out.println("**************************************");

                String ingestEventPObjectPID = ingestService
                        .registerIngestEvent(new String[]{dObjectPID},
                        new String[]{rObjectPID}, new String[0],
                        IngestTest.class.getName(), "Test ingestion");

                System.out.println("Ingest Event created with pid "
                        + ingestEventPObjectPID);
                createdObjectPIDs.add(ingestEventPObjectPID);

                EventPreservationObject ingestEventPObject = browserService
                        .getEventPreservationObject(ingestEventPObjectPID);
                System.out
                        .println("Event preservation object for ingestion is "
                        + ingestEventPObject);

                RepresentationPreservationObject rPObject = browserService
                        .getROPreservationObject(rObjectPID);

                System.out.println("Preservation object for representation "
                        + rObjectPID + " is " + rPObject);

                rPObject = browserService
                        .getRepresentationPreservationObject(rPObject.getPid());

                System.out.println("Representation preservation object is "
                        + rPObject);

                System.out.println("\n**************************************");
                System.out.println("Set normalized representation");
                System.out.println("**************************************");

                String normROPID = ingestService.setDONormalizedRepresentation(
                        dObjectPID, rObjectPID);
                System.out.println("Normalized representation PID is "
                        + normROPID);

                RepresentationObject normalizedRO = browserService
                        .getRepresentationObject(normROPID);

                System.out.println("Normalized RO " + normalizedRO);

            } catch (Exception e) {

                e.printStackTrace();

            } finally {

                System.out.println("Removing created objects "
                        + createdObjectPIDs);

                // Remove the ingest objects
                ingestService.removeObjects((String[]) createdObjectPIDs
                        .toArray(new String[createdObjectPIDs.size()]));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
