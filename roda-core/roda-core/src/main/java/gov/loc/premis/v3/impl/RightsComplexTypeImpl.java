/*
 * XML Type:  rightsComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RightsComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML rightsComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class RightsComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RightsComplexType
{
    private static final long serialVersionUID = 1L;
    
    public RightsComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RIGHTSSTATEMENT$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "rightsStatement");
    private static final javax.xml.namespace.QName RIGHTSEXTENSION$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "rightsExtension");
    private static final javax.xml.namespace.QName XMLID$4 = 
        new javax.xml.namespace.QName("", "xmlID");
    private static final javax.xml.namespace.QName VERSION$6 = 
        new javax.xml.namespace.QName("", "version");
    
    
    /**
     * Gets array of all "rightsStatement" elements
     */
    public gov.loc.premis.v3.RightsStatementComplexType[] getRightsStatementArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(RIGHTSSTATEMENT$0, targetList);
            gov.loc.premis.v3.RightsStatementComplexType[] result = new gov.loc.premis.v3.RightsStatementComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "rightsStatement" element
     */
    public gov.loc.premis.v3.RightsStatementComplexType getRightsStatementArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsStatementComplexType target = null;
            target = (gov.loc.premis.v3.RightsStatementComplexType)get_store().find_element_user(RIGHTSSTATEMENT$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "rightsStatement" element
     */
    public int sizeOfRightsStatementArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(RIGHTSSTATEMENT$0);
        }
    }
    
    /**
     * Sets array of all "rightsStatement" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setRightsStatementArray(gov.loc.premis.v3.RightsStatementComplexType[] rightsStatementArray)
    {
        check_orphaned();
        arraySetterHelper(rightsStatementArray, RIGHTSSTATEMENT$0);
    }
    
    /**
     * Sets ith "rightsStatement" element
     */
    public void setRightsStatementArray(int i, gov.loc.premis.v3.RightsStatementComplexType rightsStatement)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsStatementComplexType target = null;
            target = (gov.loc.premis.v3.RightsStatementComplexType)get_store().find_element_user(RIGHTSSTATEMENT$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(rightsStatement);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "rightsStatement" element
     */
    public gov.loc.premis.v3.RightsStatementComplexType insertNewRightsStatement(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsStatementComplexType target = null;
            target = (gov.loc.premis.v3.RightsStatementComplexType)get_store().insert_element_user(RIGHTSSTATEMENT$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "rightsStatement" element
     */
    public gov.loc.premis.v3.RightsStatementComplexType addNewRightsStatement()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsStatementComplexType target = null;
            target = (gov.loc.premis.v3.RightsStatementComplexType)get_store().add_element_user(RIGHTSSTATEMENT$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "rightsStatement" element
     */
    public void removeRightsStatement(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(RIGHTSSTATEMENT$0, i);
        }
    }
    
    /**
     * Gets array of all "rightsExtension" elements
     */
    public gov.loc.premis.v3.ExtensionComplexType[] getRightsExtensionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(RIGHTSEXTENSION$2, targetList);
            gov.loc.premis.v3.ExtensionComplexType[] result = new gov.loc.premis.v3.ExtensionComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "rightsExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType getRightsExtensionArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(RIGHTSEXTENSION$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "rightsExtension" element
     */
    public int sizeOfRightsExtensionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(RIGHTSEXTENSION$2);
        }
    }
    
    /**
     * Sets array of all "rightsExtension" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setRightsExtensionArray(gov.loc.premis.v3.ExtensionComplexType[] rightsExtensionArray)
    {
        check_orphaned();
        arraySetterHelper(rightsExtensionArray, RIGHTSEXTENSION$2);
    }
    
    /**
     * Sets ith "rightsExtension" element
     */
    public void setRightsExtensionArray(int i, gov.loc.premis.v3.ExtensionComplexType rightsExtension)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(RIGHTSEXTENSION$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(rightsExtension);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "rightsExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType insertNewRightsExtension(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().insert_element_user(RIGHTSEXTENSION$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "rightsExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType addNewRightsExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().add_element_user(RIGHTSEXTENSION$2);
            return target;
        }
    }
    
    /**
     * Removes the ith "rightsExtension" element
     */
    public void removeRightsExtension(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(RIGHTSEXTENSION$2, i);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(XMLID$4);
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
            target = (org.apache.xmlbeans.XmlID)get_store().find_attribute_user(XMLID$4);
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
            return get_store().find_attribute_user(XMLID$4) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(XMLID$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(XMLID$4);
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
            target = (org.apache.xmlbeans.XmlID)get_store().find_attribute_user(XMLID$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlID)get_store().add_attribute_user(XMLID$4);
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
            get_store().remove_attribute(XMLID$4);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VERSION$6);
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
            target = (gov.loc.premis.v3.Version3)get_store().find_attribute_user(VERSION$6);
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
            return get_store().find_attribute_user(VERSION$6) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VERSION$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(VERSION$6);
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
            target = (gov.loc.premis.v3.Version3)get_store().find_attribute_user(VERSION$6);
            if (target == null)
            {
                target = (gov.loc.premis.v3.Version3)get_store().add_attribute_user(VERSION$6);
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
            get_store().remove_attribute(VERSION$6);
        }
    }
}
