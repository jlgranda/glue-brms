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
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.BussinesEntityHome;
import org.eqaula.glue.model.BussinesEntityAttribute;
import org.eqaula.glue.model.BussinesEntityType;
import org.eqaula.glue.model.Group;
import org.eqaula.glue.model.Property;
import org.eqaula.glue.model.Structure;
import org.eqaula.glue.util.Dates;

/**
 *
 * @author jlgranda
 */
@Named
@ViewScoped
public class GroupHome extends BussinesEntityHome<Group> implements Serializable {

    private static final long serialVersionUID = 3338389062654114362L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(ProfileHome.class);
    @Inject
    @Web
    private EntityManager em;
    
    private Long profileId;
    private String name;

    public Long getGroupId() {
        return (Long) getId();
    }

    public void setGroupId(Long groupId) {
        setId(groupId);
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    

    @Override
    protected Group createInstance() {

        Date now = Calendar.getInstance().getTime();
        Group group = new Group();
        group.setCreatedOn(now);
        group.setLastUpdate(now);
        group.setActivationTime(now);
        group.setExpirationTime(Dates.addDays(now, 364));
        group.setAuthor(null); //Establecer al usuario actual
        group.buildAttributes(bussinesEntityService);
        return group;
    }

    @TransactionAttribute
    public void load() {
        log.info("eqaula --> Loading instance from ProfileHome for " + getId());
        if (isIdDefined()) {
            wire();
        } else {
            //TODO ver forma de cargas con profileId y groupName
            //Group g = bussinesEntityService.findByName(name)
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

    public Group getDefinedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @PostConstruct
    public void init() {
        setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
        //wire();
    }

    @Override
    public Class<Group> getEntityClass() {
        return Group.class;
    }

    @TransactionAttribute
    public void saveAjax() {
        if (isManaged()) {
            update();
        } else {
            persist();
        }
    }

}
