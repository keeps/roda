/*
 * An XML document type.
 * Localname: eventDetailInformation
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EventDetailInformationDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one eventDetailInformation(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EventDetailInformationDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EventDetailInformationDocument
{
    private static final long serialVersionUID = 1L;
    
    public EventDetailInformationDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EVENTDETAILINFORMATION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventDetailInformation");
    
    
    /**
     * Gets the "eventDetailInformation" element
     */
    public gov.loc.premis.v3.EventDetailInformationComplexType getEventDetailInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventDetailInformationComplexType target = null;
            target = (gov.loc.premis.v3.EventDetailInformationComplexType)get_store().find_element_user(EVENTDETAILINFORMATION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "eventDetailInformation" element
     */
    public void setEventDetailInformation(gov.loc.premis.v3.EventDetailInformationComplexType eventDetailInformation)
    {
        generatedSetterHelperImpl(eventDetailInformation, EVENTDETAILINFORMATION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "eventDetailInformation" element
     */
    public gov.loc.premis.v3.EventDetailInformationComplexType addNewEventDetailInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventDetailInformationComplexType target = null;
            target = (gov.loc.premis.v3.EventDetailInformationComplexType)get_store().add_element_user(EVENTDETAILINFORMATION$0);
            return target;
        }
    }
}
