/*
 * An XML document type.
 * Localname: otherRightsApplicableDates
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.OtherRightsApplicableDatesDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one otherRightsApplicableDates(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class OtherRightsApplicableDatesDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.OtherRightsApplicableDatesDocument
{
    private static final long serialVersionUID = 1L;
    
    public OtherRightsApplicableDatesDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName OTHERRIGHTSAPPLICABLEDATES$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "otherRightsApplicableDates");
    
    
    /**
     * Gets the "otherRightsApplicableDates" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType getOtherRightsApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().find_element_user(OTHERRIGHTSAPPLICABLEDATES$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "otherRightsApplicableDates" element
     */
    public void setOtherRightsApplicableDates(gov.loc.premis.v3.StartAndEndDateComplexType otherRightsApplicableDates)
    {
        generatedSetterHelperImpl(otherRightsApplicableDates, OTHERRIGHTSAPPLICABLEDATES$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "otherRightsApplicableDates" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType addNewOtherRightsApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().add_element_user(OTHERRIGHTSAPPLICABLEDATES$0);
            return target;
        }
    }
}
