/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.roda.core.data.common.RodaConstants;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public final class CorporaConstants {
  // Corpora constants
  public static final String SOURCE_AIP_CONTAINER = RodaConstants.STORAGE_CONTAINER_AIP;
  public static final String SOURCE_PRESERVATION_CONTAINER = RodaConstants.STORAGE_CONTAINER_PRESERVATION;
  public static final String SOURCE_AGENT_CONTAINER = RodaConstants.STORAGE_CONTAINER_AGENT;
  public static final String SOURCE_AIP_ID = "AIP_1";
  public static final String SOURCE_AIP_ID_3 = "AIP_3";
  public static final String SOURCE_AIP_VERSION_EAD_3 = "AIP_VERSION_EAD_3";
  public static final String SOURCE_AIP_VERSION_EAD_UNKNOWN = "AIP_VERSION_EAD_UNKNOWN";
  public static final String SOURCE_AIP_BUGGY_ID = "AIP_BUGGY";
  public static final String SOURCE_AIP_ID_REAL = "REAL";
  public static final String SOURCE_AIP_REP_WITH_SUBFOLDERS = "AIP_REP_WITH_SUBFOLDERS";
  public static final String SOURCE_AIP_CONVERTER_1 = "AIP_CONVERTER_1";
  public static final String SOURCE_AIP_CONVERTER_2 = "AIP_CONVERTER_2";
  public static final String SOURCE_AIP_CONVERTER_3 = "AIP_CONVERTER_3";
  public static final String SOURCE_AIP_PERMISSIONS = "AIP_PERMISSIONS";
  public static final String SOURCE_AIP_WITH_INVALID_METADATA = "AIP_WITH_INVALID_METADATA";
  public static final String SOURCE_AIP_CORRUPTED = "AIP_CORRUPTED";
  public static final String SOURCE_AIP_EMPTY = "AIP_EMPTY";
  public static final String SOURCE_INVALID_FOLDER = "invalid";
  public static final String DATE_CREATED = "2015-06-06T15:30:00.000+0000";
  public static final String DATE_MODIFIED = "2015-06-06T16:30:00.000+0000";

  public static final String DESCRIPTIVE_METADATA_ID = "ead.xml";
  public static final String DESCRIPTIVE_METADATA_TYPE = "ead";
  public static final String DESCRIPTIVE_METADATA_VERSION = "2002";
  public static final String DESCRIPTIVE_METADATA_ID_EAD3 = "ead_3.xml";
  public static final String DESCRIPTIVE_METADATA_ID_EADUNKNOWN = "ead_unknown.xml";

  public static final String REPRESENTATION_1_ID = "representation_1";
  public static final String REPRESENTATION_1_URN = "urn:roda:premis:representation:6ef01e16-7de4-385a-91ee-b63810161c55";
  public static final String REPRESENTATION_1_TYPE = RodaConstants.REPRESENTATION_TYPE_MIXED;
  public static final boolean REPRESENTATION_1_ORIGINAL = true;

  public static final List<String> REPRESENTATION_1_FILE_1_PATH = Collections.emptyList();
  public static final String REPRESENTATION_1_FILE_1_ID = "METS.xml";
  public static final String REPRESENTATION_1_FILE_1_FORMAT_MIMETYPE = "application/xml";
  public static final String REPRESENTATION_1_FILE_1_FORMAT_VERSION = "1.0";
  // puid=fmt/101

  public static final List<String> REPRESENTATION_1_FILE_2_PATH = Collections.emptyList();
  public static final String REPRESENTATION_1_FILE_2_ID = "fil_7516.jpg";
  public static final String REPRESENTATION_1_FILE_2_FORMAT_MIMETYPE = "image/jpeg";
  public static final String REPRESENTATION_1_FILE_2_FORMAT_VERSION = "1.01";
  // puid=fmt/43

  public static final String REPRESENTATION_2_ID = "representation_2";
  public static final String REPRESENTATION_2_TYPE = "digitalized_work";
  public static final boolean REPRESENTATION_2_ORIGINAL = false;

  public static final List<String> REPRESENTATION_2_FILE_1_PATH = Collections.emptyList();
  public static final String REPRESENTATION_2_FILE_1_ID = "fil_7516.jpg.tiff";
  public static final String REPRESENTATION_2_FILE_1_FORMAT_MIMETYPE = "image/tiff";
  public static final String REPRESENTATION_2_FILE_1_FORMAT_VERSION = null;

  public static final List<String> REPRESENTATION_2_FILE_2_PATH = Collections.emptyList();
  public static final String REPRESENTATION_2_FILE_2_ID = "METS.xml";
  public static final String REPRESENTATION_2_FILE_2_FORMAT_MIMETYPE = "application/xml";
  public static final String REPRESENTATION_2_FILE_2_FORMAT_VERSION = "1.0";

  public static final String OTHER_AIP_ID = "AIP_2";
  public static final List<String> OTHER_DESCRIPTIVE_METADATA_STORAGEPATH = Arrays.asList(SOURCE_AIP_CONTAINER, "AIP_2",
    "metadata", "descriptive", "dc.xml");
  public static final String OTHER_DESCRIPTIVE_METADATA_TYPE = "dcxml";
  public static final String OTHER_DESCRIPTIVE_METADATA_VERSION = null;
  public static final List<String> OTHER_REPRESENTATION_STORAGEPATH = Arrays.asList(SOURCE_AIP_CONTAINER, "AIP_2",
    "representations", "representation_1");
  public static final List<String> OTHER_FILE_STORAGEPATH = Arrays.asList(SOURCE_AIP_CONTAINER, "AIP_2",
    "representations", "representation_1", "data", "2012-roda-promo-en.pdf");
  // puid=fmt/101

  public static final String SOURCE_DESC_METADATA_CONTAINER = "DescriptiveMetadata";
  public static final String STRANGE_DESC_METADATA_FILE = "strange.xml";

  public static final String TEXT_XML = "text/xml";
  public static final String REPRESENTATION_1_PREMIS_EVENT_ID = "urn:roda:premis:event:roda_398";
  public static final String INGESTION = "ingestion";
  public static final String RODA_8 = "roda:8";
  public static final String SUCCESS = "success";
  public static final String REPRESENTATION_PREMIS_XML = "representation.premis.xml";
  public static final String PRESERVATION_LEVEL_FULL = "full";
  public static final String REPRESENTATION_CONVERTER_ID_1 = "b5dfaadc-2048-4082-b8b8-2a8e0ea1ad03";
  public static final String REPRESENTATION_CONVERTER_ID_2 = "50b2c085-ce4a-44d1-8279-d99086400349";
  public static final String REPRESENTATION_CONVERTER_ID_3 = "d53a24ed-f157-42cf-a45a-a2ac87e8b61d";

  public static final String AGENT_RODA_8 = "urn:roda:premis:agent:roda_8";
  public static final String METS_XML = "METS.xml";
  public static final String SOFTWARE_INGEST_TASK = "software:ingest_task";
  public static final String INGEST_CREATE_AIP = "Ingest/Create AIP/1.0";
  public static final String AGENT_RODA_8_PREMIS_XML = "roda_8.premis.xml";
  public static final String HTML_EVENT_PRESERVATION_OBJECT = "eventPreservationObject";
  public static final String HTML_AIPID = "aipID";
  public static final String HTML_VALUE = "value";
  public static final String HTML_FIELD = "field";
  public static final String HTML_TYPE = "type";
  public static final String HTML_REPRESENTATION_FILE_PRESERVATION_OBJECT = "representationFilePreservationObject";
  public static final String HTML_MIMETYPE = "mimetype";
  public static final String HTML_DESCRIPTIVE_METADATA = "descriptiveMetadata";
  public static final String HTML_LEVEL = "level";
  public static final String HTML_FONDS = "fonds";
  public static final String HTML_REPOSITORY_CODE = "odd.ead.did.unitid.repositorycode_txt";
  public static final String HTML_PT_KEEPS = "PT-KEEPS";
  public static final String HTML_PREMIS = "premis";
  public static final String HTML_PRESERVATION_LEVEL = "preservationLevel";
  public static final String HTML_FULL = "full";
  public static final String HTML_TITLE = "title";
  public static final String HTML_MY_EXAMPLE = "My example";
  public static final String HTML_EVENT_TYPE = "eventType";
  public static final String HTML_INGESTION = "ingestion";

  public static final int YEAR_1213 = 1213;
  public static final int YEAR_2003 = 2003;
  public static final String DESCRIPTIVE_METADATA_TYPE_EAD = "ead";
  public static final String DESCRIPTIVE_METADATA_TYPE_EAD_VERSION3 = "3";
  public static final String DESCRIPTIVE_METADATA_TYPE_EAD_VERSIONUNKNOWN = "unknown";

  public static final String SIP_FOLDER = "SIPs";
  public static final String EARK_SIP = "eark_sip.zip";
  public static final String EARK_SIP_UPDATE = "eark_sip_update.zip";
  public static final String EARK_SIP_UPDATE_USER = "admin";
  public static final String BAGIT_SIP = "bagit.zip";
  public static final String TRANSFERRED_RESOURCE = "transferredresource";
  public static final String ANCESTOR_SIP_FOLDER = "ancestorsFolder";

  /** Private empty constructor */
  private CorporaConstants() {
    // do nothing
  }

}
