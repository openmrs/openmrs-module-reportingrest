package org.openmrs.module.reportingrest.web.controller;

import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reportingrest.web.resource.DataSetDefinitionResource;

public class DataSetDefinitionResourceTest  extends BaseDelegatingResourceTest<DataSetDefinitionResource,DataSetDefinition> {

    @Override
    public DataSetDefinition newObject() {
        return DefinitionContext.getDefinitionByUuid(DataSetDefinition.class,"d9c79890-7ea9-41b1-a068-b5b99ca3d593");
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
        assertPropPresent("columnNames");
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
