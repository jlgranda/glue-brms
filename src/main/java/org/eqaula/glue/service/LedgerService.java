/*
 * Copyright 2013 jlgranda.
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eqaula.glue.model.accounting.Ledger;
import org.eqaula.glue.model.accounting.Ledger_;
import org.eqaula.glue.util.Dates;
import org.eqaula.glue.util.PersistenceUtil;
import org.eqaula.glue.util.UI;
import org.jboss.seam.transaction.Transactional;

/**
 *
 * @author jlgranda
 */
public class LedgerService extends PersistenceUtil<Ledger> implements Serializable {
    private static final long serialVersionUID = 4591338293144180367L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(LedgerService.class);

    public LedgerService() {
        super(Ledger.class);
    }

    public Ledger getLedgerById(final Long id) {
        return (Ledger) findById(Ledger.class, id);
    }
   
    public Ledger findByCode(final String code) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Ledger> query = builder.createQuery(Ledger.class);
        Root<Ledger> bussinesEntityType = query.from(Ledger.class);
        query.where(builder.equal(bussinesEntityType.get(Ledger_.code), code));
        System.out.println("Estoy en find code");
        return getSingleResult(query);
    }
    
    @Transactional
    public Ledger retrivePosting(String code){
        Ledger p = findByCode(code);
        if (p == null) {
            p = createInstance(code);
            this.create(p);
        }
        return p;
    }
    
    protected Ledger createInstance(String code) {
        Date now = Calendar.getInstance().getTime();
        Ledger ledger = new Ledger();
        ledger.setCode(code);
        ledger.setName(UI.getMessages("module.accounting.ledger") + " " + code);
        ledger.setCreatedOn(now);
        ledger.setLastUpdate(now);
        ledger.setActivationTime(now);
        ledger.setExpirationTime(Dates.addDays(now, 364));
       return ledger;
    }

    @Override
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }
    
    public List<Ledger> findAll(){
        return super.findAll(Ledger.class);
    }

}
