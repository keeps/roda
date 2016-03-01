/*
 * An XML document type.
 * Localname: termOfGrant
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.TermOfGrantDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one termOfGrant(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class TermOfGrantDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.TermOfGrantDocument
{
    private static final long serialVersionUID = 1L;
    
    public TermOfGrantDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName TERMOFGRANT$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "termOfGrant");
    
    
    /**
     * Gets the "termOfGrant" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType getTermOfGrant()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().find_element_user(TERMOFGRANT$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "termOfGrant" element
     */
    public void setTermOfGrant(gov.loc.premis.v3.StartAndEndDateComplexType termOfGrant)
    {
        generatedSetterHelperImpl(termOfGrant, TERMOFGRANT$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "termOfGrant" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType addNewTermOfGrant()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().add_element_user(TERMOFGRANT$0);
            return target;
        }
    }
}
