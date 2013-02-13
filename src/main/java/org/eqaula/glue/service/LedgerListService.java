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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.model.accounting.Ledger;
import org.eqaula.glue.util.QueryData;
import org.eqaula.glue.util.QuerySortOrder;
import org.eqaula.glue.util.UI;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 *
 * @author jlgranda
 */
@Named
@ViewScoped
public class LedgerListService extends LazyDataModel<Ledger> {

   private static final long serialVersionUID = 4819808125494695197L;
    private static final int MAX_RESULTS = 5;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(PostingListService.class);
    @Inject
    @Web
    private EntityManager entityManager;
    @Inject
    private LedgerService ledgerService;
    private List<Ledger> resultList;
    private int firstResult = 0;
    private Ledger[] selectedLedgers;
    private Ledger selectedLedger;

    public LedgerListService() {
        setPageSize(MAX_RESULTS);
        resultList = new ArrayList<Ledger>();
    }

    public List<Ledger> getResultList() {
        if (resultList.isEmpty()) {
            resultList = ledgerService.findAll();
        }
        return resultList;
    }

    public void setResultList(List<Ledger> resultList) {
        this.resultList = resultList;
    }

    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
        this.resultList = null;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public int getNextFirstResult() {
        return firstResult + this.getPageSize();
    }

    public int getPreviousFirstResult() {
        return this.getPageSize() >= firstResult ? 0 : firstResult - this.getPageSize();
    }

    public Ledger getSelectedLedger() {
        return selectedLedger;
    }

    public void setSelectedLedger(Ledger selectedLedger) {
        this.selectedLedger = selectedLedger;
    }

    @Override
    public List<Ledger> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
        int end = first + pageSize;

        QuerySortOrder order = QuerySortOrder.ASC;
        if (sortOrder == SortOrder.DESCENDING) {
            order = QuerySortOrder.DESC;
        }
        Map<String, Object> _filters = new HashMap<String, Object>();
        /*_filters.put(BussinesEntity_.type.getName(), getType()); //Filtro por defecto
         _filters.putAll(filters);*/

        QueryData<Ledger> qData = ledgerService.find(first, end, sortField, order, _filters);
        this.setRowCount(qData.getTotalResultCount().intValue());
        return qData.getResult();
    }

    @PostConstruct
    public void init() {
        ledgerService.setEntityManager(entityManager);
    }

    public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("module.account.ledger") + " " + UI.getMessages("common.selected"), ((Ledger) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("module.account.ledger") + " " + UI.getMessages("common.unselected"), ((Ledger) event.getObject()).getName());

        FacesContext.getCurrentInstance().addMessage(null, msg);
        this.setSelectedLedger(null);
    }

    @Override
    public Ledger getRowData(String rowKey) {

        return ledgerService.findByCode(rowKey);
    }

    @Override
    public Object getRowKey(Ledger entity) {
        return entity.getName();
    }
    
}
