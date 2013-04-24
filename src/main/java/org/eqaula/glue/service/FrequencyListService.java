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

import java.io.Serializable;
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
import org.eqaula.glue.model.config.Frequency;
import org.eqaula.glue.util.QueryData;
import org.eqaula.glue.util.QuerySortOrder;
import org.eqaula.glue.util.UI;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/*
 * @author dianita
 */
@Named
@ViewScoped
public class FrequencyListService extends LazyDataModel<Frequency> implements Serializable {

    private static final long serialVersionUID = -160482511900961801L;
    private static final int MAX_RESULTS = 5;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(FrequencyListService.class);
    @Inject
    @Web
    private EntityManager entityManager;
    @Inject
    private FrequencyService frequencyService;
    private List<Frequency> resultList;
    private int firstResult = 0;
    private Frequency selectedFrequency;

    public FrequencyListService() {
        setPageSize(MAX_RESULTS);
        resultList = new ArrayList<Frequency>();
    }

    public List<Frequency> getResultList() {
        log.info("load BussinesEntityType");
        if (resultList.isEmpty()) {
            resultList = frequencyService.getFrequencies(this.getPageSize(), firstResult);
            log.info("eqaula --> resultlist " + resultList);
        }
        return resultList;
    }
    
     public void setResultList(List<Frequency> resultList) {
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

    public int getNextFirstResult() {
        return firstResult + this.getPageSize();
    }

    public int getPreviousFirstResult() {
        return this.getPageSize() >= firstResult ? 0 : firstResult - this.getPageSize();
    }

    public Frequency getSelectedFrequency() {
        return selectedFrequency;
    }

    public void setSelectedFrequency(Frequency selectedFrequency) {
        this.selectedFrequency = selectedFrequency;
    }

     @Override
    public List<Frequency> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
        int end = first + pageSize;

        QuerySortOrder order = QuerySortOrder.ASC;
        if (sortOrder == SortOrder.DESCENDING) {
            order = QuerySortOrder.DESC;
        }
        Map<String, Object> _filters = new HashMap<String, Object>();
        QueryData<Frequency> qData = frequencyService.find(first, end, sortField, order, _filters);
        this.setRowCount(qData.getTotalResultCount().intValue());
        return qData.getResult();
    }

    @PostConstruct
    public void init() {
        
        frequencyService.setEntityManager(entityManager);
    }

    public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("module.frequency") + " " + UI.getMessages("common.selected"), ((Frequency) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("module.frequency") + " " + UI.getMessages("common.unselected"), ((Frequency) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
        this.setSelectedFrequency(null);
    }

    @Override
    public Frequency getRowData(String rowKey) {
        return frequencyService.findByName(rowKey);
    }

    @Override
    public Object getRowKey(Frequency entity) {
        return entity.getName();
    }

}
