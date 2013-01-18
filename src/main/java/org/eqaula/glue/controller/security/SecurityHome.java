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
import org.eqaula.glue.controller.profile.ProfileHome;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.profile.ProfileService;
import org.eqaula.glue.util.UI;
import org.jboss.seam.international.status.Messages;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.management.picketlink.IdentitySessionProducer;
import org.picketlink.idm.api.Group;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.IdentitySessionFactory;
import org.picketlink.idm.api.User;
import org.picketlink.idm.common.exception.IdentityException;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

/**
 *
 * @author cesar
 */
@Named(value="securityHome")
@ViewScoped
public class SecurityHome implements Serializable {

    private static final long serialVersionUID = 7632987414391869389L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(SecurityHome.class);
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
    private ProfileService ps;
    @Inject
    private IdentitySessionFactory identitySessionFactory;    
    @Inject
    private SecurityGroupService securityGroupService;
    private Messages msg;
    private Long profileId;
    private SecurityGroupLasyDataService lazyGroup;
    private List<Group> listGroups = new ArrayList<Group>();
    private Group selectedGroup;

    public SecurityHome() {        
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }    

    public Group getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(Group selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    public List<Group> getListGroups() {                
        return listGroups;
    }

    public void setListGroups(List<Group> listGroups) {
        this.listGroups = listGroups;
    }

    public SecurityGroupLasyDataService getLazyGroup() {
        return lazyGroup;
    }

    public void setLazyGroup(SecurityGroupLasyDataService lazyGroup) {
        this.lazyGroup = lazyGroup;
    }
    
    public void assignGroups(List<Group> g){
        if (g.isEmpty() /*&& getSelectedBussinesEntityType() != null*/) {
            //g = securityGroupService.getGroups();
            log.info("eqaula --> assignGroups " + g);
        }
    }
    
    public void initSesion() throws IdentityException {
        Map<String, Object> sessionOptions = new HashMap<String, Object>();
        sessionOptions.put(IdentitySessionProducer.SESSION_OPTION_ENTITY_MANAGER, em);
        security = identitySessionFactory.createIdentitySession("default", sessionOptions);
    }

    @PostConstruct
    public void init() {
        log.info("eqaula --> init SecurityHome ");
        ps.setEntityManager(em);        
        securityGroupService.setEntityManager(em);
    }

    //TODO: obtener el usuarios
    public User getUser() throws IdentityException {
        initSesion();
        if (identity.isLoggedIn()) {
            return security.getPersistenceManager().findUser(getProfile().getUsername());
        } else {
            return null;
        }
    }

    //TODO: buscar un grupo
//    public List<Group> getGroup(){
//        List<Group> groups = (List<Group>) security.getPersistenceManager().findGroup(null); 
//        return null;
//    }
    //TODO: asignar el usuario a un grupo 
    public void associateUserGroup() {
        try {
            security.getRelationshipManager().associateUser(getSelectedGroup(), getUser());
        } catch (IdentityException ex) {
            Logger.getLogger(SecurityHome.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @TransactionAttribute
    public String saveAssocition() {
        log.info("Eqaula SecurityHome -- save asociatedgroup");
        try {
            if (getUser() != null & getSelectedGroup() != null) {
                associateUserGroup();
            } else {
                log.info("Eqaula SecurityHome -- error save asociatedgroup");
            }
        } catch (IdentityException ex) {
            Logger.getLogger(SecurityHome.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "";

    }

    public Profile getProfile() {        
        return ps.getProfileById(profileId);

    }
    //TODO: crear roles 
    //TODO: buscar roles 
    //TODO: asignar roles
    
    public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("Group") + " " + UI.getMessages("common.selected"), ((Group) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage(UI.getMessages("Group") + " " + UI.getMessages("common.unselected"), ((Group) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
        this.setSelectedGroup(null);
    }
}
