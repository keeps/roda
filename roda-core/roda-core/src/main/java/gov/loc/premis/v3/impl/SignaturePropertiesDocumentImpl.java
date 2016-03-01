/*
 * An XML document type.
 * Localname: signatureProperties
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SignaturePropertiesDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one signatureProperties(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class SignaturePropertiesDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SignaturePropertiesDocument
{
    private static final long serialVersionUID = 1L;
    
    public SignaturePropertiesDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNATUREPROPERTIES$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "signatureProperties");
    
    
    /**
     * Gets the "signatureProperties" element
     */
    public java.lang.String getSignatureProperties()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIGNATUREPROPERTIES$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "signatureProperties" element
     */
    public org.apache.xmlbeans.XmlString xgetSignatureProperties()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SIGNATUREPROPERTIES$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "signatureProperties" element
     */
    public void setSignatureProperties(java.lang.String signatureProperties)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIGNATUREPROPERTIES$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(SIGNATUREPROPERTIES$0);
            }
            target.setStringValue(signatureProperties);
        }
    }
    
    /**
     * Sets (as xml) the "signatureProperties" element
     */
    public void xsetSignatureProperties(org.apache.xmlbeans.XmlString signatureProperties)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SIGNATUREPROPERTIES$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(SIGNATUREPROPERTIES$0);
            }
            target.set(signatureProperties);
        }
    }
}
