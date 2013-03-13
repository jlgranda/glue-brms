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
import org.eqaula.glue.model.management.Perspective;
import org.eqaula.glue.model.management.Perspective_;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.PersistenceUtil;

/**
 *
 * @author dianita
 */
public class PerspectiveService extends PersistenceUtil<Perspective> implements Serializable {

    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(BussinesEntityService.class);
    private static final long serialVersionUID = 4176655638515641421L;

    public PerspectiveService() {
        super(Perspective.class);
    }
    
    @Override
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }
    
    public long count() {
        return count(Perspective.class);
    }
    
    public List<Perspective> getPerspectives() {
        return findAll(Perspective.class);
    }

    public List<Perspective> getPerspectives(final int limit, final int offset) {
        return findAll(Perspective.class);
    }

    public Perspective getPerspectiveById(final Long id) {
        return (Perspective) findById(Perspective.class, id);
    }

    public Perspective findByName(final String name) {
        log.info("find Perspective with name " + name);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Perspective> query = builder.createQuery(Perspective.class);
        Root<Perspective> bussinesEntityType = query.from(Perspective.class);
        query.where(builder.equal(bussinesEntityType.get(Perspective_.name), name));
        return getSingleResult(query);
    }

    public List<Perspective> findByProfile(Profile profile) {
        log.info("find Perspective with profile " + profile);
        if (profile == null) return new ArrayList<Perspective>(0);

        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Perspective> query = builder.createQuery(Perspective.class);
        Root<Perspective> bussinesEntityType = query.from(Perspective.class);
        query.where(builder.equal(bussinesEntityType.get(Perspective_.author), profile));
        return getResultList(query);
    }

    public Perspective findByProfileAndCode(Profile profile, String perspectiveCode) {
        log.info("find Perspective with profile " + profile + " perspective code " + perspectiveCode);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Perspective> query = builder.createQuery(Perspective.class);
        Root<Perspective> bussinesEntityType = query.from(Perspective.class);
        query.where(builder.equal(bussinesEntityType.get(Perspective_.author), profile));
        return getSingleResult(query);
    }

    public void create(Profile loggedIn, Perspective current) {
        current.setAuthor(loggedIn);
        create(current);
    }
}
