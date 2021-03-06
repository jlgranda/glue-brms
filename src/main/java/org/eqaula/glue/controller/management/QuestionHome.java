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
package org.eqaula.glue.controller.management;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Current;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.BussinesEntityHome;
import org.eqaula.glue.model.BussinesEntityType;
import org.eqaula.glue.model.management.Question;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.service.RevisionItemService;
import org.eqaula.glue.util.Dates;
import org.eqaula.glue.util.UI;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.context.RequestContext;

/*
 * @author dianita
 */
@Named
@ViewScoped
public class QuestionHome extends BussinesEntityHome<Question> implements Serializable {

    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(QuestionHome.class);
    private static final long serialVersionUID = 6741764864014221924L;
    @Inject
    @Web
    private EntityManager em;
    @Current
    @Inject
    private Profile profile;
    @Inject
    private RevisionItemService revisionItemService;
            
    public QuestionHome() {
    }

    public Long getQuestionId() {
        return (Long) getId();
    }

    public void setQuestionId(Long questionId) {
        setId(questionId);
    }

    public String getStructureName() {
        return getInstance().getName();
    }

    @TransactionAttribute
    public void load() {
        if (isIdDefined()) {
            wire();
        }
    }

    @TransactionAttribute
    public void wire() {
        getInstance();
    }

    @PostConstruct
    public void init() {
        setEntityManager(em);
        revisionItemService.setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
    }

    @Override
    protected Question createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Question.class.getName());
        Date now = Calendar.getInstance().getTime();
        Question question = new Question();
        question.setCode(UUID.randomUUID().toString());
        question.setCreatedOn(now);
        question.setLastUpdate(now);
        question.setActivationTime(now);
        question.setExpirationTime(Dates.addDays(now, 364));
        question.setType(_type);
        question.buildAttributes(bussinesEntityService);
        return question;
    }

    @TransactionAttribute
    public String saveQuestion() {

        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            getInstance().setAuthor(this.profile);
            create(getInstance());
        }
        if(getOutcome()==null){
            return null;
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public boolean isWired() {
        return true;
    }

    public Question getDefiniedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Question> getEntityClass() {
        return Question.class;
    }

    @Transactional
    public String deleteQuestion() {
        boolean band;
        band = false;
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Question is Null");
            }
            if (getInstance().isPersistent()) {
                if (hasValuesBussinesEntity()) {
                    delete(getInstance());
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                    RequestContext.getCurrentInstance().execute("editDlg.hide()");
                } else {
                    band = true;
                    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, getInstance().getName() + " : " + UI.getMessages("module.stocklist.delete.confirm"), "");
                    FacesContext.getCurrentInstance().addMessage(null, message);
                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe una pregunta para ser borrada!", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        if(band){
            return null;
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }
    
    public boolean hasValuesBussinesEntity() {
        boolean ban =  revisionItemService.findByQuestion(getInstance()).isEmpty();
        return ban;
    }
    
    public void createNewQuestion(){
        setId(null);
        setInstance(null);
        load();
    }
}
