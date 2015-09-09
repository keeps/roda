/**
 *
 */
package config.i18n.client;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.ConstantsWithLookup;

/**
 * @author Luis Faria
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public interface BrowseConstants extends Constants, ConstantsWithLookup {

  @DefaultStringValue("Fond")
  public String addFond();

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
  public String dissemination_RepresentationDownload();

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
  @DefaultStringValue("unknown format")
  public String representation_format_unknown();

  @DefaultStringValue("AutoCAD Drawing Database File (dwg)")
  public String representation_format_application_acad();

  @DefaultStringValue("Microsoft Excel Spreadsheet (xlsx)")
  public String representation_format_application_vnd_openxmlformats_officedocument_spreadsheetml_sheet();

  @DefaultStringValue("CorelDRAW Image File (cdr)")
  public String representation_format_application_cdr();

  @DefaultStringValue("Adobe Illustrator File (ai)")
  public String representation_format_application_illustrator();

  @DefaultStringValue("Outlook Mail Message (msg)")
  public String representation_format_application_msoutlook();

  @DefaultStringValue("Microsoft Word (doc)")
  public String representation_format_application_msword();

  @DefaultStringValue("Portable Document Format (pdf)")
  public String representation_format_application_pdf();

  @DefaultStringValue("Adobe Illustrator File (ai)")
  public String representation_format_application_postscript();

  @DefaultStringValue("Rich Text Format (rtf)")
  public String representation_format_application_rtf();

  @DefaultStringValue("Rich Text Format (rtf)")
  public String representation_format_application_rtf__text_rtf();

  @DefaultStringValue("Microsoft Excel Spreadsheet (xls)")
  public String representation_format_application_vnd_ms_excel();

  @DefaultStringValue("Microsoft PowerPoint Presentation (ppt)")
  public String representation_format_application_vnd_ms_powerpoint();

  @DefaultStringValue("OpenDocument Presentation (odp)")
  public String representation_format_application_vnd_oasis_opendocument_presentation();

  @DefaultStringValue("OpenDocument Spreadsheet (ods)")
  public String representation_format_application_vnd_oasis_opendocument_spreadsheet();

  @DefaultStringValue("OpenDocument Text (odt)")
  public String representation_format_application_vnd_oasis_opendocument_text();

  @DefaultStringValue("Microsoft PowerPoint Presentation (pptx)")
  public String representation_format_application_vnd_openxmlformats_officedocument_presentationml_presentation();

  @DefaultStringValue("Microsoft Word (docx)")
  public String representation_format_application_vnd_openxmlformats_officedocument_wordprocessingml_document();

  @DefaultStringValue("Shapes File (shp)")
  public String representation_format_application_x_qgis();

  @DefaultStringValue("LaTeX Source Document (tex)")
  public String representation_format_application_x_tex();

  @DefaultStringValue("TEX file format (texi)")
  public String representation_format_application_x_texinfo();

  @DefaultStringValue("TomeRaider 2 eBook File (tr)")
  public String representation_format_application_x_troff();

  @DefaultStringValue("Audio Interchange File (aiff)")
  public String representation_format_audio_aiff();

  @DefaultStringValue("uLaw/AU Audio File(au)")
  public String representation_format_audio_basic();

  @DefaultStringValue("Free Lossless Audio Codec (flac)")
  public String representation_format_audio_flac();

  @DefaultStringValue("Musical Instrument Digital Interface (midi)")
  public String representation_format_audio_midi();

  @DefaultStringValue("MPEG Audio File")
  public String representation_format_audio_mpeg();

  @DefaultStringValue("Codec Compressed Multimedia File (ogg)")
  public String representation_format_audio_ogg();

  @DefaultStringValue("WAVE Audio File (wav)")
  public String representation_format_audio_wav();

  @DefaultStringValue("Compressed Audio Interchange File (aifc)")
  public String representation_format_audio_x_aifc();

  @DefaultStringValue("Audio Interchange File (aif)")
  public String representation_format_audio_x_aiff();

  @DefaultStringValue("MPEG Audio (mpeg)")
  public String representation_format_audio_x_mpeg();

  @DefaultStringValue("Windows Media Audio (wma)")
  public String representation_format_audio_x_ms_wma();

  @DefaultStringValue("Bitmap Image File (bmp)")
  public String representation_format_image_bmp();

  @DefaultStringValue("Graphical Interchange Format File (gif)")
  public String representation_format_image_gif();

  @DefaultStringValue("Windows Icon (ico)")
  public String representation_format_image_ico();

  @DefaultStringValue("Image File (ief)")
  public String representation_format_image_ief();

  @DefaultStringValue("JPEG 2000 Core Image File (jp2)")
  public String representation_format_image_jp2();

  @DefaultStringValue("JPEG Image (jpg)")
  public String representation_format_image_jpeg();

  @DefaultStringValue("Package - JPEG 2000 images (zip)")
  public String representation_format_image_mets_jp2();

  @DefaultStringValue("Package - JPEG images (zip)")
  public String representation_format_image_mets_jpeg();

  @DefaultStringValue("Package - TIFF images (zip)")
  public String representation_format_image_mets_tiff();

  @DefaultStringValue("Package - BMP images (zip)")
  public String representation_format_image_mets_bmp();

  @DefaultStringValue("Package - GIF images (zip)")
  public String representation_format_image_mets_gif();

  @DefaultStringValue("Portable Network Graphic (png)")
  public String representation_format_image_png();

  @DefaultStringValue(" Scalable Vector Graphics File (svg)")
  public String representation_format_image_svg_xml();

  @DefaultStringValue("Targa Graphic (tga)")
  public String representation_format_image_tga();

  @DefaultStringValue("Tagged Image File Format (tiff)")
  public String representation_format_image_tiff();

  @DefaultStringValue("X Windows Dump Image (xwd)")
  public String representation_format_image_x_xwindowdump();

  @DefaultStringValue("X11 Pixmap Graphic (xpm)")
  public String representation_format_image_xpm();

  @DefaultStringValue("E-Mail Message(eml)")
  public String representation_format_message_rfc822();

  @DefaultStringValue("Database (dbml)")
  public String representation_format_text_dbml();

  @DefaultStringValue("Hypertext Markup Language File (html)")
  public String representation_format_text_html();

  @DefaultStringValue("Plain Text File (txt)")
  public String representation_format_text_plain();

  @DefaultStringValue("XML file (xml)")
  public String representation_format_text_xml();

  @DefaultStringValue("Audio Video Interleave File (avi)")
  public String representation_format_video_avi();

  @DefaultStringValue("MPEG-4 Video File (mp4)")
  public String representation_format_video_mp4();

  @DefaultStringValue("MPEG Movie (mpeg)")
  public String representation_format_video_mpeg();

  @DefaultStringValue("MPEG-2 Video File (mpg2)")
  public String representation_format_video_mpeg2();

  @DefaultStringValue("Apple QuickTime Movie (mov)")
  public String representation_format_video_quicktime();

  @DefaultStringValue("Windows Media Video (wmv)")
  public String representation_format_video_x_ms_wmv();

  @DefaultStringValue("Audio Video Interleave File (avi)")
  public String representation_format_video_x_msvideo();

  @DefaultStringValue("Original")
  public String viewOriginal();
}
