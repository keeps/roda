/*
 * XML Type:  originalNameComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.OriginalNameComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML originalNameComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is an atomic type that is a restriction of gov.loc.premis.v3.OriginalNameComplexType.
 */
public class OriginalNameComplexTypeImpl extends org.apache.xmlbeans.impl.values.JavaStringHolderEx implements gov.loc.premis.v3.OriginalNameComplexType
{
    private static final long serialVersionUID = 1L;
    
    public OriginalNameComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType, true);
    }
    
    protected OriginalNameComplexTypeImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
    {
        super(sType, b);
    }
    
    private static final javax.xml.namespace.QName SIMPLELINK$0 = 
        new javax.xml.namespace.QName("", "simpleLink");
    
    
    /**
     * Gets the "simpleLink" attribute
     */
    public java.lang.String getSimpleLink()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SIMPLELINK$0);
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
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SIMPLELINK$0);
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
            return get_store().find_attribute_user(SIMPLELINK$0) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SIMPLELINK$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(SIMPLELINK$0);
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
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SIMPLELINK$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(SIMPLELINK$0);
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
            get_store().remove_attribute(SIMPLELINK$0);
        }
    }
}
