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
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.model.accounting.Account;
import org.eqaula.glue.model.accounting.Account_;
import org.eqaula.glue.model.management.BalancedScorecard;
import org.eqaula.glue.model.management.BalancedScorecard_;
import org.eqaula.glue.model.management.Perspective;
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
public class BalancedScorecardListService extends ListService<BalancedScorecard> {

    private static final long serialVersionUID = 1229207884508672223L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(BalancedScorecardListService.class);
    @Inject
    private BalancedScorecardService balancedScorecardService;
    private List<BalancedScorecard> resultList;
    private int firstResult = 0;
    private BalancedScorecard[] selectedBalancedScorecards;
    private BalancedScorecard selectedBalancedScorecard;
    private Long countThemes;

    public Long getCountThemes(BalancedScorecard bsc) {
        countThemes = new Long(0);
        for (Perspective perspective : bsc.getPerspectives()) {
            countThemes+=perspective.getThemes().size();
        }
        return countThemes;
    }

    public void setCountThemes(Long countThemes) {
        this.countThemes = countThemes;
    }

    public BalancedScorecardListService() {
        setPageSize(MAX_RESULTS);
        resultList = new ArrayList<BalancedScorecard>();
    }

    public List<BalancedScorecard> getResultList() {
        if (resultList.isEmpty()) {
            resultList = balancedScorecardService.getBalancedScorecards();
        }
        return resultList;
    }

    public void setResultList(List<BalancedScorecard> resultList) {
        this.resultList = resultList;
    }

    public int getFirstResult() {
        return firstResult;

    }

    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
        this.resultList = null;
    }

    public BalancedScorecard getSelectedBalancedScorecard() {
        return selectedBalancedScorecard;
    }

    public void setSelectedBalancedScorecard(BalancedScorecard selectedBalancedScorecard) {
        this.selectedBalancedScorecard = selectedBalancedScorecard;
    }

    public int getNextFirstResult() {
        return firstResult + this.getPageSize();
    }

    public int getPreviousFirstResult() {
        return this.getPageSize() >= firstResult ? 0 : firstResult - this.getPageSize();
    }

    @Override
    public List<BalancedScorecard> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        int end = first + pageSize;
        QuerySortOrder order = QuerySortOrder.ASC;
        if (sortOrder == SortOrder.DESCENDING) {
            order = QuerySortOrder.DESC;
        }
        Map<String, Object> _filters = new HashMap<String, Object>();
        _filters.put(BalancedScorecard_.organization.getName(), getOrganization());
        _filters.putAll(filters);

        QueryData<BalancedScorecard> qData = balancedScorecardService.find(first, end, sortField, order, _filters);
        this.setRowCount(qData.getTotalResultCount().intValue());
        return qData.getResult();
    }

    @PostConstruct
    @Override
    public void init() {
        super.init();
        balancedScorecardService.setEntityManager(getEntityManager());
    }

    public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("module.balancedScorecard") + " " + UI.getMessages("common.selected"), ((BalancedScorecard) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("module.balancedScorecard") + " " + UI.getMessages("common.unselected"), ((BalancedScorecard) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
        this.setSelectedBalancedScorecard(null);
    }

    @Override
    public BalancedScorecard getRowData(String rowKey) {
        return balancedScorecardService.findByName(rowKey);
    }

    @Override
    public Object getRowKey(BalancedScorecard entity) {
        return entity.getName();
    }
}
