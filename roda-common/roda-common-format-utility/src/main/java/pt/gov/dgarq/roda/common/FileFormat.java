package pt.gov.dgarq.roda.common;

import java.util.List;

/**
 * @author Luis Faria
 */
public class FileFormat {

	private String name;

	private String version;

	private String puid;

	private String mimetype;

	private List<String> extensions;

	/**
	 * Empty file format constructor
	 * 
	 */
	public FileFormat() {
	}

	/**
	 * File format constructor
	 * 
	 * @param name
	 *            format name
	 * @param version
	 *            format version
	 * @param puid
	 *            format PRONOM unique id
	 * @param mimetype
	 *            format MIME type
	 * @param extensions
	 *            the file name extensions for this format
	 */
	public FileFormat(String name, String version, String puid,
			String mimetype, List<String> extensions) {
		this.name = name;
		this.version = version;
		this.puid = puid;
		this.mimetype = mimetype;
		this.extensions = extensions;
	}

	/**
	 * @return the MIME type
	 */
	public String getMimetype() {
		return mimetype;
	}

	/**
	 * @param mimetype
	 *            the MIME type
	 */
	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	/**
	 * @return the format name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the format name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the PRONOM unique ID
	 */
	public String getPuid() {
		return puid;
	}

	/**
	 * @param puid
	 *            the PRONOM unique ID
	 */
	public void setPuid(String puid) {
		this.puid = puid;
	}

	/**
	 * @return the format version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the format version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the file name extensions for this format
	 */
	public List<String> getExtensions() {
		return extensions;
	}

	/**
	 * @param extensions
	 *            the file name extensions for this format
	 */
	public void setExtensions(List<String> extensions) {
		this.extensions = extensions;
	}

	public String toString() {
		return name + " " + version;
	}

}
