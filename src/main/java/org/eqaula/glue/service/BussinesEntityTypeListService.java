/*
 * Copyright 2012 cesar.
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
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.eqaula.glue.model.BussinesEntity;
import org.eqaula.glue.model.BussinesEntityType;
import org.eqaula.glue.util.QueryData;
import org.eqaula.glue.util.QuerySortOrder;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 *
 * @author cesar
 */
@RequestScoped
@Named
public class BussinesEntityTypeListService extends LazyDataModel<BussinesEntityType>{
    private static final long serialVersionUID = 4819808125494695200L;
    private static final int MAX_RESULTS = 5;
    @Inject
    private Logger log;
    @Inject
    private BussinesEntityTypeService bussinesEntityTypeService;
    private List<BussinesEntityType> resultList;
    private int firstResult = 0;
    private BussinesEntityType[] selectedBussinesEntitiesType;
    private BussinesEntityType selectedBussinesEntityType; //Filtro de cuenta schema

    public BussinesEntityTypeListService() {
        setPageSize(MAX_RESULTS);
        resultList = new ArrayList<BussinesEntityType>();         
    }

    public Logger getLog() {
        return log;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public BussinesEntityTypeService getBussinesEntityTypeService() {
        return bussinesEntityTypeService;
    }

    public void setBussinesEntityTypeService(BussinesEntityTypeService bussinesEntityTypeService) {
        this.bussinesEntityTypeService = bussinesEntityTypeService;
    }

    
    public List<BussinesEntityType> getResultList() {
        log.info("load BussinesEntitys");
        resultList = new ArrayList<BussinesEntityType>();
        if (resultList == null && getSelectedBussinesEntityType() != null) {
            //resultList = bussinesEntityService.find(this.getPageSize(), firstResult, getSelectedBussinesEntityType());
            resultList = bussinesEntityTypeService.find(this.getPageSize(), firstResult);            
        }
        
        return resultList;
    }
    
    public String find() {
        try {
            bussinesEntityTypeService.init();
            resultList = bussinesEntityTypeService.findAll();
            String summary = "Encontrados!";
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, summary, null));
        } catch (Exception e) {
            System.out.print("Error al iniciar los datos");
        }
        
        return "/pages/admin/bussinesentitytype/list";
    }
    
    public int getNextFirstResult() {
        return firstResult + this.getPageSize();
    }

    public int getPreviousFirstResult() {
        return this.getPageSize() >= firstResult ? 0 : firstResult - this.getPageSize();
    }
    
    public void setResultList(List<BussinesEntityType> resultList) {
        this.resultList = resultList;
    }
           
    public int getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(int firstResult) {
        log.info("set first result + firstResult");
        this.firstResult = firstResult;
        this.resultList = null;
    }
    
    public boolean isPreviousExists() {
        return firstResult > 0;
    }

    @Override
    public BussinesEntityType getRowData(String rowKey) {

        return bussinesEntityTypeService.findByName(rowKey);
    }

    @Override
    public Object getRowKey(BussinesEntityType entity) {
        return entity.getName();
    }

    @Override
    public List<BussinesEntityType> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {

        int end = first + pageSize;

        QuerySortOrder order = QuerySortOrder.ASC;
        if (sortOrder == SortOrder.DESCENDING) {
            order = QuerySortOrder.DESC;
        }
        Map<String, Object> _filters = new HashMap<String, Object>();
        /*_filters.put(BussinesEntity_.parent.getName(), getSelectedAccount()); //Filtro por defecto
        _filters.putAll(filters);*/

        QueryData<BussinesEntityType> qData = (QueryData<BussinesEntityType>) bussinesEntityTypeService.find(first, end);
        this.setRowCount(qData.getTotalResultCount().intValue());
        return qData.getResult();
    }
       
    
    public BussinesEntityType[] getSelectedBussinesEntitiesType() {
        return selectedBussinesEntitiesType;
    }

    public void setSelectedBussinesEntitiesType(BussinesEntityType[] selectedBussinesEntitiesType) {
        this.selectedBussinesEntitiesType = selectedBussinesEntitiesType;
    }
        
    public BussinesEntityType getSelectedBussinesEntityType() {
        return selectedBussinesEntityType;
    }

    public void setSelectedBussinesEntityType(BussinesEntityType selectedBussinesEntityType) {
        this.selectedBussinesEntityType = selectedBussinesEntityType;
    }

    
//    @Override
//    public List<BussinesEntityType> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

}
