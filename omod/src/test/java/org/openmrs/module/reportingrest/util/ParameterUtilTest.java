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

package org.openmrs.module.reportingrest.util;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.openmrs.EncounterType;
import org.openmrs.api.EncounterService;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ParameterUtilTest extends BaseModuleWebContextSensitiveTest {

    @Autowired
    private EncounterService encounterService;

    @Test
    public void convertParameterValues_shouldConvertBasicTypes() throws Exception {
        String DATE = "2014-05-08T07:00:00.000";

        List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter("date", "Date", Date.class));
        params.add(new Parameter("number", "Number", Integer.class));

        Map<String, Object> submitted = new HashMap<String, Object>();
        submitted.put("date", DATE);
        submitted.put("number", "7");

        Map<String, Object> converted = ParameterUtil.convertParameterValues(params, submitted);
        assertThat(converted.get("date"), is(ConversionUtil.convert(DATE, Date.class)));
        assertThat((Integer) converted.get("number"), is(7));
    }

    @Test
    public void convertParameterValues_shouldLookUpOpenmrsObjectsByUuidWhenGivenString() throws Exception {
        String UUID = "07000be2-26b6-4cce-8b40-866d8435b613";

        List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter("encounterType", "Encounter type", EncounterType.class));

        Map<String, Object> submitted = new HashMap<String, Object>();
        submitted.put("encounterType", UUID);

        Map<String, Object> converted = ParameterUtil.convertParameterValues(params, submitted);
        assertThat((EncounterType) converted.get("encounterType"), is(encounterService.getEncounterTypeByUuid(UUID)));
        assertThat(((EncounterType) converted.get("encounterType")).getId(), is(encounterService.getEncounterTypeByUuid(UUID).getId()));
    }

    @Test
    public void convertParameterValues_shouldLookUpOpenmrsObjectsByUuidFromObject() throws Exception {
        String UUID = "07000be2-26b6-4cce-8b40-866d8435b613";

        List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter("encounterType", "Encounter type", EncounterType.class));

        Map fromJson = new ObjectMapper().readValue("{\"display\": \"Emergency\", \"uuid\":\"" + UUID + "\" }", Map.class);
        Map<String, Object> submitted = new HashMap<String, Object>();
        submitted.put("encounterType", fromJson);

        Map<String, Object> converted = ParameterUtil.convertParameterValues(params, submitted);
        assertThat((EncounterType) converted.get("encounterType"), is(encounterService.getEncounterTypeByUuid(UUID)));
        assertThat(((EncounterType) converted.get("encounterType")).getId(), is(encounterService.getEncounterTypeByUuid(UUID).getId()));
    }

    @Test
    public void convertParameterValues_shouldLookUpOpenmrsObjectsInArrayByUuid() throws Exception {
        String UUID = "07000be2-26b6-4cce-8b40-866d8435b613";

        List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter("encounterTypes", "Encounter type", EncounterType.class, List.class, null));

        List fromJson = new ObjectMapper().readValue("[ {\"display\": \"Emergency\", \"uuid\":\"" + UUID + "\" } ]", List.class);
        Map<String, Object> submitted = new HashMap<String, Object>();
        submitted.put("encounterTypes", fromJson);

        Map<String, Object> converted = ParameterUtil.convertParameterValues(params, submitted);
        List<EncounterType> encounterTypes = (List<EncounterType>) converted.get("encounterTypes");
        assertThat(encounterTypes.size(), is(1));
        assertThat(encounterTypes.get(0), is(encounterService.getEncounterTypeByUuid(UUID)));
        assertThat(encounterTypes.get(0).getId(), is(encounterService.getEncounterTypeByUuid(UUID).getId()));
    }

}