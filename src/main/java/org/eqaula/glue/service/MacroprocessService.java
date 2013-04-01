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
import org.eqaula.glue.model.management.Macroprocess;
import org.eqaula.glue.model.management.Macroprocess_;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.PersistenceUtil;

/*
 * @author dianita
 */

public class MacroprocessService extends PersistenceUtil<Macroprocess> implements Serializable{
    private static final long serialVersionUID = 5073342894331902089L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(BussinesEntityService.class);

    public MacroprocessService() {
        super(Macroprocess.class);
    }
     @Override
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    public long count() {
        return count(Macroprocess.class);
    }

    public List<Macroprocess> getMacroprocess() {
        return findAll(Macroprocess.class);
    }

    public List<Macroprocess> getMacroprocess(final int limit, final int offset) {
        return findAll(Macroprocess.class);
    }
    
    public Macroprocess getMacroprocessById(final Long id) {
        return (Macroprocess) findById(Macroprocess.class, id);
    }

    public Macroprocess findByName(final String name) {
        log.info("find Macroprocess with name " + name);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Macroprocess> query = builder.createQuery(Macroprocess.class);
        Root<Macroprocess> bussinesEntityType = query.from(Macroprocess.class);
        query.where(builder.equal(bussinesEntityType.get(Macroprocess_.name), name));
        return getSingleResult(query);
    }

    public List<Macroprocess> findByProfile(Profile profile) {
        log.info("find Macroprocess with profile " + profile);
        if (profile == null) return new ArrayList<Macroprocess>(0);

        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Macroprocess> query = builder.createQuery(Macroprocess.class);
        Root<Macroprocess> bussinesEntityType = query.from(Macroprocess.class);
        query.where(builder.equal(bussinesEntityType.get(Macroprocess_.author), profile));
        return getResultList(query);
    }

    public Macroprocess  findByProfileAndCode(Profile profile, String macroprocessCode) {
        log.info("find Macroprocess with profile " + profile + " macroprocess code " + macroprocessCode);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Macroprocess> query = builder.createQuery(Macroprocess.class);
        Root<Macroprocess> bussinesEntityType = query.from(Macroprocess.class);
        query.where(builder.equal(bussinesEntityType.get(Macroprocess_.author), profile));
        return getSingleResult(query);
    }

    public void create(Profile loggedIn, Macroprocess current) {
        current.setAuthor(loggedIn);
        create(current);
    }    
    
}
