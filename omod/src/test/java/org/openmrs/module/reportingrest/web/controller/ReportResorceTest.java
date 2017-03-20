package org.openmrs.module.reportingrest.web.controller;

import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reportingrest.web.resource.ReportResource;

public class ReportResorceTest  extends BaseDelegatingResourceTest<ReportResource,ReportDesignResource> {

    @Override
    public ReportDesignResource newObject() {
        ReportDesignResource reportdes=new ReportDesignResource();
        reportdes.setUuid("c11f5354-9567-4cc5-b3ef-163e28873926");
        reportdes.setName("testResource");
        reportdes.setDescription("test");
        byte[] n=new byte[2];n[0]=1;n[1]=2;
        reportdes.setContents(n);
        reportdes.setExtension("txt");
        reportdes.setContentType("text/plain");
        return reportdes;
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
        assertPropEquals("contentType",getObject().getContentType());
        assertPropEquals("extension",getObject().getExtension());
    }

    @Override
    public void validateFullRepresentation() throws Exception {
        super.validateFullRepresentation();
        assertPropEquals("name",getObject().getName());
        assertPropEquals("description",getObject().getDescription());
        assertPropEquals("contentType",getObject().getContentType());
        assertPropEquals("extension",getObject().getExtension());
        assertPropEquals("contents",getObject().getContents());

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