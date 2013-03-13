/*
 * Copyright 2013 jlgranda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eqaula.glue.service;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.model.management.Organization;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.model.LazyDataModel;

/**
 *
 * @author jlgranda
 */
public abstract class ListService<T extends Object> extends LazyDataModel<T> implements Serializable{
    private static final long serialVersionUID = -7643921510198997417L;
    
    protected static final int MAX_RESULTS = 5;
    
    @Inject
    @Web
    private EntityManager entityManager;
    
    @Inject
    protected OrganizationService organizationService;
    
    private Organization organization;
    private Long organizationId;

        @Transactional
    public Organization getOrganization() {
        if (organization == null) {
            if (organizationId == null) {
                organization = null;
            } else {
                organization = organizationService.find(getOrganizationId());
            }
        }
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void init() {
        organizationService.setEntityManager(getEntityManager());
    }
}
