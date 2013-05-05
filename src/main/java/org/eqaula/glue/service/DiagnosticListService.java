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
import org.eqaula.glue.model.management.Diagnostic;
import org.eqaula.glue.model.management.Diagnostic_;
import org.eqaula.glue.model.management.Owner;
import static org.eqaula.glue.service.ListService.MAX_RESULTS;
import org.eqaula.glue.util.QueryData;
import org.eqaula.glue.util.QuerySortOrder;
import org.eqaula.glue.util.UI;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.SortOrder;

/*
 * @author dianita
 */
@Named
@ViewScoped
public class DiagnosticListService extends ListService<Diagnostic> {

    private static final long serialVersionUID = 1333119157936681172L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(DiagnosticListService.class);
    @Inject
    private DiagnosticService diagnosticService;
    private List<Diagnostic> resultList;
    private int firstResult = 0;
    private Diagnostic selectedDiagnostic;
    @Inject
    protected OwnerService ownerService;
    private Owner owner;
    private Long ownerId;

    public DiagnosticListService() {
        setPageSize(MAX_RESULTS);
        resultList = new ArrayList<Diagnostic>();
    }

    @Transactional
    public Owner getOwner() {
        if (owner == null) {
            if (ownerId == null) {
                owner = null;
            } else {
                owner = ownerService.find(getOwnerId());
            }
        }
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public List<Diagnostic> getResultList() {
        if (resultList.isEmpty()) {
            resultList = diagnosticService.getDiagnostics();
        }
        return resultList;
    }

    public void setResultList(List<Diagnostic> resultList) {
        this.resultList = resultList;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
        this.resultList = null;
    }

    public Diagnostic getSelectedDiagnostic() {
        return selectedDiagnostic;
    }

    public void setSelectedDiagnostic(Diagnostic selectedDiagnostic) {
        this.selectedDiagnostic = selectedDiagnostic;
    }

    public int getNextFirstResult() {
        return firstResult + this.getPageSize();
    }

    public int getPreviousFirstResult() {
        return this.getPageSize() >= firstResult ? 0 : firstResult - this.getPageSize();
    }

    @Override
    public List<Diagnostic> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
        int end = first + pageSize;
        QuerySortOrder order = QuerySortOrder.ASC;
        if (sortOrder == SortOrder.DESCENDING) {
            order = QuerySortOrder.DESC;
        }

        Map<String, Object> _filters = new HashMap<String, Object>();
        _filters.put(Diagnostic_.owner.getName(), getOwner());
        _filters.putAll(filters);

        QueryData<Diagnostic> qData = diagnosticService.find(first, end, sortField, order, _filters);
        this.setRowCount(qData.getTotalResultCount().intValue());
        return qData.getResult();
    }

    @PostConstruct
    @Override
    public void init() {
        super.init();
        ownerService.setEntityManager(getEntityManager());
        diagnosticService.setEntityManager(getEntityManager());
    }

    public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("module.diagnostic") + " " + UI.getMessages("common.selected"), ((Diagnostic) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("module.diagnostic") + " " + UI.getMessages("common.unselected"), ((Diagnostic) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
        this.setSelectedDiagnostic(null);
    }

    @Override
    public Diagnostic getRowData(String rowKey) {
        return diagnosticService.findByName(rowKey);
    }

    @Override
    public Object getRowKey(Diagnostic entity) {
        return entity.getName();
    }
}
