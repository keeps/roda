/*
 * An XML document type.
 * Localname: formatVersion
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.FormatVersionDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one formatVersion(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class FormatVersionDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.FormatVersionDocument
{
    private static final long serialVersionUID = 1L;
    
    public FormatVersionDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName FORMATVERSION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "formatVersion");
    
    
    /**
     * Gets the "formatVersion" element
     */
    public java.lang.String getFormatVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(FORMATVERSION$0, 0);
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
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(FORMATVERSION$0, 0);
            return target;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(FORMATVERSION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(FORMATVERSION$0);
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
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(FORMATVERSION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(FORMATVERSION$0);
            }
            target.set(formatVersion);
        }
    }
}
