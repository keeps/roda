/*
 * XML Type:  file
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.File
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML file(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class FileImpl extends gov.loc.premis.v3.impl.ObjectComplexTypeImpl implements gov.loc.premis.v3.File
{
    private static final long serialVersionUID = 1L;
    
    public FileImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName OBJECTIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "objectIdentifier");
    private static final javax.xml.namespace.QName PRESERVATIONLEVEL$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "preservationLevel");
    private static final javax.xml.namespace.QName SIGNIFICANTPROPERTIES$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "significantProperties");
    private static final javax.xml.namespace.QName OBJECTCHARACTERISTICS$6 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "objectCharacteristics");
    private static final javax.xml.namespace.QName ORIGINALNAME$8 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "originalName");
    private static final javax.xml.namespace.QName STORAGE$10 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "storage");
    private static final javax.xml.namespace.QName SIGNATUREINFORMATION$12 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "signatureInformation");
    private static final javax.xml.namespace.QName RELATIONSHIP$14 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relationship");
    private static final javax.xml.namespace.QName LINKINGEVENTIDENTIFIER$16 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingEventIdentifier");
    private static final javax.xml.namespace.QName LINKINGRIGHTSSTATEMENTIDENTIFIER$18 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingRightsStatementIdentifier");
    private static final javax.xml.namespace.QName XMLID$20 = 
        new javax.xml.namespace.QName("", "xmlID");
    private static final javax.xml.namespace.QName VERSION$22 = 
        new javax.xml.namespace.QName("", "version");
    
    
    /**
     * Gets array of all "objectIdentifier" elements
     */
    public gov.loc.premis.v3.ObjectIdentifierComplexType[] getObjectIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(OBJECTIDENTIFIER$0, targetList);
            gov.loc.premis.v3.ObjectIdentifierComplexType[] result = new gov.loc.premis.v3.ObjectIdentifierComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "objectIdentifier" element
     */
    public gov.loc.premis.v3.ObjectIdentifierComplexType getObjectIdentifierArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.ObjectIdentifierComplexType)get_store().find_element_user(OBJECTIDENTIFIER$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "objectIdentifier" element
     */
    public int sizeOfObjectIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(OBJECTIDENTIFIER$0);
        }
    }
    
    /**
     * Sets array of all "objectIdentifier" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setObjectIdentifierArray(gov.loc.premis.v3.ObjectIdentifierComplexType[] objectIdentifierArray)
    {
        check_orphaned();
        arraySetterHelper(objectIdentifierArray, OBJECTIDENTIFIER$0);
    }
    
    /**
     * Sets ith "objectIdentifier" element
     */
    public void setObjectIdentifierArray(int i, gov.loc.premis.v3.ObjectIdentifierComplexType objectIdentifier)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.ObjectIdentifierComplexType)get_store().find_element_user(OBJECTIDENTIFIER$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(objectIdentifier);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "objectIdentifier" element
     */
    public gov.loc.premis.v3.ObjectIdentifierComplexType insertNewObjectIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.ObjectIdentifierComplexType)get_store().insert_element_user(OBJECTIDENTIFIER$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "objectIdentifier" element
     */
    public gov.loc.premis.v3.ObjectIdentifierComplexType addNewObjectIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.ObjectIdentifierComplexType)get_store().add_element_user(OBJECTIDENTIFIER$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "objectIdentifier" element
     */
    public void removeObjectIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(OBJECTIDENTIFIER$0, i);
        }
    }
    
    /**
     * Gets array of all "preservationLevel" elements
     */
    public gov.loc.premis.v3.PreservationLevelComplexType[] getPreservationLevelArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(PRESERVATIONLEVEL$2, targetList);
            gov.loc.premis.v3.PreservationLevelComplexType[] result = new gov.loc.premis.v3.PreservationLevelComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "preservationLevel" element
     */
    public gov.loc.premis.v3.PreservationLevelComplexType getPreservationLevelArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.PreservationLevelComplexType target = null;
            target = (gov.loc.premis.v3.PreservationLevelComplexType)get_store().find_element_user(PRESERVATIONLEVEL$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "preservationLevel" element
     */
    public int sizeOfPreservationLevelArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PRESERVATIONLEVEL$2);
        }
    }
    
    /**
     * Sets array of all "preservationLevel" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setPreservationLevelArray(gov.loc.premis.v3.PreservationLevelComplexType[] preservationLevelArray)
    {
        check_orphaned();
        arraySetterHelper(preservationLevelArray, PRESERVATIONLEVEL$2);
    }
    
    /**
     * Sets ith "preservationLevel" element
     */
    public void setPreservationLevelArray(int i, gov.loc.premis.v3.PreservationLevelComplexType preservationLevel)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.PreservationLevelComplexType target = null;
            target = (gov.loc.premis.v3.PreservationLevelComplexType)get_store().find_element_user(PRESERVATIONLEVEL$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(preservationLevel);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "preservationLevel" element
     */
    public gov.loc.premis.v3.PreservationLevelComplexType insertNewPreservationLevel(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.PreservationLevelComplexType target = null;
            target = (gov.loc.premis.v3.PreservationLevelComplexType)get_store().insert_element_user(PRESERVATIONLEVEL$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "preservationLevel" element
     */
    public gov.loc.premis.v3.PreservationLevelComplexType addNewPreservationLevel()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.PreservationLevelComplexType target = null;
            target = (gov.loc.premis.v3.PreservationLevelComplexType)get_store().add_element_user(PRESERVATIONLEVEL$2);
            return target;
        }
    }
    
    /**
     * Removes the ith "preservationLevel" element
     */
    public void removePreservationLevel(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PRESERVATIONLEVEL$2, i);
        }
    }
    
    /**
     * Gets array of all "significantProperties" elements
     */
    public gov.loc.premis.v3.SignificantPropertiesComplexType[] getSignificantPropertiesArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(SIGNIFICANTPROPERTIES$4, targetList);
            gov.loc.premis.v3.SignificantPropertiesComplexType[] result = new gov.loc.premis.v3.SignificantPropertiesComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "significantProperties" element
     */
    public gov.loc.premis.v3.SignificantPropertiesComplexType getSignificantPropertiesArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.SignificantPropertiesComplexType target = null;
            target = (gov.loc.premis.v3.SignificantPropertiesComplexType)get_store().find_element_user(SIGNIFICANTPROPERTIES$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "significantProperties" element
     */
    public int sizeOfSignificantPropertiesArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SIGNIFICANTPROPERTIES$4);
        }
    }
    
    /**
     * Sets array of all "significantProperties" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setSignificantPropertiesArray(gov.loc.premis.v3.SignificantPropertiesComplexType[] significantPropertiesArray)
    {
        check_orphaned();
        arraySetterHelper(significantPropertiesArray, SIGNIFICANTPROPERTIES$4);
    }
    
    /**
     * Sets ith "significantProperties" element
     */
    public void setSignificantPropertiesArray(int i, gov.loc.premis.v3.SignificantPropertiesComplexType significantProperties)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.SignificantPropertiesComplexType target = null;
            target = (gov.loc.premis.v3.SignificantPropertiesComplexType)get_store().find_element_user(SIGNIFICANTPROPERTIES$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(significantProperties);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "significantProperties" element
     */
    public gov.loc.premis.v3.SignificantPropertiesComplexType insertNewSignificantProperties(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.SignificantPropertiesComplexType target = null;
            target = (gov.loc.premis.v3.SignificantPropertiesComplexType)get_store().insert_element_user(SIGNIFICANTPROPERTIES$4, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "significantProperties" element
     */
    public gov.loc.premis.v3.SignificantPropertiesComplexType addNewSignificantProperties()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.SignificantPropertiesComplexType target = null;
            target = (gov.loc.premis.v3.SignificantPropertiesComplexType)get_store().add_element_user(SIGNIFICANTPROPERTIES$4);
            return target;
        }
    }
    
    /**
     * Removes the ith "significantProperties" element
     */
    public void removeSignificantProperties(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SIGNIFICANTPROPERTIES$4, i);
        }
    }
    
    /**
     * Gets array of all "objectCharacteristics" elements
     */
    public gov.loc.premis.v3.ObjectCharacteristicsComplexType[] getObjectCharacteristicsArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(OBJECTCHARACTERISTICS$6, targetList);
            gov.loc.premis.v3.ObjectCharacteristicsComplexType[] result = new gov.loc.premis.v3.ObjectCharacteristicsComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "objectCharacteristics" element
     */
    public gov.loc.premis.v3.ObjectCharacteristicsComplexType getObjectCharacteristicsArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ObjectCharacteristicsComplexType target = null;
            target = (gov.loc.premis.v3.ObjectCharacteristicsComplexType)get_store().find_element_user(OBJECTCHARACTERISTICS$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "objectCharacteristics" element
     */
    public int sizeOfObjectCharacteristicsArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(OBJECTCHARACTERISTICS$6);
        }
    }
    
    /**
     * Sets array of all "objectCharacteristics" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setObjectCharacteristicsArray(gov.loc.premis.v3.ObjectCharacteristicsComplexType[] objectCharacteristicsArray)
    {
        check_orphaned();
        arraySetterHelper(objectCharacteristicsArray, OBJECTCHARACTERISTICS$6);
    }
    
    /**
     * Sets ith "objectCharacteristics" element
     */
    public void setObjectCharacteristicsArray(int i, gov.loc.premis.v3.ObjectCharacteristicsComplexType objectCharacteristics)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ObjectCharacteristicsComplexType target = null;
            target = (gov.loc.premis.v3.ObjectCharacteristicsComplexType)get_store().find_element_user(OBJECTCHARACTERISTICS$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(objectCharacteristics);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "objectCharacteristics" element
     */
    public gov.loc.premis.v3.ObjectCharacteristicsComplexType insertNewObjectCharacteristics(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ObjectCharacteristicsComplexType target = null;
            target = (gov.loc.premis.v3.ObjectCharacteristicsComplexType)get_store().insert_element_user(OBJECTCHARACTERISTICS$6, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "objectCharacteristics" element
     */
    public gov.loc.premis.v3.ObjectCharacteristicsComplexType addNewObjectCharacteristics()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ObjectCharacteristicsComplexType target = null;
            target = (gov.loc.premis.v3.ObjectCharacteristicsComplexType)get_store().add_element_user(OBJECTCHARACTERISTICS$6);
            return target;
        }
    }
    
    /**
     * Removes the ith "objectCharacteristics" element
     */
    public void removeObjectCharacteristics(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(OBJECTCHARACTERISTICS$6, i);
        }
    }
    
    /**
     * Gets the "originalName" element
     */
    public gov.loc.premis.v3.OriginalNameComplexType getOriginalName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.OriginalNameComplexType target = null;
            target = (gov.loc.premis.v3.OriginalNameComplexType)get_store().find_element_user(ORIGINALNAME$8, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "originalName" element
     */
    public boolean isSetOriginalName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ORIGINALNAME$8) != 0;
        }
    }
    
    /**
     * Sets the "originalName" element
     */
    public void setOriginalName(gov.loc.premis.v3.OriginalNameComplexType originalName)
    {
        generatedSetterHelperImpl(originalName, ORIGINALNAME$8, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "originalName" element
     */
    public gov.loc.premis.v3.OriginalNameComplexType addNewOriginalName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.OriginalNameComplexType target = null;
            target = (gov.loc.premis.v3.OriginalNameComplexType)get_store().add_element_user(ORIGINALNAME$8);
            return target;
        }
    }
    
    /**
     * Unsets the "originalName" element
     */
    public void unsetOriginalName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ORIGINALNAME$8, 0);
        }
    }
    
    /**
     * Gets array of all "storage" elements
     */
    public gov.loc.premis.v3.StorageComplexType[] getStorageArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(STORAGE$10, targetList);
            gov.loc.premis.v3.StorageComplexType[] result = new gov.loc.premis.v3.StorageComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "storage" element
     */
    public gov.loc.premis.v3.StorageComplexType getStorageArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StorageComplexType target = null;
            target = (gov.loc.premis.v3.StorageComplexType)get_store().find_element_user(STORAGE$10, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "storage" element
     */
    public int sizeOfStorageArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(STORAGE$10);
        }
    }
    
    /**
     * Sets array of all "storage" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setStorageArray(gov.loc.premis.v3.StorageComplexType[] storageArray)
    {
        check_orphaned();
        arraySetterHelper(storageArray, STORAGE$10);
    }
    
    /**
     * Sets ith "storage" element
     */
    public void setStorageArray(int i, gov.loc.premis.v3.StorageComplexType storage)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StorageComplexType target = null;
            target = (gov.loc.premis.v3.StorageComplexType)get_store().find_element_user(STORAGE$10, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(storage);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "storage" element
     */
    public gov.loc.premis.v3.StorageComplexType insertNewStorage(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StorageComplexType target = null;
            target = (gov.loc.premis.v3.StorageComplexType)get_store().insert_element_user(STORAGE$10, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "storage" element
     */
    public gov.loc.premis.v3.StorageComplexType addNewStorage()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StorageComplexType target = null;
            target = (gov.loc.premis.v3.StorageComplexType)get_store().add_element_user(STORAGE$10);
            return target;
        }
    }
    
    /**
     * Removes the ith "storage" element
     */
    public void removeStorage(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(STORAGE$10, i);
        }
    }
    
    /**
     * Gets array of all "signatureInformation" elements
     */
    public gov.loc.premis.v3.SignatureInformationComplexType[] getSignatureInformationArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(SIGNATUREINFORMATION$12, targetList);
            gov.loc.premis.v3.SignatureInformationComplexType[] result = new gov.loc.premis.v3.SignatureInformationComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "signatureInformation" element
     */
    public gov.loc.premis.v3.SignatureInformationComplexType getSignatureInformationArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.SignatureInformationComplexType target = null;
            target = (gov.loc.premis.v3.SignatureInformationComplexType)get_store().find_element_user(SIGNATUREINFORMATION$12, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "signatureInformation" element
     */
    public int sizeOfSignatureInformationArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SIGNATUREINFORMATION$12);
        }
    }
    
    /**
     * Sets array of all "signatureInformation" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setSignatureInformationArray(gov.loc.premis.v3.SignatureInformationComplexType[] signatureInformationArray)
    {
        check_orphaned();
        arraySetterHelper(signatureInformationArray, SIGNATUREINFORMATION$12);
    }
    
    /**
     * Sets ith "signatureInformation" element
     */
    public void setSignatureInformationArray(int i, gov.loc.premis.v3.SignatureInformationComplexType signatureInformation)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.SignatureInformationComplexType target = null;
            target = (gov.loc.premis.v3.SignatureInformationComplexType)get_store().find_element_user(SIGNATUREINFORMATION$12, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(signatureInformation);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "signatureInformation" element
     */
    public gov.loc.premis.v3.SignatureInformationComplexType insertNewSignatureInformation(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.SignatureInformationComplexType target = null;
            target = (gov.loc.premis.v3.SignatureInformationComplexType)get_store().insert_element_user(SIGNATUREINFORMATION$12, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "signatureInformation" element
     */
    public gov.loc.premis.v3.SignatureInformationComplexType addNewSignatureInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.SignatureInformationComplexType target = null;
            target = (gov.loc.premis.v3.SignatureInformationComplexType)get_store().add_element_user(SIGNATUREINFORMATION$12);
            return target;
        }
    }
    
    /**
     * Removes the ith "signatureInformation" element
     */
    public void removeSignatureInformation(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SIGNATUREINFORMATION$12, i);
        }
    }
    
    /**
     * Gets array of all "relationship" elements
     */
    public gov.loc.premis.v3.RelationshipComplexType[] getRelationshipArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(RELATIONSHIP$14, targetList);
            gov.loc.premis.v3.RelationshipComplexType[] result = new gov.loc.premis.v3.RelationshipComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "relationship" element
     */
    public gov.loc.premis.v3.RelationshipComplexType getRelationshipArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RelationshipComplexType target = null;
            target = (gov.loc.premis.v3.RelationshipComplexType)get_store().find_element_user(RELATIONSHIP$14, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "relationship" element
     */
    public int sizeOfRelationshipArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(RELATIONSHIP$14);
        }
    }
    
    /**
     * Sets array of all "relationship" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setRelationshipArray(gov.loc.premis.v3.RelationshipComplexType[] relationshipArray)
    {
        check_orphaned();
        arraySetterHelper(relationshipArray, RELATIONSHIP$14);
    }
    
    /**
     * Sets ith "relationship" element
     */
    public void setRelationshipArray(int i, gov.loc.premis.v3.RelationshipComplexType relationship)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RelationshipComplexType target = null;
            target = (gov.loc.premis.v3.RelationshipComplexType)get_store().find_element_user(RELATIONSHIP$14, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(relationship);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "relationship" element
     */
    public gov.loc.premis.v3.RelationshipComplexType insertNewRelationship(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RelationshipComplexType target = null;
            target = (gov.loc.premis.v3.RelationshipComplexType)get_store().insert_element_user(RELATIONSHIP$14, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "relationship" element
     */
    public gov.loc.premis.v3.RelationshipComplexType addNewRelationship()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RelationshipComplexType target = null;
            target = (gov.loc.premis.v3.RelationshipComplexType)get_store().add_element_user(RELATIONSHIP$14);
            return target;
        }
    }
    
    /**
     * Removes the ith "relationship" element
     */
    public void removeRelationship(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(RELATIONSHIP$14, i);
        }
    }
    
    /**
     * Gets array of all "linkingEventIdentifier" elements
     */
    public gov.loc.premis.v3.LinkingEventIdentifierComplexType[] getLinkingEventIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(LINKINGEVENTIDENTIFIER$16, targetList);
            gov.loc.premis.v3.LinkingEventIdentifierComplexType[] result = new gov.loc.premis.v3.LinkingEventIdentifierComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "linkingEventIdentifier" element
     */
    public gov.loc.premis.v3.LinkingEventIdentifierComplexType getLinkingEventIdentifierArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingEventIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingEventIdentifierComplexType)get_store().find_element_user(LINKINGEVENTIDENTIFIER$16, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "linkingEventIdentifier" element
     */
    public int sizeOfLinkingEventIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(LINKINGEVENTIDENTIFIER$16);
        }
    }
    
    /**
     * Sets array of all "linkingEventIdentifier" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setLinkingEventIdentifierArray(gov.loc.premis.v3.LinkingEventIdentifierComplexType[] linkingEventIdentifierArray)
    {
        check_orphaned();
        arraySetterHelper(linkingEventIdentifierArray, LINKINGEVENTIDENTIFIER$16);
    }
    
    /**
     * Sets ith "linkingEventIdentifier" element
     */
    public void setLinkingEventIdentifierArray(int i, gov.loc.premis.v3.LinkingEventIdentifierComplexType linkingEventIdentifier)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingEventIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingEventIdentifierComplexType)get_store().find_element_user(LINKINGEVENTIDENTIFIER$16, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(linkingEventIdentifier);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingEventIdentifier" element
     */
    public gov.loc.premis.v3.LinkingEventIdentifierComplexType insertNewLinkingEventIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingEventIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingEventIdentifierComplexType)get_store().insert_element_user(LINKINGEVENTIDENTIFIER$16, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingEventIdentifier" element
     */
    public gov.loc.premis.v3.LinkingEventIdentifierComplexType addNewLinkingEventIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingEventIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingEventIdentifierComplexType)get_store().add_element_user(LINKINGEVENTIDENTIFIER$16);
            return target;
        }
    }
    
    /**
     * Removes the ith "linkingEventIdentifier" element
     */
    public void removeLinkingEventIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(LINKINGEVENTIDENTIFIER$16, i);
        }
    }
    
    /**
     * Gets array of all "linkingRightsStatementIdentifier" elements
     */
    public gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType[] getLinkingRightsStatementIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(LINKINGRIGHTSSTATEMENTIDENTIFIER$18, targetList);
            gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType[] result = new gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "linkingRightsStatementIdentifier" element
     */
    public gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType getLinkingRightsStatementIdentifierArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType)get_store().find_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIER$18, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "linkingRightsStatementIdentifier" element
     */
    public int sizeOfLinkingRightsStatementIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(LINKINGRIGHTSSTATEMENTIDENTIFIER$18);
        }
    }
    
    /**
     * Sets array of all "linkingRightsStatementIdentifier" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setLinkingRightsStatementIdentifierArray(gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType[] linkingRightsStatementIdentifierArray)
    {
        check_orphaned();
        arraySetterHelper(linkingRightsStatementIdentifierArray, LINKINGRIGHTSSTATEMENTIDENTIFIER$18);
    }
    
    /**
     * Sets ith "linkingRightsStatementIdentifier" element
     */
    public void setLinkingRightsStatementIdentifierArray(int i, gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType linkingRightsStatementIdentifier)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType)get_store().find_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIER$18, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(linkingRightsStatementIdentifier);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingRightsStatementIdentifier" element
     */
    public gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType insertNewLinkingRightsStatementIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType)get_store().insert_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIER$18, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingRightsStatementIdentifier" element
     */
    public gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType addNewLinkingRightsStatementIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType)get_store().add_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIER$18);
            return target;
        }
    }
    
    /**
     * Removes the ith "linkingRightsStatementIdentifier" element
     */
    public void removeLinkingRightsStatementIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(LINKINGRIGHTSSTATEMENTIDENTIFIER$18, i);
        }
    }
    
    /**
     * Gets the "xmlID" attribute
     */
    public java.lang.String getXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(XMLID$20);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "xmlID" attribute
     */
    public org.apache.xmlbeans.XmlID xgetXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlID target = null;
            target = (org.apache.xmlbeans.XmlID)get_store().find_attribute_user(XMLID$20);
            return target;
        }
    }
    
    /**
     * True if has "xmlID" attribute
     */
    public boolean isSetXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(XMLID$20) != null;
        }
    }
    
    /**
     * Sets the "xmlID" attribute
     */
    public void setXmlID(java.lang.String xmlID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(XMLID$20);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(XMLID$20);
            }
            target.setStringValue(xmlID);
        }
    }
    
    /**
     * Sets (as xml) the "xmlID" attribute
     */
    public void xsetXmlID(org.apache.xmlbeans.XmlID xmlID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlID target = null;
            target = (org.apache.xmlbeans.XmlID)get_store().find_attribute_user(XMLID$20);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlID)get_store().add_attribute_user(XMLID$20);
            }
            target.set(xmlID);
        }
    }
    
    /**
     * Unsets the "xmlID" attribute
     */
    public void unsetXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(XMLID$20);
        }
    }
    
    /**
     * Gets the "version" attribute
     */
    public gov.loc.premis.v3.Version3.Enum getVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VERSION$22);
            if (target == null)
            {
                return null;
            }
            return (gov.loc.premis.v3.Version3.Enum)target.getEnumValue();
        }
    }
    
    /**
     * Gets (as xml) the "version" attribute
     */
    public gov.loc.premis.v3.Version3 xgetVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.Version3 target = null;
            target = (gov.loc.premis.v3.Version3)get_store().find_attribute_user(VERSION$22);
            return target;
        }
    }
    
    /**
     * True if has "version" attribute
     */
    public boolean isSetVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(VERSION$22) != null;
        }
    }
    
    /**
     * Sets the "version" attribute
     */
    public void setVersion(gov.loc.premis.v3.Version3.Enum version)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VERSION$22);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(VERSION$22);
            }
            target.setEnumValue(version);
        }
    }
    
    /**
     * Sets (as xml) the "version" attribute
     */
    public void xsetVersion(gov.loc.premis.v3.Version3 version)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.Version3 target = null;
            target = (gov.loc.premis.v3.Version3)get_store().find_attribute_user(VERSION$22);
            if (target == null)
            {
                target = (gov.loc.premis.v3.Version3)get_store().add_attribute_user(VERSION$22);
            }
            target.set(version);
        }
    }
    
    /**
     * Unsets the "version" attribute
     */
    public void unsetVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(VERSION$22);
        }
    }
}
