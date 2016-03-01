/*
 * An XML document type.
 * Localname: copyrightNote
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CopyrightNoteDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one copyrightNote(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class CopyrightNoteDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.CopyrightNoteDocument
{
    private static final long serialVersionUID = 1L;
    
    public CopyrightNoteDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName COPYRIGHTNOTE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "copyrightNote");
    
    
    /**
     * Gets the "copyrightNote" element
     */
    public java.lang.String getCopyrightNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(COPYRIGHTNOTE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "copyrightNote" element
     */
    public org.apache.xmlbeans.XmlString xgetCopyrightNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(COPYRIGHTNOTE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "copyrightNote" element
     */
    public void setCopyrightNote(java.lang.String copyrightNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(COPYRIGHTNOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(COPYRIGHTNOTE$0);
            }
            target.setStringValue(copyrightNote);
        }
    }
    
    /**
     * Sets (as xml) the "copyrightNote" element
     */
    public void xsetCopyrightNote(org.apache.xmlbeans.XmlString copyrightNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(COPYRIGHTNOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(COPYRIGHTNOTE$0);
            }
            target.set(copyrightNote);
        }
    }
}
