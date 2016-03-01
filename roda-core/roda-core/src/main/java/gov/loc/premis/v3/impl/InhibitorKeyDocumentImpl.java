/*
 * An XML document type.
 * Localname: inhibitorKey
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.InhibitorKeyDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one inhibitorKey(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class InhibitorKeyDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.InhibitorKeyDocument
{
    private static final long serialVersionUID = 1L;
    
    public InhibitorKeyDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName INHIBITORKEY$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "inhibitorKey");
    
    
    /**
     * Gets the "inhibitorKey" element
     */
    public java.lang.String getInhibitorKey()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(INHIBITORKEY$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "inhibitorKey" element
     */
    public org.apache.xmlbeans.XmlString xgetInhibitorKey()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(INHIBITORKEY$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "inhibitorKey" element
     */
    public void setInhibitorKey(java.lang.String inhibitorKey)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(INHIBITORKEY$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(INHIBITORKEY$0);
            }
            target.setStringValue(inhibitorKey);
        }
    }
    
    /**
     * Sets (as xml) the "inhibitorKey" element
     */
    public void xsetInhibitorKey(org.apache.xmlbeans.XmlString inhibitorKey)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(INHIBITORKEY$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(INHIBITORKEY$0);
            }
            target.set(inhibitorKey);
        }
    }
}
