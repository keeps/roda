/*
 * XML Type:  objectCharacteristicsComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.ObjectCharacteristicsComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML objectCharacteristicsComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class ObjectCharacteristicsComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.ObjectCharacteristicsComplexType
{
    private static final long serialVersionUID = 1L;
    
    public ObjectCharacteristicsComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName COMPOSITIONLEVEL$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "compositionLevel");
    private static final javax.xml.namespace.QName FIXITY$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "fixity");
    private static final javax.xml.namespace.QName SIZE$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "size");
    private static final javax.xml.namespace.QName FORMAT$6 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "format");
    private static final javax.xml.namespace.QName CREATINGAPPLICATION$8 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "creatingApplication");
    private static final javax.xml.namespace.QName INHIBITORS$10 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "inhibitors");
    private static final javax.xml.namespace.QName OBJECTCHARACTERISTICSEXTENSION$12 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "objectCharacteristicsExtension");
    
    
    /**
     * Gets the "compositionLevel" element
     */
    public gov.loc.premis.v3.CompositionLevelComplexType getCompositionLevel()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CompositionLevelComplexType target = null;
            target = (gov.loc.premis.v3.CompositionLevelComplexType)get_store().find_element_user(COMPOSITIONLEVEL$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "compositionLevel" element
     */
    public boolean isSetCompositionLevel()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(COMPOSITIONLEVEL$0) != 0;
        }
    }
    
    /**
     * Sets the "compositionLevel" element
     */
    public void setCompositionLevel(gov.loc.premis.v3.CompositionLevelComplexType compositionLevel)
    {
        generatedSetterHelperImpl(compositionLevel, COMPOSITIONLEVEL$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "compositionLevel" element
     */
    public gov.loc.premis.v3.CompositionLevelComplexType addNewCompositionLevel()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CompositionLevelComplexType target = null;
            target = (gov.loc.premis.v3.CompositionLevelComplexType)get_store().add_element_user(COMPOSITIONLEVEL$0);
            return target;
        }
    }
    
    /**
     * Unsets the "compositionLevel" element
     */
    public void unsetCompositionLevel()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(COMPOSITIONLEVEL$0, 0);
        }
    }
    
    /**
     * Gets array of all "fixity" elements
     */
    public gov.loc.premis.v3.FixityComplexType[] getFixityArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(FIXITY$2, targetList);
            gov.loc.premis.v3.FixityComplexType[] result = new gov.loc.premis.v3.FixityComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "fixity" element
     */
    public gov.loc.premis.v3.FixityComplexType getFixityArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.FixityComplexType target = null;
            target = (gov.loc.premis.v3.FixityComplexType)get_store().find_element_user(FIXITY$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "fixity" element
     */
    public int sizeOfFixityArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(FIXITY$2);
        }
    }
    
    /**
     * Sets array of all "fixity" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setFixityArray(gov.loc.premis.v3.FixityComplexType[] fixityArray)
    {
        check_orphaned();
        arraySetterHelper(fixityArray, FIXITY$2);
    }
    
    /**
     * Sets ith "fixity" element
     */
    public void setFixityArray(int i, gov.loc.premis.v3.FixityComplexType fixity)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.FixityComplexType target = null;
            target = (gov.loc.premis.v3.FixityComplexType)get_store().find_element_user(FIXITY$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(fixity);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "fixity" element
     */
    public gov.loc.premis.v3.FixityComplexType insertNewFixity(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.FixityComplexType target = null;
            target = (gov.loc.premis.v3.FixityComplexType)get_store().insert_element_user(FIXITY$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "fixity" element
     */
    public gov.loc.premis.v3.FixityComplexType addNewFixity()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.FixityComplexType target = null;
            target = (gov.loc.premis.v3.FixityComplexType)get_store().add_element_user(FIXITY$2);
            return target;
        }
    }
    
    /**
     * Removes the ith "fixity" element
     */
    public void removeFixity(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(FIXITY$2, i);
        }
    }
    
    /**
     * Gets the "size" element
     */
    public long getSize()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIZE$4, 0);
            if (target == null)
            {
                return 0L;
            }
            return target.getLongValue();
        }
    }
    
    /**
     * Gets (as xml) the "size" element
     */
    public org.apache.xmlbeans.XmlLong xgetSize()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlLong target = null;
            target = (org.apache.xmlbeans.XmlLong)get_store().find_element_user(SIZE$4, 0);
            return target;
        }
    }
    
    /**
     * True if has "size" element
     */
    public boolean isSetSize()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SIZE$4) != 0;
        }
    }
    
    /**
     * Sets the "size" element
     */
    public void setSize(long size)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIZE$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(SIZE$4);
            }
            target.setLongValue(size);
        }
    }
    
    /**
     * Sets (as xml) the "size" element
     */
    public void xsetSize(org.apache.xmlbeans.XmlLong size)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlLong target = null;
            target = (org.apache.xmlbeans.XmlLong)get_store().find_element_user(SIZE$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlLong)get_store().add_element_user(SIZE$4);
            }
            target.set(size);
        }
    }
    
    /**
     * Unsets the "size" element
     */
    public void unsetSize()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SIZE$4, 0);
        }
    }
    
    /**
     * Gets array of all "format" elements
     */
    public gov.loc.premis.v3.FormatComplexType[] getFormatArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(FORMAT$6, targetList);
            gov.loc.premis.v3.FormatComplexType[] result = new gov.loc.premis.v3.FormatComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "format" element
     */
    public gov.loc.premis.v3.FormatComplexType getFormatArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.FormatComplexType target = null;
            target = (gov.loc.premis.v3.FormatComplexType)get_store().find_element_user(FORMAT$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "format" element
     */
    public int sizeOfFormatArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(FORMAT$6);
        }
    }
    
    /**
     * Sets array of all "format" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setFormatArray(gov.loc.premis.v3.FormatComplexType[] formatArray)
    {
        check_orphaned();
        arraySetterHelper(formatArray, FORMAT$6);
    }
    
    /**
     * Sets ith "format" element
     */
    public void setFormatArray(int i, gov.loc.premis.v3.FormatComplexType format)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.FormatComplexType target = null;
            target = (gov.loc.premis.v3.FormatComplexType)get_store().find_element_user(FORMAT$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(format);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "format" element
     */
    public gov.loc.premis.v3.FormatComplexType insertNewFormat(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.FormatComplexType target = null;
            target = (gov.loc.premis.v3.FormatComplexType)get_store().insert_element_user(FORMAT$6, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "format" element
     */
    public gov.loc.premis.v3.FormatComplexType addNewFormat()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.FormatComplexType target = null;
            target = (gov.loc.premis.v3.FormatComplexType)get_store().add_element_user(FORMAT$6);
            return target;
        }
    }
    
    /**
     * Removes the ith "format" element
     */
    public void removeFormat(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(FORMAT$6, i);
        }
    }
    
    /**
     * Gets array of all "creatingApplication" elements
     */
    public gov.loc.premis.v3.CreatingApplicationComplexType[] getCreatingApplicationArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(CREATINGAPPLICATION$8, targetList);
            gov.loc.premis.v3.CreatingApplicationComplexType[] result = new gov.loc.premis.v3.CreatingApplicationComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "creatingApplication" element
     */
    public gov.loc.premis.v3.CreatingApplicationComplexType getCreatingApplicationArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CreatingApplicationComplexType target = null;
            target = (gov.loc.premis.v3.CreatingApplicationComplexType)get_store().find_element_user(CREATINGAPPLICATION$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "creatingApplication" element
     */
    public int sizeOfCreatingApplicationArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(CREATINGAPPLICATION$8);
        }
    }
    
    /**
     * Sets array of all "creatingApplication" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setCreatingApplicationArray(gov.loc.premis.v3.CreatingApplicationComplexType[] creatingApplicationArray)
    {
        check_orphaned();
        arraySetterHelper(creatingApplicationArray, CREATINGAPPLICATION$8);
    }
    
    /**
     * Sets ith "creatingApplication" element
     */
    public void setCreatingApplicationArray(int i, gov.loc.premis.v3.CreatingApplicationComplexType creatingApplication)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CreatingApplicationComplexType target = null;
            target = (gov.loc.premis.v3.CreatingApplicationComplexType)get_store().find_element_user(CREATINGAPPLICATION$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(creatingApplication);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "creatingApplication" element
     */
    public gov.loc.premis.v3.CreatingApplicationComplexType insertNewCreatingApplication(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CreatingApplicationComplexType target = null;
            target = (gov.loc.premis.v3.CreatingApplicationComplexType)get_store().insert_element_user(CREATINGAPPLICATION$8, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "creatingApplication" element
     */
    public gov.loc.premis.v3.CreatingApplicationComplexType addNewCreatingApplication()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CreatingApplicationComplexType target = null;
            target = (gov.loc.premis.v3.CreatingApplicationComplexType)get_store().add_element_user(CREATINGAPPLICATION$8);
            return target;
        }
    }
    
    /**
     * Removes the ith "creatingApplication" element
     */
    public void removeCreatingApplication(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(CREATINGAPPLICATION$8, i);
        }
    }
    
    /**
     * Gets array of all "inhibitors" elements
     */
    public gov.loc.premis.v3.InhibitorsComplexType[] getInhibitorsArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(INHIBITORS$10, targetList);
            gov.loc.premis.v3.InhibitorsComplexType[] result = new gov.loc.premis.v3.InhibitorsComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "inhibitors" element
     */
    public gov.loc.premis.v3.InhibitorsComplexType getInhibitorsArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.InhibitorsComplexType target = null;
            target = (gov.loc.premis.v3.InhibitorsComplexType)get_store().find_element_user(INHIBITORS$10, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "inhibitors" element
     */
    public int sizeOfInhibitorsArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(INHIBITORS$10);
        }
    }
    
    /**
     * Sets array of all "inhibitors" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setInhibitorsArray(gov.loc.premis.v3.InhibitorsComplexType[] inhibitorsArray)
    {
        check_orphaned();
        arraySetterHelper(inhibitorsArray, INHIBITORS$10);
    }
    
    /**
     * Sets ith "inhibitors" element
     */
    public void setInhibitorsArray(int i, gov.loc.premis.v3.InhibitorsComplexType inhibitors)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.InhibitorsComplexType target = null;
            target = (gov.loc.premis.v3.InhibitorsComplexType)get_store().find_element_user(INHIBITORS$10, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(inhibitors);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "inhibitors" element
     */
    public gov.loc.premis.v3.InhibitorsComplexType insertNewInhibitors(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.InhibitorsComplexType target = null;
            target = (gov.loc.premis.v3.InhibitorsComplexType)get_store().insert_element_user(INHIBITORS$10, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "inhibitors" element
     */
    public gov.loc.premis.v3.InhibitorsComplexType addNewInhibitors()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.InhibitorsComplexType target = null;
            target = (gov.loc.premis.v3.InhibitorsComplexType)get_store().add_element_user(INHIBITORS$10);
            return target;
        }
    }
    
    /**
     * Removes the ith "inhibitors" element
     */
    public void removeInhibitors(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(INHIBITORS$10, i);
        }
    }
    
    /**
     * Gets array of all "objectCharacteristicsExtension" elements
     */
    public gov.loc.premis.v3.ExtensionComplexType[] getObjectCharacteristicsExtensionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(OBJECTCHARACTERISTICSEXTENSION$12, targetList);
            gov.loc.premis.v3.ExtensionComplexType[] result = new gov.loc.premis.v3.ExtensionComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "objectCharacteristicsExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType getObjectCharacteristicsExtensionArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(OBJECTCHARACTERISTICSEXTENSION$12, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "objectCharacteristicsExtension" element
     */
    public int sizeOfObjectCharacteristicsExtensionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(OBJECTCHARACTERISTICSEXTENSION$12);
        }
    }
    
    /**
     * Sets array of all "objectCharacteristicsExtension" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setObjectCharacteristicsExtensionArray(gov.loc.premis.v3.ExtensionComplexType[] objectCharacteristicsExtensionArray)
    {
        check_orphaned();
        arraySetterHelper(objectCharacteristicsExtensionArray, OBJECTCHARACTERISTICSEXTENSION$12);
    }
    
    /**
     * Sets ith "objectCharacteristicsExtension" element
     */
    public void setObjectCharacteristicsExtensionArray(int i, gov.loc.premis.v3.ExtensionComplexType objectCharacteristicsExtension)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(OBJECTCHARACTERISTICSEXTENSION$12, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(objectCharacteristicsExtension);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "objectCharacteristicsExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType insertNewObjectCharacteristicsExtension(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().insert_element_user(OBJECTCHARACTERISTICSEXTENSION$12, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "objectCharacteristicsExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType addNewObjectCharacteristicsExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().add_element_user(OBJECTCHARACTERISTICSEXTENSION$12);
            return target;
        }
    }
    
    /**
     * Removes the ith "objectCharacteristicsExtension" element
     */
    public void removeObjectCharacteristicsExtension(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(OBJECTCHARACTERISTICSEXTENSION$12, i);
        }
    }
}
