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

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eqaula.glue.model.config.Frequency;
import org.eqaula.glue.model.config.Frequency_;
import org.eqaula.glue.util.PersistenceUtil;

/*
 * @author dianita
 */

public class FrequencyService extends PersistenceUtil<Frequency>{
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(BussinesEntityService.class);
    private static final long serialVersionUID = -6519908119365822973L;

    public FrequencyService() {
        super(Frequency.class);
    }
    @Override
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    public Frequency getFrequencyById(final Long id) {
        return (Frequency) findById(Frequency.class, id);
    }

    public List<Frequency> getFrequencies(final int limit, final int offset) {
        return findAll(Frequency.class);
    }

    public Frequency findByName(final String name) {
        log.info("find Profile with name " + name);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Frequency> query = builder.createQuery(Frequency.class);
        Root<Frequency> bussinesEntityType = query.from(Frequency.class);
        query.where(builder.equal(bussinesEntityType.get(Frequency_.name), name));
        return getSingleResult(query);
    }

    
     public Frequency getFrequencyByName(final String name) throws NoResultException {
        TypedQuery<Frequency> query = em.createQuery("SELECT p FROM Frequency p WHERE p.name = :name", Frequency.class);
        query.setParameter("name", name);
        Frequency result = query.getSingleResult();
        return result;
    }
    
    public boolean isNameAvailable(String name) {
        try {
           Frequency b = getFrequencyByName(name);
            if(b != null){                
                return false;
            }else{                
                return true;
            }
        } catch (NoResultException e) {
            return true;
        }
    }    
}
