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
import org.eqaula.glue.model.management.BalancedScorecard;
import org.eqaula.glue.model.management.Perspective;
import org.eqaula.glue.model.management.Theme;
import org.eqaula.glue.model.management.Theme_;
import org.eqaula.glue.util.QueryData;
import org.eqaula.glue.util.QuerySortOrder;
import org.eqaula.glue.util.UI;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/*
 * @author dianita
 */
@Named
@ViewScoped
public class ThemeListService extends ListService<Theme> {

    private static final int MAX_RESULTS = 5;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(ThemeListService.class);
    private static final long serialVersionUID = -3077669958110754391L;
    @Inject
    @Web
    private EntityManager entityManager;
    @Inject
    private ThemeService themeService;
    private List<Theme> resultList;
    private int firstResult = 0;
    private Theme[] selectedThemes;
    private Theme selectedTheme;
    @Inject
    protected PerspectiveService perspectiveService;
    private Perspective perspective;
    private Long perspectiveId;

    public ThemeListService() {
        setPageSize(MAX_RESULTS);
        resultList = new ArrayList<Theme>();
    }

    public List<Theme> getResultList() {
        if (resultList.isEmpty()) {
            resultList = themeService.getThemes();
        }
        return resultList;
    }

    public void setResultList(List<Theme> resultList) {
        this.resultList = resultList;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
        this.resultList = null;
    }

    public Theme getSelectedTheme() {
        return selectedTheme;
    }

    public void setSelectedTheme(Theme selectedTheme) {
        this.selectedTheme = selectedTheme;
    }

    public Long getPerspectiveId() {
        return perspectiveId;
    }

    public void setPerspectiveId(Long perspectiveId) {
        this.perspectiveId = perspectiveId;
    }

    @Transactional
    public Perspective getPerspective() {
        if (perspective == null) {
            if (perspectiveId == null) {
                perspective = null;
            } else {
                perspective = perspectiveService.find(getPerspectiveId());
            }
        }
        return perspective;
    }

    public void setPerspective(Perspective perspective) {
        this.perspective = perspective;
    }

    public int getNextFirstResult() {
        return firstResult + this.getPageSize();
    }

    public int getPreviousFirstResult() {
        return this.getPageSize() >= firstResult ? 0 : firstResult - this.getPageSize();
    }

    @Override
    public List<Theme> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
        int end = first + pageSize;
        QuerySortOrder order = QuerySortOrder.ASC;
        if (sortOrder == SortOrder.DESCENDING) {
            order = QuerySortOrder.DESC;
        }
        Map<String, Object> _filters = new HashMap<String, Object>();
        _filters.put(Theme_.organization.getName(), getOrganization());
        _filters.putAll(filters);

        QueryData<Theme> qData = themeService.find(first, end, sortField, order, _filters);
        this.setRowCount(qData.getTotalResultCount().intValue());
        return qData.getResult();
    }

    @PostConstruct
    public void init() {
        super.init();
        themeService.setEntityManager(entityManager);
    }

    public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("module.theme") + " " + UI.getMessages("common.selected"), ((Theme) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("module.theme") + " " + UI.getMessages("common.unselected"), ((Theme) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
        this.setSelectedTheme(null);
    }

    @Override
    public Theme getRowData(String rowKey) {
        return themeService.findByName(rowKey);
    }

    @Override
    public Object getRowKey(Theme entity) {
        return entity.getName();
    }
}
