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

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.webservices.rest.web.Hyperlink;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


/**
 *
 */
public class EvaluatedCohortControllerTest extends BaseModuleWebContextSensitiveTest {
	
    String UUID = "abc123";
	EvaluatedCohortController controller;
	MockHttpServletRequest request;
	MockHttpServletResponse response;
	
	@Before
	public void before() {
		controller = new EvaluatedCohortController();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}
	
	@Before
	public void createData() {
		GenderCohortDefinition cd = new GenderCohortDefinition();
		cd.setName("Males");
		cd.setDescription("male patients");
		cd.setMaleIncluded(true);
		cd.setFemaleIncluded(false);
		cd.setUnknownGenderIncluded(false);
		cd.setUuid(UUID);
		Context.getService(CohortDefinitionService.class).saveDefinition(cd);
	}
	
	@SuppressWarnings("rawtypes")
    @Test
	public void shouldRetrieveDefaultRep() throws Exception {
		Object evaluated = controller.retrieve(UUID, request);
		String json = toJson(evaluated);
		System.out.println("\n" + json + "\n");

		Assert.assertEquals("Males", path(evaluated, "definition.name"));
		Assert.assertEquals("male patients", path(evaluated, "definition.description"));
		Assert.assertEquals(UUID, path(evaluated, "definition.uuid"));
		
		Assert.assertTrue(hasLink(evaluated, "self", "/cohort/" + UUID));
		
		// should include patients 2 and 6 from standard test dataset. their uuids are:
		String[] expectedUuids = new String[] { "da7f524f-27ce-4bb2-86d6-6d1d05312bd5", "a7e04421-525f-442f-8138-05b619d16def" };

		Assert.assertEquals(expectedUuids.length, ((List) path(evaluated, "members")).size());
		for (String expected : expectedUuids) {
			Assert.assertTrue(json.contains("/patient/" + expected));
		}
	}

    @SuppressWarnings("unchecked")
    private boolean hasLink(Object obj, String rel, String uriEndsWith) {
	    List<Hyperlink> links = (List<Hyperlink>) path(obj, "links");
	    for (Hyperlink link : links) {
	    	if (link.getRel().equals(rel) && link.getUri().endsWith(uriEndsWith))
	    		return true;
	    }
	    return false;
    }

	private String toJson(Object obj) throws Exception {
	    return new ObjectMapper().writeValueAsString(obj);
    }

    private Object path(Object object, String dotSeparated) {
		String[] components = dotSeparated.split("\\.");
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
