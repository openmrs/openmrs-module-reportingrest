package org.openmrs.module.reportingrest.web.controller;

import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportProcessorConfiguration;
import org.openmrs.module.reporting.report.ReportProcessorConfiguration.ProcessorMode;
import org.openmrs.module.reportingrest.web.resource.ReportProcessorResource;

public class ReportProcessorResourceTest  extends BaseDelegatingResourceTest<ReportProcessorResource,ReportProcessorConfiguration> {

    @Override
    public ReportProcessorConfiguration newObject() {
        ReportDesign rd=new ReportDesign();
        rd.setName("reportdesign");
        ReportProcessorConfiguration config=new ReportProcessorConfiguration();
        config.setReportDesign(rd);
        config.setName("processor");config.setUuid("c18717dd-4478-4a0e-84fe-ee62c5f06321");
        config.setDescription("tests");
        config.setProcessorType("org.openmrs.module.reporting.report.processor.LoggingReportProcessor");
        config.setProcessorMode(ProcessorMode.ON_DEMAND);
        config.setRunOnError(false);
        config.setRunOnSuccess(true);
        config.setConfiguration(null);
        return config;
    }

    @Override
    public void validateRefRepresentation() throws Exception {
        super.validateRefRepresentation();
        assertPropEquals("display",getObject().getName());

    }

    @Override
    public void validateDefaultRepresentation() throws Exception {
        super.validateDefaultRepresentation();
        assertPropEquals("name",getObject().getName());
        assertPropEquals("processorType",getObject().getProcessorType());
        assertPropEquals("processorMode",getObject().getProcessorMode());
        assertPropEquals("runOnSuccess",getObject().getRunOnSuccess());
        assertPropEquals("description",getObject().getDescription());
        assertPropEquals("runOnError", getObject().getRunOnError());
    }

    @Override
    public void validateFullRepresentation() throws Exception {
        super.validateFullRepresentation();

        assertPropEquals("name",getObject().getName());
        assertPropEquals("processorType",getObject().getProcessorType());
        assertPropEquals("processorMode",getObject().getProcessorMode());
        assertPropEquals("runOnSuccess",getObject().getRunOnSuccess());
        assertPropEquals("description",getObject().getDescription());
        assertPropEquals("runOnError", getObject().getRunOnError());
        assertPropEquals("configuration",getObject().getConfiguration());
    }

    @Override
    public String getDisplayProperty() {
        return newObject().getName();
    }

    @Override
    public String getUuidProperty() {
        return newObject().getUuid();
    }

}
