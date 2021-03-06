/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-257 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.04.29 at 04:38:16 AM PDT 
//


package org.ebayopensource.turmeric.assetcreation.artifacts;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for CaptureTemplateName.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CaptureTemplateName">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Propose Service Template"/>
 *     &lt;enumeration value="New Service Template"/>
 *     &lt;enumeration value="Functional Domain Template"/>
 *     &lt;enumeration value="New Consumer"/>
 *     &lt;enumeration value="Property"/>
 *     &lt;enumeration value="Flag"/>
 *     &lt;enumeration value="Flag Set"/>
 *     &lt;enumeration value="Feature Contingency"/>
 *     &lt;enumeration value="Command"/>
 *     &lt;enumeration value="Deliverable"/>
 *     &lt;enumeration value="Search Driver"/>
 *     &lt;enumeration value="Page"/>
 *     &lt;enumeration value="Page Group"/>
 *     &lt;enumeration value="Template"/>
 *     &lt;enumeration value="Module"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum CaptureTemplateName {

    /** The PROPOS e_ servic e_ template. */
    @XmlEnumValue("Propose Service Template")
    PROPOSE_SERVICE_TEMPLATE("Propose Service Template"),
    
    /** The NE w_ servic e_ template. */
    @XmlEnumValue("New Service Template")
    NEW_SERVICE_TEMPLATE("New Service Template"),
    
    /** The FUNCTIONA l_ domai n_ template. */
    @XmlEnumValue("Functional Domain Template")
    FUNCTIONAL_DOMAIN_TEMPLATE("Functional Domain Template"),
    
    /** The NE w_ consumer. */
    @XmlEnumValue("New Consumer")
    NEW_CONSUMER("New Consumer"),
    
    /** The PROPERTY. */
    @XmlEnumValue("Property")
    PROPERTY("Property"),
    
    /** The FLAG. */
    @XmlEnumValue("Flag")
    FLAG("Flag"),
    
    /** The FLA g_ set. */
    @XmlEnumValue("Flag Set")
    FLAG_SET("Flag Set"),
    
    /** The FEATUR e_ contingency. */
    @XmlEnumValue("Feature Contingency")
    FEATURE_CONTINGENCY("Feature Contingency"),
    
    /** The COMMAND. */
    @XmlEnumValue("Command")
    COMMAND("Command"),
    
    /** The DELIVERABLE. */
    @XmlEnumValue("Deliverable")
    DELIVERABLE("Deliverable"),
    
    /** The SEARC h_ driver. */
    @XmlEnumValue("Search Driver")
    SEARCH_DRIVER("Search Driver"),
    
    /** The PAGE. */
    @XmlEnumValue("Page")
    PAGE("Page"),
    
    /** The PAG e_ group. */
    @XmlEnumValue("Page Group")
    PAGE_GROUP("Page Group"),
    
    /** The TEMPLATE. */
    @XmlEnumValue("Template")
    TEMPLATE("Template"),
    
    /** The MODULE. */
    @XmlEnumValue("Module")
    MODULE("Module");
    private final String value;

    /**
     * Instantiates a new capture template name.
     *
     * @param v the v
     */
    CaptureTemplateName(String v) {
        value = v;
    }

    /**
     * Value.
     *
     * @return the string
     */
    public String value() {
        return value;
    }

    /**
     * From value.
     *
     * @param v the v
     * @return the capture template name
     */
    public static CaptureTemplateName fromValue(String v) {
        for (CaptureTemplateName c: CaptureTemplateName.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
