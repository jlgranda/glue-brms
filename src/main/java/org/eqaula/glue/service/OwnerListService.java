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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.management.OwnerHome;
import org.eqaula.glue.model.BussinesEntity_;
import org.eqaula.glue.model.management.Owner;
import org.eqaula.glue.model.management.Owner_;
import org.eqaula.glue.util.QueryData;
import org.eqaula.glue.util.QuerySortOrder;
import org.eqaula.glue.util.UI;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.SortOrder;

/**
 *
 * @author dianita
 */
@Named
@RequestScoped
public class OwnerListService extends ListService<Owner> {

    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(OwnerListService.class);
    private static final long serialVersionUID = 7779055498085217448L;
    @Inject
    private OwnerService ownerService;
    private List<Owner> resultList;
    private int firstResult = 0;
    private Owner[] selectedOwners;
    private Owner selectedOwner;

    public OwnerListService() {
        setPageSize(MAX_RESULTS);
        resultList = new ArrayList<Owner>();
    }

    public List<Owner> getResultList() {
        if (resultList.isEmpty()) {
            resultList = ownerService.getOwners();
        }
        return resultList;
    }

    public void setResultList(List<Owner> resultList) {
        this.resultList = resultList;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
        this.resultList = null;
    }

    public Owner getSelectedOwner() {
        return selectedOwner;
    }

    public void setSelectedOwner(Owner selectedOwner) {
        this.selectedOwner = selectedOwner;
    }

    public int getNextFirstResult() {
        return firstResult + this.getPageSize();
    }

    public int getPreviousFirstResult() {
        return this.getPageSize() >= firstResult ? 0 : firstResult - this.getPageSize();
    }
    
    

    @Override
    public List<Owner> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
        int end = first + pageSize;
        QuerySortOrder order = QuerySortOrder.ASC;
        if (sortOrder == SortOrder.DESCENDING) {
            order = QuerySortOrder.DESC;
        }
        
        Map<String, Object> _filters = new HashMap<String, Object>();
        _filters.put(Owner_.organization.getName(), getOrganization());
        _filters.putAll(filters);

        QueryData<Owner> qData = ownerService.find(first, end, sortField, order, _filters);
        this.setRowCount(qData.getTotalResultCount().intValue());
        return qData.getResult();
    }

    @PostConstruct
    @Override
    public void init() {
        super.init();
        ownerService.setEntityManager(getEntityManager());
      
    }

    public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("module.owner") + " " + UI.getMessages("common.selected"), ((Owner) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("module.owner") + " " + UI.getMessages("common.unselected"), ((Owner) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
        this.setSelectedOwner(null);
    }

    @Override
    public Owner getRowData(String rowKey) {
        return ownerService.findByName(rowKey);
    }

    @Override
    public Object getRowKey(Owner entity) {
        return entity.getName();
    }

      
  /*  
    @Inject
    private OwnerHome ownerHome;
    public void createOwner(){
        if(getSelectedOwner()==null){
            ownerHome.createInstance();
            ownerHome.saveOwner();
        }else{
            ownerHome.setInstance(getSelectedOwner());
            ownerHome.saveOwner();
        }
    }*/
}
