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
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class EvaluatedDataSetResourceTest extends BaseEvaluatedResourceTest<EvaluatedDataSetResource, DataSet> {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    DataSetDefinitionService dataSetDefinitionService;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    CohortDefinitionService cohortDefinitionService;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    LocationService locationService;

    @Before
    public void setUp() throws Exception {
        executeDataSet("DataSetDefinitionTest.xml");
    }

    @Test
    public void testEvaluatingDsdWithNoParameters() throws Exception {
        SimpleObject response = (SimpleObject) getResource().retrieve("d9c79890-7ea9-41b1-a068-b5b99ca3d593", buildRequestContext());
        assertThat((String) path(response, "metadata", "columns", 0, "name"), is("PATIENT_ID"));
        List rows = (List) response.get("rows");
        assertThat(rows.size(), is(4));
        Map<String, Object> firstRow = (Map<String, Object>) rows.get(0);
        assertThat((Integer) firstRow.get("PATIENT_ID"), is(6));
    }

    @Test
    public void testEvaluatingDsdWithParameters() throws Exception {
        String uuid = "uuid-for-params-dsd";

        SqlDataSetDefinition dsd = new SqlDataSetDefinition();
        dsd.setName("Not everyone");
        dsd.setDescription("via SQL");
        dsd.setSqlQuery("select person_id, birthdate from person where voided = 0 and birthdate > :param2");
        dsd.addParameter(new Parameter("param1", "param 1", String.class));
        dsd.addParameter(new Parameter("param2", "param 2", Date.class));
        dsd.setUuid(uuid);
        dataSetDefinitionService.saveDefinition(dsd);

        RequestContext context = buildRequestContext("param1", "these are words, that we won't use", "param2", "2000-11-01");
        SimpleObject response = (SimpleObject) getResource().retrieve(uuid, context);

        List rows = (List) response.get("rows");
        assertThat(rows.size(), is(1));
        Map<String, Object> firstRow = (Map<String, Object>) rows.get(0);
        assertThat((Integer) firstRow.get("PERSON_ID"), is(6));
    }

    @Test
    public void shouldConvertCohortIndicatorDataSet() throws Exception {
        {
            GenderCohortDefinition cd = new GenderCohortDefinition();
            cd.setName("Gender = Male");
            cd.setMaleIncluded(true);

            CohortIndicator ind = new CohortIndicator("Gender = Male");
            ind.setCohortDefinition(map(cd, null));

            CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
            dsd.setName("Cohort Indicator DSD");
            dsd.setUuid("cohort-indicator-dsd");
            dsd.addColumn("1", "One", map(ind, null), "");

            dataSetDefinitionService.saveDefinition(dsd);
        }

        SimpleObject result = (SimpleObject) getResource().retrieve("cohort-indicator-dsd", buildRequestContext());
        String json = toJson(result);
        System.out.println(json);

        assertTrue(json.contains("\"uuid\":\"cohort-indicator-dsd\""));
        assertTrue(json.contains("\"1\":2")); // this is the count of matching patients
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
