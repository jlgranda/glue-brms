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
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.model.stocklist.Item;
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
@Named(value = "itemListService")
@RequestScoped
public class ItemListService extends LazyDataModel<Item>{

    private static final long serialVersionUID = 4819808125494695197L;
    private static final int MAX_RESULTS = 5;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(ItemListService.class);
    @Inject
    @Web
    private EntityManager entityManager;
    @Inject
    private ItemService itemService;
    private List<Item> resultList;
    private int firstResult = 0;
    private Item[] selectedItems;
    private Item selectedItem;

    public ItemListService() {
        setPageSize(MAX_RESULTS);
        resultList = new ArrayList<Item>();
    }

    public List<Item> getResultList() {
        log.info("load BussinesEntityType");
        if (resultList.isEmpty() /*&& getSelectedBussinesEntityType() != null*/) {
            resultList = itemService.getItems(this.getPageSize(), firstResult);
            log.info("eqaula --> resultlist " + resultList);
        }
        return resultList;
    }

    public void setResultList(List<Item> resultList) {
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

    public Item getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(Item selectedItem) {
        this.selectedItem = selectedItem;
    }

    public int getNextFirstResult() {
        return firstResult + this.getPageSize();
    }

    public int getPreviousFirstResult() {
        return this.getPageSize() >= firstResult ? 0 : firstResult - this.getPageSize();
    }

    @Override
    public List<Item> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
        int end = first + pageSize;

        QuerySortOrder order = QuerySortOrder.ASC;
        if (sortOrder == SortOrder.DESCENDING) {
            order = QuerySortOrder.DESC;
        }
        Map<String, Object> _filters = new HashMap<String, Object>();
        /*_filters.put(BussinesEntity_.type.getName(), getType()); //Filtro por defecto
         _filters.putAll(filters);*/

        QueryData<Item> qData = itemService.find(first, end, sortField, order, _filters);
        this.setRowCount(qData.getTotalResultCount().intValue());
        return qData.getResult();
    }
    
    
    @PostConstruct
    public void init() {
        log.info("Setup entityManager into ItemService...");
        itemService.setEntityManager(entityManager);
    }

    public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("module.stocklist.items") + " " + UI.getMessages("common.selected"), ((Item) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("module.stocklist.items") + " " + UI.getMessages("common.unselected"), ((Item) event.getObject()).getName());

        FacesContext.getCurrentInstance().addMessage(null, msg);
        this.setSelectedItem(null);
    }

    @Override
    public Item getRowData(String rowKey) {

        return itemService.findByName(rowKey);
    }

    @Override
    public Object getRowKey(Item entity) {
        return entity.getName();
    }
}
