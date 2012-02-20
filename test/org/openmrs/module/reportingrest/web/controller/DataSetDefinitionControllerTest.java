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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.reportingrest.web.controller.DataSetDefinitionController;
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
	
	@Test
	public void shouldGetAll() throws Exception {
		controller.getAll(request, response);
		Assert.assertEquals(200, response.getStatus());
	}
	
}
