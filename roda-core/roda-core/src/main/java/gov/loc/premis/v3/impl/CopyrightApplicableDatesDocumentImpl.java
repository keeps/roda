/*
 * An XML document type.
 * Localname: copyrightApplicableDates
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CopyrightApplicableDatesDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one copyrightApplicableDates(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class CopyrightApplicableDatesDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.CopyrightApplicableDatesDocument
{
    private static final long serialVersionUID = 1L;
    
    public CopyrightApplicableDatesDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName COPYRIGHTAPPLICABLEDATES$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "copyrightApplicableDates");
    
    
    /**
     * Gets the "copyrightApplicableDates" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType getCopyrightApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().find_element_user(COPYRIGHTAPPLICABLEDATES$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "copyrightApplicableDates" element
     */
    public void setCopyrightApplicableDates(gov.loc.premis.v3.StartAndEndDateComplexType copyrightApplicableDates)
    {
        generatedSetterHelperImpl(copyrightApplicableDates, COPYRIGHTAPPLICABLEDATES$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "copyrightApplicableDates" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType addNewCopyrightApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().add_element_user(COPYRIGHTAPPLICABLEDATES$0);
            return target;
        }
    }
}
