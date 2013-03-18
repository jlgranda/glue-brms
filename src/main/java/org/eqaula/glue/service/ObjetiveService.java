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
import org.eqaula.glue.model.management.Objetive;
import org.eqaula.glue.model.management.Objetive_;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.PersistenceUtil;

/*
 * @author dianita
 */

public class ObjetiveService extends PersistenceUtil<Objetive> implements Serializable {

    private static final long serialVersionUID = 6598014244621893145L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(BussinesEntityService.class);

    public ObjetiveService() {
        super(Objetive.class);
    }
    
    @Override
    public void setEntityManager(EntityManager em){
        this.em=em;
    }
    
    public long count(){
        return count(Objetive.class);
    }
    
    public List<Objetive> getObjetives(){
        return findAll(Objetive.class);
    }
    
    public List<Objetive> getObjetives(final int limit, final int offset) {
        return findAll(Objetive.class);
    }
    
    public Objetive getObjetiveById(final Long id) {
        return (Objetive) findById(Objetive.class, id);
    }

    public Objetive findByName(final String name) {
        log.info("find Objetive with name " + name);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Objetive> query = builder.createQuery(Objetive.class);
        Root<Objetive> bussinesEntityType = query.from(Objetive.class);
        query.where(builder.equal(bussinesEntityType.get(Objetive_.name), name));
        return getSingleResult(query);
    }

    public List<Objetive> findByProfile(Profile profile) {
        log.info("find Objetive with profile " + profile);
        if (profile == null) return new ArrayList<Objetive>(0);

        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Objetive> query = builder.createQuery(Objetive.class);
        Root<Objetive> bussinesEntityType = query.from(Objetive.class);
        query.where(builder.equal(bussinesEntityType.get(Objetive_.author), profile));
        return getResultList(query);
    }

    public Objetive findByProfileAndCode(Profile profile, String objetiveCode) {
        log.info("find Objetive with profile " + profile + " objetive code " + objetiveCode);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Objetive> query = builder.createQuery(Objetive.class);
        Root<Objetive> bussinesEntityType = query.from(Objetive.class);
        query.where(builder.equal(bussinesEntityType.get(Objetive_.author), profile));
        return getSingleResult(query);
    }

    public void create(Profile loggedIn, Objetive current) {
        current.setAuthor(loggedIn);
        create(current);
    }    
}
