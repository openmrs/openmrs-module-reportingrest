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
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.library.BuiltInCohortDefinitionLibrary;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reportingrest.adhoc.AdHocDataSet;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.StringStartsWith.startsWith;

public class AdHocDataSetResourceTest extends BaseModuleWebContextSensitiveTest {

    @Autowired
    DataSetDefinitionService dataSetDefinitionService;

    @Test
    public void testCreate() throws Exception {
        ObjectMapper jackson = new ObjectMapper();
        String json = adHocDataExportAsJson(jackson);
        System.out.println(json);

        RequestContext requestContext = new RequestContext();
        requestContext.setRepresentation(new NamedRepresentation("rowFilters"));

        AdHocDataSetResource resource = new AdHocDataSetResource();
        AdHocDataSet created = (AdHocDataSet) resource.create(jackson.readValue(json, SimpleObject.class), requestContext);

        assertThat(created.getUuid(), notNullValue());
        assertThat(created.getName(), is("Example data export"));
        assertThat(created.getType(), is(PatientDataSetDefinition.class.getName()));
        assertThat(created.getCustomRowFilterCombination(), is("1 OR 2 OR 3"));

        assertThat(created.getParameters().size(), is(2));
        assertThat(created.getParameters().get(0).getName(), is("startDate"));
        assertThat(created.getParameters().get(0).getType(), is("java.util.Date"));
        assertThat(created.getParameters().get(0).getValue(), nullValue());
        assertThat(created.getParameters().get(1).getName(), is("endDate"));
        assertThat(created.getParameters().get(1).getType(), is("java.util.Date"));
        assertThat(created.getParameters().get(1).getValue(), nullValue());

        assertThat(created.getRowFilters().size(), is(3));
        assertThat(created.getRowFilters().get(0).getKey(), is(BuiltInCohortDefinitionLibrary.PREFIX + "males"));
        assertThat(created.getRowFilters().get(0).getParameterValues().size(), is(0));
        assertThat(created.getRowFilters().get(1).getKey(), is(BuiltInCohortDefinitionLibrary.PREFIX + "anyEncounterOfTypesDuringPeriod"));
        assertThat((List<String>) created.getRowFilters().get(1).getParameterValues().get("encounterTypes"), containsInAnyOrder(repWithUuid("61ae96f4-6afe-4351-b6f8-cd4fc383cce1")));
        assertThat(created.getRowFilters().get(2).getKey(), is(BuiltInCohortDefinitionLibrary.PREFIX + "atLeastAgeOnDate"));
        assertThat((Integer) created.getRowFilters().get(2).getParameterValues().get("minAge"), is(15));
        assertThat((String) created.getRowFilters().get(2).getParameterValues().get("effectiveDate"), startsWith("2014-01-01T00:00:00.000"));

        assertThat(created.getColumns().size(), is(2));
        assertThat(created.getColumns().get(0).getKey(), is(BuiltInPatientDataLibrary.PREFIX + "preferredName.givenName"));
        assertThat(created.getColumns().get(0).getName(), is("Given Name"));
        assertThat(created.getColumns().get(0).getParameterValues().size(), is(0));
        assertThat(created.getColumns().get(1).getKey(), is(BuiltInPatientDataLibrary.PREFIX + "preferredName.familyName"));
        assertThat(created.getColumns().get(1).getName(), is("Family Name"));
        assertThat(created.getColumns().get(1).getParameterValues().size(), is(0));
    }

    private Matcher<? super Object> repWithUuid(final String uuid) {
        return new BaseMatcher<Object>() {
            @Override
            public boolean matches(Object o) {
                try {
                    return uuid.equals(PropertyUtils.getProperty(o, "uuid"));
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("resource representation with uuid " + uuid);
            }
        };
    }


    private String adHocDataExportAsJson(ObjectMapper jackson) throws IOException {
        ObjectNode post = jackson.createObjectNode();
        post.put("type", PatientDataSetDefinition.class.getName());
        post.put("name", "Example data export");
        post.put("customRowFilterCombination", "1 OR 2 OR 3");
        ArrayNode parameters = post.putArray("parameters");

        ObjectNode startDate = parameters.addObject();
        startDate.put("name", "startDate");
        startDate.put("type", "java.util.Date");
        startDate.put("value", "2008-08-01");

        ObjectNode endDate = parameters.addObject();
        endDate.put("name", "endDate");
        endDate.put("type", "java.util.Date");
        endDate.put("value", "2008-08-31");

        ArrayNode rowFilters = post.putArray("rowFilters");
        addRowFilter(rowFilters, CohortDefinition.class.getName(), BuiltInCohortDefinitionLibrary.PREFIX + "males");
        ObjectNode q = addRowFilter(rowFilters, CohortDefinition.class.getName(), BuiltInCohortDefinitionLibrary.PREFIX + "anyEncounterOfTypesDuringPeriod");
        parameterValues(q).putArray("encounterTypes").add("61ae96f4-6afe-4351-b6f8-cd4fc383cce1");
//        parameterValues(q).putArray("encounterTypes").addObject().put("uuid", "61ae96f4-6afe-4351-b6f8-cd4fc383cce1"); // does not work yet

        q = addRowFilter(rowFilters, CohortDefinition.class.getName(), BuiltInCohortDefinitionLibrary.PREFIX + "atLeastAgeOnDate");
        parameterValues(q).put("minAge", 15);
        parameterValues(q).put("effectiveDate", "2014-01-01");

        ArrayNode columns = post.putArray("columns");
        addColumn(columns, PatientDataDefinition.class.getName(), "Given Name", BuiltInPatientDataLibrary.PREFIX + "preferredName.givenName");
        addColumn(columns, PatientDataDefinition.class.getName(), "Family Name", BuiltInPatientDataLibrary.PREFIX + "preferredName.familyName");

        return jackson.writeValueAsString(post);
    }

    private ObjectNode parameterValues(ObjectNode query) {
        ObjectNode parameterValues = (ObjectNode) query.get("parameterValues");
        if (parameterValues == null) {
            parameterValues = query.putObject("parameterValues");
        }
        return parameterValues;
    }

    private ObjectNode addRowFilter(ArrayNode queries, String type, String definitionKey) {
        ObjectNode query = queries.addObject();
        query.put("type", type);
        query.put("key", definitionKey);
        return query;
    }

    private ObjectNode addColumn(ArrayNode columns, String type, String name, String definitionKey) {
        ObjectNode query = columns.addObject();
        query.put("type", type);
        query.put("name", name);
        query.put("key", definitionKey);
        return query;
    }

    @Test
    public void testPurge() throws Exception {
        // relying on resource.create for this is bad style, but I'm doing it for convenience
        ObjectMapper jackson = new ObjectMapper();
        String json = adHocDataExportAsJson(jackson);

        RequestContext createContext = new RequestContext();
        createContext.setRepresentation(new NamedRepresentation("rowFilters"));

        AdHocDataSetResource resource = new AdHocDataSetResource();
        AdHocDataSet created = (AdHocDataSet) resource.create(jackson.readValue(json, SimpleObject.class), createContext);

        assertThat(dataSetDefinitionService.getDefinitionByUuid(created.getUuid()), notNullValue());

        resource.purge(created.getUuid(), new RequestContext());

        assertThat(dataSetDefinitionService.getDefinitionByUuid(created.getUuid()), nullValue());
    }

}
