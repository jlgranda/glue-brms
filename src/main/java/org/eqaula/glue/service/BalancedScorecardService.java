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
import org.eqaula.glue.model.management.BalancedScorecard;
import org.eqaula.glue.model.management.BalancedScorecard_;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.PersistenceUtil;

/**
 *
 * @author dianita
 */
public class BalancedScorecardService extends PersistenceUtil<BalancedScorecard> implements Serializable{
    private static final long serialVersionUID = -4230776645497173487L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(BussinesEntityService.class);

    public BalancedScorecardService() {
        super(BalancedScorecard.class);
    }
    
    @Override
    public void setEntityManager(EntityManager em){
        this.em=em;
    }
    
    public long count(){
        return  count(BalancedScorecard.class);
    }
    
    public List<BalancedScorecard> getBalancedScorecards(){
        return findAll(BalancedScorecard.class);
    }
    
    public List<BalancedScorecard> getBalancedScorecards(final int limit, final int offset) {
        return findAll(BalancedScorecard.class);
    }
    
    public  BalancedScorecard getBalancedScorecardsById(final Long id){
        return (BalancedScorecard) findById(BalancedScorecard.class, id);
    }
    
    public BalancedScorecard findByName(final String name){
         log.info("find BalancedScorecard with name " + name);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<BalancedScorecard> query = builder.createQuery(BalancedScorecard.class);
        Root<BalancedScorecard> bussinesEntityType = query.from(BalancedScorecard.class);
        query.where(builder.equal(bussinesEntityType.get(BalancedScorecard_.name), name));
        return getSingleResult(query);
    }
    
    public List<BalancedScorecard> findByProfile(Profile profile) {
        log.info("find BalancedScorecard with profile " + profile);
        if (profile == null) return new ArrayList<BalancedScorecard>(0);

        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<BalancedScorecard> query = builder.createQuery(BalancedScorecard.class);
        Root<BalancedScorecard> bussinesEntityType = query.from(BalancedScorecard.class);
        query.where(builder.equal(bussinesEntityType.get(BalancedScorecard_.author), profile));
        return getResultList(query);
    }

    public BalancedScorecard findByProfileAndCode(Profile profile, String balancedScorecardCode) {
        log.info("find BalancedScorecard with profile " + profile + " BalancedScorecard code " + balancedScorecardCode);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<BalancedScorecard> query = builder.createQuery(BalancedScorecard.class);
        Root<BalancedScorecard> bussinesEntityType = query.from(BalancedScorecard.class);
        query.where(builder.equal(bussinesEntityType.get(BalancedScorecard_.author), profile));
        return getSingleResult(query);
    }

    public void create(Profile loggedIn, BalancedScorecard current) {
        current.setAuthor(loggedIn);
        create(current);
    }
}
