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
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.reporting.serializer.ReportingSerializer;
import org.openmrs.module.reportingrest.web.controller.ReportingRestController;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Resource} for evaluating {@link CohortDefinition}s
 */
@Resource(name = RestConstants.VERSION_1 + ReportingRestController.REPORTING_REST_NAMESPACE + "/cohort",
        supportedClass = EvaluatedCohort.class, supportedOpenmrsVersions = {"1.8.*", "1.9.*, 1.10.*, 1.11.*", "1.12.*", "2.0.*", "2.1.*", "2.2.*"})
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
	 * We let the user POST to an existing cohort definition in order to specify more complex parameters than they could
	 * do in a GET
	 * @param uniqueId
	 * @param postBody
	 * @param context
	 * @return
	 * @throws ResponseException
	 */
	@Override
	public Object update(String uniqueId, SimpleObject postBody, RequestContext context) throws ResponseException {
		CohortDefinitionService definitionService = DefinitionContext.getCohortDefinitionService();
		CohortDefinition definition = getDefinitionByUniqueId(definitionService, CohortDefinition.class, uniqueId);
		if (definition == null) {
			throw new ObjectNotFoundException();
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
		else if (rep instanceof RefRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("uuid"); // @PropertyGetter method below
			description.addProperty("definition");
			description.addProperty("count"); // @PropertyGetter method below
			description.addSelfLink();
		}

		return description;
	}

	/**
	 * In core and the reporting module, a cohort is a list of members ids, but for web services
	 * we should expose these as REFs of the relevant patients (because the patient_id is useless for REST).
	 * To avoid a performance penalty for large cohorts, we only query the id and uuid fields, which are enough for an
	 * anemic REF representation of the patient, with a blank 'display' property.
	 */
	@PropertyGetter("members")
	public List<Patient> getMembers(EvaluatedCohort evaluatedCohort) {
		// it is impractical to write a test case that verifies this produces an efficient hibernate query, but I manually
		// verified it with hibernate.show_sql=true
        HqlQueryBuilder qb = new HqlQueryBuilder();
        qb.select("patientId", "uuid").from(Patient.class, "p");
        qb.whereIdIn("p.patientId", evaluatedCohort.getMemberIds());
		List<Patient> ret = new ArrayList<Patient>();
		for (Object[] row : Context.getService(EvaluationService.class).evaluateToList(qb, new EvaluationContext())) {
			Patient pt = new Patient((Integer) row[0]);
			pt.setUuid((String) row[1]);
			ret.add(pt);
		}
		return ret;
	}

	@PropertyGetter("count")
	public Integer getCount(EvaluatedCohort evaluatedCohort) {
		return evaluatedCohort.size();
	}

}
