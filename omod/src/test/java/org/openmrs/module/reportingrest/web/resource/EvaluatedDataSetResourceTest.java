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
import org.openmrs.Cohort;
import org.openmrs.api.LocationService;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 *
 */
public class EvaluatedDataSetResourceTest extends BaseEvaluatedResourceTest<EvaluatedDataSetResource, DataSet> {

    DataSetDefinitionService dataSetDefinitionService;

    CohortDefinitionService cohortDefinitionService;

    @Autowired
    LocationService locationService;
    
    public static final String UUID_FOR_PARAMS_DSD = "uuid-for-params-dsd";
    
    @Before
    public void setUp() throws Exception {
        executeDataSet("DataSetDefinitionTest.xml");
        dataSetDefinitionService = DefinitionContext.getDataSetDefinitionService();
        cohortDefinitionService = DefinitionContext.getCohortDefinitionService();
    }

    @Test
    public void testEvaluatingDsdWithNoParameters() throws Exception {
        SimpleObject response = (SimpleObject) getResource().retrieve("d9c79890-7ea9-41b1-a068-b5b99ca3d593", buildRequestContext());
        assertThat((String) path(response, "metadata", "columns", 0, "name"), is("PATIENT_ID"));
        List rows = (List) response.get("rows");
        assertThat(rows.size(), is(5));
        Map<String, Object> firstRow = (Map<String, Object>) rows.get(0);
        assertThat((Integer) firstRow.get("PATIENT_ID"), is(6));
    }

    @Test
    public void testEvaluatingDsdWithParametersUsingGet() throws Exception {
        saveDsdWithParams();

        RequestContext context = buildRequestContext("param1", "these are words, that we won't use", "param2", "1995-01-01");
        SimpleObject response = (SimpleObject) getResource().retrieve(UUID_FOR_PARAMS_DSD, context);

        List rows = (List) response.get("rows");
        assertThat(rows.size(), is(1));
        Map<String, Object> firstRow = (Map<String, Object>) rows.get(0);
        assertThat((Integer) firstRow.get("PERSON_ID"), is(43));
    }
    
    @Test
    public void testEvaluatingDsdWithParametersUsingPost() throws Exception {
        saveDsdWithParams();
        
        RequestContext context = buildRequestContext();
        SimpleObject postBody = new SimpleObject()
                .add("param1", "these are words, that we won't use")
                .add("param2", "1995-01-10");
        SimpleObject response = (SimpleObject) getResource().update(UUID_FOR_PARAMS_DSD, postBody, context);
        
        List rows = (List) response.get("rows");
        assertThat(rows.size(), is(1));
        Map<String, Object> firstRow = (Map<String, Object>) rows.get(0);
        assertThat((Integer) firstRow.get("PERSON_ID"), is(43));
    }
    
    private void saveDsdWithParams() {
        SqlDataSetDefinition dsd = new SqlDataSetDefinition();
        dsd.setName("Not everyone");
        dsd.setDescription("via SQL");
        dsd.setSqlQuery("select person_id, birthdate from person where voided = 0 and birthdate > :param2");
        dsd.addParameter(new Parameter("param1", "param 1", String.class));
        dsd.addParameter(new Parameter("param2", "param 2", Date.class));
        dsd.setUuid(UUID_FOR_PARAMS_DSD);
        dataSetDefinitionService.saveDefinition(dsd);
    }

    @Test
    public void testEvaluatingDsdFromSerializedXml() throws Exception {
        String xml = "<org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition>" +
                "<parameters/>" +
                "<sqlQuery>select patient_id from patient where year(date_created) = 2006</sqlQuery>" +
                "</org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition>";
        SimpleObject response = (SimpleObject) getResource().create(new SimpleObject().add("serializedXml", xml), buildRequestContext());

        assertThat((String) path(response, "metadata", "columns", 0, "name"), is("PATIENT_ID"));
        List rows = (List) response.get("rows");
        assertThat(rows.size(), is(5));
        Map<String, Object> firstRow = (Map<String, Object>) rows.get(0);
        assertThat((Integer) firstRow.get("PATIENT_ID"), is(6));
    }

    @Test
    public void shouldConvertMapDataSetWithEvaluatedCohorts() throws Exception {
        EncounterCohortDefinition ecd = new EncounterCohortDefinition();
        ecd.addLocation(locationService.getLocation(2));
        EvaluatedCohort cohort = cohortDefinitionService.evaluate(ecd, new EvaluationContext());

        MapDataSet mapDataSet = new MapDataSet(new SqlDataSetDefinition(), new EvaluationContext());
        mapDataSet.addData(new DataSetColumn("cohort", "Cohort", Cohort.class), cohort);

        SimpleObject simple = getResource().asRepresentation(mapDataSet, Representation.DEFAULT);
        String json = toJson(simple);
        System.out.println(json);
    }

    private <T extends Parameterizable> Mapped<T> map(T parameterizable, String mappings) {
        if (parameterizable == null) {
            throw new NullPointerException("Programming error: missing parameterizable");
        }
        if (mappings == null) {
            mappings = ""; // probably not necessary, just to be safe
        }
        return new Mapped<T>(parameterizable, ParameterizableUtil.createParameterMappings(mappings));
    }

}
