package org.openmrs.module.reportingrest.web.resource;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.Evaluated;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
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
import java.util.List;

/**
 *
 */
@Resource(name = RestConstants.VERSION_1 + "/reportingrest/reportdata",
        supportedClass = ReportData.class, supportedOpenmrsVersions = {"1.8.*", "1.9.*, 1.10.*, 1.11.*", "1.12.*", "2.0.*", "2.1.*", "2.2.*", "2.3.*", "2.4.*", "2.5.*", "2.6.*"})
public class EvaluatedReportDefinitionResource extends EvaluatedResource<ReportData> {

    private Log log = LogFactory.getLog(getClass());

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        DelegatingResourceDescription description = null;

        if (rep instanceof DefaultRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("uuid"); // has @PropertyGetter so it gets definition.uuid
            description.addProperty("dataSets");
            description.addProperty("context");
            description.addProperty("definition");
            description.addSelfLink();
        }

        return description;
    }

    @Override
    public Model getGETModel(Representation rep) {
        ModelImpl modelImpl = ((ModelImpl) super.getGETModel(rep));
        if (rep instanceof DefaultRepresentation) {
            modelImpl.property("uuid", new StringProperty())
                    .property("dataSets", new StringProperty())
                    .property("context", new StringProperty())
                    .property("definition", new StringProperty());
        }
        return modelImpl;
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription delegatingResourceDescription = new DelegatingResourceDescription();
        delegatingResourceDescription.addProperty("uuid");
        delegatingResourceDescription.addProperty("dataSets");
        delegatingResourceDescription.addProperty("context");
        delegatingResourceDescription.addProperty("definition");
        return delegatingResourceDescription;
    }

    @Override
    public Model getCREATEModel(Representation rep) {
        ModelImpl modelImpl = ((ModelImpl) super.getGETModel(rep));
        modelImpl.property("uuid", new StringProperty())
                .property("dataSets", new StringProperty())
                .property("context", new StringProperty())
                .property("definition", new StringProperty());
        return modelImpl;
    }

    @PropertyGetter("dataSets")
    public List<DataSet> getDataSets(ReportData delegate) {
        return new ArrayList<DataSet>(delegate.getDataSets().values());
    }

	/**
	 * @should throw ObjectNotFoundException if resource does not exist
	 */
	@Override
    public Object retrieve(String uuid, RequestContext requestContext)
            throws ResponseException {

        ReportDefinitionService reportDefinitionService = DefinitionContext.getReportDefinitionService();
        ReportDefinition definition = getDefinitionByUniqueId(reportDefinitionService, ReportDefinition.class, uuid);
	    if (definition == null) {
		    throw new ObjectNotFoundException();
	    }

        EvaluationContext evalContext = getEvaluationContextWithParameters(definition, requestContext, null, null);
        evaluateAndSetBaseCohort(requestContext, evalContext);

        try {
            ReportData reportData = (ReportData) evaluate(definition, reportDefinitionService, evalContext);
            return asRepresentation(reportData, requestContext.getRepresentation());
        } catch (EvaluationException e) {
            throw new RuntimeException("Failed to evaluate report definition", e);
        }
    }
    
    @Override
    public Object update(String uniqueId, SimpleObject postBody, RequestContext requestContext) throws ResponseException {
        ReportDefinitionService reportDefinitionService = DefinitionContext.getReportDefinitionService();
        ReportDefinition definition = getDefinitionByUniqueId(reportDefinitionService, ReportDefinition.class, uniqueId);
        if (definition == null) {
            throw new ObjectNotFoundException();
        }
    
        EvaluationContext evalContext = getEvaluationContextWithParameters(definition, requestContext, null, postBody);
        evaluateAndSetBaseCohort(requestContext, evalContext);
    
        try {
            ReportData reportData = (ReportData) evaluate(definition, reportDefinitionService, evalContext);
            return asRepresentation(reportData, requestContext.getRepresentation());
        } catch (EvaluationException e) {
            throw new RuntimeException("Failed to evaluate report definition", e);
        }
    }
    
    private void evaluateAndSetBaseCohort(RequestContext requestContext, EvaluationContext evalContext) {
        HttpServletRequest httpRequest = requestContext.getRequest();
        
        // if there is a "cohort" parameter, use that to look for a CohortDefinition to run against, otherwise all patients
        String cohortUuid = httpRequest.getParameter("cohort");
        if (StringUtils.hasLength(cohortUuid)) {
            try {
                EvaluatedCohort cohort = new EvaluatedCohortResource().getEvaluatedCohort(cohortUuid, requestContext, "cohort.");
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
        ReportDefinition definition;
        try {
            String xml = (String) serializedXml;
            definition = Context.getSerializationService().getSerializer(ReportingSerializer.class).deserialize(xml, ReportDefinition.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid submitted data set definition", ex);
        }
        EvaluationContext evalContext = getEvaluationContextWithParameters(definition, context, null, postBody);
        try {
            Evaluated<ReportDefinition> evaluated = evaluate(definition, DefinitionContext.getReportDefinitionService(), evalContext);
            return asRepresentation((ReportData) evaluated, context.getRepresentation());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error evaluating report definition", ex);
        }
    }

    @Override
    public ReportData newDelegate() {
        return new ReportData();
    }

}
