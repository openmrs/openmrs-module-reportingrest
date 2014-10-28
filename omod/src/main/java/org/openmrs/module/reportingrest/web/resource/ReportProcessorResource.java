package org.openmrs.module.reportingrest.web.resource;

import org.openmrs.module.reporting.report.ReportProcessorConfiguration;
import org.openmrs.module.reporting.report.ReportProcessorConfiguration.ProcessorMode;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.Searchable;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.api.context.Context;
import org.openmrs.module.reportingrest.web.controller.ReportingRestController;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;

import java.util.Properties;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + ReportingRestController.REPORTING_REST_NAMESPACE + "/reportProcessor", supportedClass = ReportProcessorConfiguration.class, supportedOpenmrsVersions = {
		"1.8.*", "1.9.*" })
public class ReportProcessorResource extends MetadataDelegatingCrudResource<ReportProcessorConfiguration> implements Searchable {

	public Class<ReportProcessorConfiguration> getDefinitionType() {
		return ReportProcessorConfiguration.class;
	}

	/**
	 * @see BaseDelegatingResource#newDelegate()
	 */
	@Override
	public ReportProcessorConfiguration newDelegate() {
		try {
			return new ReportProcessorConfiguration();

		} catch (Exception e) {
			throw new RuntimeException("Unable to create new " + getDefinitionType());
		}
	}

	private ReportService getService() {
		return Context.getService(ReportService.class);
	}

	/**
	 * @see BaseDelegatingResource#getByUniqueId(String)
	 */
	@Override
	public ReportProcessorConfiguration getByUniqueId(String uuid) {
		return getService().getReportProcessorConfigurationByUuid(uuid);
	}

	/**
	 * @see BaseDelegatingResource#save(Object)
	 */
	@Override
	public ReportProcessorConfiguration save(
			ReportProcessorConfiguration definition) {
		return getService().saveReportProcessorConfiguration(definition);
	}
	
	/**
	 * @see BaseDelegatingResource#delete(Object, String, RequestContext)
	 */
	
	 @Override 
	 public void delete(ReportProcessorConfiguration definition,String reason, RequestContext context) throws ResponseException {
		 // To be Implemented
	}
	 

	/**
	 * @see BaseDelegatingResource#purge(Object, RequestContext)
	 */
	@Override
	public void purge(ReportProcessorConfiguration definition,
			RequestContext context) throws ResponseException {
		getService().purgeReportProcessorConfiguration(definition);
	}

	/**
	 * @see BaseDelegatingResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(
			Representation rep) {
		DelegatingResourceDescription description = null;

		if (rep instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("processorType");
			description.addProperty("processorMode");
			description.addProperty("runOnSuccess");
			description.addProperty("runOnError");
			description.addProperty("reportDesign");
			description.addSelfLink();
			description.addLink("full", ".?v="
					+ RestConstants.REPRESENTATION_FULL);
		} else if (rep instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("processorType");
			description.addProperty("processorMode");
			description.addProperty("runOnSuccess");
			description.addProperty("runOnError");
			description.addProperty("reportDesign");
			description.addProperty("Configuration");
			description.addSelfLink();
		}
		return description;
	}

	@PropertyGetter("processorType")
	public String getProcessorType(ReportProcessorConfiguration rd) {
		return rd.getProcessorType();
	}

	@PropertyGetter("Configuration")
	public Properties getProcessorConfig(ReportProcessorConfiguration rd) {
		return rd.getConfiguration();
	}

	@PropertyGetter("processorMode")
	public ProcessorMode getProcessorMode(ReportProcessorConfiguration rd) {
		return rd.getProcessorMode();
	}

	@PropertyGetter("runOnSuccess")
	public Boolean getProcessorRunOnSucss(ReportProcessorConfiguration rd) {
		return rd.getRunOnSuccess();
	}

	@PropertyGetter("runOnError")
	public Boolean getProcessorOnerror(ReportProcessorConfiguration rd) {
		return rd.getRunOnError();
	}

	@PropertyGetter("reportDesign")
	public String getProcessorDesign(ReportProcessorConfiguration rd) {
		return rd.getReportDesign().toString();
	}


	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	public PageableResult doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<ReportProcessorConfiguration>(getService().getAllReportProcessorConfigurations(false), context);
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
					+ getUniqueId((ReportProcessorConfiguration) delegate);
			url = url.replace("/rest/", "/reporting/"); // hacky :-(
			return url;
		}
		throw new RuntimeException(getClass()
				+ " needs a @Resource or @SubResource annotation");

	}
}
