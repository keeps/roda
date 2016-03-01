/*
 * An XML document type.
 * Localname: messageDigestAlgorithm
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.MessageDigestAlgorithmDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one messageDigestAlgorithm(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class MessageDigestAlgorithmDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.MessageDigestAlgorithmDocument
{
    private static final long serialVersionUID = 1L;
    
    public MessageDigestAlgorithmDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName MESSAGEDIGESTALGORITHM$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "messageDigestAlgorithm");
    
    
    /**
     * Gets the "messageDigestAlgorithm" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getMessageDigestAlgorithm()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(MESSAGEDIGESTALGORITHM$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "messageDigestAlgorithm" element
     */
    public void setMessageDigestAlgorithm(gov.loc.premis.v3.StringPlusAuthority messageDigestAlgorithm)
    {
        generatedSetterHelperImpl(messageDigestAlgorithm, MESSAGEDIGESTALGORITHM$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "messageDigestAlgorithm" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewMessageDigestAlgorithm()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(MESSAGEDIGESTALGORITHM$0);
            return target;
        }
    }
}
