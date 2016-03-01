/*
 * An XML document type.
 * Localname: environmentNote
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EnvironmentNoteDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one environmentNote(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EnvironmentNoteDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EnvironmentNoteDocument
{
    private static final long serialVersionUID = 1L;
    
    public EnvironmentNoteDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENVIRONMENTNOTE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentNote");
    
    
    /**
     * Gets the "environmentNote" element
     */
    public java.lang.String getEnvironmentNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTNOTE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "environmentNote" element
     */
    public org.apache.xmlbeans.XmlString xgetEnvironmentNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTNOTE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "environmentNote" element
     */
    public void setEnvironmentNote(java.lang.String environmentNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTNOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ENVIRONMENTNOTE$0);
            }
            target.setStringValue(environmentNote);
        }
    }
    
    /**
     * Sets (as xml) the "environmentNote" element
     */
    public void xsetEnvironmentNote(org.apache.xmlbeans.XmlString environmentNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTNOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(ENVIRONMENTNOTE$0);
            }
            target.set(environmentNote);
        }
    }
}
