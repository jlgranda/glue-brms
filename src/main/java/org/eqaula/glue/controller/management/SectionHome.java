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
import org.eqaula.glue.model.management.Section;
import org.eqaula.glue.model.management.Diagnostic;
import org.eqaula.glue.model.management.Owner;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.service.BussinesEntityService;
import org.eqaula.glue.service.DiagnosticService;
import org.eqaula.glue.service.OwnerService;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.context.RequestContext;


/*
 * @author dianita
 */
@Named
@ViewScoped
public class SectionHome extends BussinesEntityHome<Section> implements Serializable {

    private static final long serialVersionUID = 9048490549279855474L;
    @Inject
    @Web
    private EntityManager em;
    @Inject
    private BussinesEntityService bussinesEntityService;
    @Current
    @Inject
    private Profile profile;
    private Diagnostic diagnostic;
    private Long diagnosticId;
    @Inject
    private DiagnosticService diagnosticService;

    public SectionHome() {
    }

    public Long getSectionId() {
        return (Long) getId();
    }

    public void setSectionId(Long sectionId) {
        setId(sectionId);
    }

    public String getStructureName() {
        return getInstance().getName();
    }

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

    public Long getDiagnosticId() {
        return diagnosticId;
    }

    public void setDiagnosticId(Long diagnosticId) {
        this.diagnosticId = diagnosticId;
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
        diagnosticService.setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
    }

    @Override
    protected Section createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Section.class.getName());
        Date now = Calendar.getInstance().getTime();
        Section section = new Section();
        section.setCode(UUID.randomUUID().toString());
        section.setCreatedOn(now);
        section.setLastUpdate(now);
        section.setActivationTime(now);
        section.setExpirationTime(Dates.addDays(now, 364));
        section.setType(_type);
        section.buildAttributes(bussinesEntityService);
        return section;
    }

    @TransactionAttribute
    public String saveSection() {

        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            getInstance().setAuthor(this.profile);
            getInstance().setDiagnostic(getDiagnostic());
            create(getInstance());
        }
        if (getInstance().getDiagnostic().getId()!= null) {
            return getOutcome() + "?diagnosticId=" + getInstance().getDiagnostic().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public boolean isWired() {
        return true;
    }

    public Section getDefiniedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Section> getEntityClass() {
        return Section.class;
    }

    @Transactional
    public String deleteSection() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Section is Null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe una sección para ser borrada!", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        if (getInstance().getDiagnostic().getId()!= null) {
            return getOutcome() + "?diagnosticId=" +getInstance().getDiagnostic().getId()+ "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    @Override
    public String getCanonicalPath() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
