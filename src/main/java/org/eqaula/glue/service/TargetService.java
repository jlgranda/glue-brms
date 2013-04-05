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

import org.eqaula.glue.model.management.Target;
import org.eqaula.glue.model.management.Target_;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.PersistenceUtil;

/**
 *
 * @author dianita
 */


public class TargetService extends PersistenceUtil<Target> implements Serializable{
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(BussinesEntityService.class);
    private static final long serialVersionUID = -5325081442738873666L;

    public TargetService() {
        super(Target.class);
    }
    
    @Override
    public void setEntityManager(EntityManager em){
        this.em=em;
    }
    
    public Long count(){
        return count(Target.class);
    }
    
    public List<Target> getTargets() {
        return findAll(Target.class);
    }

    public List<Target> getTargets(final int limit, final int offset) {
        return findAll(Target.class);
    }

    public Target getTargetById(final Long id) {
        return (Target) findById(Target.class, id);
    }
    
    public Target findByName(final String name) {
        log.info("find Target with name " + name);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Target> query = builder.createQuery(Target.class);
        Root<Target> bussinesEntityType = query.from(Target.class);
        query.where(builder.equal(bussinesEntityType.get(Target_.name), name));
        return getSingleResult(query);
    }

    public List<Target> findByProfile(Profile profile) {
        log.info("find Target with profile " + profile);
        if (profile == null) return new ArrayList<Target>(0);

        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Target> query = builder.createQuery(Target.class);
        Root<Target> bussinesEntityType = query.from(Target.class);
        query.where(builder.equal(bussinesEntityType.get(Target_.author), profile));
        return getResultList(query);
    }

    public Target findByProfileAndCode(Profile profile, String targetCode) {
        log.info("find Target with profile " + profile + " target code " + targetCode);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Target> query = builder.createQuery(Target.class);
        Root<Target> bussinesEntityType = query.from(Target.class);
        query.where(builder.equal(bussinesEntityType.get(Target_.author), profile));
        return getSingleResult(query);
    }
    
    public void create(Profile loggedIn, Target current) {
        current.setAuthor(loggedIn);
        create(current);
    }
    
    
}
