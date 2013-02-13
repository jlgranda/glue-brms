/*
 * Copyright 2013 Hp.
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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eqaula.glue.model.accounting.Posting;
import org.eqaula.glue.model.accounting.Posting_;
import org.eqaula.glue.model.config.Setting;
import org.eqaula.glue.model.config.Setting_;
import org.eqaula.glue.util.Dates;
import org.eqaula.glue.util.PersistenceUtil;

/**
 *
 * @author Luis Flores
 */
public class PostingService extends PersistenceUtil<Posting> {

    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(PostingService.class);
    private static final long serialVersionUID = -2654253198159918622L;

    public PostingService() {
        super(Posting.class);
    }

    @Override
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    public Posting getPostingById(final Long id) {
        return (Posting) findById(Posting.class, id);
    }
    
    public Posting findByName(final String name) {
        log.info("find Profile with name " + name);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Posting> query = builder.createQuery(Posting.class);
        Root<Posting> bussinesEntityType = query.from(Posting.class);
        query.where(builder.equal(bussinesEntityType.get(Posting_.name), name));
        return getSingleResult(query);
    }
    
    public List<Posting> findAll(){
        return super.findAll(Posting.class);
    }
}
