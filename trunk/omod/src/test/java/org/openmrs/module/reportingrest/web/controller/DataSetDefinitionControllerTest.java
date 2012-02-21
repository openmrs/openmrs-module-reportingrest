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
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


/**
 *
 */
public class DataSetDefinitionControllerTest extends BaseModuleWebContextSensitiveTest {
	
	DataSetDefinitionController controller;
	MockHttpServletRequest request;
	MockHttpServletResponse response;
	
	@Before
	public void before() {
		controller = new DataSetDefinitionController();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}
	
	@Before
	public void createData() {
		SqlDataSetDefinition dsd = new SqlDataSetDefinition();
		dsd.setName("Everyone");
		dsd.setDescription("via SQL");
		dsd.setSqlQuery("select person_id, sex, birthdate from person where voided = 0");
		Context.getService(DataSetDefinitionService.class).saveDefinition(dsd);
	}
	
	@Test
	public void shouldSearch() throws Exception {
		SimpleObject result = controller.search("every", request, response);
		List results = (List) result.get("results");
		Assert.assertEquals(1, results.size());
		System.out.println(new ObjectMapper().writeValueAsString(result));
		// uri on self link comes back as "NEED-TO-CONFIGURE/ws/rest/v1/dataSetDefinition/a3c427b2-f1c3-4c81-806d-3af8ce0bc8ed"
		// TODO find out how to point this at a different base url.
	}
	
}
