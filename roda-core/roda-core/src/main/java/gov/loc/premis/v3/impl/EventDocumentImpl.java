/*
 * An XML document type.
 * Localname: event
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EventDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one event(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EventDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EventDocument
{
    private static final long serialVersionUID = 1L;
    
    public EventDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EVENT$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "event");
    
    
    /**
     * Gets the "event" element
     */
    public gov.loc.premis.v3.EventComplexType getEvent()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventComplexType target = null;
            target = (gov.loc.premis.v3.EventComplexType)get_store().find_element_user(EVENT$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "event" element
     */
    public void setEvent(gov.loc.premis.v3.EventComplexType event)
    {
        generatedSetterHelperImpl(event, EVENT$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "event" element
     */
    public gov.loc.premis.v3.EventComplexType addNewEvent()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventComplexType target = null;
            target = (gov.loc.premis.v3.EventComplexType)get_store().add_element_user(EVENT$0);
            return target;
        }
    }
}
