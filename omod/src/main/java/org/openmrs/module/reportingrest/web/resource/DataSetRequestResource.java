package org.openmrs.module.reportingrest.web.resource;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.ReportRequest.Status;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reportingrest.web.controller.ReportingRestController;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reporting.report.ReportRequest.Priority;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.openmrs.module.reporting.web.renderers.DefaultWebRenderer;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.indicator.IndicatorResult;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.Searchable;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * {@link Resource} for {@link ReportDefinition}s, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + ReportingRestController.REPORTING_REST_NAMESPACE + "/datasetrequest",
        supportedClass = ReportRequest.class, supportedOpenmrsVersions = {"1.8.*", "1.9.*"})
public class DataSetRequestResource extends DelegatingCrudResource<ReportRequest> implements Searchable {

    /**
     * @return the ReportService
     */
    private ReportService getService() {
        return Context.getService(ReportService.class);
    }

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
            description.addProperty("priority");
            description.addProperty("requestedBy", Representation.REF);
            description.addProperty("requestDate");
            description.addProperty("status");
            description.addProperty("evaluateStartDatetime");
            description.addProperty("evaluateCompleteDatetime");
            description.addProperty("dataSetDefinition");
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
        }
        else if (rep instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("priority");
            description.addProperty("requestedBy", Representation.DEFAULT);
            description.addProperty("requestDate");
            description.addProperty("status");
            description.addProperty("evaluateStartDatetime");
            description.addProperty("evaluateCompleteDatetime");
            description.addProperty("dataSetDefinition");
            description.addProperty("schedule");
            description.addProperty("dataRows");
            description.addSelfLink();
        }
        else if (rep instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("dataSetDefinition");
            description.addProperty("status");
            return description;
        }
        return description;
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
            List<ReportRequest> m=getService().getReportRequests(null, null, null,null,null);

            Iterator<ReportRequest> it =m.iterator();
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

    @PropertyGetter("dataSetDefinition")
    public String getDataSetdef(ReportRequest reportRequest) {
        ReportDefinition rd=reportRequest.getReportDefinition().getParameterizable();
        return "check this out";
    }


    @PropertyGetter("schedule")
    public String getSchedule(ReportRequest reportRequest) {
        return reportRequest.getSchedule();
    }

    @PropertyGetter("dataRows")
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
        description.addProperty("dataSetDefinition");
        description.addProperty("schedule");
        description.addProperty("priority");
        return description;
    }

    @PropertySetter("dataSetDefinition")
    public static void setDef(ReportRequest reportRequest, String uuid) {
        //create report definition
        ReportDefinition reportdef=new ReportDefinition();
        String key="testKey";
        //get the data set definition
        DataSetDefinition tis=DefinitionContext.getDefinitionByUuid(DataSetDefinition.class,uuid);
        Mapped<DataSetDefinition> nn=new Mapped<DataSetDefinition>();nn.setParameterizable(tis);
        Map<String,Mapped<? extends DataSetDefinition>> dod=new HashMap<String,Mapped<? extends DataSetDefinition>>();
        dod.put(key,nn);
        //set the data set definition to the report definition
        reportdef.setDataSetDefinitions(dod);
        reportdef.setName("DataSetwrapper");
        reportdef.setDescription("Helper for create dataSet request");
        DefinitionContext.saveDefinition(reportdef);
        Mapped<ReportDefinition> mapped=new Mapped<ReportDefinition>();
        mapped.setParameterizable(reportdef);
        //set the report definition to the report request
        reportRequest.setReportDefinition(mapped);
        //set the rendering mode
        RenderingMode mode=new RenderingMode();
        DefaultWebRenderer render=new DefaultWebRenderer();
        mode.setRenderer(render);
        reportRequest.setRenderingMode(mode);
        reportRequest.setPriority(Priority.NORMAL);
        reportRequest.setStatus(Status.REQUESTED);
    }

    @PropertySetter("description")
    public static void setDescription(ReportRequest reportRequest, String description) {
        reportRequest.setDescription(description);
    }

    @PropertySetter("schedule")
    public static void setSched(ReportRequest reportRequest, String schedule) {
        reportRequest.setSchedule(schedule);
    }

    @PropertySetter("priority")
    public static void setPriority(ReportRequest reportRequest, String priority) {
        if(priority.equalsIgnoreCase("NORMAL")){
            reportRequest.setPriority(Priority.NORMAL);
        }else if(priority.equalsIgnoreCase("HIGH")){
            reportRequest.setPriority(Priority.HIGH);
        }else if(priority.equalsIgnoreCase("HIGHEST")){
            reportRequest.setPriority(Priority.HIGHEST);
        }else if(priority.equalsIgnoreCase("LOW")){
            reportRequest.setPriority(Priority.LOW);
        }else if(priority.equalsIgnoreCase("LOWEST")){
            reportRequest.setPriority(Priority.LOWEST);
        }else{
            reportRequest.setPriority(Priority.NORMAL);
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



