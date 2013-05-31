/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.modules.msAccess;

import pt.gov.dgarq.roda.common.convert.db.modules.SQLHelper;

/**
 * @author Luis Faria
 * 
 */
public class MsAccessHelper extends SQLHelper {

	public String selectTableSQL(String tableName) {
		return "SELECT * FROM [" + tableName + "]";
	}
}
