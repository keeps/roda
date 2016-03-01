/*
 * An XML document type.
 * Localname: signatureInformation
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SignatureInformationDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one signatureInformation(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class SignatureInformationDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SignatureInformationDocument
{
    private static final long serialVersionUID = 1L;
    
    public SignatureInformationDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNATUREINFORMATION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "signatureInformation");
    
    
    /**
     * Gets the "signatureInformation" element
     */
    public gov.loc.premis.v3.SignatureInformationComplexType getSignatureInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.SignatureInformationComplexType target = null;
            target = (gov.loc.premis.v3.SignatureInformationComplexType)get_store().find_element_user(SIGNATUREINFORMATION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "signatureInformation" element
     */
    public void setSignatureInformation(gov.loc.premis.v3.SignatureInformationComplexType signatureInformation)
    {
        generatedSetterHelperImpl(signatureInformation, SIGNATUREINFORMATION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "signatureInformation" element
     */
    public gov.loc.premis.v3.SignatureInformationComplexType addNewSignatureInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.SignatureInformationComplexType target = null;
            target = (gov.loc.premis.v3.SignatureInformationComplexType)get_store().add_element_user(SIGNATUREINFORMATION$0);
            return target;
        }
    }
}
