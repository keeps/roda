package pt.gov.dgarq.roda.core.data;

import java.io.Serializable;

/**
 * @author Luis Faria
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public class FileFormat implements Serializable {

	private static final long serialVersionUID = -1542372451417885666L;
	
	private String name;
    private String version;
    private String puid;
    private String mimetype;
    private String formatRegistryName;
    private String[] extensions;

    /**
     * Empty file format constructor
     *
     */
    public FileFormat() {
    }

    /**
     * File format constructor
     *
     * @param name format name
     * @param version format version
     * @param puid format PRONOM unique id
     * @param mimetype format MIME type
     * @param formatRegistryName registry name
     * @param extensions the file name extensions for this format
     */
    public FileFormat(String name, String version, String puid,
            String mimetype, String formatRegistryName, String[] extensions) {
        this.name = name;
        this.version = version;
        this.puid = puid;
        this.mimetype = mimetype;
        this.formatRegistryName = formatRegistryName;
        this.extensions = extensions;
    }

    /**
     * Construct a {@link FileFormat} cloning an existing {@link FileFormat}.
     *
     * @param FileFormat the {@link FileFormat} to clone.
     */
    public FileFormat(FileFormat fileFormat) {
        this(fileFormat.getName(), fileFormat.getVersion(), fileFormat.getPuid(), fileFormat.getMimetype(), fileFormat.getFormatRegistryName(), fileFormat.getExtensions());
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("FileFormat(name=");
        strBuilder.append(getName());
        strBuilder.append(", version=");
        strBuilder.append(getVersion());
        strBuilder.append(", puid=");
        strBuilder.append(getPuid());
        strBuilder.append(", mimetype=");
        strBuilder.append(getMimetype());
        strBuilder.append(", formatRegistryName=");
        strBuilder.append(getFormatRegistryName());
        strBuilder.append(")");
        return strBuilder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FileFormat) {
            FileFormat other = (FileFormat) obj;
            if ((getName() == null) && (other.getName() != null)) {
                return false;
            }
            if (!getName().equals(other.getName())) {
                return false;
            }

            if ((getPuid() == null) && (other.getPuid() != null)) {
                return false;
            }
            if (!getPuid().equals(other.getPuid())) {
                return false;
            }

            if ((getFormatRegistryName() == null) && (other.getFormatRegistryName() != null)) {
                return false;
            }
            if (!getFormatRegistryName().equals(other.getFormatRegistryName())) {
                return false;
            }

            if ((getVersion() == null) && (other.getVersion() != null)) {
                return false;
            }
            if (!getVersion().equals(other.getVersion())) {
                return false;
            }

            if ((getMimetype() == null) && (other.getMimetype() != null)) {
                return false;
            }
            if (!getMimetype().equals(other.getMimetype())) {
                return false;
            }

            // TODO extensions
        } else {
            return false;
        }
        return true;
    }

    public void importFileFormat(FileFormat fileFormat) {
        this.setExtensions(fileFormat.getExtensions());
        this.setFormatRegistryName(fileFormat.getFormatRegistryName());
        this.setMimetype(fileFormat.getMimetype());
        this.setName(fileFormat.getName());
        this.setPuid(fileFormat.getPuid());
        this.setVersion(fileFormat.getVersion());
    }

    public FileFormat exportFileFormat() {
        return this;
    }

    /**
     * @return the MIME type
     */
    public String getMimetype() {
        return mimetype;
    }

    /**
     * @param mimetype the MIME type
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
     * @param name the format name
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
     * @param puid the PRONOM unique ID
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
     * @param version the format version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the file format extensions for this format
     */
    public String[] getExtensions() {
        return extensions;
    }

    /**
     * @param extensions the file format extensions for this format
     */
    public void setExtensions(String[] extensions) {
        this.extensions = extensions;
    }

    /**
     * A designation identifying the referenced format registry.
     *
     * @return
     */
    public String getFormatRegistryName() {
        return formatRegistryName;
    }

    /**
     * A designation identifying the referenced format registry.
     *
     * @param formatRegistryName
     */
    public void setFormatRegistryName(String formatRegistryName) {
        this.formatRegistryName = formatRegistryName;
    }
}
