package org.openmrs.module.reportingrest.web.controller;

import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reportingrest.web.resource.ReportDefinitionResource;

public class ReportDefinitionResourceTest  extends BaseDelegatingResourceTest<ReportDefinitionResource,ReportDefinition> {

    @Override
    public ReportDefinition newObject() {
        return DefinitionContext.getDefinitionByUuid(ReportDefinition.class,"c11f5354-9567-4cc5-b3ef-163e28873926");
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
        assertPropEquals("description",getObject().getDescription());
        assertPropEquals("parameters",getObject().getParameters());
    }

    @Override
    public void validateFullRepresentation() throws Exception {
        super.validateFullRepresentation();
        assertPropEquals("name",getObject().getName());
        assertPropEquals("description",getObject().getDescription());
        assertPropEquals("parameters",getObject().getParameters());
        assertPropPresent("baseCohortDefinition");
        assertPropPresent("dataSetDefintions");
        assertPropPresent("types");
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
