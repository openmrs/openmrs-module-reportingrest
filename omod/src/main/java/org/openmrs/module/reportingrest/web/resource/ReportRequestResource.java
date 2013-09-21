/**
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
package org.openmrs.module.reportingrest.web.resource;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.indicator.IndicatorResult;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.*;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reporting.web.renderers.DefaultWebRenderer;
import org.openmrs.module.reporting.web.renderers.LogicReportWebRenderer;
import org.openmrs.module.reportingrest.web.controller.ReportingRestController;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.reporting.report.ReportRequest.Priority;
import java.util.*;

/**
 * {@link Resource} for {@link ReportRequest}s, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + ReportingRestController.REPORTING_REST_NAMESPACE + "/reportrequest",
        supportedClass = ReportRequest.class, supportedOpenmrsVersions = {"1.8.*", "1.9.*"})
public class ReportRequestResource extends DelegatingCrudResource<ReportRequest> {

	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#newDelegate()
	 */
	@Override
    public ReportRequest newDelegate() {
		return new ReportRequest();
	}

	/**
	 * @see BaseDelegatingResource#getByUniqueId(String)
	 */
	@Override
	public ReportRequest getByUniqueId(String uuid) {
		return getService().getReportRequestByUuid(uuid);
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@Override
    public ReportRequest save(ReportRequest reportRequest) {
		return getService().saveReportRequest(reportRequest);
	}

	/**
	 * @see BaseDelegatingResource#delete(Object, String, RequestContext)
	 */
	@Override
	protected void delete(ReportRequest reportRequest, String reason, RequestContext context) throws ResponseException {
		purge(reportRequest, context);
	}

	/**
	 * @see BaseDelegatingResource#purge(Object, RequestContext)
	 */
	@Override
	public void purge(ReportRequest reportRequest, RequestContext context) throws ResponseException {
		if (reportRequest != null) {
			getService().purgeReportRequest(reportRequest);
		}
	}

	/**
	 * @see BaseDelegatingResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = null;
		if (rep instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("uuid");
            description.addProperty("baseCohortDefinition");
            description.addProperty("definitionName");
			description.addProperty("renderingMode");
			description.addProperty("priority");
			description.addProperty("requestedBy", Representation.REF);
			description.addProperty("requestDate");
			description.addProperty("status");
			description.addProperty("evaluateStartDatetime");
			description.addProperty("evaluateCompleteDatetime");
			description.addProperty("renderCompleteDatetime");
			description.addProperty("description");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
		}
		else if (rep instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("uuid");
            description.addProperty("baseCohortDefinition");
            description.addProperty("definitionName");
			description.addProperty("renderingMode");
			description.addProperty("priority");
			description.addProperty("requestedBy", Representation.DEFAULT);
			description.addProperty("requestDate");
			description.addProperty("status");
			description.addProperty("evaluateStartDatetime");
			description.addProperty("evaluateCompleteDatetime");
			description.addProperty("renderCompleteDatetime");
			description.addProperty("description");
            description.addProperty("dataSets");
			description.addSelfLink();
		}else if (rep instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("definitionName");
            description.addProperty("status");
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            return description;
        }
		return description;
	}

	/**
	 * @return the ReportService
	 */
	private ReportService getService() {
		return Context.getService(ReportService.class);
	}

    public PageableResult doGetAll(RequestContext context) throws ResponseException {
        return new NeedsPaging<ReportRequest>(getService().getReportRequests(null, null, null,null,null),context);
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        String lastrecent = context.getRequest().getParameter("recent");
        String name = context.getRequest().getParameter("q");
        String user = context.getRequest().getParameter("user");
        if(StringUtils.isNotBlank(lastrecent)){
            return new NeedsPaging<ReportRequest>(getService().getReportRequests(null, null, null,Integer.parseInt(lastrecent),null),context);
        }
        else if(StringUtils.isNotBlank(name)){
            List<ReportRequest> fin=new ArrayList<ReportRequest>();
            List<ReportRequest> m=getService().getReportRequests(null, null, null,null,null);

            Iterator<ReportRequest> it =m.iterator();
            while(it.hasNext())
            {
                ReportRequest obj = it.next();
                if(obj.getReportDefinition().getParameterizable().getName().equals(name)){
                    fin.add(obj);
                }
            }
            return new NeedsPaging<ReportRequest>(fin,context);
        }
        else if(StringUtils.isNotBlank(user)){
            List<ReportRequest> fin=new ArrayList<ReportRequest>();
            List<ReportRequest> rq=getService().getReportRequests(null, null, null,null,null);

            Iterator<ReportRequest> it =rq.iterator();
            while(it.hasNext())
            {
                ReportRequest obj = it.next();
                if(obj.getRequestedBy().getSystemId().equals(user)){
                    fin.add(obj);
                }
            }
            return new NeedsPaging<ReportRequest>(fin,context);
        }
        else{
            return null;
        }

    }

    @PropertyGetter("baseCohortDefinition")
    public String getTypes(ReportRequest reportRequest) {
        if(reportRequest.getReportDefinition().getParameterizable()!=null){
            if(null==reportRequest.getReportDefinition().getParameterizable().getBaseCohortDefinition()){
                if(reportRequest.getBaseCohort()==null){
                    return "All Patients";
                }
                else{
                    return reportRequest.getBaseCohort().getParameterizable().getName();
                }
            }else return reportRequest.getReportDefinition().getParameterizable().getBaseCohortDefinition().getParameterizable().getName();
        }
        return null;
    }

    @PropertyGetter("definitionName")
    public String getDefinitionreport(ReportRequest reportRequest) {
        if(reportRequest.getReportDefinition().getParameterizable()!=null){
            return reportRequest.getReportDefinition().getParameterizable().getName();
        }
        else return null;
    }

    @PropertyGetter("isProcessAutomatically")
    public Boolean processAuto(ReportRequest reportRequest) {
        return reportRequest.isProcessAutomatically();
    }

    @PropertyGetter("schedule")
    public String getschedule(ReportRequest reportRequest) {
        return reportRequest.getSchedule();
    }

    @PropertyGetter("dataSets")
    public List<Map<String, Object>> getData(ReportRequest reportRequest) {
        ReportData reportdata=Context.getService(ReportService.class).loadReportData(reportRequest);
        List<Map<String, Object>> rows=null;
        if(reportdata!=null){
            rows = new ArrayList<Map<String, Object>>();
            Map<String, DataSet> dataSets=reportdata.getDataSets();
            DataSet dset=null;
            String keyval=null;
            Map<String, Object> rowMap=null;
            for (Map.Entry<String, DataSet> entry : dataSets.entrySet())
            {
                dset=entry.getValue();
                keyval=entry.getKey();
                Iterator<DataSetRow> iterator = dset.iterator();
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
                        if (value instanceof IndicatorResult) {
                            value = ((IndicatorResult) value).getValue();
                        }
                        rowMap.put(rowEntry.getKey().getName(), value);
                    }
                    rows.add(rowMap);
                }
            }
        }
        return rows;
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("status");
        description.addProperty("description");
        description.addProperty("definitionName");
        description.addProperty("schedule");
        description.addProperty("baseCohortDefinition");
        description.addProperty("priority");
        description.addProperty("renderingMode");
        return description;
    }

    @PropertySetter("definitionName")
    public static void setDef(ReportRequest reportRequest, String a) {
        ReportDefinition d=DefinitionContext.getDefinitionByUuid(ReportDefinition.class,a);
        Mapped<ReportDefinition> mapped=new Mapped<ReportDefinition>();
        mapped.setParameterizable(d);
        reportRequest.setReportDefinition(mapped);


    }

    @PropertySetter("status")
    public static void setstatus(ReportRequest reportRequest, String a) {
        reportRequest.setStatus(ReportRequest.Status.REQUESTED);
    }

    @PropertySetter("description")
    public static void setDiscription(ReportRequest reportRequest, String a) {
        reportRequest.setDescription(a);
    }

    @PropertySetter("schedule")
    public static void setSchedule(ReportRequest reportRequest, String a) {
        reportRequest.setSchedule(a);
    }

    @PropertySetter("baseCohortDefinition")
    public static void setBaseCohort(ReportRequest reportRequest, String a) {
        CohortDefinition d= DefinitionContext.getDefinitionByUuid(CohortDefinition.class, a);
        Mapped<CohortDefinition> mapped=new Mapped<CohortDefinition>();
        mapped.setParameterizable(d);
        reportRequest.setBaseCohort(mapped);
    }

    @PropertySetter("priority")
    public static void setPriority(ReportRequest reportRequest, String a) {
        if(a.equalsIgnoreCase("NORMAL")){
            reportRequest.setPriority(Priority.NORMAL);
        }else if(a.equalsIgnoreCase("HIGH")){
            reportRequest.setPriority(Priority.HIGH);
        }else if(a.equalsIgnoreCase("HIGHEST")){
            reportRequest.setPriority(Priority.HIGHEST);
        }else if(a.equalsIgnoreCase("LOW")){
            reportRequest.setPriority(Priority.LOW);
        }else if(a.equalsIgnoreCase("LOWEST")){
            reportRequest.setPriority(Priority.LOWEST);
        }else{
            reportRequest.setPriority(Priority.NORMAL);
        }
    }

    @PropertySetter("renderingMode")
    public static void setRenderer(ReportRequest reportRequest, String a) {
        RenderingMode mode=new RenderingMode();
        if(a.equalsIgnoreCase("default")){
            DefaultWebRenderer render=new DefaultWebRenderer();
            mode.setRenderer(render);
            reportRequest.setRenderingMode(mode);
        }else if(a.equalsIgnoreCase("text")){
            TextTemplateRenderer render=new TextTemplateRenderer();
            mode.setRenderer(render);
            reportRequest.setRenderingMode(mode);
        }else if(a.equalsIgnoreCase("excel")){
            ExcelTemplateRenderer render=new ExcelTemplateRenderer();
            mode.setRenderer(render);
            reportRequest.setRenderingMode(mode);
        }else if(a.equalsIgnoreCase("html")){
            SimpleHtmlReportRenderer render=new SimpleHtmlReportRenderer();
            mode.setRenderer(render);
            reportRequest.setRenderingMode(mode);
        }else if(a.equalsIgnoreCase("tsv")){
            TsvReportRenderer render=new TsvReportRenderer();
            mode.setRenderer(render);
            reportRequest.setRenderingMode(mode);
        }else if(a.equalsIgnoreCase("xls")){
            XlsReportRenderer render=new XlsReportRenderer();
            mode.setRenderer(render);
            reportRequest.setRenderingMode(mode);
        }else if(a.equalsIgnoreCase("logic")){
            LogicReportWebRenderer render=new LogicReportWebRenderer();
            mode.setRenderer(render);
            reportRequest.setRenderingMode(mode);
        }else if(a.equalsIgnoreCase("cohort")){
            CohortDetailReportRenderer render=new CohortDetailReportRenderer();
            mode.setRenderer(render);
            reportRequest.setRenderingMode(mode);
        }else{
            DefaultWebRenderer render=new DefaultWebRenderer();
            mode.setRenderer(render);
            reportRequest.setRenderingMode(mode);
        }
    }

    /**
     * @param delegate
     * @return the URI for the given delegate object
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getUri(Object delegate) {
        if (delegate == null)
            return "";

        Resource res = getClass().getAnnotation(Resource.class);
        if (res != null) {
            String url = RestConstants.URI_PREFIX + res.name() + "/"
                    + getUniqueId((ReportRequest) delegate);
            url = url.replace("/rest/", "/reporting/");
            return url;
        }
        throw new RuntimeException(getClass()
                + " needs a @Resource or @SubResource annotation");
    }
}
