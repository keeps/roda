/*
 * An XML document type.
 * Localname: signatureInformationExtension
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SignatureInformationExtensionDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one signatureInformationExtension(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class SignatureInformationExtensionDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SignatureInformationExtensionDocument
{
    private static final long serialVersionUID = 1L;
    
    public SignatureInformationExtensionDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNATUREINFORMATIONEXTENSION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "signatureInformationExtension");
    
    
    /**
     * Gets the "signatureInformationExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType getSignatureInformationExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(SIGNATUREINFORMATIONEXTENSION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "signatureInformationExtension" element
     */
    public void setSignatureInformationExtension(gov.loc.premis.v3.ExtensionComplexType signatureInformationExtension)
    {
        generatedSetterHelperImpl(signatureInformationExtension, SIGNATUREINFORMATIONEXTENSION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "signatureInformationExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType addNewSignatureInformationExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().add_element_user(SIGNATUREINFORMATIONEXTENSION$0);
            return target;
        }
    }
}
