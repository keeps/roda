/*
 * An XML document type.
 * Localname: eventType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EventTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one eventType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EventTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EventTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public EventTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EVENTTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventType");
    
    
    /**
     * Gets the "eventType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getEventType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(EVENTTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "eventType" element
     */
    public void setEventType(gov.loc.premis.v3.StringPlusAuthority eventType)
    {
        generatedSetterHelperImpl(eventType, EVENTTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "eventType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewEventType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(EVENTTYPE$0);
            return target;
        }
    }
}
