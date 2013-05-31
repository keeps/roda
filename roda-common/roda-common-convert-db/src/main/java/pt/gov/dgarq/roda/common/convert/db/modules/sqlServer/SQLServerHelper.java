/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.modules.sqlServer;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.convert.db.model.exception.UnknownTypeException;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeBinary;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeDateTime;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeString;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.Type;
import pt.gov.dgarq.roda.common.convert.db.modules.SQLHelper;

/**
 * SQL Server 2005 Helper
 * 
 * @author Luis Faria
 * 
 */
public class SQLServerHelper extends SQLHelper {

	private final Logger logger = Logger.getLogger(SQLServerHelper.class);

	protected String createTypeSQL(Type type, boolean isPkey, boolean isFkey)
			throws UnknownTypeException {
		String ret;
		if (type instanceof SimpleTypeString) {
			SimpleTypeString string = (SimpleTypeString) type;
			if (string.isLengthVariable()) {
				if (string.getLength().intValue() > 8000) {
					if (isPkey) {
						ret = "varchar(8000)";
						logger.warn("Resizing column length to 8000"
								+ " so it can be a primary key");
					} else {
						ret = "text";
					}
				} else {
					ret = "varchar(" + string.getLength() + ")";
				}
			} else {
				if (string.getLength().intValue() > 8000) {
					ret = "text";
				} else {
					ret = "char(" + string.getLength() + ")";
				}
			}

		} else if (type instanceof SimpleTypeDateTime) {
			logger.warn("Using string instead of datetime type because "
					+ "SQL Server doesn't support dates before 1753-01-01");
			ret = "char(23)";

		} else if (type instanceof SimpleTypeBinary) {
			SimpleTypeBinary binType = (SimpleTypeBinary) type;
			if (binType.getFormatRegistryName().matches("image.*")) {
				ret = "image";
			} else {
				ret = "varbinary";
			}
		} else {
			ret = super.createTypeSQL(type, isPkey, isFkey);
		}
		return ret;
	}
}
