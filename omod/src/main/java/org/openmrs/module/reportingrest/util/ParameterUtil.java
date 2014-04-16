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

import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.webservices.rest.web.ConversionUtil;

import java.lang.reflect.Array;
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
                Object convertedValue;
                if (parameter.getCollectionType() != null) {
                    // this looks hacky, but there's no good way to construct a ParameterizedType at run-time; the only
                    // way I know to programmatically construct something like a collection with type information is by
                    // using an array
                    Class<?> arrayClass = Array.newInstance(parameter.getType(), 0).getClass();
                    Object array = ConversionUtil.convert(entry.getValue(), arrayClass);

                    Collection target = parameter.getCollectionType().equals(Set.class) ? new HashSet() : new ArrayList();

                    int length = Array.getLength(array);
                    for (int i = 0; i < length; ++i) {
                        target.add(Array.get(array, i));
                    }
                    convertedValue = target;
                }
                else {
                    convertedValue = ConversionUtil.convert(entry.getValue(), parameter.getType());
                }
                convertedParameterValues.put(entry.getKey(), convertedValue);
            }
        }

        return convertedParameterValues;
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
