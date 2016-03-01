/*
 * An XML document type.
 * Localname: termOfRestriction
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.TermOfRestrictionDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one termOfRestriction(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class TermOfRestrictionDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.TermOfRestrictionDocument
{
    private static final long serialVersionUID = 1L;
    
    public TermOfRestrictionDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName TERMOFRESTRICTION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "termOfRestriction");
    
    
    /**
     * Gets the "termOfRestriction" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType getTermOfRestriction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().find_element_user(TERMOFRESTRICTION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "termOfRestriction" element
     */
    public void setTermOfRestriction(gov.loc.premis.v3.StartAndEndDateComplexType termOfRestriction)
    {
        generatedSetterHelperImpl(termOfRestriction, TERMOFRESTRICTION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "termOfRestriction" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType addNewTermOfRestriction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().add_element_user(TERMOFRESTRICTION$0);
            return target;
        }
    }
}
