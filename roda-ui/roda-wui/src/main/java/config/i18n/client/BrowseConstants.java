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
public interface BrowseConstants extends Constants, ConstantsWithLookup {

	// Tree

	@DefaultStringValue("_")
	public String noDate();

	@DefaultStringValue("<no title>")
	public String noTitle();

	@DefaultStringValue("loading...")
	public String treeLoading();

	// Element Scroll Panel
	@DefaultStringValue("Reference")
	public String elementHeaderId();

	@DefaultStringValue("Title")
	public String elementHeaderTitle();

	@DefaultStringValue("Initial date")
	public String elementHeaderDateInitial();

	@DefaultStringValue("Final date")
	public String elementHeaderDateFinal();

	// Item popup

	@DefaultStringValue("No description")
	public String noDescription();

	// View Panel / Window
	@DefaultStringValue("CLOSE")
	public String close();

	@DefaultStringValue("EAD")
	public String downloadEadC();

	@DefaultStringValue("PREMIS")
	public String downloadPremis();

	@DefaultStringValue("Description")
	public String descriptiveMetadata();

	@DefaultStringValue("View")
	public String representations();

	@DefaultStringValue("Preservation")
	public String preservationMetadata();

	@DefaultStringValue("Producers")
	public String producers();

	@DefaultStringValue("Permissions")
	public String objPermissions();

	@DefaultStringValue("loading...")
	public String viewWindowLoading();

	@DefaultStringValue("Hide optional fields")
	public String hideOptionalFields();

	@DefaultStringValue("Show optional fields")
	public String showOptionalFields();

	@DefaultStringValue("The object no longer exists in the repository. It may have been deleted or the user does not have permissions to access.")
	public String viewWindowObjectNoLongerExists();

	// Disseminations Panel
	@DefaultStringValue("download")
	public String disseminationDownloadLabel();

	// Browser
	@DefaultStringValue("Open description of each element in a side panel")
	public String viewPanelToggleTitle();

	@DefaultStringValue("Open description of each element in a new window")
	public String viewWindowToggleTitle();

	@DefaultStringValue("The repository is empty")
	public String repositoryEmpty();

	// Edit mode
	@DefaultStringValue("EDIT")
	public String editDescriptiveMetadata();

	@DefaultStringValue("SAVE")
	public String saveDescriptiveMetadata();

	@DefaultStringValue("CANCEL")
	public String cancelEditDescriptiveMetadata();

	@DefaultStringValue("Do you want to save changes before exiting?")
	public String saveBeforeClosing();

	@DefaultStringValue("SUBLEVEL")
	public String createElementChild();

	@DefaultStringValue("<no reference>")
	public String newFonds();

	@DefaultStringValue("<no reference>")
	public String newElement();

	@DefaultStringValue("MOVE")
	public String moveElement();

	@DefaultStringValue("REMOVE")
	public String removeElement();

	@DefaultStringValue("Are you sure you want to remove this element?")
	public String confirmElementRemove();

	@DefaultStringValue("This element contains sub-elements will be removed recursively. Are you sure you want to continue?")
	public String confirmRecursiveRemove();

	@DefaultStringValue("CLONE")
	public String cloneElement();

	// Dissemination Panel
	@DefaultStringValue("(original)")
	public String disseminationOfOriginal();

	@DefaultStringValue("(original and normalized)")
	public String disseminationOfOriginalAndNormalized();

	@DefaultStringValue("(normalized)")
	public String disseminationOfNormalized();

	// * Disseminations
	@DefaultStringValue("Download representation")
	public String dissemination_AIPDownload();

	@DefaultStringValue("Download signed representation")
	public String dissemination_Signature();

	@DefaultStringValue("Photo preview")
	public String dissemination_SimpleViewer();

	@DefaultStringValue("Book preview")
	public String dissemination_FlashPageFlipFree();

	@DefaultStringValue("Book preview")
	public String dissemination_FlashPageFlipPaid();

	@DefaultStringValue("Database preview")
	public String dissemination_PhpMyAdmin();

	@DefaultStringValue("Media preview")
	public String dissemination_MediaPlayer();

	@DefaultStringValue("Audio preview")
	public String dissemination_MediaPlayerAudio();

	@DefaultStringValue("Video preview")
	public String dissemination_MediaPlayerVideo();

	// Preservation Metadata Panel
	@DefaultStringValue("Day")
	public String timeUnitDay();

	@DefaultStringValue("Month")
	public String timeUnitMonth();

	@DefaultStringValue("Year")
	public String timeUnitYear();

	// Representation formats
	@DefaultStringValue("PDF")
	public String representation_format_application_pdf();

	@DefaultStringValue("Word 97_2004")
	public String representation_format_application_msword();

	@DefaultStringValue("Word 2007")
	public String representation_format_application_vnd_openxmlformats_officedocument_wordprocessingml_document();

	@DefaultStringValue("OpenOffice")
	public String representation_format_application_vnd_oasis_opendocument_text();

	@DefaultStringValue("RTF")
	public String representation_format_application_rtf();

	@DefaultStringValue("Plain Text")
	public String representation_format_text_plain();

	@DefaultStringValue("TIFF")
	public String representation_format_image_mets_tiff();

	@DefaultStringValue("JPEG")
	public String representation_format_image_mets_jpeg();

	@DefaultStringValue("PNG")
	public String representation_format_image_mets_png();

	@DefaultStringValue("Windows Bitmap")
	public String representation_format_image_mets_bmp();

	@DefaultStringValue("GIF")
	public String representation_format_image_mets_gif();

	@DefaultStringValue("Windows Icon")
	public String representation_format_image_mets_ico();

	@DefaultStringValue("XMP")
	public String representation_format_image_mets_xpm();

	@DefaultStringValue("TGA")
	public String representation_format_image_mets_tga();

	@DefaultStringValue("Scanned document")
	public String representation_format_image_mets_misc();

	@DefaultStringValue("Database(DBML)")
	public String representation_format_application_dbml();

	@DefaultStringValue("Database(DBML)")
	public String representation_format_application_dbml_octet_stream();

	@DefaultStringValue("Audio WAV")
	public String representation_format_audio_wav();

	@DefaultStringValue("Audio MP3")
	public String representation_format_audio_mpeg();

	@DefaultStringValue("Audio FLAC")
	public String representation_format_audio_flac();

	@DefaultStringValue("Audio AIFF")
	public String representation_format_audio_aiff();

	@DefaultStringValue("Audio OGG")
	public String representation_format_audio_ogg();

	@DefaultStringValue("MPEG 1")
	public String representation_format_video_mpeg();

	@DefaultStringValue("MPEG 2")
	public String representation_format_video_mpeg2();

	@DefaultStringValue("MPEG 4")
	public String representation_format_video_mp4();

	@DefaultStringValue("Video AVI")
	public String representation_format_video_avi();

	@DefaultStringValue("Windows Media Video")
	public String representation_format_video_x_ms_wmv();

	@DefaultStringValue("Quicktime")
	public String representation_format_video_quicktime();

	@DefaultStringValue("unknown format")
	public String representation_format_unknown();

}
