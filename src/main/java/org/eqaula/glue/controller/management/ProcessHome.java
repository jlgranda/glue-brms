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
import org.eqaula.glue.model.management.Macroprocess;
import org.eqaula.glue.model.management.Process;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.service.MacroprocessService;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.context.RequestContext;

/*
 * @author dianita
 */
@Named
@ViewScoped
public class ProcessHome extends BussinesEntityHome<Process> implements Serializable {

    private static final long serialVersionUID = 5723310840982465737L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(ProcessHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Current
    @Inject
    private Profile profile;
    private Macroprocess macroprocess;
    private Long macroprocessId;
    @Inject
    private MacroprocessService macroprocessService;

    public ProcessHome() {
    }

    public Long getProcessId() {
        return (Long) getId();

    }

    public void setProcessId(Long processId) {
        setId(processId);
    }

    public String getStructureName() {
        return getInstance().getName();
    }

    @Transactional
    public Macroprocess getMacroprocess() {
        if (macroprocess == null) {
            if (macroprocessId == null) {
                macroprocess = null;
            } else {
                macroprocess = macroprocessService.find(getMacroprocessId());
            }
        }
        return macroprocess;
    }

    public void setMacroprocess(Macroprocess macroprocess) {
        this.macroprocess = macroprocess;
    }

    public Long getMacroprocessId() {
        return macroprocessId;
    }

    public void setMacroprocessId(Long macroprocessId) {
        this.macroprocessId = macroprocessId;
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
        macroprocessService.setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
    }

    @Override
    protected Process createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Process.class.getName());
        Date now = Calendar.getInstance().getTime();
        Process process = new Process();
        process.setCode(UUID.randomUUID().toString());
        process.setCreatedOn(now);
        process.setLastUpdate(now);
        process.setActivationTime(now);
        process.setExpirationTime(Dates.addDays(now, 364));
        process.setType(_type);
        process.setMacroprocess(getMacroprocess());
        process.buildAttributes(bussinesEntityService);
        return process;
    }

    @TransactionAttribute
    public String saveProcess() {
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            getInstance().setAuthor(this.profile);
            create(getInstance());
        }
        if(getOutcome()==null){
                 return  "/pages/management/organization/processmap.xhtml?organizationId="+ getInstance().getMacroprocess().getTheme().getOrganization().getId() +"&faces-redirect=true&includeViewParams=true";
        }
        if (getMacroprocessId() != null) {
            return getOutcome() + "?organizationId=" + getMacroprocess().getTheme().getOrganization().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    @TransactionAttribute
    public String saveAnotherProcess() {
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        getInstance().setAuthor(this.profile);
        getInstance().setMacroprocess(getMacroprocess());
        create(getInstance());

        if (getMacroprocessId() != null) {
            return "/pages/management/process/process?" + "?macroprocessId=" + getMacroprocessId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public boolean isWired() {
        return true;
    }

    public Process getDefiniedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Process> getEntityClass() {
        return Process.class;
    }

    @Transactional
    public String deleteProcess() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Process is Null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe un proceso para ser borrado!", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        if(getOutcome()==null){
                     return  "/pages/management/organization/processmap.xhtml?organizationId="+ getInstance().getMacroprocess().getTheme().getOrganization().getId() +"&faces-redirect=true&includeViewParams=true";
        }
        if (getMacroprocessId() != null) {
            return getOutcome() + "?organizationId=" + getMacroprocess().getTheme().getOrganization().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

   public void createNewProcess() {
        setId(null);
        setInstance(null);
        wire();
    }

    public void editProcess(Long id) {
        setId(id);
        load();
    }
}
