package org.openmrs.module.reportingrest.web.controller;

import java.util.LinkedHashSet;
import java.util.Set;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reportingrest.web.resource.EvaluatedCohortResource;

public class EvaluatedCohortResourceTest extends BaseDelegatingResourceTest<EvaluatedCohortResource,EvaluatedCohort> {

    @Override
    public EvaluatedCohort newObject() {
        EvaluatedCohort evalcohort=new EvaluatedCohort();
        evalcohort.setUuid("c11f5354-9567-4cc5-b3ef-163e28873926");
        Set<Integer> idSet=new LinkedHashSet<Integer>();idSet.add(345);idSet.add(432);
        evalcohort.setMemberIds(idSet);
        evalcohort.setName("testCohort");
        evalcohort.setDescription("testing");
        SqlCohortDefinition q=new SqlCohortDefinition();q.setName("sql");
        evalcohort.setDefinition(q);
        q.setUuid("c11f5354-9567-4cc5-b3ef-163e28873926");
        return evalcohort;
    }

    @Override
    public void validateRefRepresentation() throws Exception {
        super.validateRefRepresentation();
        assertPropEquals("uuid",newObject().getUuid());
        assertPropPresent("definition");
    }

    @Override
    public void validateDefaultRepresentation() throws Exception {
        assertPropPresent("members");
        assertPropPresent("definition");
    }

    @Override
    public void validateFullRepresentation() throws Exception {
        super.validateFullRepresentation();
        assertPropPresent("definition");
        assertPropPresent("members");
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
