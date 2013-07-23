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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.webservices.rest.SimpleObject;
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

import org.openmrs.module.reporting.dataset.definition.CohortCrossTabDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortCrossTabDataSetDefinition.CohortDataSetColumn;
/**
 * {@link Resource} for {@link DataSetDefinition}s, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/reportingrest/dataSetDefinition", supportedClass = DataSetDefinition.class, supportedOpenmrsVersions = {"1.8.*", "1.9.*"})
public class DataSetDefinitionResource extends MetadataDelegatingCrudResource<DataSetDefinition> implements Searchable {
	
	/**
	 * @return the definition type that this resource wraps
	 */
	public Class<DataSetDefinition> getDefinitionType() {
		return DataSetDefinition.class;
	}

	/**
	 * @see BaseDelegatingResource#newDelegate()
	 */
	@Override
    public DataSetDefinition newDelegate() {
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
	public DataSetDefinition getByUniqueId(String uuid) {
		return DefinitionContext.getDefinitionByUuid(getDefinitionType(), uuid);
	}
	
	/**
	 * @see BaseDelegatingResource#save(Object)
	 */
	@Override
	public DataSetDefinition save(DataSetDefinition definition) {
		return DefinitionContext.saveDefinition(definition);
	}

	/**
	 * @see BaseDelegatingResource#delete(Object, String, RequestContext)
	 */
	@Override
	public void delete(DataSetDefinition definition, String reason, RequestContext context) throws ResponseException {
		definition.setRetireReason(reason);
		DefinitionContext.retireDefinition(definition);
	}
	
	/**
	 * @see BaseDelegatingResource#purge(Object, RequestContext)
	 */
	@Override
	public void purge(DataSetDefinition definition, RequestContext context) throws ResponseException {
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
			description.addProperty("columnNames");
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
		List<DataSetDefinition> results = DefinitionContext.getDefinitionService(getDefinitionType()).getDefinitions(query, false);
		return new NeedsPaging<DataSetDefinition>(results, context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	public PageableResult doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<DataSetDefinition>(DefinitionContext.getDefinitionService(getDefinitionType()).getAllDefinitions(false), context);
	}
	
	@PropertyGetter("columnNames")
	public SimpleObject getCohort(DataSetDefinition def) {
		if(def instanceof CohortCrossTabDataSetDefinition){
			CohortCrossTabDataSetDefinition crosstab=(CohortCrossTabDataSetDefinition)def;
			List<CohortDataSetColumn> col= crosstab.getDataSetColumns();
			Iterator<CohortDataSetColumn> iterator =col.iterator();
			List<Map<String, String>> columns = new ArrayList<Map<String, String>>();
			
			while (iterator.hasNext()) {
				DataSetColumn column=iterator.next();
				Map<String, String> columnMap = new HashMap<String, String>();
				columnMap.put("name", column.getName());
				columnMap.put("label", column.getLabel());
				columnMap.put("datatype", column.getDataType().getName());
				columns.add(columnMap);
			}
			return new SimpleObject().add("columns", columns);
		}
		else return null;
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
			String url = RestConstants.URI_PREFIX + res.name() + "/" + getUniqueId((DataSetDefinition) delegate);
			url = url.replace("/rest/", "/reporting/"); // hacky :-(
			return url;
		}
		throw new RuntimeException(getClass() + " needs a @Resource or @SubResource annotation");
		
	}
}
