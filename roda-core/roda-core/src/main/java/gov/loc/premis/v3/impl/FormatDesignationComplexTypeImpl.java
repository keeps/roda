/*
 * XML Type:  formatDesignationComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.FormatDesignationComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML formatDesignationComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class FormatDesignationComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.FormatDesignationComplexType
{
    private static final long serialVersionUID = 1L;
    
    public FormatDesignationComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName FORMATNAME$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "formatName");
    private static final javax.xml.namespace.QName FORMATVERSION$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "formatVersion");
    
    
    /**
     * Gets the "formatName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getFormatName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(FORMATNAME$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "formatName" element
     */
    public void setFormatName(gov.loc.premis.v3.StringPlusAuthority formatName)
    {
        generatedSetterHelperImpl(formatName, FORMATNAME$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "formatName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewFormatName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(FORMATNAME$0);
            return target;
        }
    }
    
    /**
     * Gets the "formatVersion" element
     */
    public java.lang.String getFormatVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(FORMATVERSION$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "formatVersion" element
     */
    public org.apache.xmlbeans.XmlString xgetFormatVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(FORMATVERSION$2, 0);
            return target;
        }
    }
    
    /**
     * True if has "formatVersion" element
     */
    public boolean isSetFormatVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(FORMATVERSION$2) != 0;
        }
    }
    
    /**
     * Sets the "formatVersion" element
     */
    public void setFormatVersion(java.lang.String formatVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(FORMATVERSION$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(FORMATVERSION$2);
            }
            target.setStringValue(formatVersion);
        }
    }
    
    /**
     * Sets (as xml) the "formatVersion" element
     */
    public void xsetFormatVersion(org.apache.xmlbeans.XmlString formatVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(FORMATVERSION$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(FORMATVERSION$2);
            }
            target.set(formatVersion);
        }
    }
    
    /**
     * Unsets the "formatVersion" element
     */
    public void unsetFormatVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(FORMATVERSION$2, 0);
        }
    }
}
