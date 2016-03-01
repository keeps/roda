/*
 * An XML document type.
 * Localname: signer
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SignerDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one signer(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class SignerDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SignerDocument
{
    private static final long serialVersionUID = 1L;
    
    public SignerDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "signer");
    
    
    /**
     * Gets the "signer" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getSigner()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(SIGNER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "signer" element
     */
    public void setSigner(gov.loc.premis.v3.StringPlusAuthority signer)
    {
        generatedSetterHelperImpl(signer, SIGNER$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "signer" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewSigner()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(SIGNER$0);
            return target;
        }
    }
}
