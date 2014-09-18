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
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.model.management.Section;

import org.eqaula.glue.model.management.Diagnostic;
import org.eqaula.glue.model.management.Perspective;
import org.eqaula.glue.model.management.Section_;
import org.eqaula.glue.util.QueryData;
import org.eqaula.glue.util.QuerySortOrder;
import org.eqaula.glue.util.UI;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.SortOrder;

/**
 *
 * @author dianita
 */
@Named
@RequestScoped
public class SectionListService extends ListService<Section> {

    private static final int MAX_RESULTS = 5;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(SectionListService.class);
    private static final long serialVersionUID = 1567380650587310415L;
    @Inject
    @Web
    private EntityManager entityManager;
    @Inject
    private SectionService sectionService;
    private List<Section> resultList;
    private int firstResult = 0;
    private Section selectedSection;
    @Inject
    protected DiagnosticService diagnosticService;
    private Diagnostic diagnostic;
    private Long diagnosticId;

    public SectionListService() {
        setPageSize(MAX_RESULTS);
        resultList = new ArrayList<Section>();
    }

    public List<Section> getResultList() {
        if (resultList.isEmpty()) {
            resultList = sectionService.getSections();
        }
        return resultList;
    }

    public void setResultList(List<Section> resultList) {
        this.resultList = resultList;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
        this.resultList = null;
    }

    public Section getSelectedSection() {
        return selectedSection;
    }

    public void setSelectedSection(Section selectedSection) {
        this.selectedSection = selectedSection;
    }


    public Long getDiagnosticId() {
        return diagnosticId;
    }

    public void setDiagnosticId(Long diagnosticId) {
        this.diagnosticId = diagnosticId;
    }

    @Transactional
    public Diagnostic getDiagnostic() {
        if (diagnostic == null) {
            if (diagnosticId == null) {
                diagnostic = null;
            } else {
                diagnostic = diagnosticService.find(getDiagnosticId());
            }
        }
        return diagnostic;
    }

    public void setDiagnostic(Diagnostic diagnostic) {
        this.diagnostic = diagnostic;
    }

    public int getNextFirstResult() {
        return firstResult + this.getPageSize();
    }

    public int getPreviousFirstResult() {
        return this.getPageSize() >= firstResult ? 0 : firstResult - this.getPageSize();
    }

    @Override
    public List<Section> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        int end = first + pageSize;
        QuerySortOrder order = QuerySortOrder.ASC;
        if (sortOrder == SortOrder.DESCENDING) {
            order = QuerySortOrder.DESC;
        }
        Map<String, Object> _filters = new HashMap<String, Object>();
        _filters.put(Section_.diagnostic.getName(), getDiagnostic());
        _filters.putAll(filters);

        QueryData<Section> qData = sectionService.find(first, end, sortField, order, _filters);
        this.setRowCount(qData.getTotalResultCount().intValue());
        return qData.getResult();
    }

    @PostConstruct
    public void init() {
        super.init();
        diagnosticService.setEntityManager(entityManager);
        sectionService.setEntityManager(entityManager);
    }

    public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("module.section") + " " + UI.getMessages("common.selected"), ((Section) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("module.section") + " " + UI.getMessages("common.unselected"), ((Section) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
        this.setSelectedSection(null);
    }

    @Override
    public Section getRowData(String rowKey) {
        return sectionService.findByName(rowKey);
    }

    @Override
    public Object getRowKey(Section entity) {
        return entity.getName();
    }
}
