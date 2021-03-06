/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/

package org.ebayopensource.turmeric.repository.wso2;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.repository.v2.services.AssetKey;
import org.ebayopensource.turmeric.repository.v2.services.BasicAssetInfo;
import org.ebayopensource.turmeric.repository.v2.services.CreateAssetRequest;
import org.ebayopensource.turmeric.repository.v2.services.CreateAssetResponse;
import org.ebayopensource.turmeric.services.repositoryservice.impl.RepositoryServiceProvider;

/**
 * @author mgorovoy
 * 
 */
public class CreateAssetTest extends Wso2Base {
   private static final String assetName = "CreateAssetTest";
   private static final String assetDesc = "CreateAssetTest description";
   private static final String namespace = "http://www.domain.com/assets";

   @Override
   @Before
   public void setUp() throws Exception {
      super.setUp();

      boolean exists = false;
      try {
         exists = RSProviderUtil.getRegistry().resourceExists("/");
      } catch (Exception ex) {
      }

      assertTrue(exists);
   }

   private CreateAssetResponse createAsset() {
      AssetKey key = new AssetKey();
      key.setAssetName(assetName);

      BasicAssetInfo basicInfo = new BasicAssetInfo();
      basicInfo.setAssetKey(key);
      basicInfo.setAssetName(assetName);
      basicInfo.setAssetDescription(assetDesc);
      basicInfo.setAssetType("Service");
      basicInfo.setVersion("1.0.0");
      basicInfo.setNamespace(namespace);

      CreateAssetRequest request = new CreateAssetRequest();
      request.setBasicAssetInfo(basicInfo);

      RepositoryServiceProvider provider = new RepositoryServiceProviderImpl();
      return provider.createAsset(request);
   }

   @Test
   public void createTest() {
      CreateAssetResponse response = createAsset();

      assertEquals("Error: " + getErrorMessage(response), AckValue.SUCCESS, response.getAck());
      assertEquals(null, response.getErrorMessage());
      validateAssetKey(response.getAssetKey());
   }

   @Test
   public void createDuplicateTest() {
      CreateAssetResponse response = createAsset();
      assertEquals("Error: " + getErrorMessage(response), AckValue.FAILURE, response.getAck());
   }
}
