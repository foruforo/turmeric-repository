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
// Generated on: 2010.10.18 at 03:20:10 AM PDT 
//


package org.ebayopensource.turmeric.services.repository.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for RepositoryServiceProviderConfig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RepositoryServiceProviderConfig">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="default" type="{http://www.w3.org/2001/XMLSchema}token"/>
 *         &lt;element name="provider-config-list" type="{http://www.ebayopensource.org/turmeric/services/repository/config}RepositoryServiceProviderList"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RepositoryServiceProviderConfig", propOrder = {
    "_default",
    "providerConfigList"
})
public class RepositoryServiceProviderConfig {

    @XmlElement(name = "default", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String _default;
    @XmlElement(name = "provider-config-list", required = true)
    protected RepositoryServiceProviderList providerConfigList;

    /**
     * Gets the value of the default property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefault() {
        return _default;
    }

    /**
     * Sets the value of the default property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefault(String value) {
        this._default = value;
    }

    /**
     * Gets the value of the providerConfigList property.
     * 
     * @return
     *     possible object is
     *     {@link RepositoryServiceProviderList }
     *     
     */
    public RepositoryServiceProviderList getProviderConfigList() {
        return providerConfigList;
    }

    /**
     * Sets the value of the providerConfigList property.
     * 
     * @param value
     *     allowed object is
     *     {@link RepositoryServiceProviderList }
     *     
     */
    public void setProviderConfigList(RepositoryServiceProviderList value) {
        this.providerConfigList = value;
    }

}