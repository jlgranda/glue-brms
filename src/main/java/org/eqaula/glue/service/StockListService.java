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

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.model.stocklist.Stock;
import org.eqaula.glue.model.stocklist.Stock_;
import org.eqaula.glue.util.QueryData;
import org.eqaula.glue.util.QuerySortOrder;
import org.eqaula.glue.util.UI;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 *
 * @author lucho
 */
@Named(value = "stockListService")
@RequestScoped
public class StockListService extends ListService<Stock> {

    private static final long serialVersionUID = 4819808125494695197L;
    private static final int MAX_RESULTS = 5;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(StockListService.class);
    @Inject
    @Web
    private EntityManager entityManager;
    @Inject
    private StockService stockService;
    private List<Stock> resultList;
    private int firstResult = 0;
    private Stock[] selectedStocks;
    private Stock selectedStock;

    public StockListService() {
        setPageSize(MAX_RESULTS);
        resultList = new ArrayList<Stock>();
        //log.info("Service initialized!");
    }

    public List<Stock> getResultList() {
        log.info("load BussinesEntityType");
        if (resultList.isEmpty() /*&& getSelectedBussinesEntityType() != null*/) {
            resultList = stockService.getStock(this.getPageSize(), firstResult);
            log.info("eqaula --> resultlist " + resultList);
        }
        return resultList;
    }

    public void setResultList(List<Stock> resultList) {
        this.resultList = resultList;
    }

    public void setFirstResult(int firstResult) {
        log.info("set first result + firstResult");
        this.firstResult = firstResult;
        this.resultList = null;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public Stock getSelectedStock() {
        return selectedStock;
    }

    public void setSelectedStock(Stock selectedStock) {
        this.selectedStock = selectedStock;
    }

    public int getNextFirstResult() {
        return firstResult + this.getPageSize();
    }

    public int getPreviousFirstResult() {
        return this.getPageSize() >= firstResult ? 0 : firstResult - this.getPageSize();
    }

    @Override
    public List<Stock> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        int end = first + pageSize;

        QuerySortOrder order = QuerySortOrder.ASC;
        if (sortOrder == SortOrder.DESCENDING) {
            order = QuerySortOrder.DESC;
        }
        Map<String, Object> _filters = new HashMap<String, Object>();
       
        _filters.put(Stock_.organization.getName(), getOrganization());
        _filters.putAll(filters);
       

        QueryData<Stock> qData = stockService.find(first, end, sortField, order, _filters);
        this.setRowCount(qData.getTotalResultCount().intValue());
        return qData.getResult();
    }

    /**
     *
     */
    @PostConstruct
    @Override
    public void init() {
        super.init();
        log.info("Setup entityManager into StockService...");
        stockService.setEntityManager(entityManager);
    }

    public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("module.stocklist.warehouse") + " " + UI.getMessages("common.selected"), ((Stock) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("module.stocklist.warehouse") + " " + UI.getMessages("common.unselected"), ((Stock) event.getObject()).getName());

        FacesContext.getCurrentInstance().addMessage(null, msg);
        this.setSelectedStock(null);
    }

    @Override
    public Stock getRowData(String rowKey) {

        return stockService.findByName(rowKey);
    }

    @Override
    public Object getRowKey(Stock entity) {
        return entity.getName();
    }
}
