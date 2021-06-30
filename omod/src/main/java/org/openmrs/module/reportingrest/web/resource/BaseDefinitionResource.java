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

import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.Definition;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
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

import java.util.List;

/**
 * Base {@link Resource} for {@link Definition}s, supporting standard CRUD operations
 */
public abstract class BaseDefinitionResource<T extends Definition> extends MetadataDelegatingCrudResource<T> implements Searchable {
	
	/**
	 * @return the definition type that this resource wraps
	 */
	public abstract Class<T> getDefinitionType();
	
	/**
	 * @see BaseDelegatingResource#newDelegate()
	 */
	@Override
    public T newDelegate() {
		try {
			return getDefinitionType().newInstance();
		}
		catch (Exception e) {
			throw new RuntimeException("Unable to create new " + getDefinitionType());
		}
	}
	
	/**
	 * @see BaseDelegatingResource#getByUniqueId(String)
	 */
	@Override
	public T getByUniqueId(String uuid) {
		return DefinitionContext.getDefinitionByUuid(getDefinitionType(), uuid);
	}
	
	/**
	 * @see BaseDelegatingResource#save(Object)
	 */
	@Override
	public T save(T definition) {
		return DefinitionContext.saveDefinition(definition);
	}

	/**
	 * @see BaseDelegatingResource#delete(Object, String, RequestContext)
	 */
	@Override
	public void delete(T definition, String reason, RequestContext context) throws ResponseException {
		definition.setRetireReason(reason);
		DefinitionContext.retireDefinition(definition);
	}
	
	/**
	 * @see BaseDelegatingResource#purge(Object, RequestContext)
	 */
	@Override
	public void purge(T definition, RequestContext context) throws ResponseException {
		DefinitionContext.purgeDefinition(getDefinitionType(), definition.getUuid());
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
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("parameters");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
		} 
		else if (rep instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("parameters");
			description.addSelfLink();
		}
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected PageableResult doSearch(RequestContext context) {
        String query = context.getParameter("q");
		List<T> results = DefinitionContext.getDefinitionService(getDefinitionType()).getDefinitions(query, false);
		return new NeedsPaging<T>(results, context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	public PageableResult doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<T>(DefinitionContext.getDefinitionService(getDefinitionType()).getAllDefinitions(false), context);
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
			String url = RestConstants.URI_PREFIX + res.name() + "/" + getUniqueId((T) delegate);
			url = url.replace("/rest/", "/reporting/"); // hacky :-(
			return url;
		}
		throw new RuntimeException(getClass() + " needs a @Resource or @SubResource annotation");
		
	}
	
}
