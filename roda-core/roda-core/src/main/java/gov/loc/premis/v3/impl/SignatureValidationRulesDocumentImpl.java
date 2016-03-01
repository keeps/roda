/*
 * An XML document type.
 * Localname: signatureValidationRules
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SignatureValidationRulesDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one signatureValidationRules(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class SignatureValidationRulesDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SignatureValidationRulesDocument
{
    private static final long serialVersionUID = 1L;
    
    public SignatureValidationRulesDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNATUREVALIDATIONRULES$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "signatureValidationRules");
    
    
    /**
     * Gets the "signatureValidationRules" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getSignatureValidationRules()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(SIGNATUREVALIDATIONRULES$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "signatureValidationRules" element
     */
    public void setSignatureValidationRules(gov.loc.premis.v3.StringPlusAuthority signatureValidationRules)
    {
        generatedSetterHelperImpl(signatureValidationRules, SIGNATUREVALIDATIONRULES$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "signatureValidationRules" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewSignatureValidationRules()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(SIGNATUREVALIDATIONRULES$0);
            return target;
        }
    }
}
