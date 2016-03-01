/*
 * An XML document type.
 * Localname: eventDateTime
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EventDateTimeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one eventDateTime(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EventDateTimeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EventDateTimeDocument
{
    private static final long serialVersionUID = 1L;
    
    public EventDateTimeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EVENTDATETIME$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventDateTime");
    
    
    /**
     * Gets the "eventDateTime" element
     */
    public java.lang.String getEventDateTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(EVENTDATETIME$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "eventDateTime" element
     */
    public org.apache.xmlbeans.XmlString xgetEventDateTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(EVENTDATETIME$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "eventDateTime" element
     */
    public void setEventDateTime(java.lang.String eventDateTime)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(EVENTDATETIME$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(EVENTDATETIME$0);
            }
            target.setStringValue(eventDateTime);
        }
    }
    
    /**
     * Sets (as xml) the "eventDateTime" element
     */
    public void xsetEventDateTime(org.apache.xmlbeans.XmlString eventDateTime)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(EVENTDATETIME$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(EVENTDATETIME$0);
            }
            target.set(eventDateTime);
        }
    }
}
