/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.reportingrest.web.controller;

import java.util.List;

import junit.framework.Assert;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 *
 */
public class DataSetDefinitionControllerTest extends BaseModuleWebContextSensitiveTest {
//
//	DataSetDefinitionController controller;
//	MockHttpServletRequest request;
//	MockHttpServletResponse response;
//
//	@Before
//	public void before() {
//		controller = new DataSetDefinitionController();
//		request = new MockHttpServletRequest();
//		response = new MockHttpServletResponse();
//	}
//
//	@Before
//	public void createData() {
//		SqlDataSetDefinition dsd = new SqlDataSetDefinition();
//		dsd.setName("Everyone");
//		dsd.setDescription("via SQL");
//		dsd.setSqlQuery("select person_id, sex, birthdate from person where voided = 0");
//		dsd.addParameter(new Parameter("param1", "param 1", String.class));
//		dsd.setUuid("12345");
//		Context.getService(DataSetDefinitionService.class).saveDefinition(dsd);
//	}
//
//	@SuppressWarnings("rawtypes")
//    @Test
//	public void shouldSearch() throws Exception {
//		SimpleObject result = controller.search("every", request, response);
//		List results = (List) result.get("results");
//		Assert.assertEquals(1, results.size());
//		System.out.println(new ObjectMapper().writeValueAsString(result));
//	}
//
//	@SuppressWarnings("rawtypes")
//    @Test
//	public void shouldGetAll() throws Exception {
//		SimpleObject result = controller.get("dataSetDefinition", request, response);
//		List results = (List) result.get("results");
//		Assert.assertEquals(1, results.size());
//		System.out.println(new ObjectMapper().writeValueAsString(results));
//	}
//
//	@Test
//	public void shouldGetDefault() throws Exception {
//		Object dsd = controller.retrieve("dataSetDefinition", "12345", request);
//		System.out.println(new ObjectMapper().writeValueAsString(dsd));
//	}
//
}
