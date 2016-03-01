/*
 * XML Type:  compositionLevelComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CompositionLevelComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML compositionLevelComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is an atomic type that is a restriction of gov.loc.premis.v3.CompositionLevelComplexType.
 */
public class CompositionLevelComplexTypeImpl extends org.apache.xmlbeans.impl.values.JavaIntegerHolderEx implements gov.loc.premis.v3.CompositionLevelComplexType
{
    private static final long serialVersionUID = 1L;
    
    public CompositionLevelComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType, true);
    }
    
    protected CompositionLevelComplexTypeImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
    {
        super(sType, b);
    }
    
    private static final javax.xml.namespace.QName UNKNOWN$0 = 
        new javax.xml.namespace.QName("", "unknown");
    
    
    /**
     * Gets the "unknown" attribute
     */
    public gov.loc.premis.v3.CompositionLevelComplexType.Unknown.Enum getUnknown()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(UNKNOWN$0);
            if (target == null)
            {
                return null;
            }
            return (gov.loc.premis.v3.CompositionLevelComplexType.Unknown.Enum)target.getEnumValue();
        }
    }
    
    /**
     * Gets (as xml) the "unknown" attribute
     */
    public gov.loc.premis.v3.CompositionLevelComplexType.Unknown xgetUnknown()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CompositionLevelComplexType.Unknown target = null;
            target = (gov.loc.premis.v3.CompositionLevelComplexType.Unknown)get_store().find_attribute_user(UNKNOWN$0);
            return target;
        }
    }
    
    /**
     * True if has "unknown" attribute
     */
    public boolean isSetUnknown()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(UNKNOWN$0) != null;
        }
    }
    
    /**
     * Sets the "unknown" attribute
     */
    public void setUnknown(gov.loc.premis.v3.CompositionLevelComplexType.Unknown.Enum unknown)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(UNKNOWN$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(UNKNOWN$0);
            }
            target.setEnumValue(unknown);
        }
    }
    
    /**
     * Sets (as xml) the "unknown" attribute
     */
    public void xsetUnknown(gov.loc.premis.v3.CompositionLevelComplexType.Unknown unknown)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CompositionLevelComplexType.Unknown target = null;
            target = (gov.loc.premis.v3.CompositionLevelComplexType.Unknown)get_store().find_attribute_user(UNKNOWN$0);
            if (target == null)
            {
                target = (gov.loc.premis.v3.CompositionLevelComplexType.Unknown)get_store().add_attribute_user(UNKNOWN$0);
            }
            target.set(unknown);
        }
    }
    
    /**
     * Unsets the "unknown" attribute
     */
    public void unsetUnknown()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(UNKNOWN$0);
        }
    }
    /**
     * An XML unknown(@).
     *
     * This is an atomic type that is a restriction of gov.loc.premis.v3.CompositionLevelComplexType$Unknown.
     */
    public static class UnknownImpl extends org.apache.xmlbeans.impl.values.JavaStringEnumerationHolderEx implements gov.loc.premis.v3.CompositionLevelComplexType.Unknown
    {
        private static final long serialVersionUID = 1L;
        
        public UnknownImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType, false);
        }
        
        protected UnknownImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
        {
            super(sType, b);
        }
    }
}
