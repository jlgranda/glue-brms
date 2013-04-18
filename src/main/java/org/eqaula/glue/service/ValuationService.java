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
import org.eqaula.glue.model.management.Valuation;
import org.eqaula.glue.model.management.Valuation_;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.PersistenceUtil;

/**
 *
 * @author dianita
 */
public class ValuationService extends PersistenceUtil<Valuation> implements Serializable{
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(BussinesEntityService.class);
    private static final long serialVersionUID = -1765207040695993093L;

    public ValuationService() {
        super(Valuation.class);
    }
    
     @Override
    public void setEntityManager(EntityManager em){
        this.em=em;
    }
    
    public Long count(){
        return count(Valuation.class);
    }
    
    public List<Valuation> getValuations() {
        return findAll(Valuation.class);
    }

    public List<Valuation> getValuations(final int limit, final int offset) {
        return findAll(Valuation.class);
    }

    public Valuation getValuationById(final Long id) {
        return (Valuation) findById(Valuation.class, id);
    }
    
    public Valuation findByName(final String name) {
        log.info("find Valuation with name " + name);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Valuation> query = builder.createQuery(Valuation.class);
        Root<Valuation> bussinesEntityType = query.from(Valuation.class);
        query.where(builder.equal(bussinesEntityType.get(Valuation_.name), name));
        return getSingleResult(query);
    }

    public List<Valuation> findByProfile(Profile profile) {
        log.info("find Valuation with profile " + profile);
        if (profile == null) return new ArrayList<Valuation>(0);

        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Valuation> query = builder.createQuery(Valuation.class);
        Root<Valuation> bussinesEntityType = query.from(Valuation.class);
        query.where(builder.equal(bussinesEntityType.get(Valuation_.author), profile));
        return getResultList(query);
    }

    public Valuation findByProfileAndCode(Profile profile, String valuationCode) {
        log.info("find Valuation with profile " + profile + " valuation code " + valuationCode);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Valuation> query = builder.createQuery(Valuation.class);
        Root<Valuation> bussinesEntityType = query.from(Valuation.class);
        query.where(builder.equal(bussinesEntityType.get(Valuation_.author), profile));
        return getSingleResult(query);
    }
    
    public void create(Profile loggedIn, Valuation current) {
        current.setAuthor(loggedIn);
        create(current);
    }
    
}
