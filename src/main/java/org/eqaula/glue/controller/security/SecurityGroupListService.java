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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.ListDataModel;
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
import org.primefaces.model.SelectableDataModel;
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
    private String groupName;

    public SecurityGroupListService() {        
        setPageSize(MAX_RESULTS); 
        resultList = new ArrayList<Group>();
    }

    public SecurityGroupListService(List<Group> groups) {
        this.resultList = groups;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<Group> getResultList() {
        return resultList;
    }

    public void setResultList(List<Group> resultList) {
        this.resultList = resultList;
    }

    public int getNextFirstResult() {
        return firstResult + this.getPageSize();
    }

    public int getPreviousFirstResult() {
        return this.getPageSize() >= firstResult ? 0 : firstResult - this.getPageSize();
    }

    public int getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(int firstResult) {
        log.info("set first result + firstResult");
        this.firstResult = firstResult;
        this.resultList = null;
    }

    public Group getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(Group selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    public void assignGroups(List<Group> g){        
        if (g.isEmpty() /*&& getSelectedBussinesEntityType() != null*/) {
            g = securityGroupService.getGroups();
            log.info("eqaula --> resultlist " + resultList);
        }
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
        for (Group group : this.resultList) {
            if (group.getName().equals(rowKey)) {
                return group;
            }
        }
        return p;
    }

    @Override
    public Object getRowKey(Group entity) {
        return entity.getName();
    }

    @Override
    public List<Group> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
        List<Group> data = new ArrayList<Group>();
        assignGroups(this.resultList);
        //filter  
        for (Group group : this.resultList) {
            boolean match = true;

            for (Iterator<String> it = filters.keySet().iterator(); it.hasNext();) {
                try {
                    String filterProperty = it.next();
                    String filterValue = filters.get(filterProperty);
                    String fieldValue = String.valueOf(group.getClass().getField(filterProperty).get(group));

                    if (filterValue == null || fieldValue.startsWith(filterValue)) {
                        match = true;
                    } else {
                        match = false;
                        break;
                    }
                } catch (Exception e) {
                    match = false;
                }
            }

            if (match) {
                data.add(group);
            }
        }

//        //sort  
//        if(sortField != null) {  
//            Collections.sort(data, new LazySorter(sortField, sortOrder));  
//        }  

        //rowCount  
        int dataSize = data.size();
        this.setRowCount(dataSize);

        //paginate  
        if (dataSize > pageSize) {
            try {
                return data.subList(first, first + pageSize);
            } catch (IndexOutOfBoundsException e) {
                return data.subList(first, first + (dataSize % pageSize));
            }
        } else {
            return data;
        }
    }
}
