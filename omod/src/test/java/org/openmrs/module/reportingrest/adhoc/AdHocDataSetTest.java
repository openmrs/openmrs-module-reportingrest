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

package org.openmrs.module.reportingrest.adhoc;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AdHocDataSetTest {

    public static final String JSON = "{\n" +
            "  \"name\": \"Females\",\n" +
            "  \"description\": null,\n" +
            "  \"uuid\": null,\n" +
            "  \"parameters\": [\n" +
            "    {\n" +
            "      \"name\": \"startDate\",\n" +
            "      \"type\": \"java.util.Date\",\n" +
            "      \"collectionType\": null,\n" +
            "      \"value\": \"2013-12-18T08:00:00.000Z\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"endDate\",\n" +
            "      \"type\": \"java.util.Date\",\n" +
            "      \"collectionType\": null,\n" +
            "      \"value\": \"2013-12-18T08:00:00.000Z\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"rowFilters\": [\n" +
            "    {\n" +
            "      \"label\": \"Females (Patient gender is female)\",\n" +
            "      \"value\": \"Females (Patient gender is female)\",\n" +
            "      \"type\": \"org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition\",\n" +
            "      \"key\": \"reporting.library.cohortDefinition.builtIn.females\",\n" +
            "      \"name\": \"Females\",\n" +
            "      \"description\": \"Patient gender is female\",\n" +
            "      \"parameters\": [\n" +
            "        \n" +
            "      ]\n" +
            "    }\n" +
            "  ],\n" +
            "  \"columns\": [\n" +
            "    {\n" +
            "      \"label\": \"ZL EMR ID (most recent) (The ZL EMR ID most recently entered into the EMR for this patient)\",\n" +
            "      \"value\": \"ZL EMR ID (most recent) (The ZL EMR ID most recently entered into the EMR for this patient)\",\n" +
            "      \"type\": \"org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition\",\n" +
            "      \"key\": \"mirebalais.patientDataCalculation.mostRecentZlEmrId.identifier\",\n" +
            "      \"name\": \"ZL EMR ID (most recent)\",\n" +
            "      \"description\": \"The ZL EMR ID most recently entered into the EMR for this patient\",\n" +
            "      \"parameters\": [\n" +
            "        \n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"label\": \"Birthdate, computer-friendly (Patient's birthdate (YYYY-MM-DD))\",\n" +
            "      \"value\": \"Birthdate, computer-friendly (Patient's birthdate (YYYY-MM-DD))\",\n" +
            "      \"type\": \"org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition\",\n" +
            "      \"key\": \"reporting.library.patientDataDefinition.builtIn.birthdate.ymd\",\n" +
            "      \"name\": \"Birthdate, computer-friendly\",\n" +
            "      \"description\": \"Patient's birthdate (YYYY-MM-DD)\",\n" +
            "      \"parameters\": [\n" +
            "        \n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    @Test
    public void testParseJson() throws Exception {
        AdHocDataSet adHocDataSet = new ObjectMapper().readValue(JSON, AdHocDataSet.class);
        assertThat(adHocDataSet.getName(), is("Females"));
    }

}
