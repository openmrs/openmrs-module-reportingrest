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

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class DataSetDefinitionResourceTest extends BaseDelegatingResourceTest<DataSetDefinitionResource, DataSetDefinition> {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    DataSetDefinitionService dsdService;

    @Before
    public void setUp() throws Exception {
        executeDataSet("DataSetDefinitionTest.xml");
    }

    @Override
    public DataSetDefinition newObject() {
        return dsdService.getDefinitionByUuid(getUuidProperty());
    }

    @Override
    public String getDisplayProperty() {
        return "Patients created in 2006";
    }

    @Override
    public String getUuidProperty() {
        return "d9c79890-7ea9-41b1-a068-b5b99ca3d593";
    }

    @Test
    public void testSearchFindsIt() throws Exception {
        RequestContext context = buildRequestContext("q", "patients");
        SimpleObject response = getResource().search(context);
        List<SimpleObject> results = (List<SimpleObject>) response.get("results");
        assertThat(results.size(), is(1));
        assertThat((String) results.get(0).get("uuid"), is(getUuidProperty()));
    }

    @Test
    public void testSearchFindsNothing() throws Exception {
        RequestContext context = buildRequestContext("q", "sakjdfhsad");
        SimpleObject response = getResource().search(context);
        List<SimpleObject> results = (List<SimpleObject>) response.get("results");
        assertThat(results.size(), is(0));
    }

    @Test
    public void testGetAll() throws Exception {
        RequestContext context = buildRequestContext();
        SimpleObject response = getResource().getAll(context);
        List<SimpleObject> results = (List<SimpleObject>) response.get("results");
        assertThat(results.size(), is(1));
        assertThat((String) results.get(0).get("uuid"), is(getUuidProperty()));
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
