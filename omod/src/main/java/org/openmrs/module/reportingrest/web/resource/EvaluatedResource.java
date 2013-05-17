package org.openmrs.module.reportingrest.web.resource;

import org.openmrs.module.reporting.evaluation.Definition;
import org.openmrs.module.reporting.evaluation.Evaluated;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Retrievable;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.wrapper.openmrs1_8.CohortMember1_8;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Common functionality for resources that evaluate definitions
 */
public abstract class EvaluatedResource<T extends Evaluated> extends DelegatingCrudResource<T> implements Retrievable {

    /**
     * @param evaluated the delegate
     * @return the uuid of the definition that is defined on this object
     */
    @PropertyGetter("uuid")
    public String getUuidOfEvaluatedDefinition(T evaluated) {
        return evaluated.getDefinition().getUuid();
    }

    /**
     * Overridden here since the unique id is not on Evaluated directly
     * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getUniqueId(java.lang.Object)
     */
    @Override
    public String getUniqueId(T evaluated) {
        return evaluated.getDefinition().getUuid();
    }

    @Override
    public List<Representation> getAvailableRepresentations() {
        return Arrays.asList(Representation.DEFAULT);
    }


    @Override
    public T getByUniqueId(String uniqueId) {
        // not used
        return null;
    }

    @Override
    public T newDelegate() {
        // not used (?)
        return null;
    }

    @Override
    public T save(T delegate) {
        // not used
        return null;
    }

    @Override
    protected void delete(T delegate, String reason, RequestContext context) throws ResponseException {
        // not used
    }

    @Override
    public void purge(T delegate, RequestContext context) throws ResponseException {
        // not used
    }

    protected EvaluationContext getEvaluationContextWithParameters(Definition definition, RequestContext requestContext) throws ConversionException {
        HttpServletRequest request = requestContext.getRequest();
        EvaluationContext evalContext = new EvaluationContext();

        // get the params off the requestContext and put them on the evalContext
        for (Parameter param : definition.getParameters()) {
            Object convertedValue;

            if (param.getCollectionType() != null) {
                Collection collection;
                if (Set.class.isAssignableFrom(param.getCollectionType())) {
                    collection = new LinkedHashSet();
                } else if (List.class.isAssignableFrom(param.getCollectionType())) {
                    collection = new ArrayList();
                } else {
                    throw new IllegalStateException("Cannot handle collection type: " + param.getCollectionType());
                }

                for (String httpParamValue : request.getParameterValues(param.getName())) {
                    collection.add(ConversionUtil.convert(httpParamValue, param.getType()));
                }
                convertedValue = collection;

            } else {
                String httpParamValue = request.getParameter(param.getName());
                convertedValue = ConversionUtil.convert(httpParamValue, param.getType());
            }

            if (convertedValue == null) {
                throw new IllegalArgumentException("Missing parameter: " + param.getName());
            }

            evalContext.addParameterValue(param.getName(), convertedValue);
        }
        return evalContext;
    }
}
