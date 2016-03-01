/*
 * An XML document type.
 * Localname: rightsBasis
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RightsBasisDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one rightsBasis(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RightsBasisDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RightsBasisDocument
{
    private static final long serialVersionUID = 1L;
    
    public RightsBasisDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RIGHTSBASIS$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "rightsBasis");
    
    
    /**
     * Gets the "rightsBasis" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getRightsBasis()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(RIGHTSBASIS$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "rightsBasis" element
     */
    public void setRightsBasis(gov.loc.premis.v3.StringPlusAuthority rightsBasis)
    {
        generatedSetterHelperImpl(rightsBasis, RIGHTSBASIS$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "rightsBasis" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewRightsBasis()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(RIGHTSBASIS$0);
            return target;
        }
    }
}
