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
package org.eqaula.glue.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.eqaula.glue.model.BussinesEntity;
import org.eqaula.glue.model.BussinesEntityAttribute;
import org.eqaula.glue.model.Property;
import org.eqaula.glue.model.management.Organization;
import org.eqaula.glue.service.BussinesEntityService;
import org.eqaula.glue.service.OrganizationService;
import org.jboss.seam.transaction.Transactional;

/**
 *
 * @author jlgranda
 */
@TransactionAttribute
public abstract class BussinesEntityHome<E> extends Home<EntityManager, E> implements Serializable {

    private static final long serialVersionUID = -8910921676468441272L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(BussinesEntityHome.class);
    @Inject
    protected BussinesEntityService bussinesEntityService;
    @Inject
    protected OrganizationService organizationService;
    protected BussinesEntity bussinesEntity;
    private Property property;
    private String outcome;
    private String command;
    private Long organizationId;
    private Organization organization;
    private boolean editionEnabled = true;
    /**
     * Bandera para detectar cambios
     */
    private boolean modified;

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public boolean isEditionEnabled() {
        return editionEnabled;
    }

    public void setEditionEnabled(boolean editionEnabled) {
        this.editionEnabled = editionEnabled;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    @Transactional
    public Organization getOrganization() {
        if (organization == null) {
            if (organizationId == null) {
                organization = null;
            } else {                
                organization = organizationService.find(getOrganizationId());
            }

        }
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @Override
    public void create() {
        super.create();
        if (getEntityManager() == null) {
            throw new IllegalStateException("entityManager is null");
        }
    }

    public boolean isManaged() {
        return getInstance() != null
                && getEntityManager().contains(getInstance());
    }

    @Transactional
    public String update() {
        super.save(getInstance());
        getEntityManager().flush();
        updatedMessage();
        return "updated";
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String persist() {
        super.create(getInstance());
        createdMessage();
        return "persisted";
    }

    @Transactional
    public String remove() {
        super.delete(getInstance());
        getEntityManager().flush();
        deletedMessage();
        return "removed";
    }

    @Transactional
    @Override
    public E find() {
        if (getEntityManager().isOpen()) {
            E result = loadInstance();
            if (result == null) {
                result = handleNotFound();
            }
            return result;
        } else {
            return null;
        }
    }

    protected E loadInstance() {
        return getEntityManager().find(getEntityClass(), getId());
    }

    @Override
    protected String getPersistenceContextName() {
        return "entityManager";
    }

    @Override
    protected String getEntityName() {
        //return PersistenceProvider.instance().getName(getInstance(), getEntityManager());
        return "empty";
    }

    public BussinesEntity getBussinesEntity() {
        return bussinesEntity;
    }

    public void setBussinesEntity(BussinesEntity bussinesEntity) {
        this.bussinesEntity = bussinesEntity;
    }

    //protected abstract void buildAttributes(E entity);
    @Deprecated
    protected BussinesEntityAttribute buildAtribute(Property p) {
        BussinesEntityAttribute attribute = new BussinesEntityAttribute();
        attribute.setName(p.getName());
        attribute.setType(p.getType());
        attribute.setValue((Serializable) p.getValue());
        attribute.setProperty(p); //Relaciona los objetos de forma directa
        return attribute;
    }

    /**
     * Return a List of BussinesEntityAttribute for structure names
     *
     * @param names
     * @return a List of BussinesEntityAttribute for structure names
     */
    public List<BussinesEntityAttribute> findAttributes(final String... names) {

        List<BussinesEntityAttribute> _buffer = new ArrayList<BussinesEntityAttribute>();
        for (String name : names) {
            if (attrs.containsKey(name)) {
                _buffer.addAll(attrs.get(name));
            } else {
                _buffer.addAll(((BussinesEntity) getInstance()).getBussinessEntityAttributes(name));
                putAttributesIntoMap(name, _buffer);
            }
        }
        return _buffer;
    }
    protected Map<String, List<BussinesEntityAttribute>> attrs = new HashMap<String, List<BussinesEntityAttribute>>();

    public void putAttributesIntoMap(String key, List<BussinesEntityAttribute> _attrs) {
        attrs.put(key, _attrs);
    }

    /**
     * Return list of BussinesEntityAttribute for BussinesEntityType names lists
     *
     * @param names list of names for BussinessEntityType
     * @return list of BussinesEntityAttribute for BussinesEntityType names
     * lists
     */
    public List<BussinesEntityAttribute> findBussinesEntityAttribute(final String names) {
        if (getInstance() == null) {
            return new ArrayList<BussinesEntityAttribute>();
        }

        List<BussinesEntityAttribute> temp = findAttributes(names.split(","));

        return temp;
    }

    public String resolveOutcome() {
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }
  
}
