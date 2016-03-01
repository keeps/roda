/*
 * An XML document type.
 * Localname: eventIdentifier
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EventIdentifierDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one eventIdentifier(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EventIdentifierDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EventIdentifierDocument
{
    private static final long serialVersionUID = 1L;
    
    public EventIdentifierDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EVENTIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventIdentifier");
    
    
    /**
     * Gets the "eventIdentifier" element
     */
    public gov.loc.premis.v3.EventIdentifierComplexType getEventIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.EventIdentifierComplexType)get_store().find_element_user(EVENTIDENTIFIER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "eventIdentifier" element
     */
    public void setEventIdentifier(gov.loc.premis.v3.EventIdentifierComplexType eventIdentifier)
    {
        generatedSetterHelperImpl(eventIdentifier, EVENTIDENTIFIER$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "eventIdentifier" element
     */
    public gov.loc.premis.v3.EventIdentifierComplexType addNewEventIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.EventIdentifierComplexType)get_store().add_element_user(EVENTIDENTIFIER$0);
            return target;
        }
    }
}
