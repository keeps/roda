/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.modules.postgreSql;

import pt.gov.dgarq.roda.common.convert.db.model.exception.UnknownTypeException;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeBinary;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeDateTime;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeString;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.Type;
import pt.gov.dgarq.roda.common.convert.db.modules.SQLHelper;

/**
 * @author Luis Faria
 * 
 */
public class PostgreSQLHelper extends SQLHelper {

	/**
	 * Grant table read permissions to public
	 * 
	 * @param tableName
	 *            the table name
	 * @return the SQL
	 */
	public String grantPermissionsSQL(String tableName) {
		return "GRANT SELECT ON " + tableName + " TO PUBLIC";
	}

	protected String createTypeSQL(Type type, boolean isPkey, boolean isFkey)
			throws UnknownTypeException {
		String ret;
		if (type instanceof SimpleTypeString) {
			SimpleTypeString string = (SimpleTypeString) type;
			if (string.getLength().intValue() > 10485760) {
				ret = "text";
			} else if (string.isLengthVariable()) {
				ret = "varchar(" + string.getLength() + ")";
			} else {
				ret = "char(" + string.getLength() + ")";
			}
		} else if (type instanceof SimpleTypeDateTime) {
			SimpleTypeDateTime dateTime = (SimpleTypeDateTime) type;
			if (!dateTime.getTimeDefined() && !dateTime.getTimeZoneDefined()) {
				ret = "date";
			} else if (dateTime.getTimeZoneDefined()) {
				ret = "timestamp with time zone";
			} else {
				ret = "timestamp without time zone";
			}

		} else if (type instanceof SimpleTypeBinary) {
			ret = "bytea";
		} else {
			ret = super.createTypeSQL(type, isPkey, isFkey);
		}
		return ret;
	}

}
