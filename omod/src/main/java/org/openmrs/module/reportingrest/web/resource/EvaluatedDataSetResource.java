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

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetMetaData;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.Evaluated;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.indicator.IndicatorResult;
import org.openmrs.module.reporting.query.IdSet;
import org.openmrs.module.reporting.serializer.ReportingSerializer;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * {@link Resource} for evaluating {@link DataSetDefinition}s
 */
@Resource(name = RestConstants.VERSION_1 + "/reportingrest/dataSet",
		supportedClass = DataSet.class, supportedOpenmrsVersions = {"1.8.* - 9.9.*"})
public class EvaluatedDataSetResource extends EvaluatedResource<DataSet> {
	
	private static Log log = LogFactory.getLog(EvaluatedDataSetResource.class);

	@Override
	public Object retrieve(String uuid, RequestContext requestContext)
			throws ResponseException {

		DataSetDefinitionService dataSetDefinitionService = DefinitionContext.getDataSetDefinitionService();
		DataSetDefinition definition = getDefinitionByUniqueId(dataSetDefinitionService, DataSetDefinition.class, uuid);
		if (definition == null) {
			throw new ObjectNotFoundException();
		}

		EvaluationContext evalContext = getEvaluationContextWithParameters(definition, requestContext, null, null);
		evaluateAndSetBaseCohort(requestContext, evalContext);

		try {
			DataSet dataSet = (DataSet) evaluate(definition, dataSetDefinitionService, evalContext);
			return asRepresentation(dataSet, requestContext.getRepresentation());
		} catch (EvaluationException ex) {
			throw new IllegalArgumentException(ex);
		}
	}
	
	/**
	 * Allow POSTing to an existing dsd, in case the user needs to specify more complex parameter values than can be done
	 * in a GET.
	 * @param uniqueId
	 * @param postBody
	 * @param requestContext
	 * @return
	 * @throws ResponseException
	 */
	@Override
	public Object update(String uniqueId, SimpleObject postBody, RequestContext requestContext) throws ResponseException {
		DataSetDefinitionService dataSetDefinitionService = DefinitionContext.getDataSetDefinitionService();
		DataSetDefinition definition = getDefinitionByUniqueId(dataSetDefinitionService, DataSetDefinition.class, uniqueId);
		if (definition == null) {
			throw new ObjectNotFoundException();
		}
		
		EvaluationContext evalContext = getEvaluationContextWithParameters(definition, requestContext, null, postBody);
		evaluateAndSetBaseCohort(requestContext, evalContext);
		
		try {
			DataSet dataSet = (DataSet) evaluate(definition, dataSetDefinitionService, evalContext);
			return asRepresentation(dataSet, requestContext.getRepresentation());
		} catch (EvaluationException ex) {
			throw new IllegalArgumentException(ex);
		}
	}
	
	/**
	 * If there is a "cohort" parameter in the request, use that to look for a CohortDefinition to run against, otherwise
	 * we'll use all patients
	 * @param requestContext
	 * @param evalContext
	 */
	private void evaluateAndSetBaseCohort(RequestContext requestContext, EvaluationContext evalContext) {
		HttpServletRequest httpRequest = requestContext.getRequest();
		
		String cohortUniqueId = httpRequest.getParameter("cohort");
		if (StringUtils.hasLength(cohortUniqueId)) {
			try {
				EvaluatedCohort cohort = new EvaluatedCohortResource().getEvaluatedCohort(cohortUniqueId, requestContext, "cohort.");
				evalContext.setBaseCohort(cohort);
			} catch (EvaluationException ex) {
				throw new IllegalStateException("Failed to evaluated cohort", ex);
			}
		}
	}
	
	/**
	 * We let the user POST the serialized XML version of a Definition to this resource in order to evaluate a non-saved
	 * definition on the fly.
	 *
	 * @param postBody
	 * @param context
	 * @return
	 * @throws ResponseException
	 */
	@Override
	public Object create(SimpleObject postBody, RequestContext context) throws ResponseException {
		Object serializedXml = postBody.get("serializedXml");
		DataSetDefinition definition;
		try {
			String xml = (String) serializedXml;
			definition = Context.getSerializationService().getSerializer(ReportingSerializer.class).deserialize(xml, DataSetDefinition.class);
		} catch (Exception ex) {
			throw new IllegalArgumentException("Invalid submitted data set definition", ex);
		}
		EvaluationContext evalContext = getEvaluationContextWithParameters(definition, context, null, postBody);
		try {
			Evaluated<DataSetDefinition> evaluated = evaluate(definition, DefinitionContext.getDataSetDefinitionService(), evalContext);
			return asRepresentation((DataSet) evaluated, context.getRepresentation());
		} catch (Exception ex) {
			throw new IllegalArgumentException("Error evaluating data set definition", ex);
		}
	}

    @Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = null;
		
		if (rep instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("uuid"); // see @PropertyGetter method below
			description.addProperty("metadata"); // remapped property
			description.addProperty("rows"); // see @PropertyGetter method below
			description.addProperty("definition");
			description.addSelfLink();
		}

		return description;
	}

	@Override
	public Schema<?> getGETSchema(Representation rep) {
		Schema<?> modelImpl = super.getGETSchema(rep);
		if (rep instanceof DefaultRepresentation) {
			modelImpl.addProperty("uuid", new StringSchema())
					.addProperty("metadata", new StringSchema())
					.addProperty("rows", new StringSchema())
					.addProperty("definition", new StringSchema());
		}
		return modelImpl;
	}

	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription delegatingResourceDescription = new DelegatingResourceDescription();
		delegatingResourceDescription.addProperty("uuid");
		delegatingResourceDescription.addProperty("metadata");
		delegatingResourceDescription.addProperty("rows");
		delegatingResourceDescription.addProperty("definition");
		return delegatingResourceDescription;
	}

	@Override
	public Schema<?> getCREATESchema(Representation rep) {
		Schema<?> modelImpl = super.getGETSchema(rep);
		modelImpl.addProperty("uuid", new StringSchema())
				.addProperty("metadata", new StringSchema())
				.addProperty("rows", new StringSchema())
				.addProperty("definition", new StringSchema());
		return modelImpl;
	}

	/**
	 * returns [ { col1 : val1, col2, val2 }, {col1a, val1a, col2a, val2a } ]
	 * 
	 * @param dataSet the delegate
	 * @return the a list of maps for the rows
	 */
	@PropertyGetter("rows")
	public List<Map<String, Object>> getRowsOfDataSetDefinition(DataSet dataSet) {
		
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
		
		Iterator<DataSetRow> iterator = dataSet.iterator();
		while (iterator.hasNext()) {
			DataSetRow row = iterator.next();
			Map<String, Object> rowMap = new HashMap<String, Object>();
			for (Map.Entry<DataSetColumn, Object> rowEntry : row.getColumnValues().entrySet()) {
                Object value = rowEntry.getValue();

                // If the value we return has any pointers to hibernate proxies, conversion to JSON will fail when we
                // try to return it to the client. If we pass through an indicator result with an EvaluationContext,
                // its cache will likely contain hibernate proxies an break things. So we just return the numeric value,
                // and not the pointers to how we evaluated things. (Plus I don't think we really should be sending mor
                // than the value back anyway.)
                if (value instanceof IndicatorResult) {
                    value = ((IndicatorResult) value).getValue();
                }
                else if (value instanceof IdSet) {
                    IdSet idSet = (IdSet) value;
                    value = new SimpleObject().add("size", idSet.getSize()).add("memberIds", idSet.getMemberIds());
                }
                else if (value instanceof Cohort) {
                    // EvaluatedCohort implements IdSet, but Cohort doesn't
                    Cohort cohort = (Cohort) value;
                    value = new SimpleObject().add("size", cohort.size()).add("memberIds", cohort.getMemberIds());
                }

                rowMap.put(rowEntry.getKey().getName(), value);
			}
			rows.add(rowMap);
		}
		
		return rows;
	}

    /**
     * Maps "metaData" property to "metadata"
     *
     * @param instance
     * @return
     */
    @PropertyGetter("metadata")
    public DataSetMetaData getMetadata(DataSet instance) {
        return instance.getMetaData();
    }
	
}
