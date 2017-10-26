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

package org.openmrs.module.reportingrest;

import org.codehaus.jackson.annotate.JsonProperty;
import org.openmrs.module.reporting.query.IdSet;

import java.util.HashSet;
import java.util.Set;

/**
 * A simple implementation of IdSet that can be safely serialized by Jackson or XStream.
 */
public class SimpleIdSet implements IdSet {

    @JsonProperty
    private Set<Integer> memberIds;

    public SimpleIdSet() {
        memberIds = new HashSet<Integer>();
    }

    public SimpleIdSet(Set<Integer> memberIds) {
        this.memberIds = memberIds;
    }

    @Override
    public Set<Integer> getMemberIds() {
        return memberIds;
    }

    @Override
    public boolean contains(Integer id) {
        return memberIds.contains(id);
    }

    @Override
    public int getSize() {
        return memberIds.size();
    }

    @Override
    public boolean isEmpty() {
        return memberIds.isEmpty();
    }

    @Override
    public IdSet clone() {
        return new SimpleIdSet(new HashSet<Integer>(memberIds));
    }

    @Override
    public void retainAll(IdSet idSet) { memberIds.retainAll(idSet.getMemberIds()); }

    @Override
    public void removeAll(IdSet idSet) { memberIds.removeAll(idSet.getMemberIds()); }

    @Override
    public void addAll(IdSet idSet) { memberIds.addAll(idSet.getMemberIds()); }

    @Override
    public void setMemberIds(Set set) { memberIds = set; }

}
