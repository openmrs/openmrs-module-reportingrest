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

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.Retrievable;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * {@link Resource} for evaluating {@link CohortDefinition}s
 */
public class EvaluatedCohortResource extends BaseDelegatingResource<EvaluatedCohort> implements Retrievable {
	
	private static Log log = LogFactory.getLog(EvaluatedCohortResource.class);
	
	@Override
	public Object retrieve(String uuid, RequestContext requestContext)
			throws ResponseException {
		
		// evaluate the cohort
		
		// the passed in uuid is the CohortDefinition uuid
		
		EvaluatedCohort evaldCohort = getEvaluatedCohort(uuid, requestContext, "");
		
		return convertDelegateToRepresentation(evaldCohort, getRepresentationDescription(Representation.DEFAULT));
	}

	/**
	 * Helper method used by this and the EvaluatedDataSetResource
	 * 
	 * @param uuid CohortDefinition uuid
	 * @param requestContext
	 * @param parameterPrefix if non null, the parameter lookups are done with "parameterPrefix"+param
	 * @return
	 */
	protected static EvaluatedCohort getEvaluatedCohort(String uuid, RequestContext requestContext, String parameterPrefix) {
		if (parameterPrefix == null)
			parameterPrefix = "";
		
		CohortDefinitionService cohortDefinitionService = DefinitionContext.getCohortDefinitionService();
		
		CohortDefinition definition = cohortDefinitionService.getDefinitionByUuid(uuid);
		
		// fail early if the def doesn't exist
		if (definition == null)
			return null;
		
		// get the params off the requestContext and put them on the evalContext
		EvaluationContext evalContext = new EvaluationContext();
		HttpServletRequest httpRequest = requestContext.getRequest();
		for (Parameter param : definition.getParameters()) {
			String httpParamValue = httpRequest.getParameter(parameterPrefix + param.getName());
			
			// TODO error check if value not found?
			
			evalContext.addParameterValue(param.getName(), httpParamValue);
		}
		
		// actually do the evaluation
		try {
			EvaluatedCohort evaluated = cohortDefinitionService.evaluate(definition, evalContext);

			// there seems to be a bug in the reporting module that doesn't set these
			if (evaluated.getDefinition().getName() == null)
				evaluated.getDefinition().setName(definition.getName());
			if (evaluated.getDefinition().getDescription() == null)
				evaluated.getDefinition().setDescription(definition.getDescription());
			if (evaluated.getDefinition().getUuid() == null)
				evaluated.getDefinition().setUuid(definition.getUuid());

			return evaluated;
		} catch (EvaluationException e) {
			log.error("Unable to evaluate definition with uuid: " + uuid);
			return null;
		}
	}

	@Override
	public List<Representation> getAvailableRepresentations() {
		return Arrays.asList(Representation.DEFAULT);
	}

	@Override
	public String getUri(Object instance) {
		// TODO, use annotation?
		return RestConstants.URI_PREFIX.replace("/rest", "/reporting") + "cohort/" + getUuidOfCohortDefinition((EvaluatedCohort) instance);
	}

	@Override
	public EvaluatedCohort getByUniqueId(String uniqueId) {
		// not used
		return null;
	}

	@Override
	protected EvaluatedCohort newDelegate() {
		// not used (?)
		return new EvaluatedCohort();
	}

	@Override
	protected EvaluatedCohort save(EvaluatedCohort delegate) {
		// not used
		return null;
	}

	@Override
	protected void delete(EvaluatedCohort delegate, String reason,
			RequestContext context) throws ResponseException {
		// not used
	}

	@Override
	public void purge(EvaluatedCohort delegate, RequestContext context)
			throws ResponseException {
		// not used
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
	 * @param evaluatedCohort the delegate
	 * @return the uuid of the cohort definintion that is defined on this object
	 */
	@PropertyGetter("uuid")
	public String getUuidOfCohortDefinition(EvaluatedCohort evaluatedCohort) {
		return evaluatedCohort.getDefinition().getUuid();
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
