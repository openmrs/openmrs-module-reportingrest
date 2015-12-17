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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.Evaluated;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.serializer.ReportingSerializer;
import org.openmrs.module.reportingrest.web.controller.ReportingRestController;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.List;

/**
 * {@link Resource} for evaluating {@link CohortDefinition}s
 */
@Resource(name = RestConstants.VERSION_1 + ReportingRestController.REPORTING_REST_NAMESPACE + "/cohort",
        supportedClass = EvaluatedCohort.class, supportedOpenmrsVersions = {"1.8.*", "1.9.*, 1.10.*, 1.11.*", "1.12.*", "2.0.*"})
public class EvaluatedCohortResource extends EvaluatedResource<EvaluatedCohort> {

	private static Log log = LogFactory.getLog(EvaluatedCohortResource.class);

    public EvaluatedCohortResource() {
    }

	@Override
	public Object retrieve(String uuid, RequestContext requestContext)
			throws ResponseException {

		try {
			EvaluatedCohort evaluatedCohort = getEvaluatedCohort(uuid, requestContext, null);
			return asRepresentation(evaluatedCohort, requestContext.getRepresentation());
		}
		catch (EvaluationException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * We let the user POST the serialized XML version of a CohortDefinition to this resource in order to evaluate a
	 * non-saved cohort definition on the fly.
	 *
	 * @param postBody
	 * @param context
	 * @return
	 * @throws ResponseException
     */
	@Override
	public Object create(SimpleObject postBody, RequestContext context) throws ResponseException {
		Object serializedXml = postBody.get("serializedXml");
		CohortDefinition definition;
		try {
			String xml = (String) serializedXml;
			definition = Context.getSerializationService().getSerializer(ReportingSerializer.class).deserialize(xml, CohortDefinition.class);
		} catch (Exception ex) {
			throw new IllegalArgumentException("Invalid submitted cohort definition", ex);
		}
		EvaluationContext evalContext = getEvaluationContextWithParameters(definition, context, null, postBody);
		try {
			Evaluated<CohortDefinition> evaluatedCohort = evaluate(definition, DefinitionContext.getCohortDefinitionService(), evalContext);
			return asRepresentation((EvaluatedCohort) evaluatedCohort, context.getRepresentation());
		} catch (Exception ex) {
			throw new IllegalArgumentException("Error evaluating cohort definition", ex);
		}
	}

	/**
	 * Helper method used by this and other resources that can have base cohorts (like EvaluatedDataSetResource)
	 *
	 * @param uuid CohortDefinition uuid
	 * @param requestContext
	 * @param parameterPrefix if non null, the parameter lookups are done with "parameterPrefix"+param
	 * @return
	 */
	protected EvaluatedCohort getEvaluatedCohort(String uuid, RequestContext requestContext, String parameterPrefix) throws EvaluationException {
		CohortDefinitionService definitionService = DefinitionContext.getCohortDefinitionService();
		CohortDefinition definition = getDefinitionByUniqueId(definitionService, CohortDefinition.class, uuid);
		if (definition == null) {
			throw new ObjectNotFoundException();
		}

		return (EvaluatedCohort) evaluate(definition, definitionService, getEvaluationContextWithParameters(definition, requestContext, parameterPrefix, null));
	}

	@Override
	public DelegatingResourceDescription getRepresentationDescription(
			Representation rep) {
		DelegatingResourceDescription description = null;

		if (rep instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("uuid"); // @PropertyGetter method below
			description.addProperty("definition");
			description.addProperty("members", Representation.REF); // @PropertyGetter method below
			description.addSelfLink();
		}

		return description;
	}

	/**
	 * In core and the reporting module, a cohort is a list of members ids, but for web services
	 * we should expose these as REFs to the relevant patients.
	 * There's a performance penalty for this, so we should consider changing this to include a
	 * URI that lets you look up the patient, but is based on patientId, so we don't have to hit
	 * the database.
	 */
	@PropertyGetter("members")
	public List<Patient> getMembers(EvaluatedCohort evaluatedCohort) {
		return Context.getPatientSetService().getPatients(evaluatedCohort.getMemberIds());
	}

}
