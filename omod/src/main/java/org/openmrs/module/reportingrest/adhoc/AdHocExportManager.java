/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.reportingrest.adhoc;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.RowPerObjectDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.definition.DefinitionSummary;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.renderer.XlsReportRenderer;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Equivalent to a Service for managing AdHocDataSets, which are a simplified, web-client-friendly view of
 * row-per-xyz data set definitions
 *
 * This class, and the supporting AdHocDataSets classes, don't feel like they belong in this specific code location,
 * but being here allows them to use standard webservices.rest tooling to handle parameters.
 */
@Component
public class AdHocExportManager {

    public static final String NAME_PREFIX = "[AdHocDataExport] ";

    @Autowired
    private ReportDefinitionService reportDefinitionService;

    @Autowired
    private DataSetDefinitionService dataSetDefinitionService;

    @Autowired
    private ReportService reportService;

    public List<AdHocDataSet> getAdHocDataSets(User user) {
        List<AdHocDataSet> list = new ArrayList<AdHocDataSet>();
        for (DefinitionSummary summary : dataSetDefinitionService.getAllDefinitionSummaries(false)) {
            if (summary.getName().startsWith(NAME_PREFIX)) {
                DataSetDefinition dsd = dataSetDefinitionService.getDefinitionByUuid(summary.getUuid());
                if (user != null && !user.equals(dsd.getCreator())) {
                    continue;
                }
                list.add(new AdHocDataSet(dsd));
            }
        }

        return list;
    }

    public RowPerObjectDataSetDefinition saveAdHocDataSet(RowPerObjectDataSetDefinition dataSetDefinition) {
        markAsAdHoc(dataSetDefinition);
        return dataSetDefinitionService.saveDefinition(dataSetDefinition);
    }

    public RowPerObjectDataSetDefinition getAdHocDataSetByUuid(String uuid) {
        DataSetDefinition dsd = dataSetDefinitionService.getDefinitionByUuid(uuid);
        if (dsd == null) {
            return null;
        }
        verifyAdHoc(dsd);
        return (RowPerObjectDataSetDefinition) dsd;
    }

    public ReportRequest buildExportRequest(List<String> dsdUuids, Map<String, Object> paramValues, RenderingMode renderingMode) {
        ReportDefinition rd = new ReportDefinition();
        List<String> datasetNames = new ArrayList<String>();
        for (String uuid : dsdUuids) {
            RowPerObjectDataSetDefinition dsd = getAdHocDataSetByUuid(uuid);
            datasetNames.add(dsd.getName());
            rd.addDataSetDefinition(removeNamePrefix(dsd.getName()), Mapped.mapStraightThrough(dsd));
        }
        copyParametersFromDsds(rd);

        rd.setName(NAME_PREFIX + "by " + Context.getAuthenticatedUser().getUsername() + " at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        rd.setDescription(OpenmrsUtil.join(datasetNames, ", "));

        reportDefinitionService.saveDefinition(rd);

        if (renderingMode == null) {
            renderingMode = new RenderingMode(new XlsReportRenderer(), "XLS", null, 0);
        }

        ReportRequest request = new ReportRequest();
        request.setReportDefinition(new Mapped<ReportDefinition>(rd, paramValues));
        request.setRenderingMode(renderingMode);

        return request;
    }

    private static String removeNamePrefix(String withPrefix) {
        if (withPrefix.startsWith(NAME_PREFIX)) {
            return withPrefix.substring(NAME_PREFIX.length());
        }
        else {
            return withPrefix;
        }
    }

    public static void verifyAdHoc(DataSetDefinition dsd) {
        if (!dsd.getName().startsWith(NAME_PREFIX)) {
            throw new IllegalArgumentException(dsd.getName() + " is not an Ad Hoc Data Export");
        }
    }

    private void markAsAdHoc(DataSetDefinition dataSetDefinition) {
        if (!dataSetDefinition.getName().startsWith(NAME_PREFIX)) {
            dataSetDefinition.setName(NAME_PREFIX + dataSetDefinition.getName());
        }
    }

    private void copyParametersFromDsds(ReportDefinition reportDefinition) {
        List<Parameter> parameters = new ArrayList<Parameter>();
        for (Mapped<? extends DataSetDefinition> mapped : reportDefinition.getDataSetDefinitions().values()) {
            DataSetDefinition dsd = mapped.getParameterizable();
            for (Parameter p : dsd.getParameters()) {
                addParameterSafely(parameters, p);
            }
        }
        reportDefinition.setParameters(parameters);
    }

    private void addParameterSafely(List<Parameter> parameters, Parameter p) {
        for (Parameter already : parameters) {
            if (already.getName().equals(p.getName())) {
                if (OpenmrsUtil.nullSafeEquals((Class) already.getType(), (Class) p.getType()) &&
                        OpenmrsUtil.nullSafeEquals((Class) already.getCollectionType(), (Class) p.getCollectionType())) {
                    return;
                }
                else {
                    throw new IllegalArgumentException("Inconsistent types for parameter " +
                            p.getName() + ": " + p.getType() + "/" + p.getCollectionType() + " vs. " +
                            already.getType() + "/" + already.getCollectionType());
                }
            }
        }
        parameters.add(p);
    }

    /**
     * Deletes any ReportDefinitions that:
     * <ul>
     *     <li>Are tagged as [AdHocDataExport]</li>
     *     <li>Are not referenced by any existing ReportRequest</li>
     * </ul>
     *
     * The intended lifecycle is that:
     * <ol>
     *     <li>When running an Ad Hoc Export, we create a (transient) ReportDefinition, and a ReportRequest to evaluate it</li>
     *     <li>The ReportRequest is evaluated</li>
     *     <li>Eventually the ReportRequest is deleted due to age (unless a user marked it as Saved)</li>
     *     <li>At that point, org.openmrs.module.reportingui.task.DeleteOldOldAdHocReportDefinitionsTask will call this method, which deletes the ReportRequest</li>
     * </ol>
     */
    public void deleteTransientReportDefinitions() {
        for (DefinitionSummary summary : reportDefinitionService.getAllDefinitionSummaries(true)) {
            if (summary.getName().startsWith(NAME_PREFIX)) {
                // tagged as [AdHocDataExport]
                ReportDefinition candidate = reportDefinitionService.getDefinitionByUuid(summary.getUuid());
                List<ReportRequest> requestsForCandidate = reportService.getReportRequests(candidate, null, null, 1);
                if (requestsForCandidate.size() == 0) {
                    // not referenced by any existing ReportRequest
                    deleteReportDefinitionAndDesigns(candidate);
                }
            }
        }
    }

    private void deleteReportDefinitionAndDesigns(ReportDefinition definition) {
        for (ReportDesign design : reportService.getReportDesigns(definition, null, true)) {
            reportService.purgeReportDesign(design);
        }
        reportDefinitionService.purgeDefinition(definition);
    }


    public class AdHocDataSet {

        private String uuid;

        private String name;

        private String description;

        private String type;

        private List<AdHocParameter> parameters;

        public AdHocDataSet(DataSetDefinition dsd) {
            this.uuid = dsd.getUuid();
            this.name = dsd.getName();
            if (this.name.startsWith(NAME_PREFIX)) {
                this.name = this.name.substring(NAME_PREFIX.length());
            }
            this.description = dsd.getDescription();
            this.type = dsd.getClass().getSimpleName();

            parameters = new ArrayList<AdHocParameter>();
            for (Parameter parameter : dsd.getParameters()) {
                parameters.add(new AdHocParameter(parameter));
            }
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<AdHocParameter> getParameters() {
            return parameters;
        }

        public void setParameters(List<AdHocParameter> parameters) {
            this.parameters = parameters;
        }
    }

}