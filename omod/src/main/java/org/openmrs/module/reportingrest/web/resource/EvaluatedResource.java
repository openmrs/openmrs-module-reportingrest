package org.openmrs.module.reportingrest.web.resource;

import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
import org.openmrs.module.reporting.definition.service.DefinitionService;
import org.openmrs.module.reporting.evaluation.Definition;
import org.openmrs.module.reporting.evaluation.Evaluated;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Retrievable;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.util.StringUtils;

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

    /**
     * Fetches all parameters needed for definition from the request. First looks at request parameters, and looks
     * at postBody next
     *
     * @param definition
     * @param requestContext
     * @param parameterPrefix
     * @param postBody optional
     * @return
     * @throws ConversionException
     */
    protected EvaluationContext getEvaluationContextWithParameters(Definition definition, RequestContext requestContext, String parameterPrefix, SimpleObject postBody) throws ConversionException {
        HttpServletRequest request = requestContext.getRequest();
        EvaluationContext evalContext = new EvaluationContext();

        // get the params off the requestContext and put them on the evalContext
        for (Parameter param : definition.getParameters()) {
            String paramName = StringUtils.hasText(parameterPrefix) ? parameterPrefix + param.getName() : param.getName();
            Object convertedValue = null;

            if (param.getCollectionType() != null) {
                Collection collection;
                if (Set.class.isAssignableFrom(param.getCollectionType())) {
                    collection = new LinkedHashSet();
                } else if (List.class.isAssignableFrom(param.getCollectionType())) {
                    collection = new ArrayList();
                } else {
                    throw new IllegalStateException("Cannot handle collection type: " + param.getCollectionType());
                }

                if (request.getParameterValues(paramName) != null) {
                    for (String httpParamValue : request.getParameterValues(paramName)) {
                        collection.add(ConversionUtil.convert(httpParamValue, param.getType()));
                    }
                } else {
                    // if there were no request params, look at the postBody
                    Object posted = postBody.get(paramName);
                    if (posted != null) {
                        if (posted instanceof Collection) {
                            for (Object item : ((Collection) posted)) {
                                collection.add(ConversionUtil.convert(item, param.getType()));
                            }
                        } else {
                            throw new IllegalArgumentException("Parameter " + paramName + " in POST body should be an array");
                        }
                    }
                }
                convertedValue = collection;

            } else {
                String httpParamValue = request.getParameter(paramName);
                if (httpParamValue != null) {
                    convertedValue = ConversionUtil.convert(httpParamValue, param.getType());
                } else if (postBody != null) {
                    convertedValue = ConversionUtil.convert(postBody.get(paramName), param.getType());
                }
            }

            if (param.isRequired() && convertedValue == null) {
                throw new IllegalArgumentException("Missing parameter: " + paramName);
            }

            evalContext.addParameterValue(paramName, convertedValue);
        }
        return evalContext;
    }

    protected <Def extends Definition> Def getDefinitionByUniqueId(DefinitionService<Def> svc, Class<Def> clazz, String uniqueId) {
        AllDefinitionLibraries definitionLibraries = Context.getRegisteredComponents(AllDefinitionLibraries.class).get(0);
        Def definition = definitionLibraries.getDefinition(clazz, uniqueId);
        if (definition == null) {
            definition = svc.getDefinitionByUuid(uniqueId);
        }
        return definition;
    }

    protected <Def extends Definition> Evaluated<Def> evaluate(Def definition, DefinitionService<Def> svc, EvaluationContext ctx) throws EvaluationException {
        Evaluated<Def> evaluated = svc.evaluate(definition, ctx);

        // there seems to be a bug in the reporting module that doesn't set these
        if (evaluated.getDefinition().getName() == null)
            evaluated.getDefinition().setName(definition.getName());
        if (evaluated.getDefinition().getDescription() == null)
            evaluated.getDefinition().setDescription(definition.getDescription());
        if (evaluated.getDefinition().getUuid() == null)
            evaluated.getDefinition().setUuid(definition.getUuid());

        return evaluated;
    }
}
