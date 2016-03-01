/*
 * An XML document type.
 * Localname: eventDetail
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EventDetailDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one eventDetail(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EventDetailDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EventDetailDocument
{
    private static final long serialVersionUID = 1L;
    
    public EventDetailDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EVENTDETAIL$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventDetail");
    
    
    /**
     * Gets the "eventDetail" element
     */
    public java.lang.String getEventDetail()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(EVENTDETAIL$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "eventDetail" element
     */
    public org.apache.xmlbeans.XmlString xgetEventDetail()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(EVENTDETAIL$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "eventDetail" element
     */
    public void setEventDetail(java.lang.String eventDetail)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(EVENTDETAIL$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(EVENTDETAIL$0);
            }
            target.setStringValue(eventDetail);
        }
    }
    
    /**
     * Sets (as xml) the "eventDetail" element
     */
    public void xsetEventDetail(org.apache.xmlbeans.XmlString eventDetail)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(EVENTDETAIL$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(EVENTDETAIL$0);
            }
            target.set(eventDetail);
        }
    }
}
