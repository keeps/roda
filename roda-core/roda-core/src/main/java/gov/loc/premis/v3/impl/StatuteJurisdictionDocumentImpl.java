/*
 * An XML document type.
 * Localname: statuteJurisdiction
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.StatuteJurisdictionDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one statuteJurisdiction(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class StatuteJurisdictionDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.StatuteJurisdictionDocument
{
    private static final long serialVersionUID = 1L;
    
    public StatuteJurisdictionDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName STATUTEJURISDICTION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "statuteJurisdiction");
    
    
    /**
     * Gets the "statuteJurisdiction" element
     */
    public gov.loc.premis.v3.CountryCode getStatuteJurisdiction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CountryCode target = null;
            target = (gov.loc.premis.v3.CountryCode)get_store().find_element_user(STATUTEJURISDICTION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "statuteJurisdiction" element
     */
    public void setStatuteJurisdiction(gov.loc.premis.v3.CountryCode statuteJurisdiction)
    {
        generatedSetterHelperImpl(statuteJurisdiction, STATUTEJURISDICTION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "statuteJurisdiction" element
     */
    public gov.loc.premis.v3.CountryCode addNewStatuteJurisdiction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CountryCode target = null;
            target = (gov.loc.premis.v3.CountryCode)get_store().add_element_user(STATUTEJURISDICTION$0);
            return target;
        }
    }
}
