package org.openmrs.module.reportingrest.web.resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Resource(name = RestConstants.VERSION_1 + "/reportingrest/reportdata", supportedClass = ReportData.class, supportedOpenmrsVersions = {"1.8.*", "1.9.*"})
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

    @PropertyGetter("dataSets")
    public List<DataSet> getDataSets(ReportData delegate) {
        return new ArrayList<DataSet>(delegate.getDataSets().values());
    }

    @Override
    public Object retrieve(String uuid, RequestContext requestContext)
            throws ResponseException {

        ReportDefinitionService reportDefinitionService = DefinitionContext.getReportDefinitionService();

        // the passed in uuid is the DataSetDefinition uuid
        ReportDefinition definition = reportDefinitionService.getDefinitionByUuid(uuid);

        EvaluationContext evalContext = getEvaluationContextWithParameters(definition, requestContext);

        HttpServletRequest httpRequest = requestContext.getRequest();

        // if there is a "cohort" parameter, use that to look for a CohortDefinition to run against, otherwise all patients
        String cohortUuid = httpRequest.getParameter("cohort");
        if (StringUtils.hasLength(cohortUuid)) {
            EvaluatedCohort cohort = new EvaluatedCohortResource().getEvaluatedCohort(cohortUuid, requestContext, "cohort.");
            evalContext.setBaseCohort(cohort);
        }

        // actually do the evaluation
        ReportData reportData = null;
        try {
            reportData = reportDefinitionService.evaluate(definition, evalContext);

        } catch (EvaluationException e) {
            throw new RuntimeException("Failed to evaluate report definition", e);
        }

//        DataSet dataSet = null;
//        try {
//            dataSet = dataSetDefinitionService.evaluate(definition, evalContext);
//            // there seems to be a bug in the underlying reporting module that doesn't set this
//            if (dataSet.getDefinition().getUuid() == null)
//                dataSet.getDefinition().setUuid(definition.getUuid());
//            if (dataSet.getDefinition().getName() == null)
//                dataSet.getDefinition().setName(definition.getName());
//            if (dataSet.getDefinition().getDescription() == null)
//                dataSet.getDefinition().setDescription(definition.getDescription());
//        } catch (EvaluationException e) {
//            log.error("Unable to evaluate definition with uuid: " + uuid);
//        }

        return asRepresentation(reportData, requestContext.getRepresentation());
    }

}
