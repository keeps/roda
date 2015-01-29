package pt.gov.dgarq.roda.core.services;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.RODAServiceException;

public class ConfigurationManager extends RODAWebService {

  final static private Logger logger = Logger.getLogger(ConfigurationManager.class);

  public ConfigurationManager() throws RODAServiceException {
    super();
    logger.info(getClass().getSimpleName() + " initialised OK");
  }

  /**
   * Method to retrieve a property value from RODA-CORE
   * 
   * @param propertyName
   *          name of the property to retrieve
   * 
   * @return the value of the property
   * 
   * @throws RODAServiceException
   *           if propertyName is null or blank
   * */
  public String getProperty(String propertyName) throws RODAServiceException {
    if (!StringUtils.isNotBlank(propertyName)) {
      throw new RODAServiceException("Property name must not be empty!");
    }
    return getConfiguration().getString(propertyName);
  }

  /**
   * Method to retrieve a list of properties from RODA-CORE
   * 
   * @param propertyNames
   *          an array with properties name to retrieve
   * 
   * @return array with properties values (index position match property
   *         requested)
   * 
   * @throws RODAServiceException
   *           if the array provided is null or empty
   * */
  public String[] getProperties(String[] propertyNames) throws RODAServiceException {
    if (propertyNames == null || propertyNames.length == 0) {
      throw new RODAServiceException("Must provide at least one property name!");
    }
    String[] res = new String[propertyNames.length];
    for (int i = 0; i < propertyNames.length; i++) {
      res[i] = getConfiguration().getString(propertyNames[i]);
    }

    return res;
  }

}
