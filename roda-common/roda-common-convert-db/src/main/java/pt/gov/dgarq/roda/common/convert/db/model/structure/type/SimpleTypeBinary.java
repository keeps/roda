/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.model.structure.type;

/**
 * A value of binary string type (known as a binary large object, or BLOB) is a
 * variable length sequence of octets, up to an implementation-defined maximum.
 * 
 * @author Luis Faria
 */
public class SimpleTypeBinary extends Type {
	private String formatRegistryName;

	private String formatRegistryKey;

	/**
	 * Binary type constructor, with no optional fields. Format registry name
	 * and key will be null
	 * 
	 */
	public SimpleTypeBinary() {
		formatRegistryName = null;
		formatRegistryKey = null;
	}

	/**
	 * Binary type constructor, with optional fields
	 * 
	 * @param formatRegistryName
	 *            a file format registry, like MIME or PRONOM
	 * @param formatRegistryKey
	 *            the file format according to the designated registry, e.g.
	 *            image/png for MIME Type
	 */
	public SimpleTypeBinary(String formatRegistryName, String formatRegistryKey) {
		this.formatRegistryName = formatRegistryName;
		this.formatRegistryKey = formatRegistryKey;
	}

	/**
	 * @return the file format registry, like MIME or PRONOM
	 */
	public String getFormatRegistryKey() {
		return formatRegistryKey;
	}

	/**
	 * @return the file format according to the designated registry, e.g.
	 *         image/png for MIME Type
	 */
	public String getFormatRegistryName() {
		return formatRegistryName;
	}

	/**
	 * The file format according to a designated registry
	 * 
	 * @param name
	 *            the name of the registry, like MIME or PRONOM
	 * @param key
	 *            the file format, like image/png for MIME
	 */
	public void setFormatRegistry(String name, String key) {
		this.formatRegistryName = name;
		this.formatRegistryKey = key;
	}

}
