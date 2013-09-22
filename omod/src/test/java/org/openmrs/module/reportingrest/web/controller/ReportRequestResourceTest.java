package org.openmrs.module.reportingrest.web.controller;

import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.ReportRequest.Priority;
import org.openmrs.module.reporting.report.ReportRequest.Status;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.web.renderers.DefaultWebRenderer;
import org.openmrs.module.reportingrest.web.resource.ReportRequestResource;

public class ReportRequestResourceTest extends BaseDelegatingResourceTest<ReportRequestResource,ReportRequest> {

    @Override
    public ReportRequest newObject() {
        ReportRequest reportRequest=new ReportRequest();
        ReportDefinition reportdef=new ReportDefinition();
        reportdef.setName("test");
        reportdef.setName("Cohortwrapper");
        reportdef.setDescription("Helper for create cohort request");
        Mapped<ReportDefinition> mappedRep=new Mapped<ReportDefinition>();
        mappedRep.setParameterizable(reportdef);
        reportRequest.setReportDefinition(mappedRep);
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
        return "tests";
    }

    @Override
    public String getUuidProperty() {
        return newObject().getUuid();
    }
}