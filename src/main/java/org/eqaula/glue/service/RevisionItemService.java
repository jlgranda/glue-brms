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
import org.eqaula.glue.model.management.Question;
import org.eqaula.glue.model.management.RevisionItem;
import org.eqaula.glue.model.management.RevisionItem_;
import org.eqaula.glue.model.management.Scale;
import org.eqaula.glue.model.management.Theme;
import org.eqaula.glue.model.management.Theme_;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.PersistenceUtil;

/*
 * @author dianita
 */
public class RevisionItemService extends PersistenceUtil<RevisionItem> implements Serializable {

    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(BussinesEntityService.class);
    private static final long serialVersionUID = 8797010243353074851L;

    public RevisionItemService() {
        super(RevisionItem.class);
    }

    @Override
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }
    
    public long count() {
        return count(RevisionItem.class);
    }
    
    public List<RevisionItem> getRevisionItems() {
        return findAll(RevisionItem.class);
    }

    public List<RevisionItem> getRevisionItems(final int limit, final int offset) {
        return findAll(RevisionItem.class);
    }

    public RevisionItem getRevisionItemById(final Long id) {
        return (RevisionItem) findById(RevisionItem.class, id);
    }

    public RevisionItem findByName(final String name) {
        log.info("find RevisionItem with name " + name);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<RevisionItem> query = builder.createQuery(RevisionItem.class);
        Root<RevisionItem> bussinesEntityType = query.from(RevisionItem.class);
        query.where(builder.equal(bussinesEntityType.get(RevisionItem_.name), name));
        return getSingleResult(query);
    }

    public List<RevisionItem> findByProfile(Profile profile) {
        log.info("find RevisionItem with profile " + profile);
        if (profile == null) return new ArrayList<RevisionItem>(0);

        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<RevisionItem> query = builder.createQuery(RevisionItem.class);
        Root<RevisionItem> bussinesEntityType = query.from(RevisionItem.class);
        query.where(builder.equal(bussinesEntityType.get(RevisionItem_.author), profile));
        return getResultList(query);
    }

    public RevisionItem findByProfileAndCode(Profile profile, String revisionItemCode) {
        log.info("find RevisionItem with profile " + profile + " resource code " + revisionItemCode);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<RevisionItem> query = builder.createQuery(RevisionItem.class);
        Root<RevisionItem> bussinesEntityType = query.from(RevisionItem.class);
        query.where(builder.equal(bussinesEntityType.get(RevisionItem_.author), profile));
        return getSingleResult(query);
    }

   
    public List<RevisionItem> findByQuestion(Question question) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<RevisionItem> query = builder.createQuery(RevisionItem.class);
        Root<RevisionItem> bussinesEntityType = query.from(RevisionItem.class);
        query.where(builder.equal(bussinesEntityType.get(RevisionItem_.question),question));
        return getResultList(query);
    }
    
     public List<RevisionItem> findByScale(Scale scale) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<RevisionItem> query = builder.createQuery(RevisionItem.class);
        Root<RevisionItem> bussinesEntityType = query.from(RevisionItem.class);
        query.where(builder.equal(bussinesEntityType.get(RevisionItem_.scale),scale));
        return getResultList(query);
    }
    
     public void create(Profile loggedIn, RevisionItem current) {
        current.setAuthor(loggedIn);
        create(current);
    }
    
}
