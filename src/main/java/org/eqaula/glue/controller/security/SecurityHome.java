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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.profile.ProfileService;
import org.eqaula.glue.util.UI;
import org.jboss.seam.international.status.Messages;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.jboss.seam.transaction.Transactional;
import org.picketlink.idm.api.Group;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.User;
import org.picketlink.idm.common.exception.IdentityException;
import org.picketlink.idm.impl.api.model.SimpleUser;
import org.primefaces.context.RequestContext;

/**
 *
 * @author cesar
 */
@Named(value = "securityHome")
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
    private SecurityGroupService securityGroupService;
    private Messages msg;
    private String username;
    private Group group;
    private User user;

    public SecurityHome() {
    }

    @PostConstruct
    public void init() {
        log.info("eqaula --> init SecurityHome ");
        ps.setEntityManager(em);
        securityGroupService.setEntityManager(em);
        securityGroupService.setSecurity(security);

    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    private User getUser() throws IdentityException {
        if (this.user == null) {
            user = new SimpleUser(getUsername());
            if (getUsername() != null && !getUsername().isEmpty()) {
                user = securityGroupService.findUser(getUsername());
            }
        }
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Transactional
    public void associateTo() {
        try {
            if (getGroup() != null && getUser() != null) {
                if (!securityGroupService.isAssociated(group, user)) {
                    securityGroupService.associate(getGroup(), getUser());
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Authorization was established succesfully into " + getGroup().getName() + " for " + getUser().getKey(), null));
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Authorization is assined for " + getGroup().getName() + " and " + getUser().getKey(), null));
                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cann't assign authorization for " + getGroup().getName() + " and " + getUser().getKey(), null));
            }

        } catch (IdentityException ex) {
            Logger.getLogger(SecurityHome.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Transactional
    public void disassociateTo() {
        try {
            if (getGroup() != null && getUser() != null) {
                if (securityGroupService.isAssociated(group, user)) {
                    securityGroupService.disassociate(getGroup(), getUser());
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Disassociated  was established succesfully into" + getGroup().getName() + " for " + getUser().getKey(), null));
                    RequestContext.getCurrentInstance().execute("deletedDlg.hide();");
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Not is assined for " + getGroup().getName() + " and " + getUser().getKey(), null));
                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cann't assign authorization for " + getGroup().getName() + " and " + getUser().getKey(), null));
            }

        } catch (IdentityException ex) {
            Logger.getLogger(SecurityHome.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Retorna la lista de Grupos de autorización como una lista de SelectedItem
     *
     * @return
     */
    public List<SelectItem> getGroupsAsSelectItem() {
        List<SelectItem> items = new ArrayList<SelectItem>();
        SelectItem item = null;
        item = new SelectItem(null, UI.getMessages("common.choice"));
        items.add(item);
        try {
            for (org.picketlink.idm.api.Group g : security.getPersistenceManager().findGroup("GROUP")) {
                item = new SelectItem(g, g.getName());
                items.add(item);
                log.info("eqaula --> Items add grupo " + item.getValue());
            }
        } catch (IdentityException ex) {
            Logger.getLogger(SecurityHome.class.getName()).log(Level.SEVERE, null, ex);
        }
        return items;
    }

    public List<Group> findAllGroups() {
        try {
            return new ArrayList<Group>(security.getPersistenceManager().findGroup("GROUP"));
        } catch (IdentityException ex) {
            Logger.getLogger(SecurityHome.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<Group>();
    }

    public List<Group> findGroups() {
        List<Group> groups = new ArrayList<Group>();
        try {
            groups = new ArrayList<Group>(securityGroupService.find(getUser()));
        } catch (IdentityException ex) {
            Logger.getLogger(SecurityHome.class.getName()).log(Level.SEVERE, null, ex);
        }
        return groups;
    }     
    
}
