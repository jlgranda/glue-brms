/*
 * Copyright 2012 cesar.
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
package org.eqaula.glue.controller;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.apache.commons.lang.SerializationUtils;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.profile.ProfileHome;
import org.eqaula.glue.model.Property;
import org.eqaula.glue.service.BussinesEntityTypeService;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

/**
 *
 * @author cesar
 */
@Named
@ViewScoped
public class PropertyHome extends BussinesEntityHome<Property> implements Serializable {

    private static final long serialVersionUID = 7632987414391869389L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(ProfileHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Inject
    private BussinesEntityTypeHome bussinesEntityTypeHome;
    @Inject
    private BussinesEntityTypeService bussinesEntityTypeService;
    private Long structureId;
    private String propertyStringValue;

    public PropertyHome() {
        log.info("eqaula --> Inicializo Property Home");
    }

    public Long getPropertyId() {
        return (Long) getId();
    }

    public void setPropertyId(Long propertyId) {
        setId(propertyId);
    }

    public Long getStructureId() {
        return structureId;
    }

    public void setStructureId(Long structureId) {
        this.structureId = structureId;
    }

    public String getPropertyStringValue() {

        if (this.propertyStringValue != null) {
            setPropertyStringValue((String) getInstance().getValue());
        }
        return propertyStringValue;
    }

    public void setPropertyStringValue(String propertyStringValue) {
        this.propertyStringValue = propertyStringValue;
    }

    @Override
    protected Property createInstance() {
        Property property = new Property();

        return property;
    }

    @TransactionAttribute
    public void load() {
        log.info("eqaula --> Loading instance from Property for " + getId());
        if (isIdDefined()) {
            wire();
        }
        log.info("eqaula --> Loaded instance " + getInstance());
    }

    @TransactionAttribute
    public void wire() {
        getInstance();
    }

    public boolean isWired() {
        return true;
    }

    public Property getDefinedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @PostConstruct
    public void init() {
        setEntityManager(em);
        bussinesEntityTypeService.setEntityManager(em);
    }

    @Override
    public Class<Property> getEntityClass() {
        return Property.class;
    }

    @TransactionAttribute
    public String saveProperty() {
        log.info("eqaula --> saving " + getInstance().getName());

        if (getInstance().getId() != null) {
            this.getInstance().setValue(converterToType(propertyStringValue));
            save(getInstance());
        } else {
            try {
                log.info("eqaula --> saving new" + getInstance().getName());
                log.info("eqaula --> id for " + getStructureId());
                //bussinesEntityTypeHome.getInstance().getStructures().get(0).getProperties().add(this.getInstance());
                this.getInstance().setStructure(bussinesEntityTypeHome.getInstance().getStructures().get(0));
                save(getInstance());

            } catch (Exception ex) {
                log.info("eqaula --> error saving new" + getInstance().getName());
            }
        }

        return "/pages/admin/bussinesentitytype/bussinesentitytype";
    }

    public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage("Property Selected ", ((Property) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage("Property Unselected ", ((Property) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
        this.setBussinesEntity(null);
    }

    public Serializable converterToType(String value) {

        Object o = new Object();
        try {
            if ("java.lang.String".equals(getInstance().getType()) || "java.lang.String[]".equals(getInstance().getType()) || "java.lang.MultiLineString".equals(getInstance().getType())) {
                o = value;
            } else if ("java.lang.Long".equals(getInstance().getType())) {
                o = Long.valueOf(value);
            } else if ("java.lang.Float".equals(getInstance().getType())) {
                o = Float.valueOf(value);
            } else if ("java.lang.Boolena".equals(getInstance().getType())) {
                o = Boolean.valueOf(value);
            } else if ("java.util.Date".equals(getInstance().getType())) {
                SimpleDateFormat sdf;
                sdf = new SimpleDateFormat("dd/MM/yyyy");
                Date fec = null;
                try {
                    fec = sdf.parse(value);
                } catch (ParseException exFecha) {
                    log.info("eqaula --> error converter date");
                }
                o = sdf;
            } else if ("java.lang.Double".equals(getInstance().getType())) {
                o = Double.valueOf(value);
            } else {
                o = value;
            }
        } catch (Exception e) {
            log.info("eqaula --> error converter: " + value);
        }
        return (Serializable) o;

    }
}
