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

import java.util.List;

import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.Definition;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.SearchResult;
import org.openmrs.module.webservices.rest.web.resource.api.Searchable;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

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
	protected T newDelegate() {
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
	 * @see BaseDelegatingResource#getRepresentationDescription(representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = null;
		
		
		// TODO: Add more properties below, particularly parameters
		
		
		if (rep instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
		} 
		else if (rep instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addSelfLink();
		}
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(java.lang.String, org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected SearchResult doSearch(String query, RequestContext context) {
		List<T> results = DefinitionContext.getDefinitionService(getDefinitionType()).getDefinitions(query, false);
		return new NeedsPaging<T>(results, context);
	}
	
	
}
