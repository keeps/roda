/*
 * An XML document type.
 * Localname: otherRightsBasis
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.OtherRightsBasisDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one otherRightsBasis(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class OtherRightsBasisDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.OtherRightsBasisDocument
{
    private static final long serialVersionUID = 1L;
    
    public OtherRightsBasisDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName OTHERRIGHTSBASIS$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "otherRightsBasis");
    
    
    /**
     * Gets the "otherRightsBasis" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getOtherRightsBasis()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(OTHERRIGHTSBASIS$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "otherRightsBasis" element
     */
    public void setOtherRightsBasis(gov.loc.premis.v3.StringPlusAuthority otherRightsBasis)
    {
        generatedSetterHelperImpl(otherRightsBasis, OTHERRIGHTSBASIS$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "otherRightsBasis" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewOtherRightsBasis()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(OTHERRIGHTSBASIS$0);
            return target;
        }
    }
}
