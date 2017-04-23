/**
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

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.reportingrest.web.controller.ReportingRestController;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;

/**
 * {@link Resource} for evaluating {@link CohortDefinition}s for the cohort builder
 */
@Resource(name = RestConstants.VERSION_1 + ReportingRestController.REPORTING_REST_NAMESPACE
        + "/cohortbuilder", supportedClass = EvaluatedCohort.class, supportedOpenmrsVersions = { "1.8.*",
                "1.9.*, 1.10.*, 1.11.*",
                "1.12.*", "2.0.*", "2.1.*", "2.2.*" })
public class EvaluatedCohortBuilderResource extends EvaluatedCohortResource {
	
	/**
	 * In core and the reporting module, a cohort is a list of members ids, but for web services we
	 * should expose these as REFs of the relevant patients (because the patient_id is useless for
	 * REST). Though this may have a performance penalty for large cohorts, the cohort builder needs
	 * to display the name of patients in the 'display' property, hence the need to load the full
	 * patient.
	 */
	@Override
	@PropertyGetter("members")
	public List<Patient> getMembers(EvaluatedCohort evaluatedCohort) {
		HqlQueryBuilder qb = new HqlQueryBuilder();
		qb.select("patientId", "uuid").from(Patient.class, "p");
		qb.whereIdIn("p.patientId", evaluatedCohort.getMemberIds());
		PatientService patientService = Context.getPatientService();
		List<Patient> ret = new ArrayList<Patient>();
		for (Object[] row : Context.getService(EvaluationService.class).evaluateToList(qb, new EvaluationContext())) {
			Patient pt = patientService.getPatient((Integer) row[0]);
			ret.add(pt);
		}
		return ret;
	}
}