package org.openmrs.module.reportingrest.web.resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetMetaData;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.indicator.IndicatorResult;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionServiceImpl;

@Resource(name = RestConstants.VERSION_1 + "/reportingrest/report", supportedClass = ReportData.class, supportedOpenmrsVersions = {"1.8.*", "1.9.*"})
public class EvaluatedReportResource extends EvaluatedResource<ReportData> {

    private static Log log = LogFactory.getLog(EvaluatedReportResource.class);
    @Override
    public Object retrieve(String uuid, RequestContext requestContext)throws ResponseException {
        ReportDefinitionService dataSetDefinitionService = new ReportDefinitionServiceImpl();
        ReportDefinition definition = DefinitionContext.getDefinitionByUuid(ReportDefinition.class, uuid);
        EvaluationContext evalContext = getEvaluationContextWithParameters(definition, requestContext);
        ReportData dataSet = null;
        try {
            dataSet = dataSetDefinitionService.evaluate(definition, evalContext);
            // there seems to be a bug in the underlying reporting module that doesn't set this
            if (dataSet.getDefinition().getUuid() == null)
                dataSet.getDefinition().setUuid(definition.getUuid());
            if (dataSet.getDefinition().getName() == null)
                dataSet.getDefinition().setName(definition.getName());
            if (dataSet.getDefinition().getDescription() == null)
                dataSet.getDefinition().setDescription(definition.getDescription());
        } catch (EvaluationException e) {
            log.error("Unable to evaluate definition with uuid: " + uuid);
        }
        return asRepresentation(dataSet, requestContext.getRepresentation());
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        DelegatingResourceDescription description = null;

        if (rep instanceof DefaultRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("reportName");
            description.addProperty("dataRows");
            description.addProperty("metadata");
            description.addSelfLink();
        }else if (rep instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("reportName");
            description.addSelfLink();
        }else if (rep instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("reportName");
            description.addProperty("dataRows");
            description.addProperty("metadata");
            description.addSelfLink();
        }

        return description;
    }

    @PropertyGetter("metadata")
    public SimpleObject getMetadata(ReportData def) {
        Map<String,DataSet> n=def.getDataSets();
        List<Map<String, String>> columns = new ArrayList<Map<String, String>>();
        for (Map.Entry<String, DataSet> entry : n.entrySet())
        {
            DataSet th=entry.getValue();
            DataSetMetaData metadata=th.getMetaData();
            for (DataSetColumn column : metadata.getColumns()) {
                Map<String, String> columnMap = new HashMap<String, String>();
                columnMap.put("name", column.getName());
                columnMap.put("label", column.getLabel());
                columnMap.put("datatype", column.getDataType().getName());
                columns.add(columnMap);
            }
        }
        return new SimpleObject().add("columns", columns);
    }

    @PropertyGetter("reportName")
    public String getReportName(ReportData def) {
        return def.getDefinition().getName();
    }

    @PropertyGetter("dataRows")
    public List<Map<String, Object>> getCohort(ReportData def) {
        Map<String, DataSet> dataSets=def.getDataSets();
        if(dataSets!=null){
            DataSet d=null;
            String k=null;
            Map<String, Object> rowMap=null; List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
            for (Map.Entry<String, DataSet> entry : dataSets.entrySet())
            {
                d=entry.getValue();
                k=entry.getKey();
                Iterator<DataSetRow> iterator = d.iterator();
                while (iterator.hasNext()) {
                    DataSetRow row = iterator.next();
                    rowMap = new HashMap<String, Object>();
                    for (Map.Entry<DataSetColumn, Object> rowEntry : row.getColumnValues().entrySet()) {
                        Object value = rowEntry.getValue();
                        if(value instanceof Cohort){
                            Cohort t=(Cohort)value;
                            rowMap.put(rowEntry.getKey().getName(),t.getMemberIds().size()+" patients");
                            continue;
                        }
                        // If the value we return has any pointers to hibernate proxies, conversion to JSON will fail when we
                        // try to return it to the client. If we pass through an indicator result with an EvaluationContext,
                        // its cache will likely contain hibernate proxies an break things. So we just return the numeric value,
                        // and not the pointers to how we evaluated things. (Plus I don't think we really should be sending mor
                        // than the value back anyway.)
                        if (value instanceof IndicatorResult) {
                            value = ((IndicatorResult) value).getValue();
                        }
                        rowMap.put(rowEntry.getKey().getName(), value);
                    }
                    rows.add(rowMap);
                }
            }
            return rows;
        }else{
            return null;
        }

    }
}

