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
import org.eqaula.glue.model.management.Scale;
import org.eqaula.glue.model.management.Scale_;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.PersistenceUtil;

/*
 * @author dianita
 */

public class ScaleService extends PersistenceUtil<Scale> implements Serializable {
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(BussinesEntityService.class);
    private static final long serialVersionUID = -7772081791640843376L;

    public ScaleService() {
        super(Scale.class);
    }
     @Override
    public void setEntityManager(EntityManager em){
        this.em=em;
    }
    
    public Long count(){
        return count(Scale.class);
    }
    
    public List<Scale> getScales() {
        return findAll(Scale.class);
    }

    public List<Scale> getScales(final int limit, final int offset) {
        return findAll(Scale.class);
    }

    public Scale getScaleById(final Long id) {
        return (Scale) findById(Scale.class, id);
    }
    
    public Scale findByName(final String name) {
        log.info("find Scale with name " + name);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Scale> query = builder.createQuery(Scale.class);
        Root<Scale> bussinesEntityType = query.from(Scale.class);
        query.where(builder.equal(bussinesEntityType.get(Scale_.name), name));
        return getSingleResult(query);
    }

    public List<Scale> findByProfile(Profile profile) {
        log.info("find Scale with profile " + profile);
        if (profile == null) return new ArrayList<Scale>(0);

        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Scale> query = builder.createQuery(Scale.class);
        Root<Scale> bussinesEntityType = query.from(Scale.class);
        query.where(builder.equal(bussinesEntityType.get(Scale_.author), profile));
        return getResultList(query);
    }

    public Scale findByProfileAndCode(Profile profile, String scaleCode) {
        log.info("find Scale with profile " + profile + " scale code " + scaleCode);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Scale> query = builder.createQuery(Scale.class);
        Root<Scale> bussinesEntityType = query.from(Scale.class);
        query.where(builder.equal(bussinesEntityType.get(Scale_.author), profile));
        return getSingleResult(query);
    }
    
    public void create(Profile loggedIn, Scale current) {
        current.setAuthor(loggedIn);
        create(current);
    }
    
}
