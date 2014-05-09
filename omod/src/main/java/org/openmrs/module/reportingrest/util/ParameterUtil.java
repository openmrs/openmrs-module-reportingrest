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

package org.openmrs.module.reportingrest.util;

import org.apache.commons.beanutils.PropertyUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParameterUtil {

    /**
     * Converts simple JSON types to the correct types as specified in _parameters_.
     *
     * This supports the scenario where a REST client submits a minimal representation of an object by looking up the
     * real object, and not just converting the submitted object's properties, e.g. you could submit an encounter type
     * that looks like {"display": "Emergency", "uuid": "07000be2-26b6-4cce-8b40-866d8435b613"} or even just
     * "07000be2-26b6-4cce-8b40-866d8435b613"
     *
     * @param parameters
     * @param parameterValues
     * @return
     */
    public static Map<String, Object> convertParameterValues(List<Parameter> parameters, Map<String, Object> parameterValues) throws Exception {
        if (parameterValues == null) {
            return null;
        }
        Map<String, Object> convertedParameterValues = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : parameterValues.entrySet()) {
            Parameter parameter = findParameterByName(parameters, entry.getKey());
            if (parameter != null) {
                Resource resource = getResourceFor(parameter.getType());
                Object convertedValue;
                if (parameter.getCollectionType() != null) {
                    Collection target = newCollectionFor(parameter);
                    if (resource != null) {
                        // use the resource for this type, assuming a uuid for minimal representation was submitted
                        List original = (List) entry.getValue();
                        for (Object item : original) {
                            target.add(getRealInstanceByUuid(resource, item));
                        }
                        convertedValue = target;
                    }
                    else {
                        // no resource for this type, so it's probably a simple type (date, etc), and we use the REST
                        // module's ConversionUtil to convert it

                        // this looks hacky, but there's no good way to construct a ParameterizedType at run-time; the only
                        // way I know to programmatically construct something like a collection with type information is by
                        // using an array
                        Class<?> arrayClass = Array.newInstance(parameter.getType(), 0).getClass();
                        Object array = ConversionUtil.convert(entry.getValue(), arrayClass);

                        int length = Array.getLength(array);
                        for (int i = 0; i < length; ++i) {
                            target.add(Array.get(array, i));
                        }
                        convertedValue = target;
                    }
                }
                else { // single value, not collection
                    if (resource != null) {
                        // use the resource for this type, assuming a uuid for minimal representation was submitted
                        convertedValue = getRealInstanceByUuid(resource, entry.getValue());
                    }
                    else {
                        // no resource for this type, so it's probably a simple type (date, etc), and we use the REST
                        // module's ConversionUtil to convert it
                        convertedValue = ConversionUtil.convert(entry.getValue(), parameter.getType());
                    }
                }

                convertedParameterValues.put(entry.getKey(), convertedValue);
            }
        }

        return convertedParameterValues;
    }

    /**
     * Expects you to pass in a String UUID, or else an object with a String "uuid" property.
     * Also assumes that resource is a conventional OpenMRS implementation, with a "getByUniqueId" method
     * @param resource
     * @param item
     * @return
     */
    private static Object getRealInstanceByUuid(Resource resource, Object item) {
        try {
            Method method = getByUniqueIdMethod(resource);
            String uuid = (String) (item instanceof String ? item : PropertyUtils.getProperty(item, "uuid"));
            return method.invoke(resource, uuid);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Failed to find item=" + item + " using resource=" + resource, e);
        }
    }

    private static Method getByUniqueIdMethod(Resource resource) {
        try {
            return resource.getClass().getMethod("getByUniqueId", String.class);
        }
        catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static Collection newCollectionFor(Parameter parameter) {
        Class<? extends Collection> collectionType = parameter.getCollectionType();
        return collectionType == null ? null :
                collectionType.equals(Set.class) ? new HashSet() : new ArrayList();
    }

    private static Resource getResourceFor(Class<?> type) {
        try {
            return Context.getService(RestService.class).getResourceBySupportedClass(type);
        }
        catch (APIException ex) {
            // e.g. org.openmrs.api.A#PIException: Unknown resource: class java.util.Date
            return null;
        }
    }

    private static Parameter findParameterByName(List<Parameter> parameters, String name) {
        for (Parameter parameter : parameters) {
            if (parameter.getName().equals(name)) {
                return parameter;
            }
        }
        return null;
    }

    /**
     * If any parameters on mapped.parameterizable are not explicitly mapped, they will be mapped to ${paramName}
     * @param mapped
     */
    public static void mapMissingParametersStraightThrough(Mapped<? extends Parameterizable> mapped) {
        Map<String, Object> mappings = mapped.getParameterMappings();
        Parameterizable parameterizable = mapped.getParameterizable();
        for (Parameter parameter : parameterizable.getParameters()) {
            if (mappings.get(parameter.getName()) == null) {
                mappings.put(parameter.getName(), parameter.getExpression());
            }
        }
    }

}
