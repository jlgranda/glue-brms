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
package org.eqaula.glue.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eqaula.glue.model.management.Diagnostic;
import org.eqaula.glue.model.management.Diagnostic_;
import org.eqaula.glue.model.management.Owner;
import org.eqaula.glue.model.management.Owner_;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.PersistenceUtil;

/**
 *
 * @author dianita
 */
public class DiagnosticService extends PersistenceUtil<Diagnostic> implements Serializable {
    private static final long serialVersionUID = -1361321355690263386L;
    private static final org.jboss.solder.logging.Logger LOG = org.jboss.solder.logging.Logger.getLogger(BussinesEntityService.class);

    public DiagnosticService() {
        super(Diagnostic.class);
    }
    
    @Override
    public void setEntityManager(EntityManager em){
        this.em=em;
    }
    
    public Long count(){
        return count(Diagnostic.class);
    }
    
    public List<Diagnostic> getDiagnostics(){
        return findAll(Diagnostic.class);
    }
    
    public List<Diagnostic> getDiagnostics(final int limit, final int offset){
        return findAll(Diagnostic.class);
    }
    
    public Diagnostic getDiagnosticById(final Long id){
        return (Diagnostic)findById(Diagnostic.class, id);
    }
            
    public Diagnostic findByName(final String name){
        LOG.info("find Diagnostic with name "+ name);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Diagnostic> query = builder.createQuery(Diagnostic.class);
        Root<Diagnostic> bussinesEntityType= query.from(Diagnostic.class);
        query.where(builder.equal(bussinesEntityType.get(Diagnostic_.name), name));
        return getSingleResult(query);
    }
    
    public List<Diagnostic> findByOwner(Owner owner) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Diagnostic> query = builder.createQuery(Diagnostic.class);
        Root<Diagnostic> bussinesEntityType = query.from(Diagnostic.class);
        query.where(builder.equal(bussinesEntityType.get(Diagnostic_.owner), owner));
        return getResultList(query);
    }
    
    public List<Diagnostic> findByProfile(Profile profile) {
        LOG.info("find Diagnostic with profile " + profile);
        if (profile == null) return new ArrayList<Diagnostic>(0);

        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Diagnostic> query = builder.createQuery(Diagnostic.class);
        Root<Diagnostic> bussinesEntityType = query.from(Diagnostic.class);
        query.where(builder.equal(bussinesEntityType.get(Diagnostic_.author), profile));
        //TODO agregar busqueda por organizationCode
        return getResultList(query);
    }

    public Diagnostic findByProfileAndCode(Profile profile, String diagnosticCode) {
        LOG.info("find Owner with profile " + profile + " owner code " + diagnosticCode);
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Diagnostic> query = builder.createQuery(Diagnostic.class);
        Root<Diagnostic> bussinesEntityType = query.from(Diagnostic.class);
        query.where(builder.equal(bussinesEntityType.get(Diagnostic_.author), profile));
        //TODO agregar busqueda por organizationCode
        return getSingleResult(query);
    }

    public void create(Profile loggedIn, Diagnostic current) {
        current.setAuthor(loggedIn);
        create(current);
    }
    
    
}
