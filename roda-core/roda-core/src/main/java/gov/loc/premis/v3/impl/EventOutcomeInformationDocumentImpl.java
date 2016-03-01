/*
 * An XML document type.
 * Localname: eventOutcomeInformation
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EventOutcomeInformationDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one eventOutcomeInformation(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EventOutcomeInformationDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EventOutcomeInformationDocument
{
    private static final long serialVersionUID = 1L;
    
    public EventOutcomeInformationDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EVENTOUTCOMEINFORMATION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventOutcomeInformation");
    
    
    /**
     * Gets the "eventOutcomeInformation" element
     */
    public gov.loc.premis.v3.EventOutcomeInformationComplexType getEventOutcomeInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventOutcomeInformationComplexType target = null;
            target = (gov.loc.premis.v3.EventOutcomeInformationComplexType)get_store().find_element_user(EVENTOUTCOMEINFORMATION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "eventOutcomeInformation" element
     */
    public void setEventOutcomeInformation(gov.loc.premis.v3.EventOutcomeInformationComplexType eventOutcomeInformation)
    {
        generatedSetterHelperImpl(eventOutcomeInformation, EVENTOUTCOMEINFORMATION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "eventOutcomeInformation" element
     */
    public gov.loc.premis.v3.EventOutcomeInformationComplexType addNewEventOutcomeInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventOutcomeInformationComplexType target = null;
            target = (gov.loc.premis.v3.EventOutcomeInformationComplexType)get_store().add_element_user(EVENTOUTCOMEINFORMATION$0);
            return target;
        }
    }
}
