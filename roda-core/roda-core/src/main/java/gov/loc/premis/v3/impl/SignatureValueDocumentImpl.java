/*
 * An XML document type.
 * Localname: signatureValue
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SignatureValueDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one signatureValue(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class SignatureValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SignatureValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public SignatureValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNATUREVALUE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "signatureValue");
    
    
    /**
     * Gets the "signatureValue" element
     */
    public java.lang.String getSignatureValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIGNATUREVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "signatureValue" element
     */
    public org.apache.xmlbeans.XmlString xgetSignatureValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SIGNATUREVALUE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "signatureValue" element
     */
    public void setSignatureValue(java.lang.String signatureValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIGNATUREVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(SIGNATUREVALUE$0);
            }
            target.setStringValue(signatureValue);
        }
    }
    
    /**
     * Sets (as xml) the "signatureValue" element
     */
    public void xsetSignatureValue(org.apache.xmlbeans.XmlString signatureValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SIGNATUREVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(SIGNATUREVALUE$0);
            }
            target.set(signatureValue);
        }
    }
}
