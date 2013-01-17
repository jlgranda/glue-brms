/*
 * Copyright 2013 lucho.
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
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eqaula.glue.model.stocklist.Stock;
import org.eqaula.glue.model.stocklist.Stock_;
import org.eqaula.glue.util.PersistenceUtil;

/**
 *
 * @author lucho
 */
public class StockService extends PersistenceUtil<Stock> implements Serializable {

    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(BussinesEntityService.class);

    public StockService() {
        super(Stock.class);
    }

    @Override
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    public Stock getStockById(final Long id) {
        return (Stock) findById(Stock.class, id);
    }

    public List<Stock> getStock(final int limit, final int offset) {
        return findAll(Stock.class);
    }

    public Stock findByName(final String name) {
        log.info("find Profile with name " + name);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Stock> query = builder.createQuery(Stock.class);
        Root<Stock> bussinesEntityType = query.from(Stock.class);
        query.where(builder.equal(bussinesEntityType.get(Stock_.name), name));
        return getSingleResult(query);
    }
}
