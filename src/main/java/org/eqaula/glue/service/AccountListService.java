/*
 * Copyright 2013 cesar.
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
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.eqaula.glue.accounting.AccountService;
import org.eqaula.glue.model.accounting.Account;
import org.eqaula.glue.model.accounting.Account.Type;
import org.eqaula.glue.model.accounting.Account_;
import org.eqaula.glue.util.QueryData;
import org.eqaula.glue.util.QuerySortOrder;
import org.eqaula.glue.util.UI;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.SortOrder;

/**
 *
 * @author cesar
 */
@Named
@RequestScoped
public class AccountListService extends ListService<Account> {

    private static final long serialVersionUID = 4819808125494695197L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(AccountListService.class);
    
    @Inject
    private AccountService accountService;
    private List<Account> resultList;
    private int firstResult = 0;
    private Account[] selectedAccounts;
    private Account selectedAccount;

    public AccountListService() {
        setPageSize(MAX_RESULTS);
        resultList = new ArrayList<Account>();
    }

    public List<Account> getResultList() {
        if (resultList.isEmpty()) {
            resultList = accountService.getAccounts(this.getPageSize(), firstResult);
        }
        return resultList;
    }

    public void setResultList(List<Account> resultList) {
        this.resultList = resultList;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
        this.resultList = null;
    }

    public Account getSelectedAccount() {
        return selectedAccount;
    }

    public void setSelectedAccount(Account selectedAccount) {
        this.selectedAccount = selectedAccount;
    }

    public int getNextFirstResult() {
        return firstResult + this.getPageSize();
    }

    public int getPreviousFirstResult() {
        return this.getPageSize() >= firstResult ? 0 : firstResult - this.getPageSize();
    }

    @Override
    public List<Account> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
         int end = first + pageSize;
        QuerySortOrder order = QuerySortOrder.ASC;
        if (sortOrder == SortOrder.DESCENDING) {
            order = QuerySortOrder.DESC;
        }
        Map<String, Object> _filters = new HashMap<String, Object>();
        _filters.put(Account_.accountType.getName(), Type.SCHEMA); //Filtro por defecto
        _filters.put(Account_.organization.getName(), getOrganization());
        _filters.putAll(filters);

        if (sortField==null){
            sortField = "code";
        }
        
        QueryData<Account> qData = accountService.find(first, end, sortField, order, _filters);
        this.setRowCount(qData.getTotalResultCount().intValue());
        return qData.getResult();
    }
    
    @PostConstruct
    @Override
    public void init() {
         super.init();
        accountService.setEntityManager(getEntityManager());         
    }

    public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("Account") + " " + UI.getMessages("common.selected"), ((Account) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("Account") + " " + UI.getMessages("common.unselected"), ((Account) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
        this.setSelectedAccount(null);
    }
    @Override
    public Account getRowData(String rowKey) {
        return accountService.getAccountByName(rowKey);         
    }

    @Override
    public Object getRowKey(Account entity) {
        return entity.getName();
    }
}
