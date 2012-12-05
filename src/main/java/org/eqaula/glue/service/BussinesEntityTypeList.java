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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.eqaula.glue.model.BussinesEntity;
import org.eqaula.glue.model.BussinesEntityType;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 *
 * @author cesar
 */
@RequestScoped
@Named
public class BussinesEntityTypeList extends LazyDataModel<BussinesEntityType>{
    private static final long serialVersionUID = 4819808125494695200L;
    private static final int MAX_RESULTS = 5;
    @Inject
    private Logger log;
    @Inject
    private BussinesEntityService bussinesEntityService;
    private List<BussinesEntityType> resultList;
    private int firstResult = 0;
    private BussinesEntityType[] selectedBussinesEntities;
    private BussinesEntityType selectedBussinesEntityType; //Filtro de cuenta schema

    public BussinesEntityTypeList() {
        setPageSize(MAX_RESULTS);
    }

    public Logger getLog() {
        return log;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public BussinesEntityService getBussinesEntityService() {
        return bussinesEntityService;
    }

    public void setBussinesEntityService(BussinesEntityService bussinesEntityService) {
        this.bussinesEntityService = bussinesEntityService;
    }

    public List<BussinesEntityType> getResultList() {
        log.info("load BussinesEntitys");
        resultList = new ArrayList<BussinesEntityType>();
        if (resultList == null && getSelectedBussinesEntityType() != null) {
            resultList = bussinesEntityService.find(this.getPageSize(), firstResult, getSelectedBussinesEntityType());
        }
        
        return resultList;
    }

    public void setResultList(List<BussinesEntityType> resultList) {
        this.resultList = resultList;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
    }

    public BussinesEntityType[] getSelectedBussinesEntities() {
        return selectedBussinesEntities;
    }

    public void setSelectedBussinesEntities(BussinesEntityType[] selectedBussinesEntities) {
        this.selectedBussinesEntities = selectedBussinesEntities;
    }

    public BussinesEntityType getSelectedBussinesEntityType() {
        return selectedBussinesEntityType;
    }

    public void setSelectedBussinesEntityType(BussinesEntityType selectedBussinesEntityType) {
        this.selectedBussinesEntityType = selectedBussinesEntityType;
    }

    
    @Override
    public List<BussinesEntityType> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
