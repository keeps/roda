/*
 * An XML document type.
 * Localname: signature
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SignatureDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one signature(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class SignatureDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SignatureDocument
{
    private static final long serialVersionUID = 1L;
    
    public SignatureDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNATURE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "signature");
    
    
    /**
     * Gets the "signature" element
     */
    public gov.loc.premis.v3.SignatureComplexType getSignature()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.SignatureComplexType target = null;
            target = (gov.loc.premis.v3.SignatureComplexType)get_store().find_element_user(SIGNATURE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "signature" element
     */
    public void setSignature(gov.loc.premis.v3.SignatureComplexType signature)
    {
        generatedSetterHelperImpl(signature, SIGNATURE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "signature" element
     */
    public gov.loc.premis.v3.SignatureComplexType addNewSignature()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.SignatureComplexType target = null;
            target = (gov.loc.premis.v3.SignatureComplexType)get_store().add_element_user(SIGNATURE$0);
            return target;
        }
    }
}
