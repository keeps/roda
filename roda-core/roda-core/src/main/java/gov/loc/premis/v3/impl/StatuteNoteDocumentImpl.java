/*
 * An XML document type.
 * Localname: statuteNote
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.StatuteNoteDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one statuteNote(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class StatuteNoteDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.StatuteNoteDocument
{
    private static final long serialVersionUID = 1L;
    
    public StatuteNoteDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName STATUTENOTE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "statuteNote");
    
    
    /**
     * Gets the "statuteNote" element
     */
    public java.lang.String getStatuteNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STATUTENOTE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "statuteNote" element
     */
    public org.apache.xmlbeans.XmlString xgetStatuteNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(STATUTENOTE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "statuteNote" element
     */
    public void setStatuteNote(java.lang.String statuteNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STATUTENOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(STATUTENOTE$0);
            }
            target.setStringValue(statuteNote);
        }
    }
    
    /**
     * Sets (as xml) the "statuteNote" element
     */
    public void xsetStatuteNote(org.apache.xmlbeans.XmlString statuteNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(STATUTENOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(STATUTENOTE$0);
            }
            target.set(statuteNote);
        }
    }
}
