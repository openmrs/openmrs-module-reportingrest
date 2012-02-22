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

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 *
 */
public class EvaluatedDataSetControllerTest extends BaseModuleWebContextSensitiveTest {

	EvaluatedDataSetController controller;
	MockHttpServletRequest request;
	MockHttpServletResponse response;
	
	@Before
	public void before() {
		controller = new EvaluatedDataSetController();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}
	
	@Before
	public void createData() {
		SqlDataSetDefinition dsd = new SqlDataSetDefinition();
		dsd.setName("Everyone");
		dsd.setDescription("via SQL");
		dsd.setSqlQuery("select person_id, birthdate from person where voided = 0");
		dsd.addParameter(new Parameter("param1", "param 1", String.class));
		dsd.setUuid("12345");
		Context.getService(DataSetDefinitionService.class).saveDefinition(dsd);
	}
	
	@Test
	public void shouldEvaluateADSDWithNoParams() throws Exception {
		Object object = controller.retrieve("12345", request);
		System.out.println(new ObjectMapper().writeValueAsString(object));
		
		Assert.assertEquals("12345", path(object, "uuid"));
		Iterator cols = ((Collection) path(object, "metadata.columns")).iterator();
		{
			Object col = cols.next();
			Assert.assertEquals("PERSON_ID", path(col, "name"));
			Assert.assertEquals("PERSON_ID", path(col, "label"));
			Assert.assertEquals("java.lang.Integer", path(col, "datatype"));
		}
		{
			Object col = cols.next();
			Assert.assertEquals("BIRTHDATE", path(col, "name"));
			Assert.assertEquals("BIRTHDATE", path(col, "label"));
			Assert.assertEquals("java.sql.Timestamp", path(col, "datatype"));
		}
		
	}

	public static Object path(Object object, String path) {
		String[] components = path.split("\\.");
		for (String s : components) {
			try {
	            object = PropertyUtils.getProperty(object, s);
            }
            catch (Exception ex) {
	            return null;
            }
		}
		return object;
	}


}
