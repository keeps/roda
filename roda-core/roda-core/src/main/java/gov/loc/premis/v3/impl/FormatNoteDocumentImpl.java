/*
 * An XML document type.
 * Localname: formatNote
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.FormatNoteDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one formatNote(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class FormatNoteDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.FormatNoteDocument
{
    private static final long serialVersionUID = 1L;
    
    public FormatNoteDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName FORMATNOTE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "formatNote");
    
    
    /**
     * Gets the "formatNote" element
     */
    public java.lang.String getFormatNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(FORMATNOTE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "formatNote" element
     */
    public org.apache.xmlbeans.XmlString xgetFormatNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(FORMATNOTE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "formatNote" element
     */
    public void setFormatNote(java.lang.String formatNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(FORMATNOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(FORMATNOTE$0);
            }
            target.setStringValue(formatNote);
        }
    }
    
    /**
     * Sets (as xml) the "formatNote" element
     */
    public void xsetFormatNote(org.apache.xmlbeans.XmlString formatNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(FORMATNOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(FORMATNOTE$0);
            }
            target.set(formatNote);
        }
    }
}
