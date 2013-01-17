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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import org.eqaula.glue.util.PersistenceUtil;
import org.jboss.seam.security.management.picketlink.IdentitySessionProducer;
import org.picketlink.idm.api.Group;
import org.picketlink.idm.api.IdentitySearchCriteria;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.IdentitySessionFactory;
import org.picketlink.idm.common.exception.IdentityException;
import org.picketlink.idm.impl.api.IdentitySearchCriteriaImpl;

/**
 *
 * @author cesar
 */
public class SecurityGroupService extends PersistenceUtil<Group> {

    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(SecurityGroupService.class);
    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    private EntityManager entityManager;
    @Inject
    private IdentitySessionFactory identitySessionFactory;
    @Inject
    private IdentitySession security;

    public SecurityGroupService() {
        super(Group.class);
        try {
            initSesion();
        } catch (IdentityException ex) {
            Logger.getLogger(Group.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initSesion() throws IdentityException {
        log.info("eqaula --> ingreso a iniciar sesion" );
        //Map<String, Object> sessionOptions = new HashMap<String, Object>();
//        sessionOptions.put(IdentitySessionProducer.SESSION_OPTION_ENTITY_MANAGER, getEntityManager());
//        security = identitySessionFactory.createIdentitySession("default", sessionOptions);
    }

    @Override
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    //metodo count
    public long count() {
        return count(Group.class);
    }

    public List<Group> getGroups() {
        List<Group> groups = null;
        try {
            groups = (List<Group>) security.getPersistenceManager().findGroup("GROUP", new IdentitySearchCriteriaImpl());
        } catch (IdentityException ex) {
            Logger.getLogger(SecurityGroupService.class.getName()).log(Level.SEVERE, null, ex);
        }
        log.info("eqaula --> lista de grupos "+groups.get(0).toString() );
        return groups;
    }

    public Group getGroupById(final Long id) throws IdentityException {
        Group g = security.getPersistenceManager().findGroupByKey(String.valueOf(id));
        log.info("eqaula --> grupo key "+g.getKey());
        return g;
        
    }

    public Group findByName(final String name) throws IdentityException {
        return security.getPersistenceManager().findGroup(name, "GROUP");
    }
}
