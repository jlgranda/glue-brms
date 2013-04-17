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
import org.eqaula.glue.model.management.Question;
import org.eqaula.glue.util.QueryData;
import org.eqaula.glue.util.QuerySortOrder;
import org.eqaula.glue.util.UI;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 *
 * @author dianita
 */
@Named
@RequestScoped
public class QuestionListService extends LazyDataModel<Question> {
    private static final long serialVersionUID = 6774547640960610920L;
    private static final int MAX_RESULTS = 5;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(QuestionListService.class);
    
    @Inject
    @Web
    private EntityManager entityManager;
    @Inject
    private QuestionService questionService;
    private List<Question> resultList;
    private int firstResult = 0;
    private Question selectedQuestion;

    public QuestionListService() {
        setPageSize(MAX_RESULTS);
        resultList = new ArrayList<Question>();
    }


    public List<Question> getResultList() {
        if (resultList.isEmpty()) {
            resultList = questionService.getQuestions();
        }
        return resultList;
    }

    public void setResultList(List<Question> resultList) {
        this.resultList = resultList;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
        this.resultList = null;
    }

    public Question getSelectedQuestion() {
        return selectedQuestion;
    }

    public void setSelectedQuestion(Question selectedQuestion) {
        this.selectedQuestion = selectedQuestion;
    }

    public int getNextFirstResult() {
        return firstResult + this.getPageSize();
    }

    public int getPreviousFirstResult() {
        return this.getPageSize() >= firstResult ? 0 : firstResult - this.getPageSize();
    }

    @Override
    public List<Question> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
        int end = first + pageSize;
        QuerySortOrder order = QuerySortOrder.ASC;
        if (sortOrder == SortOrder.DESCENDING) {
            order = QuerySortOrder.DESC;
        }
        Map<String, Object> _filters = new HashMap<String, Object>();
        QueryData<Question> qData = questionService.find(first, end, sortField, order, _filters);
        this.setRowCount(qData.getTotalResultCount().intValue());
        return qData.getResult();
    }

    @PostConstruct
    public void init() {
        questionService.setEntityManager(entityManager);
    }

    public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("module.question") + " " + UI.getMessages("common.selected"), ((Question) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("module.question") + " " + UI.getMessages("common.unselected"), ((Question) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
        this.setSelectedQuestion(null);
    }

    @Override
    public Question getRowData(String rowKey) {
        return questionService.findByName(rowKey);
    }

    @Override
    public Object getRowKey(Question entity) {
        return entity.getName();
    }
}
