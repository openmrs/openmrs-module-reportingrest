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
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.library.BuiltInCohortDefinitionLibrary;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class EvaluatedCohortResourceTest extends BaseEvaluatedResourceTest<EvaluatedCohortResource, EvaluatedCohort> {

    @Autowired
    CohortDefinitionService cohortDefinitionService;

    private String UUID = "abc123";

	@Before
	public void before() {
		GenderCohortDefinition cd = new GenderCohortDefinition();
		cd.setName("Males");
		cd.setDescription("male patients");
		cd.setMaleIncluded(true);
		cd.setFemaleIncluded(false);
		cd.setUnknownGenderIncluded(false);
		cd.setUuid(UUID);
		cohortDefinitionService.saveDefinition(cd);
	}

    @Test
	public void testEvaluateWithNoParameters() throws Exception {
		Object evaluated = getResource().retrieve(UUID, buildRequestContext());
		String json = toJson(evaluated);

        assertThat((String) path(evaluated, "definition", "name"), is("Males"));
        assertThat((String) path(evaluated, "definition", "description"), is("male patients"));
        assertThat((String) path(evaluated, "definition", "uuid"), is(UUID));
		assertTrue(hasLink(evaluated, "self", "/cohort/" + UUID));

		// should include patients 2 and 6 from standard test dataset. their uuids are:
		String[] expectedUuids = new String[] { "da7f524f-27ce-4bb2-86d6-6d1d05312bd5", "a7e04421-525f-442f-8138-05b619d16def" };

		assertCohortMembers(evaluated, json, expectedUuids);
	}

	@Test
	public void testEvaluateBuiltInDefinitionWithNoParameters() throws Exception {
		Object evaluated = getResource().retrieve(BuiltInCohortDefinitionLibrary.PREFIX + "males", buildRequestContext());
		String json = toJson(evaluated);

		assertThat((String) path(evaluated, "definition", "name"), is("Males"));
		assertThat((String) path(evaluated, "definition", "uuid"), is("reporting.library.cohortDefinition.builtIn.males"));

		// should include patients 2 and 6 from standard test dataset. their uuids are:
		String[] expectedUuids = new String[] { "da7f524f-27ce-4bb2-86d6-6d1d05312bd5", "a7e04421-525f-442f-8138-05b619d16def" };

		assertCohortMembers(evaluated, json, expectedUuids);
	}

	public void evaluateWithMissingParametersShouldThrowClientException() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> getResource().retrieve(BuiltInCohortDefinitionLibrary.PREFIX + "atLeastAgeOnDate", buildRequestContext()));
	}
	
	@Test
	public void testEvaluateBuiltInDefinitionWithParametersUsingPost() throws Exception {
		SimpleObject postBody = new SimpleObject()
				.add("effectiveDate", "1996-08-30")
				.add("maxAge", "20");
		
		Object evaluated = getResource().update(BuiltInCohortDefinitionLibrary.PREFIX + "upToAgeOnDate", postBody, buildRequestContext());
		String json = toJson(evaluated);
		
		assertThat((String) path(evaluated, "definition", "uuid"), is("reporting.library.cohortDefinition.builtIn.upToAgeOnDate"));
		
		// should include patient 7 from standard test dataset. their uuids are:
		String[] expectedUuids = new String[] { "5946f880-b197-400b-9caa-a3c661d23041" };
		
		assertCohortMembers(evaluated, json, expectedUuids);
	}

	@Test
	public void testEvaluatingSerializedCohortDefintionWithNoParams() throws Exception {
		StringBuilder xml = new StringBuilder();
		xml.append("<org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition>");
		xml.append("  <maleIncluded>true</maleIncluded>");
		xml.append("  <femaleIncluded>false</femaleIncluded>");
		xml.append("  <unknownGenderIncluded>false</unknownGenderIncluded>");
		xml.append("</org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition>");
		SimpleObject post = new SimpleObject();
		post.put("serializedXml", xml.toString());

		Object evaluated = getResource().create(post, buildRequestContext());
		String json = toJson(evaluated);

		// should include patients 2 and 6 from standard test dataset. their uuids are:
		String[] expectedUuids = new String[] { "da7f524f-27ce-4bb2-86d6-6d1d05312bd5", "a7e04421-525f-442f-8138-05b619d16def" };

		assertCohortMembers(evaluated, json, expectedUuids);
	}

	@Test
	public void testEvaluatingSerializedCohortDefintionWithParams() throws Exception {
		StringBuilder xml = new StringBuilder();
		xml.append("<org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition>");
		xml.append("  <parameters>\n" +
				"    <org.openmrs.module.reporting.evaluation.parameter.Parameter id=\"387\">\n" +
				"      <name>onOrAfter</name>\n" +
				"      <label></label>\n" +
				"      <type>java.util.Date</type>\n" +
				"      <required>true</required>\n" +
				"    </org.openmrs.module.reporting.evaluation.parameter.Parameter>\n" +
				"    <org.openmrs.module.reporting.evaluation.parameter.Parameter id=\"388\">\n" +
				"      <name>onOrBefore</name>\n" +
				"      <label></label>\n" +
				"      <type>java.util.Date</type>\n" +
				"      <required>true</required>\n" +
				"    </org.openmrs.module.reporting.evaluation.parameter.Parameter>\n" +
				"  </parameters>");
		xml.append("  <timeQualifier>ANY</timeQualifier>");
		xml.append("  <returnInverse>false</returnInverse>");
		xml.append("</org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition>");
		SimpleObject post = new SimpleObject();
		post.put("serializedXml", xml.toString());
		post.put("onOrAfter", "2008-08-01");
		post.put("onOrBefore", "2008-08-01");

		Object evaluated = getResource().create(post, buildRequestContext());
		String json = toJson(evaluated);
		System.out.println(json);

		// should include patient 7 from standard test dataset. their uuids are:
		String[] expectedUuids = new String[] { "5946f880-b197-400b-9caa-a3c661d23041" };

		assertCohortMembers(evaluated, json, expectedUuids);
	}

	private void assertCohortMembers(Object evaluated, String json, String[] expectedUuids) throws Exception {
		assertEquals(expectedUuids.length, ((List) path(evaluated, "members")).size());
		for (String expected : expectedUuids) {
			assertTrue(json.contains("/patient/" + expected));
		}
	}

}
