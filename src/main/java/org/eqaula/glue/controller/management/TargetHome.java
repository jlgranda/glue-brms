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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
import org.eqaula.glue.model.management.Initiative;
import org.eqaula.glue.model.management.Measure;
import org.eqaula.glue.model.management.Method;
import org.eqaula.glue.model.management.Target;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.service.MeasureService;
import org.eqaula.glue.util.Dates;
import org.eqaula.glue.util.UI;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.context.RequestContext;


/*
 * @author dianita
 */
@Named
@ViewScoped
public class TargetHome extends BussinesEntityHome<Target> implements Serializable {

    private static final long serialVersionUID = 5699917875695874467L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(TargetHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Current
    @Inject
    private Profile profile;
    private Measure measure;
    private Long measureId;
    @Inject
    private MeasureService measureService;
    private Method selectedMethod;
    private List<Method> methods;
    private Initiative selectedInitiative;
    
    private boolean modifiedMethods = false;
    private boolean modifiedInitiatives = false;

    public TargetHome() {
    }

    public Long getTargetId() {
        return (Long) getId();
    }

    public void setTargetId(Long targetId) {
        setId(targetId);
    }

    public String getStructureName() {
        return getInstance().getName();
    }

    public Measure getMeasure() {
        if (measure == null) {
            if (measureId == null) {
                measure = null;
            } else {
                measure = measureService.find(getMeasureId());
            }
        }
        return measure;
    }

    public void setMeasure(Measure measure) {
        this.measure = measure;
    }

    public Long getMeasureId() {
        return measureId;
    }

    public void setMeasureId(Long measureId) {
        this.measureId = measureId;
    }

    public Method getSelectedMethod() {
        return selectedMethod;
    }

    public void setSelectedMethod(Method selectedMethod) {
        this.selectedMethod = selectedMethod;
    }

    public Initiative getSelectedInitiative() {
        return selectedInitiative;
    }

    public void setSelectedInitiative(Initiative selectedInitiative) {
        this.selectedInitiative = selectedInitiative;
    }

    public List<Method> getMethods() {
        methods = new ArrayList<Method>();
        methods = getInstance().getMethods();
        return methods;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    public boolean isModifiedMethods() {
        return modifiedMethods;
    }

    public void setModifiedMethods(boolean modifiedMethods) {
        this.modifiedMethods = modifiedMethods;
    }

    public boolean isModifiedInitiatives() {
        return modifiedInitiatives;
    }

    public void setModifiedInitiatives(boolean modifiedInitiatives) {
        this.modifiedInitiatives = modifiedInitiatives;
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
        measureService.setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
    }

    @Override
    protected Target createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Target.class.getName());
        Date now = Calendar.getInstance().getTime();
        Target target = new Target();
        target.setCode(UUID.randomUUID().toString());
        target.setCreatedOn(now);
        target.setLastUpdate(now);
        target.setActivationTime(now);
        target.setExpirationTime(Dates.addDays(now, 364));
        target.setType(_type);
        target.setMeasure(getMeasure());
        target.buildAttributes(bussinesEntityService);
        return target;
    }

    @TransactionAttribute
    public String saveTarget() {

        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            getInstance().setAuthor(this.profile);
            create(getInstance());
        }
        if (getMeasure() != null) {
            return getOutcome() + "?balancedScorecardId=" + getMeasure().getObjetive().getTheme().getPerspective().getBalancedScorecard().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public boolean isWired() {
        return true;
    }

    public Target getDefiniedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Target> getEntityClass() {
        return Target.class;
    }

    @Transactional
    public String deleteTarget() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Target is Null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe una meta para ser borrada!", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        if(getOutcome()==null){
            return "/pages/management/balancedscorecard/view.xhtml?balancedScorecardId=" + getInstance().getMeasure().getObjetive().getTheme().getPerspective().getBalancedScorecard().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        if (getMeasure() != null) {
            return getOutcome() + "?balancedScorecardId=" + getMeasure().getObjetive().getTheme().getPerspective().getBalancedScorecard().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }
    @Inject
    private MethodHome methodHome;

    public void wireMethod(Method method) {
               
        if (method == null) {
            setSelectedMethod(methodHome.createInstance());
        } else {
            setModifiedMethods(true); 
            setSelectedMethod(method);
        }
    }

    public void createMethod() {
        wireMethod(null);
    }

    public void addMethod() {
        if (!getSelectedMethod().isPersistent()) {
            getInstance().addMethod(getSelectedMethod());
            setModifiedMethods(true); 
        }
    }
    
    public void removeMethod() {
        if(containsMethod()){
            getInstance().removeMethod(getSelectedMethod());
            setModifiedMethods(true); 
        }
        /**if (getInstance().getMethods().contains(getSelectedMethod())) {
            getInstance().removeMethod(getSelectedMethod());
        }**/
    }
    
    public boolean containsMethod() {
        if (getInstance().getMethods().contains(getSelectedMethod())) {
            return true;
        }
        return false;
    }

    public void clearMethod() {
        setSelectedMethod(null);
    }

    public void createInitiative() {
        wireInitiative(null);
    }
    @Inject
    private InitiativeHome initiativeHome;

    public void wireInitiative(Initiative initiative) {
        if (initiative == null) {
            setSelectedInitiative(initiativeHome.createInstance());
        } else {
             setModifiedInitiatives(true);
            setSelectedInitiative(initiative);
        }
    }
    
    public boolean containsInitiative() {
        if (getInstance().getInitiatives().contains(getSelectedInitiative())) {
            return true;
        }
        return false;
    }


    public void addInitiative() {
        if (!getSelectedInitiative().isPersistent()) {
            getInstance().addInitiative(getSelectedInitiative());
            setModifiedInitiatives(true);
        }
    }

    public void removeInitiative() {
        if (containsInitiative()) {
            getInstance().removeInitiative(getSelectedInitiative());
            setModifiedInitiatives(true);
        }
    }
    
    public void clearInitiative() {
        setSelectedInitiative(null);
    }
    
    public void createNewTarget() {
        setId(null);
        setInstance(null);
        wire();
    }

    public void editTarget(Long id) {
        setId(id);
        load();
    }

    @TransactionAttribute
    public String saveTargetDialog() {
        saveTarget();
        editTarget(getInstance().getId());
        setModifiedMethods(false);
        setModifiedInitiatives(false);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.action.sucessfully"), ""));
        return null;
       //return "/pages/management/balancedscorecard/view.xhtml?balancedScorecardId=" + getInstance().getMeasure().getObjetive().getTheme().getPerspective().getBalancedScorecard().getId() + "&faces-redirect=true&includeViewParams=true";
    }
}