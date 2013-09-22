package org.openmrs.module.reportingrest.web.controller;

import java.util.HashMap;
import java.util.Map;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortCrossTabDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.ReportRequest.Priority;
import org.openmrs.module.reporting.report.ReportRequest.Status;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.web.renderers.DefaultWebRenderer;
import org.openmrs.module.reportingrest.web.resource.ReportRequestResource;

public class CohortRequestResourceTest  extends BaseDelegatingResourceTest<ReportRequestResource,ReportRequest> {

    @Override
    public ReportRequest newObject() {
        ReportRequest reportRequest=new ReportRequest();
        CohortDefinition cohort=new AgeCohortDefinition();cohort.setName("asd");
        //create a CohortCrossTabDataSetDefinition which can hold the cohort definition in its columns
        CohortCrossTabDataSetDefinition crosstab=new CohortCrossTabDataSetDefinition();
        crosstab.setName("cohortWrappingDataset");
        Map<String, Mapped<? extends CohortDefinition>> mapi=new HashMap<String, Mapped<? extends CohortDefinition>>();
        Mapped<CohortDefinition> mapped=new Mapped<CohortDefinition>();mapped.setParameterizable(cohort);
        mapi.put("cohort key created by program", mapped);
        //set the cohort definition to the datasetdefinition
        crosstab.setColumns(mapi);
        Mapped<DataSetDefinition> mappedData=new Mapped<DataSetDefinition>(); mappedData.setParameterizable(crosstab);
        Map<String, Mapped<? extends DataSetDefinition>> definition=new HashMap<String, Mapped<? extends DataSetDefinition>>();
        definition.put("cohort key created by program",  mappedData);
        //create a report definition
        ReportDefinition reportdef=new ReportDefinition();
        //set the datasetdefinition to the report definition
        reportdef.setDataSetDefinitions(definition);
        reportdef.setName("Cohortwrapper");
        reportdef.setDescription("Helper for create cohort request");
        //DefinitionContext.saveDefinition(reportdef); ************//////////////**************//////////////////
        Mapped<ReportDefinition> mappedRep=new Mapped<ReportDefinition>();
        mappedRep.setParameterizable(reportdef);
        //set the report definition to the report requests
        reportRequest.setReportDefinition(mappedRep);
        //set the default rendering mode for the report request
        RenderingMode mode=new RenderingMode();
        DefaultWebRenderer render=new DefaultWebRenderer();
        mode.setRenderer(render);
        reportRequest.setRenderingMode(mode);
        reportRequest.setPriority(Priority.NORMAL);
        reportRequest.setStatus(Status.REQUESTED);
        reportRequest.setUuid("c11f5354-9567-4cc5-b3ef-163e28873926");
        return reportRequest;
    }

    @Override
    public void validateRefRepresentation() throws Exception {
        super.validateRefRepresentation();
        assertPropEquals("status",newObject().getStatus());

    }

    @Override
    public void validateDefaultRepresentation() throws Exception {
        super.validateDefaultRepresentation();
        assertPropEquals("priority",newObject().getPriority());
        assertPropPresent("requestedBy");
        assertPropPresent("requestDate");
        assertPropEquals("status",newObject().getStatus());
        assertPropEquals("evaluateStartDatetime",newObject().getEvaluateStartDatetime());
        assertPropEquals("evaluateCompleteDatetime",newObject().getEvaluateCompleteDatetime());
    }

    @Override
    public void validateFullRepresentation() throws Exception {
        super.validateFullRepresentation();
        assertPropEquals("priority",newObject().getPriority());
        assertPropPresent("requestedBy");
        assertPropPresent("requestDate");
        assertPropEquals("status",newObject().getStatus());
        assertPropEquals("evaluateStartDatetime",newObject().getEvaluateStartDatetime());
        assertPropEquals("evaluateCompleteDatetime",newObject().getEvaluateCompleteDatetime());
        assertPropEquals("schedule",newObject().getSchedule());

    }

    @Override
    public String getDisplayProperty() {
        return "Neverjhbdd Never Landd";
    }

    @Override
    public String getUuidProperty() {
        return "c11f5354-9567-4cc5-b3ef-163e28873926";
    }
}
