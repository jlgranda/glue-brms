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
import org.eqaula.glue.model.management.Owner;
import org.eqaula.glue.model.management.Owner_;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.PersistenceUtil;

/**
 *
 * @author dianita
 */
public class OwnerService extends PersistenceUtil<Owner> implements Serializable{
    private static final long serialVersionUID = 8552261011129814047L;
    private static final org.jboss.solder.logging.Logger LOG = org.jboss.solder.logging.Logger.getLogger(BussinesEntityService.class);

    public OwnerService() {
        super(Owner.class);
    }
    
    @Override
    public void setEntityManager(EntityManager em){
        this.em=em;
    }
    
    public Long count(){
        return count(Owner.class);
    }
    
    public List<Owner> getOwners(){
        return findAll(Owner.class);
    }
    
    public List<Owner> getOwners(final int limit, final int offset){
        return findAll(Owner.class);
    }
    
    public Owner getOwerById(final Long id){
        return (Owner)findById(Owner.class, id);
    }
            
    public Owner findByName(final String name){
        LOG.info("find Owner with name "+ name);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Owner> query = builder.createQuery(Owner.class);
        Root<Owner> bussinesEntityType= query.from(Owner.class);
        query.where(builder.equal(bussinesEntityType.get(Owner_.name), name));
        return getSingleResult(query);
    }
    
    public List<Owner> findByProfile(Profile profile) {
        LOG.info("find Owner with profile " + profile);
        if (profile == null) return new ArrayList<Owner>(0);

        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Owner> query = builder.createQuery(Owner.class);
        Root<Owner> bussinesEntityType = query.from(Owner.class);
        query.where(builder.equal(bussinesEntityType.get(Owner_.author), profile));
        //TODO agregar busqueda por organizationCode
        return getResultList(query);
    }

    public Owner findByProfileAndCode(Profile profile, String ownerCode) {
        LOG.info("find Owner with profile " + profile + " owner code " + ownerCode);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Owner> query = builder.createQuery(Owner.class);
        Root<Owner> bussinesEntityType = query.from(Owner.class);
        query.where(builder.equal(bussinesEntityType.get(Owner_.author), profile));
        //TODO agregar busqueda por organizationCode
        return getSingleResult(query);
    }

    public void create(Profile loggedIn, Owner current) {
        current.setAuthor(loggedIn);
        create(current);
    }
}
