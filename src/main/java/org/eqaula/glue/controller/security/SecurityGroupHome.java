/*
 * Copyright 2013 cesar.
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
package org.eqaula.glue.controller.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.eqaula.glue.controller.profile.ProfileHome;
import org.eqaula.glue.profile.ProfileService;
import org.hibernate.mapping.Collection;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.GroupImpl;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.management.picketlink.IdentitySessionProducer;
import org.jboss.seam.transaction.Transactional;
import org.picketlink.idm.api.Group;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.IdentitySessionFactory;
import org.picketlink.idm.common.exception.IdentityException;
import org.picketlink.idm.impl.api.model.SimpleGroup;

/**
 *
 * @author cesar
 */
@Named
@ViewScoped
public class SecurityGroupHome extends BussinesEntityHome<Group> implements Serializable {

    private static final long serialVersionUID = 7632987414391869389L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(SecurityGroupHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Inject
    private Identity identity;
    @Inject
    private Credentials credentials;
    @Inject
    private IdentitySession security;
    @Inject
    private SecurityGroupService securityGroupService;
    @Inject
    private SecurityGroupService sgs;    
    private String groupName;
    private String groupType;

    public Long getGroupId() {
        return (Long) getId();
    }

    public void setGroupId(Long groupId) {
        setId(groupId);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupType() {
        if (this.isPersistent()) {
            groupType = getInstance().getGroupType();
        }
        return groupType;
    }
    
    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public Class<Group> getEntityClass() {
        return Group.class;
    }

    @PostConstruct
    public void init() {
        //initSesion();
        setEntityManager(em);
        try {
            log.info("eqaula security -->  Inicio Security group");

        } catch (Exception e) {
        }

    }

    @TransactionAttribute
    private void createGroup() throws IdentityException {
        Group u = security.getPersistenceManager().createGroup(getGroupName(), getGroupType());
        log.info("Eqaula SecurityGroup -- new group ");
    }

    @TransactionAttribute
    public String saveGroup() {
        if (groupName != null) {
            try {
                //security.getPersistenceManager().removeGroup(getInstance(), false);
                if (securityGroupService.findByName(groupName) != null) {                    
                    createGroup();                    
                } else {
                    createGroup();
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Se agrego correctamente grupo ", null));
                    log.info("Eqaula  save new 1");
                }

            } catch (IdentityException ex) {
                Logger.getLogger(Group.class.getName()).log(Level.SEVERE, null, ex);
                log.info("Eqaula error save new");
            }

        }
        return "/pages/admin/security/list?faces-redirect=true";
    }

    @Override
    protected Group createInstance() {
        Group u = new SimpleGroup("", "GROUP");
        return u;
    }

    @TransactionAttribute
    public Group load() {
        log.info("Eqaula SecurityGroup load");
        if (this.isPersistent()) {
        } else {
            if (identity.isLoggedIn()) {
                try {
                    setInstance(security.getPersistenceManager().findGroupByKey(this.groupName));
                    log.info("Eqaula  load");
                } catch (IdentityException ex) {
                    log.info("Eqaula  load");
                    Logger.getLogger(SecurityGroupHome.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                log.info("Eqaula");
            }
        }
        return getInstance();
    }

    @Transactional
    public String deleteGroup() {
        
        return "/pages/admin/security/list?faces-redirect=true";
    }

    @TransactionAttribute
    public void wire() {
        getInstance();
    }

    public List<String> TypesGroup() {
        List<String> type = em.createQuery("select t from IdentityObjectType t").getResultList();
        log.info("typeGroup list... " + type.get(0).toString());
        return type;
    }

    public List<Group> getGroups() throws IdentityException {
        List<Group> groups = (List<Group>) security.getPersistenceManager().findGroup("GROUP");
        return groups;
    }

    public boolean isPersistent() {
        return getInstance() != null;
    }
}
