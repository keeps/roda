/*
 * An XML document type.
 * Localname: rightsGrantedNote
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RightsGrantedNoteDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one rightsGrantedNote(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RightsGrantedNoteDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RightsGrantedNoteDocument
{
    private static final long serialVersionUID = 1L;
    
    public RightsGrantedNoteDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RIGHTSGRANTEDNOTE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "rightsGrantedNote");
    
    
    /**
     * Gets the "rightsGrantedNote" element
     */
    public java.lang.String getRightsGrantedNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RIGHTSGRANTEDNOTE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "rightsGrantedNote" element
     */
    public org.apache.xmlbeans.XmlString xgetRightsGrantedNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(RIGHTSGRANTEDNOTE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "rightsGrantedNote" element
     */
    public void setRightsGrantedNote(java.lang.String rightsGrantedNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RIGHTSGRANTEDNOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(RIGHTSGRANTEDNOTE$0);
            }
            target.setStringValue(rightsGrantedNote);
        }
    }
    
    /**
     * Sets (as xml) the "rightsGrantedNote" element
     */
    public void xsetRightsGrantedNote(org.apache.xmlbeans.XmlString rightsGrantedNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(RIGHTSGRANTEDNOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(RIGHTSGRANTEDNOTE$0);
            }
            target.set(rightsGrantedNote);
        }
    }
}
