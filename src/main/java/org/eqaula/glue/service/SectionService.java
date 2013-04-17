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
import org.eqaula.glue.model.management.Section;
import org.eqaula.glue.model.management.Section_;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.PersistenceUtil;

/**
 *
 * @author dianita
 */
public class SectionService extends PersistenceUtil<Section> implements Serializable{
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(BussinesEntityService.class);
    private static final long serialVersionUID = -3848045453500636478L;

    public SectionService() {
        super(Section.class);
    }
    
    @Override
    public void setEntityManager(EntityManager em){
        this.em=em;
    }
    
    public Long count(){
        return count(Section.class);
    }
    
    public List<Section> getSections() {
        return findAll(Section.class);
    }

    public List<Section> getSections(final int limit, final int offset) {
        return findAll(Section.class);
    }

    public Section getSectionById(final Long id) {
        return (Section) findById(Section.class, id);
    }
    
    public Section findByName(final String name) {
        log.info("find Section with name " + name);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Section> query = builder.createQuery(Section.class);
        Root<Section> bussinesEntityType = query.from(Section.class);
        query.where(builder.equal(bussinesEntityType.get(Section_.name), name));
        return getSingleResult(query);
    }

    public List<Section> findByProfile(Profile profile) {
        log.info("find Section with profile " + profile);
        if (profile == null) return new ArrayList<Section>(0);

        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Section> query = builder.createQuery(Section.class);
        Root<Section> bussinesEntityType = query.from(Section.class);
        query.where(builder.equal(bussinesEntityType.get(Section_.author), profile));
        return getResultList(query);
    }

    public Section findByProfileAndCode(Profile profile, String sectionCode) {
        log.info("find Section with profile " + profile + " section code " + sectionCode);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Section> query = builder.createQuery(Section.class);
        Root<Section> bussinesEntityType = query.from(Section.class);
        query.where(builder.equal(bussinesEntityType.get(Section_.author), profile));
        return getSingleResult(query);
    }
    
    public void create(Profile loggedIn, Section current) {
        current.setAuthor(loggedIn);
        create(current);
    }
    
}
