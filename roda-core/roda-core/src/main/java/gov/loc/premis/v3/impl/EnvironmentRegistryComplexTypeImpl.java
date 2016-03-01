/*
 * XML Type:  environmentRegistryComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EnvironmentRegistryComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML environmentRegistryComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class EnvironmentRegistryComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EnvironmentRegistryComplexType
{
    private static final long serialVersionUID = 1L;
    
    public EnvironmentRegistryComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENVIRONMENTREGISTRYNAME$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentRegistryName");
    private static final javax.xml.namespace.QName ENVIRONMENTREGISTRYKEY$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentRegistryKey");
    private static final javax.xml.namespace.QName ENVIRONMENTREGISTRYROLE$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentRegistryRole");
    
    
    /**
     * Gets the "environmentRegistryName" element
     */
    public java.lang.String getEnvironmentRegistryName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTREGISTRYNAME$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "environmentRegistryName" element
     */
    public org.apache.xmlbeans.XmlString xgetEnvironmentRegistryName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTREGISTRYNAME$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "environmentRegistryName" element
     */
    public void setEnvironmentRegistryName(java.lang.String environmentRegistryName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTREGISTRYNAME$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ENVIRONMENTREGISTRYNAME$0);
            }
            target.setStringValue(environmentRegistryName);
        }
    }
    
    /**
     * Sets (as xml) the "environmentRegistryName" element
     */
    public void xsetEnvironmentRegistryName(org.apache.xmlbeans.XmlString environmentRegistryName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTREGISTRYNAME$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(ENVIRONMENTREGISTRYNAME$0);
            }
            target.set(environmentRegistryName);
        }
    }
    
    /**
     * Gets the "environmentRegistryKey" element
     */
    public java.lang.String getEnvironmentRegistryKey()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTREGISTRYKEY$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "environmentRegistryKey" element
     */
    public org.apache.xmlbeans.XmlString xgetEnvironmentRegistryKey()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTREGISTRYKEY$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "environmentRegistryKey" element
     */
    public void setEnvironmentRegistryKey(java.lang.String environmentRegistryKey)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTREGISTRYKEY$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ENVIRONMENTREGISTRYKEY$2);
            }
            target.setStringValue(environmentRegistryKey);
        }
    }
    
    /**
     * Sets (as xml) the "environmentRegistryKey" element
     */
    public void xsetEnvironmentRegistryKey(org.apache.xmlbeans.XmlString environmentRegistryKey)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTREGISTRYKEY$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(ENVIRONMENTREGISTRYKEY$2);
            }
            target.set(environmentRegistryKey);
        }
    }
    
    /**
     * Gets the "environmentRegistryRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getEnvironmentRegistryRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(ENVIRONMENTREGISTRYROLE$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "environmentRegistryRole" element
     */
    public boolean isSetEnvironmentRegistryRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ENVIRONMENTREGISTRYROLE$4) != 0;
        }
    }
    
    /**
     * Sets the "environmentRegistryRole" element
     */
    public void setEnvironmentRegistryRole(gov.loc.premis.v3.StringPlusAuthority environmentRegistryRole)
    {
        generatedSetterHelperImpl(environmentRegistryRole, ENVIRONMENTREGISTRYROLE$4, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "environmentRegistryRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewEnvironmentRegistryRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(ENVIRONMENTREGISTRYROLE$4);
            return target;
        }
    }
    
    /**
     * Unsets the "environmentRegistryRole" element
     */
    public void unsetEnvironmentRegistryRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ENVIRONMENTREGISTRYROLE$4, 0);
        }
    }
}
