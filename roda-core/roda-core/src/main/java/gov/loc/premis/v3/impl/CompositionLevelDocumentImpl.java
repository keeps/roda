/*
 * An XML document type.
 * Localname: compositionLevel
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CompositionLevelDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one compositionLevel(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class CompositionLevelDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.CompositionLevelDocument
{
    private static final long serialVersionUID = 1L;
    
    public CompositionLevelDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName COMPOSITIONLEVEL$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "compositionLevel");
    
    
    /**
     * Gets the "compositionLevel" element
     */
    public gov.loc.premis.v3.CompositionLevelComplexType getCompositionLevel()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CompositionLevelComplexType target = null;
            target = (gov.loc.premis.v3.CompositionLevelComplexType)get_store().find_element_user(COMPOSITIONLEVEL$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "compositionLevel" element
     */
    public void setCompositionLevel(gov.loc.premis.v3.CompositionLevelComplexType compositionLevel)
    {
        generatedSetterHelperImpl(compositionLevel, COMPOSITIONLEVEL$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "compositionLevel" element
     */
    public gov.loc.premis.v3.CompositionLevelComplexType addNewCompositionLevel()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CompositionLevelComplexType target = null;
            target = (gov.loc.premis.v3.CompositionLevelComplexType)get_store().add_element_user(COMPOSITIONLEVEL$0);
            return target;
        }
    }
}
