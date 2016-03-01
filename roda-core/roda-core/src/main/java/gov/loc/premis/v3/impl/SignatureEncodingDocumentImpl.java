/*
 * An XML document type.
 * Localname: signatureEncoding
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SignatureEncodingDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one signatureEncoding(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class SignatureEncodingDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SignatureEncodingDocument
{
    private static final long serialVersionUID = 1L;
    
    public SignatureEncodingDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNATUREENCODING$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "signatureEncoding");
    
    
    /**
     * Gets the "signatureEncoding" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getSignatureEncoding()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(SIGNATUREENCODING$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "signatureEncoding" element
     */
    public void setSignatureEncoding(gov.loc.premis.v3.StringPlusAuthority signatureEncoding)
    {
        generatedSetterHelperImpl(signatureEncoding, SIGNATUREENCODING$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "signatureEncoding" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewSignatureEncoding()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(SIGNATUREENCODING$0);
            return target;
        }
    }
}
