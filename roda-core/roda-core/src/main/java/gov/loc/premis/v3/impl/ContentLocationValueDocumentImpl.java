/*
 * An XML document type.
 * Localname: contentLocationValue
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.ContentLocationValueDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one contentLocationValue(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class ContentLocationValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.ContentLocationValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public ContentLocationValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName CONTENTLOCATIONVALUE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "contentLocationValue");
    
    
    /**
     * Gets the "contentLocationValue" element
     */
    public java.lang.String getContentLocationValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(CONTENTLOCATIONVALUE$0, 0);
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
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(CONTENTLOCATIONVALUE$0, 0);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(CONTENTLOCATIONVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(CONTENTLOCATIONVALUE$0);
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
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(CONTENTLOCATIONVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(CONTENTLOCATIONVALUE$0);
            }
            target.set(contentLocationValue);
        }
    }
}
