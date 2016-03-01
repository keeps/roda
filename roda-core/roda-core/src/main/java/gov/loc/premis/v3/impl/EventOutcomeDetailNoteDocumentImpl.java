/*
 * An XML document type.
 * Localname: eventOutcomeDetailNote
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EventOutcomeDetailNoteDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one eventOutcomeDetailNote(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EventOutcomeDetailNoteDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EventOutcomeDetailNoteDocument
{
    private static final long serialVersionUID = 1L;
    
    public EventOutcomeDetailNoteDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EVENTOUTCOMEDETAILNOTE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventOutcomeDetailNote");
    
    
    /**
     * Gets the "eventOutcomeDetailNote" element
     */
    public java.lang.String getEventOutcomeDetailNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(EVENTOUTCOMEDETAILNOTE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "eventOutcomeDetailNote" element
     */
    public org.apache.xmlbeans.XmlString xgetEventOutcomeDetailNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(EVENTOUTCOMEDETAILNOTE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "eventOutcomeDetailNote" element
     */
    public void setEventOutcomeDetailNote(java.lang.String eventOutcomeDetailNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(EVENTOUTCOMEDETAILNOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(EVENTOUTCOMEDETAILNOTE$0);
            }
            target.setStringValue(eventOutcomeDetailNote);
        }
    }
    
    /**
     * Sets (as xml) the "eventOutcomeDetailNote" element
     */
    public void xsetEventOutcomeDetailNote(org.apache.xmlbeans.XmlString eventOutcomeDetailNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(EVENTOUTCOMEDETAILNOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(EVENTOUTCOMEDETAILNOTE$0);
            }
            target.set(eventOutcomeDetailNote);
        }
    }
}
