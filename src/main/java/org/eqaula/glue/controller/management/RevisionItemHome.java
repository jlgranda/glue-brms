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
import org.eqaula.glue.model.management.RevisionItem;
import org.eqaula.glue.model.management.Section;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.service.SectionService;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.context.RequestContext;

/*
 * @author dianita
 */


@Named
@ViewScoped
public class RevisionItemHome extends BussinesEntityHome<RevisionItem> implements Serializable{
    private static final long serialVersionUID = 2761195215890197804L;
    
    @Inject
    @Web
    private EntityManager em;
    @Current
    @Inject
    private Profile profile;
    private Section section;
    private Long sectionId;
    @Inject
    private SectionService sectionService;

    public RevisionItemHome() {
    }

    public Long getRevisionItemId() {
        return (Long) getId();
    }

    public void setRevisionItemId(Long revisionItemId) {
        setId(revisionItemId);
    }

    public String getStructureName() {
        return getInstance().getName();
    }

    public Section getSection() {
        if (section == null) {
            if (sectionId == null) {
                section = null;
            } else {
                section = sectionService.find(getSectionId());
            }
        }
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
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
        sectionService.setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
    }

    @Override
    protected RevisionItem createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(RevisionItem.class.getName());
        Date now = Calendar.getInstance().getTime();
        RevisionItem revisionItem = new RevisionItem();
        revisionItem.setCode(UUID.randomUUID().toString());
        revisionItem.setCreatedOn(now);
        revisionItem.setLastUpdate(now);
        revisionItem.setActivationTime(now);
        revisionItem.setExpirationTime(Dates.addDays(now, 364));
        revisionItem.setType(_type);
        revisionItem.buildAttributes(bussinesEntityService);
        revisionItem.setSection(getSection());
        return revisionItem;
    }

    @TransactionAttribute
    public String saveRevisionItem() {

        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            getInstance().setAuthor(this.profile);
            create(getInstance());
        }
        if (getInstance().getSection().getId() != null) {
            return getOutcome() + "?diagnosticId=" + getInstance().getSection().getDiagnostic().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public boolean isWired() {
        return true;
    }

    public RevisionItem getDefiniedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<RevisionItem> getEntityClass() {
        return RevisionItem.class;
    }

    @Transactional
    public String deleteRevisionItem() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("RevisionItem is Null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe un item para ser borrado!", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        //if (getDiagnosticId()!= null) {
        //    return getOutcome() + "?diagnosticId=" + getDiagnosticId() + "&faces-redirect=true&includeViewParams=true";
        //}
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    
    
}
