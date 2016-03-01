/*
 * XML Type:  creatingApplicationComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CreatingApplicationComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML creatingApplicationComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class CreatingApplicationComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.CreatingApplicationComplexType
{
    private static final long serialVersionUID = 1L;
    
    public CreatingApplicationComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName CREATINGAPPLICATIONNAME$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "creatingApplicationName");
    private static final javax.xml.namespace.QName CREATINGAPPLICATIONVERSION$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "creatingApplicationVersion");
    private static final javax.xml.namespace.QName DATECREATEDBYAPPLICATION$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "dateCreatedByApplication");
    private static final javax.xml.namespace.QName CREATINGAPPLICATIONEXTENSION$6 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "creatingApplicationExtension");
    
    
    /**
     * Gets the "creatingApplicationName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getCreatingApplicationName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(CREATINGAPPLICATIONNAME$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "creatingApplicationName" element
     */
    public boolean isSetCreatingApplicationName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(CREATINGAPPLICATIONNAME$0) != 0;
        }
    }
    
    /**
     * Sets the "creatingApplicationName" element
     */
    public void setCreatingApplicationName(gov.loc.premis.v3.StringPlusAuthority creatingApplicationName)
    {
        generatedSetterHelperImpl(creatingApplicationName, CREATINGAPPLICATIONNAME$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "creatingApplicationName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewCreatingApplicationName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(CREATINGAPPLICATIONNAME$0);
            return target;
        }
    }
    
    /**
     * Unsets the "creatingApplicationName" element
     */
    public void unsetCreatingApplicationName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(CREATINGAPPLICATIONNAME$0, 0);
        }
    }
    
    /**
     * Gets the "creatingApplicationVersion" element
     */
    public java.lang.String getCreatingApplicationVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(CREATINGAPPLICATIONVERSION$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "creatingApplicationVersion" element
     */
    public org.apache.xmlbeans.XmlString xgetCreatingApplicationVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(CREATINGAPPLICATIONVERSION$2, 0);
            return target;
        }
    }
    
    /**
     * True if has "creatingApplicationVersion" element
     */
    public boolean isSetCreatingApplicationVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(CREATINGAPPLICATIONVERSION$2) != 0;
        }
    }
    
    /**
     * Sets the "creatingApplicationVersion" element
     */
    public void setCreatingApplicationVersion(java.lang.String creatingApplicationVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(CREATINGAPPLICATIONVERSION$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(CREATINGAPPLICATIONVERSION$2);
            }
            target.setStringValue(creatingApplicationVersion);
        }
    }
    
    /**
     * Sets (as xml) the "creatingApplicationVersion" element
     */
    public void xsetCreatingApplicationVersion(org.apache.xmlbeans.XmlString creatingApplicationVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(CREATINGAPPLICATIONVERSION$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(CREATINGAPPLICATIONVERSION$2);
            }
            target.set(creatingApplicationVersion);
        }
    }
    
    /**
     * Unsets the "creatingApplicationVersion" element
     */
    public void unsetCreatingApplicationVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(CREATINGAPPLICATIONVERSION$2, 0);
        }
    }
    
    /**
     * Gets the "dateCreatedByApplication" element
     */
    public java.lang.String getDateCreatedByApplication()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(DATECREATEDBYAPPLICATION$4, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "dateCreatedByApplication" element
     */
    public gov.loc.premis.v3.EdtfSimpleType xgetDateCreatedByApplication()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EdtfSimpleType target = null;
            target = (gov.loc.premis.v3.EdtfSimpleType)get_store().find_element_user(DATECREATEDBYAPPLICATION$4, 0);
            return target;
        }
    }
    
    /**
     * True if has "dateCreatedByApplication" element
     */
    public boolean isSetDateCreatedByApplication()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(DATECREATEDBYAPPLICATION$4) != 0;
        }
    }
    
    /**
     * Sets the "dateCreatedByApplication" element
     */
    public void setDateCreatedByApplication(java.lang.String dateCreatedByApplication)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(DATECREATEDBYAPPLICATION$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(DATECREATEDBYAPPLICATION$4);
            }
            target.setStringValue(dateCreatedByApplication);
        }
    }
    
    /**
     * Sets (as xml) the "dateCreatedByApplication" element
     */
    public void xsetDateCreatedByApplication(gov.loc.premis.v3.EdtfSimpleType dateCreatedByApplication)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EdtfSimpleType target = null;
            target = (gov.loc.premis.v3.EdtfSimpleType)get_store().find_element_user(DATECREATEDBYAPPLICATION$4, 0);
            if (target == null)
            {
                target = (gov.loc.premis.v3.EdtfSimpleType)get_store().add_element_user(DATECREATEDBYAPPLICATION$4);
            }
            target.set(dateCreatedByApplication);
        }
    }
    
    /**
     * Unsets the "dateCreatedByApplication" element
     */
    public void unsetDateCreatedByApplication()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(DATECREATEDBYAPPLICATION$4, 0);
        }
    }
    
    /**
     * Gets array of all "creatingApplicationExtension" elements
     */
    public gov.loc.premis.v3.ExtensionComplexType[] getCreatingApplicationExtensionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(CREATINGAPPLICATIONEXTENSION$6, targetList);
            gov.loc.premis.v3.ExtensionComplexType[] result = new gov.loc.premis.v3.ExtensionComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "creatingApplicationExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType getCreatingApplicationExtensionArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(CREATINGAPPLICATIONEXTENSION$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "creatingApplicationExtension" element
     */
    public int sizeOfCreatingApplicationExtensionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(CREATINGAPPLICATIONEXTENSION$6);
        }
    }
    
    /**
     * Sets array of all "creatingApplicationExtension" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setCreatingApplicationExtensionArray(gov.loc.premis.v3.ExtensionComplexType[] creatingApplicationExtensionArray)
    {
        check_orphaned();
        arraySetterHelper(creatingApplicationExtensionArray, CREATINGAPPLICATIONEXTENSION$6);
    }
    
    /**
     * Sets ith "creatingApplicationExtension" element
     */
    public void setCreatingApplicationExtensionArray(int i, gov.loc.premis.v3.ExtensionComplexType creatingApplicationExtension)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(CREATINGAPPLICATIONEXTENSION$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(creatingApplicationExtension);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "creatingApplicationExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType insertNewCreatingApplicationExtension(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().insert_element_user(CREATINGAPPLICATIONEXTENSION$6, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "creatingApplicationExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType addNewCreatingApplicationExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().add_element_user(CREATINGAPPLICATIONEXTENSION$6);
            return target;
        }
    }
    
    /**
     * Removes the ith "creatingApplicationExtension" element
     */
    public void removeCreatingApplicationExtension(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(CREATINGAPPLICATIONEXTENSION$6, i);
        }
    }
}
