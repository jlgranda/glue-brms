/*
 * Copyright 2012 jlgranda.
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
package org.eqaula.glue.controller.profile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.BussinesEntityHome;
import org.eqaula.glue.model.BussinesEntity;
import org.eqaula.glue.model.BussinesEntityAttribute;
import org.eqaula.glue.model.BussinesEntityType;
import org.eqaula.glue.model.Group;
import org.eqaula.glue.model.Property;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;

/**
 *
 * @author jlgranda
 */
@Named
@ViewScoped
public class ProfileHome extends BussinesEntityHome<Profile> implements Serializable {

    private static final long serialVersionUID = 7632987414391869389L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(ProfileHome.class);
    @Inject
    @Web
    private EntityManager em;
    private String structureName;

    public Long getProfileId() {
        return (Long) getId();
    }

    public void setProfileId(Long profileId) {
        setId(profileId);
    }

    public String getStructureName() {
        return structureName;
    }

    public void setStructureName(String structureName) {
        this.structureName = structureName;
    }

    @Override
    protected Profile createInstance() {

        Date now = Calendar.getInstance().getTime();
        Profile profile = new Profile();
        profile.setCreatedOn(now);
        profile.setLastUpdate(now);
        profile.setActivationTime(now);
        profile.setExpirationTime(Dates.addDays(now, 364));
        profile.setAuthor(null); //Establecer al usuario actual
        profile.buildAttributes(bussinesEntityService);
        return profile;
    }

    @TransactionAttribute
    public void load() {
        log.info("eqaula --> Loading instance from ProfileHome for " + getId());
        if (isIdDefined()) {
            wire();
        }
        log.info("eqaula --> Loaded instance" + getInstance());
    }

    @TransactionAttribute
    public void wire() {
        getInstance();
    }

    public boolean isWired() {
        return true;
    }

    public Profile getDefinedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @PostConstruct
    public void init() {
        setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
    }

    @Override
    public Class<Profile> getEntityClass() {
        return Profile.class;
    }

    @TransactionAttribute
    public String saveAjax() {
        Date now = Calendar.getInstance().getTime();
        log.info("eqaula --> saving " + getInstance().getName());
        getInstance().setLastUpdate(now);
        for(BussinesEntityAttribute a : getInstance().getAttributes()){
            if (a.getProperty().getType().equals("java.lang.String") && a.getValue() == null){
                a.setValue(a.getName());
                a.setStringValue(a.getName());
            }
        }
        save(getInstance());
        return "/pages/home?faces-redirect=true";
    }

    @TransactionAttribute
    public void displayBootcampAjax() {
        getInstance().setShowBootcamp(true);
        update();
    }

    @TransactionAttribute
    public void dismissBootcampAjax() {
        getInstance().setShowBootcamp(false);
        update();
    }

    @Transactional
    public void addBussinesEntity(Group group) {
        Date now = Calendar.getInstance().getTime();
        String name = "Nuevo " + (group.getProperty() != null ? group.getProperty().getLabel() : "elemento") + " " + (group.findOtherMembers(this.getInstance()).size() + 1);
        BussinesEntity entity = new BussinesEntity();
        entity.setName(name);
        //TODO implementar generador de códigos para entidad de negocio
        entity.setCode((group.getProperty() != null ? group.getProperty().getLabel() : "elemento") + " " + (group.findOtherMembers(this.getInstance()).size() + 1));
        entity.setCreatedOn(now);
        entity.setLastUpdate(now);
        entity.setActivationTime(now);
        entity.setExpirationTime(Dates.addDays(now, 364));
        entity.setAuthor(null); //Establecer al usuario actual
        entity.buildAttributes(bussinesEntityService);
        log.info("eqaula --> start attributes for " + group.getName() + " into entity " + entity.getName() + "");
        //buildAttributesFor(entity, group.getName());
        //Set default values into dinamycs properties
        //TODO idear un mecanismo generico de inicialización de variables dinamicas
        //entity.getBussinessEntityAttribute("title").setValue(name);

        group.add(entity);

        setBussinesEntity(entity); //Establecer para edición
    }

    @Transactional
    public void saveBussinesEntity() {

        try {
            if (getBussinesEntity() == null) {
                throw new NullPointerException("bussinessEntity is null");
            }

            if (getBussinesEntity().getName().equalsIgnoreCase("")) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "No name", null));
            } else {
                save(getBussinesEntity());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se actualizó con exito " + bussinesEntity.getName(), ""));
            }

        } catch (Exception e) {
            System.out.println("saveBussinesEntity ERROR = " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
    }
}
