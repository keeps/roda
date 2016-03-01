/*
 * XML Type:  formatRegistryComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.FormatRegistryComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML formatRegistryComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class FormatRegistryComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.FormatRegistryComplexType
{
    private static final long serialVersionUID = 1L;
    
    public FormatRegistryComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName FORMATREGISTRYNAME$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "formatRegistryName");
    private static final javax.xml.namespace.QName FORMATREGISTRYKEY$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "formatRegistryKey");
    private static final javax.xml.namespace.QName FORMATREGISTRYROLE$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "formatRegistryRole");
    private static final javax.xml.namespace.QName SIMPLELINK$6 = 
        new javax.xml.namespace.QName("", "simpleLink");
    
    
    /**
     * Gets the "formatRegistryName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getFormatRegistryName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(FORMATREGISTRYNAME$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "formatRegistryName" element
     */
    public void setFormatRegistryName(gov.loc.premis.v3.StringPlusAuthority formatRegistryName)
    {
        generatedSetterHelperImpl(formatRegistryName, FORMATREGISTRYNAME$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "formatRegistryName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewFormatRegistryName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(FORMATREGISTRYNAME$0);
            return target;
        }
    }
    
    /**
     * Gets the "formatRegistryKey" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getFormatRegistryKey()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(FORMATREGISTRYKEY$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "formatRegistryKey" element
     */
    public void setFormatRegistryKey(gov.loc.premis.v3.StringPlusAuthority formatRegistryKey)
    {
        generatedSetterHelperImpl(formatRegistryKey, FORMATREGISTRYKEY$2, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "formatRegistryKey" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewFormatRegistryKey()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(FORMATREGISTRYKEY$2);
            return target;
        }
    }
    
    /**
     * Gets the "formatRegistryRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getFormatRegistryRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(FORMATREGISTRYROLE$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "formatRegistryRole" element
     */
    public boolean isSetFormatRegistryRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(FORMATREGISTRYROLE$4) != 0;
        }
    }
    
    /**
     * Sets the "formatRegistryRole" element
     */
    public void setFormatRegistryRole(gov.loc.premis.v3.StringPlusAuthority formatRegistryRole)
    {
        generatedSetterHelperImpl(formatRegistryRole, FORMATREGISTRYROLE$4, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "formatRegistryRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewFormatRegistryRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(FORMATREGISTRYROLE$4);
            return target;
        }
    }
    
    /**
     * Unsets the "formatRegistryRole" element
     */
    public void unsetFormatRegistryRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(FORMATREGISTRYROLE$4, 0);
        }
    }
    
    /**
     * Gets the "simpleLink" attribute
     */
    public java.lang.String getSimpleLink()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SIMPLELINK$6);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "simpleLink" attribute
     */
    public org.apache.xmlbeans.XmlAnyURI xgetSimpleLink()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SIMPLELINK$6);
            return target;
        }
    }
    
    /**
     * True if has "simpleLink" attribute
     */
    public boolean isSetSimpleLink()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(SIMPLELINK$6) != null;
        }
    }
    
    /**
     * Sets the "simpleLink" attribute
     */
    public void setSimpleLink(java.lang.String simpleLink)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SIMPLELINK$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(SIMPLELINK$6);
            }
            target.setStringValue(simpleLink);
        }
    }
    
    /**
     * Sets (as xml) the "simpleLink" attribute
     */
    public void xsetSimpleLink(org.apache.xmlbeans.XmlAnyURI simpleLink)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SIMPLELINK$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(SIMPLELINK$6);
            }
            target.set(simpleLink);
        }
    }
    
    /**
     * Unsets the "simpleLink" attribute
     */
    public void unsetSimpleLink()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(SIMPLELINK$6);
        }
    }
}
