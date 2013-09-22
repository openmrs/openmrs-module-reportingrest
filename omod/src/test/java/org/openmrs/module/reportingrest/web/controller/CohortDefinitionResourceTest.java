package org.openmrs.module.reportingrest.web.controller;

import java.util.ArrayList;
import java.util.List;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reportingrest.web.resource.CohortDefinitionResource;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;

public class CohortDefinitionResourceTest extends BaseDelegatingResourceTest<CohortDefinitionResource,CohortDefinition> {

    @Override
    public CohortDefinition newObject() {
        SqlCohortDefinition sql=new SqlCohortDefinition();
        sql.setName("SQLcohort");
        sql.setUuid("d11f5354-9567-4cc5-b3ef-163e28873555");
        sql.setDescription("testing");
        Parameter p=new Parameter();p.setLabel("label1");p.setName("name1");p.setType(String.class);
        List<Parameter> pp=new ArrayList<Parameter>();pp.add(p);
        sql.setParameters(pp);
        return sql;
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
        assertPropPresent("parameters");
    }

    @Override
    public void validateFullRepresentation() throws Exception {
        super.validateFullRepresentation();
        assertPropEquals("name",getObject().getName());
        assertPropEquals("description",getObject().getDescription());
        assertPropPresent("parameters");
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
