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
import org.eqaula.glue.model.management.Measure;
import org.eqaula.glue.model.management.Measure_;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.PersistenceUtil;

/**
 *
 * @author dianita
 */
public class MeasureService extends PersistenceUtil<Measure> implements Serializable {

    private static final long serialVersionUID = -524387471932589115L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(BussinesEntityService.class);

    public MeasureService() {
        super(Measure.class);
    }

    @Override
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    public long count() {
        return count(Measure.class);
    }

    public List<Measure> getMeasures() {
        return findAll(Measure.class);
    }

    public List<Measure> getMeasures(final int limit, final int offset) {
        return findAll(Measure.class);
    }
    
    public Measure getMeasureById(final Long id) {
        return (Measure) findById(Measure.class, id);
    }

    public Measure findByName(final String name) {
        log.info("find Measure with name " + name);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Measure> query = builder.createQuery(Measure.class);
        Root<Measure> bussinesEntityType = query.from(Measure.class);
        query.where(builder.equal(bussinesEntityType.get(Measure_.name), name));
        return getSingleResult(query);
    }

    public List<Measure> findByProfile(Profile profile) {
        log.info("find Measure with profile " + profile);
        if (profile == null) return new ArrayList<Measure>(0);

        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Measure> query = builder.createQuery(Measure.class);
        Root<Measure> bussinesEntityType = query.from(Measure.class);
        query.where(builder.equal(bussinesEntityType.get(Measure_.author), profile));
        return getResultList(query);
    }

    public Measure findByProfileAndCode(Profile profile, String measureCode) {
        log.info("find Measure with profile " + profile + " measure code " + measureCode);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Measure> query = builder.createQuery(Measure.class);
        Root<Measure> bussinesEntityType = query.from(Measure.class);
        query.where(builder.equal(bussinesEntityType.get(Measure_.author), profile));
        return getSingleResult(query);
    }

    public void create(Profile loggedIn, Measure current) {
        current.setAuthor(loggedIn);
        create(current);
    }    
}
