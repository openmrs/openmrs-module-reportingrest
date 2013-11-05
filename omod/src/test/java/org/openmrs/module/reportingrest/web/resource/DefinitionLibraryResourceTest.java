/*
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

package org.openmrs.module.reportingrest.web.resource;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.data.patient.definition.PatientIdDataDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class DefinitionLibraryResourceTest extends BaseModuleWebContextSensitiveTest {

    private DefinitionLibraryResource resource;

    @Before
    public void setUp() throws Exception {
        RestService restService = Context.getService(RestService.class);
        resource = (DefinitionLibraryResource) restService.getResourceByName(RestConstants.VERSION_1 + "/reportingrest/definitionlibrary");
    }

    @Test
    public void testListByType() throws Exception {
        RequestContext context = buildRequestContext("q", "patientData");
        SimpleObject result = resource.search(context);
        assertThat(result.get("results"), notNullValue());

        String json = new ObjectMapper().writeValueAsString(result);
        assertTrue(json.contains(BuiltInPatientDataLibrary.PREFIX + "patientId"));

        Matcher<?> elementMatcher = hasProperty("key", BuiltInPatientDataLibrary.PREFIX + "patientId");
        boolean found = false;
        for (Object item : (List<Object>) result.get("results")) {
            if (elementMatcher.matches(item)) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testRetrieveOne() throws Exception {
        Object o = resource.retrieve(BuiltInPatientDataLibrary.PREFIX + "patientId", buildRequestContext());
        assertTrue(o instanceof PatientIdDataDefinition);
    }

    private Matcher<?> hasProperty(final String name, final Object expectedValue) {
        return new BaseMatcher<Object>() {
            @Override
            public boolean matches(Object o) {
                Object actualValue;
                try {
                    actualValue = PropertyUtils.getProperty(o, name);
                } catch (Exception e) {
                    return false;
                }
                return actualValue == null ? (expectedValue == null) : actualValue.equals(expectedValue);
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(name + " = " + expectedValue);
            }
        };
    }

    private RequestContext buildRequestContext(String... paramNamesAndValues) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        for (int i = 0; i < paramNamesAndValues.length; i += 2) {
            request.addParameter(paramNamesAndValues[i], paramNamesAndValues[i + 1]);
        }
        RequestContext context = new RequestContext();
        context.setRequest(request);
        return context;
    }

}
