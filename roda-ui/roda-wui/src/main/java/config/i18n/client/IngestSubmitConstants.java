/**
 * 
 */
package config.i18n.client;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.ConstantsWithLookup;

/**
 * @author Luis Faria
 * 
 */
public interface IngestSubmitConstants extends Constants, ConstantsWithLookup {

	// Upload
	@DefaultStringValue("Send packet")
	String uploadTabTitle();

	@DefaultStringValue("Please select the packets to load")
	String uploadHeader();

	@DefaultStringValue("Download RODA-in")
	String uploadSubmitGetRodaIn();

	@DefaultStringValue("SUBMIT")
	String uploadSubmitButton();

	@DefaultStringValue("Loading data...")
	String uploadLoadingData();

	@DefaultStringValue("Ingesting packet...")
	String uploadLoadingIngest();

	@DefaultStringValue("The packets could not be submitted due to an error.")
	String uploadSubmitFailure();

	// Create
	@DefaultStringValue("Create packet")
	String createTabTitle();

	@DefaultStringValue("Fill in the descriptive metadata")
	String createMetadataHeader();

	@DefaultStringValue("Add the representation (up to 2GB)")
	String createRepresentationHeader();

	@DefaultStringValue("SUBMIT")
	String createSubmitButton();

	@DefaultStringValue("Download RODA-in")
	String createSipGetRodaIn();

	@DefaultStringValue("Show optional fields")
	String createShowOptionalMetadata();

	@DefaultStringValue("Hide optional fields")
	String createHideOptionalMetadata();

	@DefaultStringValue("Choose the destination")
	String createDestinationHeader();

	@DefaultStringValue("fill descriptive metadata")
	String createMetadataInvalidWarning();

	@DefaultStringValue("add a representation")
	String createNoFilesWarning();

	@DefaultStringValue("the file list is not valid for the chosen type of representation")
	String createInvalidFilesWarning();

	@DefaultStringValue("choose a destination")
	String createNoDestinationWarning();

	@DefaultStringValue("Invalid destination, the level of description of the destination must be greater than that of file")
	String createInvalidDestinationWarning();

	@DefaultStringValue("sending data...")
	String createSubmitUploadingMessage();

	@DefaultStringValue("creating and submiting packet...")
	String createSubmitSubmitingMessage();

	@DefaultStringValue("an error occurred and the package can not be submited")
	String createSubmitFailureMessage();

	// * content model selectors
	@DefaultStringValue("Structured Text")
	String cModel_structured_text_title();

	@DefaultStringValue("Written text documents as PDF, Word (doc and docx), OpenOffice (odt), RTF or TXT")
	String cModel_structured_text_description();

	@DefaultStringValue("Images")
	String cModel_digitalized_work_title();

	@DefaultStringValue("Photos or scans with or without structure as TIFF, JPEG, PNG, BMP, GIF, ICO, XPM and TGA")
	String cModel_digitalized_work_description();

	@DefaultStringValue("Relational databases")
	String cModel_relational_database_title();

	@DefaultStringValue("Databases in a standardized DBML format (XML file any more)")
	String cModel_relational_database_description();

	@DefaultStringValue("Video")
	String cModel_video_title();

	@DefaultStringValue("Videos as AVI, MPEG, DVD, MP4 or Quicktime")
	String cModel_video_description();

	@DefaultStringValue("Audio")
	String cModel_audio_title();

	@DefaultStringValue("Audio files like WAV, MP3, MP4, Ogg, Flac and WMA")
	String cModel_audio_description();

	@DefaultStringValue("Unknown")
	String cModel_unknown_title();

	@DefaultStringValue("If any of above types do not fit, choose this type.")
	String cModel_unknown_description();

}
