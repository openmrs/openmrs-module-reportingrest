package org.openmrs.module.reportingrest.web.resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.webservices.rest.web.Hyperlink;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 *
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
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

		assertEquals(expectedUuids.length, ((List) path(evaluated, "members")).size());
		for (String expected : expectedUuids) {
			assertTrue(json.contains("/patient/" + expected));
		}
	}

}
