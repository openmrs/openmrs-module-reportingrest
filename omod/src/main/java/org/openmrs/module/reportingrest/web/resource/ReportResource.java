package org.openmrs.module.reportingrest.web.resource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reportingrest.web.controller.ReportingRestController;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.Searchable;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.ReportDesign;

;

@Resource(name = RestConstants.VERSION_1
		+ ReportingRestController.REPORTING_REST_NAMESPACE + "/reportResource", supportedClass = ReportDesignResource.class, supportedOpenmrsVersions = {
		"1.8.*", "1.9.*" })
public class ReportResource extends	MetadataDelegatingCrudResource<ReportDesignResource> implements	Searchable {

	public Class<ReportDesignResource> getDefinitionType() {
		return ReportDesignResource.class;
	}

	/**
	 * @see BaseDelegatingResource#newDelegate()
	 */
	@Override
	public ReportDesignResource newDelegate() {
		try {
			return new ReportDesignResource();

		} catch (Exception e) {
			throw new RuntimeException("Unable to create new "
					+ getDefinitionType());
		}
	}

	private ReportService getService() {
		return Context.getService(ReportService.class);
	}

	/**
	 * @see BaseDelegatingResource#getByUniqueId(String)
	 */
	ReportDefinition mydef = null;

	@Override
	public ReportDesignResource getByUniqueId(String uuid) {

		Iterator<ReportDesign> iter = getService().getAllReportDesigns(false).iterator();
		List<ReportDesignResource> repdes = new ArrayList<ReportDesignResource>();
		while (iter.hasNext()) {
			Set<ReportDesignResource> resSet = ((ReportDesign) iter.next()).getResources();
			Iterator<ReportDesignResource> iterdes = resSet.iterator();
			while (iterdes.hasNext()) {
				repdes.add(iterdes.next());
			}
		}

		Iterator<ReportDesignResource> iterator = repdes.iterator();
		while (iterator.hasNext()) {
			ReportDesignResource resource = iterator.next();
			if (resource!= null) {
				if (resource.getUuid().equals(uuid)) {
					return resource;
				}
			}
		}
		return null;
	}
	
	/**
	 * @see BaseDelegatingResource#save(Object)
	 */
	@Override
	public ReportDesignResource save(ReportDesignResource definition) {
		// To be implemented
		return new ReportDesignResource();
	}

	/**
	 * @see BaseDelegatingResource#delete(Object, String, RequestContext)
	 */
	
	 @Override 
	 public void delete(ReportDesignResource definition, String reason, RequestContext context) throws ResponseException {
		 // not used
	}
	 
	/**
	 * @see BaseDelegatingResource#purge(Object, RequestContext)
	 */
	@Override
	public void purge(ReportDesignResource definition, RequestContext context) throws ResponseException {
		// To be implemented
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
			description.addProperty("ContentType");
			description.addProperty("Extension");
			description.addSelfLink();
			description.addLink("full", ".?v="+ RestConstants.REPRESENTATION_FULL);
		} else if (rep instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("ContentType");
			description.addProperty("Extension");
			description.addProperty("Contents");
			description.addSelfLink();
		}
		return description;
	}

	@PropertyGetter("ContentType")
	public String getProcessorType(ReportDesignResource designres) {
		return designres.getContentType();
	}

	@PropertyGetter("Extension")
	public String getProcessorConfig(ReportDesignResource designres) {
		return designres.getExtension();
	}

	@PropertyGetter("Contents")
	public byte[] getProcessorMode(ReportDesignResource designres) {
		return designres.getContents();
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	public PageableResult doGetAll(RequestContext context)
			throws ResponseException {
		Iterator<ReportDesign> iter = getService().getAllReportDesigns(false).iterator();
		List<ReportDesignResource> repdes = new ArrayList<ReportDesignResource>();
		while (iter.hasNext()) {
			Set<ReportDesignResource> resSet = ((ReportDesign) iter.next()).getResources();
			Iterator<ReportDesignResource> iterdes = resSet.iterator();
			while (iterdes.hasNext()) {
				repdes.add(iterdes.next());
			}
		}
		return new NeedsPaging<ReportDesignResource>(repdes, context);
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
					+ getUniqueId((ReportDesignResource) delegate);
			url = url.replace("/rest/", "/reporting/"); // hacky :-(
			return url;
		}
		throw new RuntimeException(getClass()
				+ " needs a @Resource or @SubResource annotation");

	}
}
