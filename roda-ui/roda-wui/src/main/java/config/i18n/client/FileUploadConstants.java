/**
 * 
 */
package config.i18n.client;

import com.google.gwt.i18n.client.Constants;

/**
 * @author Luis Faria
 * 
 */
public interface FileUploadConstants extends Constants {
	@DefaultStringValue("Add")
	String fileUploadAddButton();

	@DefaultStringValue("Please select a file")
	String fileUploadNoFileChosenWarning();

	@DefaultStringValue("Please add a file")
	String fileUploadNoFilesWarning();

	@DefaultStringValue("The list of files is not valid for the chosen representation type.")
	String fileUploadInvalidFilename();

	@DefaultStringValue("Uploading files")
	String waitWindowTitle();

	@DefaultStringValue("Please wait ...")
	String waitWindowLabel();

	@DefaultStringValue("Uploaded")
	String fileUploadProgress();

	@DefaultStringValue("cancel")
	String waitWindowCancel();

}
