/*
 * An XML document type.
 * Localname: eventOutcome
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EventOutcomeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one eventOutcome(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EventOutcomeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EventOutcomeDocument
{
    private static final long serialVersionUID = 1L;
    
    public EventOutcomeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EVENTOUTCOME$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventOutcome");
    
    
    /**
     * Gets the "eventOutcome" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getEventOutcome()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(EVENTOUTCOME$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "eventOutcome" element
     */
    public void setEventOutcome(gov.loc.premis.v3.StringPlusAuthority eventOutcome)
    {
        generatedSetterHelperImpl(eventOutcome, EVENTOUTCOME$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "eventOutcome" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewEventOutcome()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(EVENTOUTCOME$0);
            return target;
        }
    }
}
