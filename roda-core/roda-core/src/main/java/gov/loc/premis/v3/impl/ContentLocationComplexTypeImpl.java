/*
 * XML Type:  contentLocationComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.ContentLocationComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML contentLocationComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class ContentLocationComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.ContentLocationComplexType
{
    private static final long serialVersionUID = 1L;
    
    public ContentLocationComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName CONTENTLOCATIONTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "contentLocationType");
    private static final javax.xml.namespace.QName CONTENTLOCATIONVALUE$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "contentLocationValue");
    private static final javax.xml.namespace.QName SIMPLELINK$4 = 
        new javax.xml.namespace.QName("", "simpleLink");
    
    
    /**
     * Gets the "contentLocationType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getContentLocationType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(CONTENTLOCATIONTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "contentLocationType" element
     */
    public void setContentLocationType(gov.loc.premis.v3.StringPlusAuthority contentLocationType)
    {
        generatedSetterHelperImpl(contentLocationType, CONTENTLOCATIONTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "contentLocationType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewContentLocationType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(CONTENTLOCATIONTYPE$0);
            return target;
        }
    }
    
    /**
     * Gets the "contentLocationValue" element
     */
    public java.lang.String getContentLocationValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(CONTENTLOCATIONVALUE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "contentLocationValue" element
     */
    public org.apache.xmlbeans.XmlString xgetContentLocationValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(CONTENTLOCATIONVALUE$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "contentLocationValue" element
     */
    public void setContentLocationValue(java.lang.String contentLocationValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(CONTENTLOCATIONVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(CONTENTLOCATIONVALUE$2);
            }
            target.setStringValue(contentLocationValue);
        }
    }
    
    /**
     * Sets (as xml) the "contentLocationValue" element
     */
    public void xsetContentLocationValue(org.apache.xmlbeans.XmlString contentLocationValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(CONTENTLOCATIONVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(CONTENTLOCATIONVALUE$2);
            }
            target.set(contentLocationValue);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SIMPLELINK$4);
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
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SIMPLELINK$4);
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
            return get_store().find_attribute_user(SIMPLELINK$4) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SIMPLELINK$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(SIMPLELINK$4);
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
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SIMPLELINK$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(SIMPLELINK$4);
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
            get_store().remove_attribute(SIMPLELINK$4);
        }
    }
}
