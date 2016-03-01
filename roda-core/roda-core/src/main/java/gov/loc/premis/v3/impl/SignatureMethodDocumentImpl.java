/*
 * An XML document type.
 * Localname: signatureMethod
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SignatureMethodDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one signatureMethod(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class SignatureMethodDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SignatureMethodDocument
{
    private static final long serialVersionUID = 1L;
    
    public SignatureMethodDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNATUREMETHOD$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "signatureMethod");
    
    
    /**
     * Gets the "signatureMethod" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getSignatureMethod()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(SIGNATUREMETHOD$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "signatureMethod" element
     */
    public void setSignatureMethod(gov.loc.premis.v3.StringPlusAuthority signatureMethod)
    {
        generatedSetterHelperImpl(signatureMethod, SIGNATUREMETHOD$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "signatureMethod" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewSignatureMethod()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(SIGNATUREMETHOD$0);
            return target;
        }
    }
}
