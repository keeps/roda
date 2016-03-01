/*
 * An XML document type.
 * Localname: messageDigest
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.MessageDigestDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one messageDigest(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class MessageDigestDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.MessageDigestDocument
{
    private static final long serialVersionUID = 1L;
    
    public MessageDigestDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName MESSAGEDIGEST$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "messageDigest");
    
    
    /**
     * Gets the "messageDigest" element
     */
    public java.lang.String getMessageDigest()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(MESSAGEDIGEST$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "messageDigest" element
     */
    public org.apache.xmlbeans.XmlString xgetMessageDigest()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(MESSAGEDIGEST$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "messageDigest" element
     */
    public void setMessageDigest(java.lang.String messageDigest)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(MESSAGEDIGEST$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(MESSAGEDIGEST$0);
            }
            target.setStringValue(messageDigest);
        }
    }
    
    /**
     * Sets (as xml) the "messageDigest" element
     */
    public void xsetMessageDigest(org.apache.xmlbeans.XmlString messageDigest)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(MESSAGEDIGEST$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(MESSAGEDIGEST$0);
            }
            target.set(messageDigest);
        }
    }
}
