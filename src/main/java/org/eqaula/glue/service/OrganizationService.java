/*
 * Copyright 2012 cesar.
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
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eqaula.glue.model.management.Organization;
import org.eqaula.glue.model.management.Organization_;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.PersistenceUtil;

/**
 *
 * @author cesar
 */
public class OrganizationService extends PersistenceUtil<Organization> implements Serializable {

    private static final long serialVersionUID = 6569835981443699931L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(BussinesEntityService.class);

    public OrganizationService() {
        super(Organization.class);
    }

    @Override
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    //metodo count
    public long count() {
        return count(Organization.class);
    }

    public List<Organization> getOrganizations() {
        return findAll(Organization.class);
    }
    
    public List<Organization> getOrganizations(final int limit, final int offset) {
        return findAll(Organization.class);
    }
    
    public Organization getOrganizationById(final Long id) {
        return (Organization) findById(Organization.class, id);
    }
    
    public Organization findByName(final String name) {
        log.info("find Organization with name " + name);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Organization> query = builder.createQuery(Organization.class);
        Root<Organization> bussinesEntityType = query.from(Organization.class);
        query.where(builder.equal(bussinesEntityType.get(Organization_.name), name));
        return getSingleResult(query);
    }

    public List<Organization> findByProfile(Profile profile) {
        log.info("find Organization with profile " + profile );
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Organization> query = builder.createQuery(Organization.class);
        Root<Organization> bussinesEntityType = query.from(Organization.class);
        query.where(builder.equal(bussinesEntityType.get(Organization_.author), profile));
        //TODO agregar busqueda por organizationCode
        return getResultList(query);
    }
    
    public Organization findByProfileAndCode(Profile profile, String organizationCode) {
        log.info("find Organization with profile " + profile + " organization code " + organizationCode);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Organization> query = builder.createQuery(Organization.class);
        Root<Organization> bussinesEntityType = query.from(Organization.class);
        query.where(builder.equal(bussinesEntityType.get(Organization_.author), profile));
        //TODO agregar busqueda por organizationCode
        return getSingleResult(query);
    }

    public void create(Profile loggedIn, Organization current) {
        current.setAuthor(loggedIn);
        create(current);
    }
}
