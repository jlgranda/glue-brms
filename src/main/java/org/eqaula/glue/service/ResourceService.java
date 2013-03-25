/*
 * Copyright 2013 dianita.
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
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eqaula.glue.model.management.Resource;
import org.eqaula.glue.model.management.Resource_;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.PersistenceUtil;

/**
 *
 * @author dianita
 */
public class ResourceService  extends PersistenceUtil<Resource> implements Serializable{
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(BussinesEntityService.class);
    private static final long serialVersionUID = -1769350982058700578L;

    public ResourceService() {
        super(Resource.class);
    }
    
     @Override
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }
    
    public long count() {
        return count(Resource.class);
    }
    
    public List<Resource> getResources() {
        return findAll(Resource.class);
    }

    public List<Resource> getResources(final int limit, final int offset) {
        return findAll(Resource.class);
    }

    public Resource getResourceById(final Long id) {
        return (Resource) findById(Resource.class, id);
    }

    public Resource findByName(final String name) {
        log.info("find Resource with name " + name);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Resource> query = builder.createQuery(Resource.class);
        Root<Resource> bussinesEntityType = query.from(Resource.class);
        query.where(builder.equal(bussinesEntityType.get(Resource_.name), name));
        return getSingleResult(query);
    }

    public List<Resource> findByProfile(Profile profile) {
        log.info("find Resource with profile " + profile);
        if (profile == null) return new ArrayList<Resource>(0);

        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Resource> query = builder.createQuery(Resource.class);
        Root<Resource> bussinesEntityType = query.from(Resource.class);
        query.where(builder.equal(bussinesEntityType.get(Resource_.author), profile));
        return getResultList(query);
    }

    public Resource findByProfileAndCode(Profile profile, String resourceCode) {
        log.info("find Resource with profile " + profile + " resource code " + resourceCode);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Resource> query = builder.createQuery(Resource.class);
        Root<Resource> bussinesEntityType = query.from(Resource.class);
        query.where(builder.equal(bussinesEntityType.get(Resource_.author), profile));
        return getSingleResult(query);
    }

    public void create(Profile loggedIn, Resource current) {
        current.setAuthor(loggedIn);
        create(current);
    }
    
    
    
}
