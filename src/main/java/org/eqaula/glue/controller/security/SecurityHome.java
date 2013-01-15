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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.profile.ProfileHome;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.profile.ProfileService;
import org.jboss.seam.international.status.Messages;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.management.picketlink.IdentitySessionProducer;
import org.picketlink.idm.api.Group;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.IdentitySessionFactory;
import org.picketlink.idm.api.User;
import org.picketlink.idm.common.exception.IdentityException;

/**
 *
 * @author cesar
 */
@Named
@ViewScoped
public class SecurityHome implements Serializable{
    
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
    private Messages msg;
    private Long profileId;

    
    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    } 
    
    public void initSesion() throws IdentityException{
        Map<String, Object> sessionOptions = new HashMap<String, Object>();
        sessionOptions.put(IdentitySessionProducer.SESSION_OPTION_ENTITY_MANAGER, em);
        security = identitySessionFactory.createIdentitySession("default", sessionOptions);
    }
    
    @PostConstruct
    public void init() {        
        ps.setEntityManager(em);        
    }
    
    
    //TODO: obtener el usuarios
    public User getUser() throws IdentityException{
        initSesion();
         if (identity.isLoggedIn()){
                Profile p = ps.getProfileById(profileId);
                return security.getPersistenceManager().findUser(p.getUsername());
            } else {
                return null;
            }
    }
    //TODO: crear un grupo 
    public Group getGroup(){
        
        return null;
    }
    //TODO: buscar un grupo
//    public List<Group> getGroup(){
//        List<Group> groups = (List<Group>) security.getPersistenceManager().findGroup(null); 
//        return null;
//    }
    //TODO: asignar el usuario a un grupo 
    public void associateUserGroup(){
        
    }
    //TODO: crear roles 
    //TODO: buscar roles 
    //TODO: asignar roles 
}
