/*
 * An XML document type.
 * Localname: otherRightsNote
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.OtherRightsNoteDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one otherRightsNote(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class OtherRightsNoteDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.OtherRightsNoteDocument
{
    private static final long serialVersionUID = 1L;
    
    public OtherRightsNoteDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName OTHERRIGHTSNOTE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "otherRightsNote");
    
    
    /**
     * Gets the "otherRightsNote" element
     */
    public java.lang.String getOtherRightsNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(OTHERRIGHTSNOTE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "otherRightsNote" element
     */
    public org.apache.xmlbeans.XmlString xgetOtherRightsNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(OTHERRIGHTSNOTE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "otherRightsNote" element
     */
    public void setOtherRightsNote(java.lang.String otherRightsNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(OTHERRIGHTSNOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(OTHERRIGHTSNOTE$0);
            }
            target.setStringValue(otherRightsNote);
        }
    }
    
    /**
     * Sets (as xml) the "otherRightsNote" element
     */
    public void xsetOtherRightsNote(org.apache.xmlbeans.XmlString otherRightsNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(OTHERRIGHTSNOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(OTHERRIGHTSNOTE$0);
            }
            target.set(otherRightsNote);
        }
    }
}
