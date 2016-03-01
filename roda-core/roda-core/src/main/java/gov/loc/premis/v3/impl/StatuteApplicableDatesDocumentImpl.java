/*
 * An XML document type.
 * Localname: statuteApplicableDates
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.StatuteApplicableDatesDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one statuteApplicableDates(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class StatuteApplicableDatesDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.StatuteApplicableDatesDocument
{
    private static final long serialVersionUID = 1L;
    
    public StatuteApplicableDatesDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName STATUTEAPPLICABLEDATES$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "statuteApplicableDates");
    
    
    /**
     * Gets the "statuteApplicableDates" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType getStatuteApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().find_element_user(STATUTEAPPLICABLEDATES$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "statuteApplicableDates" element
     */
    public void setStatuteApplicableDates(gov.loc.premis.v3.StartAndEndDateComplexType statuteApplicableDates)
    {
        generatedSetterHelperImpl(statuteApplicableDates, STATUTEAPPLICABLEDATES$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "statuteApplicableDates" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType addNewStatuteApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().add_element_user(STATUTEAPPLICABLEDATES$0);
            return target;
        }
    }
}
