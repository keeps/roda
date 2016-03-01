/*
 * XML Type:  stringPlusAuthority
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.StringPlusAuthority
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML stringPlusAuthority(@http://www.loc.gov/premis/v3).
 *
 * This is an atomic type that is a restriction of gov.loc.premis.v3.StringPlusAuthority.
 */
public class StringPlusAuthorityImpl extends org.apache.xmlbeans.impl.values.JavaStringHolderEx implements gov.loc.premis.v3.StringPlusAuthority
{
    private static final long serialVersionUID = 1L;
    
    public StringPlusAuthorityImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType, true);
    }
    
    protected StringPlusAuthorityImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
    {
        super(sType, b);
    }
    
    private static final javax.xml.namespace.QName AUTHORITY$0 = 
        new javax.xml.namespace.QName("", "authority");
    private static final javax.xml.namespace.QName AUTHORITYURI$2 = 
        new javax.xml.namespace.QName("", "authorityURI");
    private static final javax.xml.namespace.QName VALUEURI$4 = 
        new javax.xml.namespace.QName("", "valueURI");
    
    
    /**
     * Gets the "authority" attribute
     */
    public java.lang.String getAuthority()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(AUTHORITY$0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "authority" attribute
     */
    public org.apache.xmlbeans.XmlString xgetAuthority()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(AUTHORITY$0);
            return target;
        }
    }
    
    /**
     * True if has "authority" attribute
     */
    public boolean isSetAuthority()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(AUTHORITY$0) != null;
        }
    }
    
    /**
     * Sets the "authority" attribute
     */
    public void setAuthority(java.lang.String authority)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(AUTHORITY$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(AUTHORITY$0);
            }
            target.setStringValue(authority);
        }
    }
    
    /**
     * Sets (as xml) the "authority" attribute
     */
    public void xsetAuthority(org.apache.xmlbeans.XmlString authority)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(AUTHORITY$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(AUTHORITY$0);
            }
            target.set(authority);
        }
    }
    
    /**
     * Unsets the "authority" attribute
     */
    public void unsetAuthority()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(AUTHORITY$0);
        }
    }
    
    /**
     * Gets the "authorityURI" attribute
     */
    public java.lang.String getAuthorityURI()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(AUTHORITYURI$2);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "authorityURI" attribute
     */
    public org.apache.xmlbeans.XmlAnyURI xgetAuthorityURI()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(AUTHORITYURI$2);
            return target;
        }
    }
    
    /**
     * True if has "authorityURI" attribute
     */
    public boolean isSetAuthorityURI()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(AUTHORITYURI$2) != null;
        }
    }
    
    /**
     * Sets the "authorityURI" attribute
     */
    public void setAuthorityURI(java.lang.String authorityURI)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(AUTHORITYURI$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(AUTHORITYURI$2);
            }
            target.setStringValue(authorityURI);
        }
    }
    
    /**
     * Sets (as xml) the "authorityURI" attribute
     */
    public void xsetAuthorityURI(org.apache.xmlbeans.XmlAnyURI authorityURI)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(AUTHORITYURI$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(AUTHORITYURI$2);
            }
            target.set(authorityURI);
        }
    }
    
    /**
     * Unsets the "authorityURI" attribute
     */
    public void unsetAuthorityURI()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(AUTHORITYURI$2);
        }
    }
    
    /**
     * Gets the "valueURI" attribute
     */
    public java.lang.String getValueURI()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VALUEURI$4);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "valueURI" attribute
     */
    public org.apache.xmlbeans.XmlAnyURI xgetValueURI()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(VALUEURI$4);
            return target;
        }
    }
    
    /**
     * True if has "valueURI" attribute
     */
    public boolean isSetValueURI()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(VALUEURI$4) != null;
        }
    }
    
    /**
     * Sets the "valueURI" attribute
     */
    public void setValueURI(java.lang.String valueURI)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VALUEURI$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(VALUEURI$4);
            }
            target.setStringValue(valueURI);
        }
    }
    
    /**
     * Sets (as xml) the "valueURI" attribute
     */
    public void xsetValueURI(org.apache.xmlbeans.XmlAnyURI valueURI)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(VALUEURI$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(VALUEURI$4);
            }
            target.set(valueURI);
        }
    }
    
    /**
     * Unsets the "valueURI" attribute
     */
    public void unsetValueURI()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(VALUEURI$4);
        }
    }
}
