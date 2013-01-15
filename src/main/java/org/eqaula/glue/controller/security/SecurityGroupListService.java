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
package org.eqaula.glue.controller.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.eqaula.glue.accounting.AccountService;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.model.accounting.Account;
import org.eqaula.glue.model.management.Organization;
import org.eqaula.glue.service.AccountListService;
import org.eqaula.glue.util.QueryData;
import org.eqaula.glue.util.QuerySortOrder;
import org.eqaula.glue.util.UI;
import org.picketlink.idm.api.Group;
import org.picketlink.idm.common.exception.IdentityException;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 *
 * @author cesar
 */
@Named
@RequestScoped
public class SecurityGroupListService extends LazyDataModel<Group> {

    private static final long serialVersionUID = 4819808125494695197L;
    private static final int MAX_RESULTS = 5;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(SecurityGroupListService.class);
    @Inject
    @Web
    private EntityManager entityManager;
    @Inject
    private SecurityGroupService securityGroupService;
    private List<Group> resultList;
    private int firstResult = 0;
    private Group[] selectedGroups;
    private Group selectedGroup;

    public SecurityGroupListService() {
        setPageSize(MAX_RESULTS);
        resultList = new ArrayList<Group>();
    }

    public List<Group> getResultList() {
        if (resultList.isEmpty() /*&& getSelectedBussinesEntityType() != null*/) {
            resultList = securityGroupService.getGroups(this.getPageSize(), firstResult);
            log.info("eqaula --> resultlist " + resultList);
        }
        return resultList;

    }

    public void setResultList(List<Group> resultList) {
        this.resultList = resultList;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(int firstResult) {

        this.firstResult = firstResult;
        this.resultList = null;
    }

    public Group getSelectedGroup() {         
        return selectedGroup;
    }

    public void setSelectedGroup(Group selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    public int getNextFirstResult() {
        return firstResult + this.getPageSize();
    }

    public int getPreviousFirstResult() {
        return this.getPageSize() >= firstResult ? 0 : firstResult - this.getPageSize();
    }

    @PostConstruct
    public void init() {
        log.info("Setup entityManager into GroupService...");
        securityGroupService.setEntityManager(entityManager);
    }
    
    public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("Group") + " " + UI.getMessages("common.selected"), ((Group) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("Group") + " " + UI.getMessages("common.unselected"), ((Group) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
        this.setSelectedGroup(null);
    }
    @Override
    public Group getRowData(String rowKey) {
        Group p = null;
        try {         
            p =  securityGroupService.findByName(rowKey);
        } catch (IdentityException ex) {
            log.info("Error "+ex);
        }        
        return p;
    }

    @Override
    public Object getRowKey(Group entity) {
        return entity.getName();
    }

    @Override
    public List<Group> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
         log.info("Eqaula-->  ingreso a buscar Grupos");
        int end = first + pageSize;
        QuerySortOrder order = QuerySortOrder.ASC;
        if (sortOrder == SortOrder.DESCENDING) {
            order = QuerySortOrder.DESC;
        }
        Map<String, Object> _filters = new HashMap<String, Object>();
        /*_filters.put(BussinesEntity_.type.getName(), getType()); //Filtro por defecto
         _filters.putAll(filters);*/      
        
        //TODO revisar metodo en vista si me 
        log.info("Eqaula-->  lista de lazy Group "+securityGroupService.getGroups(pageSize, first).toString());
        return securityGroupService.getGroups(pageSize, first);
    }
}
