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

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.openmrs.contrib.testdata.TestDataManager;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.library.BuiltInCohortDefinitionLibrary;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.query.IdSet;
import org.openmrs.module.reportingrest.web.AdHocRowFilterResults;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;

public class AdHocQueryResourceTest extends BaseModuleWebContextSensitiveTest {

    @Autowired
    TestDataManager testData;

    @Autowired
    CohortDefinitionService cohortDefinitionService;

    @Test
    public void testRowFilterQuery() throws Exception {
        ObjectMapper jackson = new ObjectMapper();
        String json = adHocDataExportAsJson(jackson);

        RequestContext requestContext = new RequestContext();
        requestContext.setRepresentation(new NamedRepresentation("rowFilters"));

        AdHocQueryResource resource = new AdHocQueryResource();
        AdHocRowFilterResults result = (AdHocRowFilterResults) resource.create(jackson.readValue(json, SimpleObject.class), requestContext);

        System.out.println(toJson(result));

        assertThat(result.getIndividualResults().size(), is(3));
        assertThat(result.getResult(), hasExactlyIds(2, 6, 7)); // male OR encounter during period OR adult on date

        assertThat(result.getIndividualResults().get(0), hasExactlyIds(2, 6));
        assertThat(result.getIndividualResults().get(1), hasExactlyIds(2, 7));
        assertThat(result.getIndividualResults().get(2), hasExactlyIds(2, 6, 7));
    }

    @Test
    public void testPreview() throws Exception {
        ObjectMapper jackson = new ObjectMapper();
        String json = adHocDataExportAsJson(jackson);

        RequestContext requestContext = new RequestContext();
        requestContext.setRepresentation(new NamedRepresentation("preview"));

        AdHocQueryResource resource = new AdHocQueryResource();
        Object result = resource.create(jackson.readValue(json, SimpleObject.class), requestContext);

        System.out.println(toJson(result));
    }

    @Test
    public void testPreviewWithNoFilters() throws Exception {
        ObjectMapper jackson = new ObjectMapper();
        String json = jackson.writeValueAsString(setupBasicPost(jackson));

        RequestContext requestContext = new RequestContext();
        requestContext.setRepresentation(new NamedRepresentation("preview"));

        AdHocQueryResource resource = new AdHocQueryResource();
        Object result = resource.create(jackson.readValue(json, SimpleObject.class), requestContext);

        assertThat(((IdSet) ((SimpleObject) result).get("allRows")).getSize(), is(not(0)));
    }

    @Test
    public void testDefaultRepresentation() throws Exception {
        ObjectMapper jackson = new ObjectMapper();
        String json = adHocDataExportAsJson(jackson);

        RequestContext requestContext = new RequestContext();
        requestContext.setRepresentation(Representation.DEFAULT);

        AdHocQueryResource resource = new AdHocQueryResource();
        Object result = resource.create(jackson.readValue(json, SimpleObject.class), requestContext);

        System.out.println(toJson(result));
    }

    @Test
    public void testRowQueryWithSomeParametersSetAndSomeGlobal() throws Exception {
        String ENCOUNTER_TYPE_UUID = "07000be2-26b6-4cce-8b40-866d8435b613";

        // someone other than patient 7 else needs to have an encounter of this type, outside the time window, so we
        // know parameters are really being applied
        testData.encounter().encounterDatetime("2001-01-01").encounterType(ENCOUNTER_TYPE_UUID).patient(2).save();

        ObjectMapper jackson = new ObjectMapper();
        ObjectNode post = setupBasicPost(jackson);

        ArrayNode rowFilters = post.putArray("rowFilters");
        ObjectNode q = addRowFilter(rowFilters, CohortDefinition.class.getName(), BuiltInCohortDefinitionLibrary.PREFIX + "anyEncounterOfTypesDuringPeriod");
        ArrayNode encounterTypes = parameterValues(q).putArray("encounterTypes");
        ObjectNode encounterType = encounterTypes.addObject();
        encounterType.put("uuid", ENCOUNTER_TYPE_UUID);
        encounterType.put("display", "Emergency");

        String json = jackson.writeValueAsString(post);

        RequestContext requestContext = new RequestContext();
        requestContext.setRepresentation(new NamedRepresentation("rowFilters"));

        AdHocQueryResource resource = new AdHocQueryResource();
        AdHocRowFilterResults result = (AdHocRowFilterResults) resource.create(jackson.readValue(json, SimpleObject.class), requestContext);

        System.out.println(toJson(result));

        assertThat(result.getIndividualResults().size(), is(1));
        assertThat(result.getResult(), hasExactlyIds(7));

        assertThat(result.getIndividualResults().get(0), hasExactlyIds(7));
    }

    private String adHocDataExportAsJson(ObjectMapper jackson) throws IOException {
        ObjectNode post = setupBasicPost(jackson);
        post.put("customRowFilterCombination", "1 OR 2 OR 3");

        ArrayNode rowFilters = post.putArray("rowFilters");
        addRowFilter(rowFilters, CohortDefinition.class.getName(), BuiltInCohortDefinitionLibrary.PREFIX + "males");
        addRowFilter(rowFilters, CohortDefinition.class.getName(), BuiltInCohortDefinitionLibrary.PREFIX + "anyEncounterDuringPeriod");
        ObjectNode q = addRowFilter(rowFilters, CohortDefinition.class.getName(), BuiltInCohortDefinitionLibrary.PREFIX + "atLeastAgeOnDate");
        parameterValues(q).put("minAge", 15);
        parameterValues(q).put("effectiveDate", "2014-01-01");

        return jackson.writeValueAsString(post);
    }

    private ObjectNode setupBasicPost(ObjectMapper jackson) {
        ObjectNode post = jackson.createObjectNode();
        post.put("type", PatientDataSetDefinition.class.getName());
        ArrayNode parameters = post.putArray("parameters");

        ObjectNode startDate = parameters.addObject();
        startDate.put("name", "startDate");
        startDate.put("type", "java.util.Date");
        startDate.put("value", "2008-08-01");

        ObjectNode endDate = parameters.addObject();
        endDate.put("name", "endDate");
        endDate.put("type", "java.util.Date");
        endDate.put("value", "2008-08-31");

        ArrayNode columns = post.putArray("columns");
        addColumn(columns, PatientDataDefinition.class.getName(), "Given Name", BuiltInPatientDataLibrary.PREFIX + "preferredName.givenName");
        addColumn(columns, PatientDataDefinition.class.getName(), "Family Name", BuiltInPatientDataLibrary.PREFIX + "preferredName.familyName");
        return post;
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

    // copied from ReportingMatchers in the reporting module to avoid needing to add the test-jar dependency
    private static Matcher<IdSet<?>> hasExactlyIds(final Integer... expectedMemberIds) {
        return new BaseMatcher<IdSet<?>>() {
            @Override
            public boolean matches(Object o) {
                Set<Integer> actual = ((IdSet<?>) o).getMemberIds();
                return (actual.size() == expectedMemberIds.length) && containsInAnyOrder(expectedMemberIds).matches(actual);
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue("IdSet with " + expectedMemberIds.length + " members: " + OpenmrsUtil.join(Arrays.asList(expectedMemberIds), ", "));
            }
        };
    }

    private String toJson(Object object) throws IOException {
        return new ObjectMapper().writeValueAsString(object);
    }

}
