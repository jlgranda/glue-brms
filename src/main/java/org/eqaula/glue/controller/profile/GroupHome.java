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
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.BussinesEntityHome;
import org.eqaula.glue.model.BussinesEntity;
import org.eqaula.glue.model.BussinesEntityAttribute;
import org.eqaula.glue.model.Group;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

/**
 *
 * @author jlgranda
 */
@Named
@ViewScoped
public class GroupHome extends BussinesEntityHome<Group> implements Serializable {

    private static final long serialVersionUID = 3338389062654114362L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(GroupHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Inject
    private ProfileHome profileHome;
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
        log.info("eqaula --> Loading instance from GroupHome for " + getGroupId());
        log.info("eqaula --> Required profile " + getProfileId());
        if (isIdDefined()) {
            wire();
            loadProfileHome(); //Cargar el objeto ProfileHome relacionado al grupo
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

    @Transactional
    public void addBussinesEntity(Group group) {
        //comparar con maximum + 1, ya que el mismo objeto es parte del grupo
        if (group.getProperty().getMaximumMembers() == 0L || group.getMembers().size() < group.getProperty().getMaximumMembers() + 1) {
            Date now = Calendar.getInstance().getTime();
            //TODO internacionalizar cadenas estáticas
            String name = "Nuevo " + (group.getProperty() != null ? group.getProperty().getLabel() : "elemento") + " " + (group.findOtherMembers(getProfile()).size() + 1);
            BussinesEntity entity = new BussinesEntity();
            entity.setName(name);
            //TODO implementar generador de códigos para entidad de negocio
            entity.setCode((group.getProperty() != null ? group.getProperty().getLabel() : "elemento") + " " + (group.findOtherMembers(getProfile()).size() + 1));
            entity.setCreatedOn(now);
            entity.setLastUpdate(now);
            entity.setActivationTime(now);
            entity.setExpirationTime(Dates.addDays(now, 364));
            entity.setAuthor(null); //Establecer al usuario actual
            entity.buildAttributes(getInstance().getName(), bussinesEntityService); //Construir atributos de grupos
            group.add(entity);

            setBussinesEntity(entity); //Establecer para edición
            RequestContext.getCurrentInstance().execute("editDlg.show()"); //abrir el popup si se añadió correctamente
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "No es posible agregar más " + group.getProperty().getLabel() + ". El número máximo es " + group.getProperty().getMaximumMembers(), ""));
        }
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
                boolean validate = true;
                for (BussinesEntityAttribute a : getBussinesEntity().getAttributes()) {
                    if (a.getValue() == null && a.getProperty().isRequired()) {
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se actualizó con exito " + bussinesEntity.getName(), ""));
                        validate = false;
                    } else {
                        a.setStringValue(a.getValue() == null ? a.getProperty().getName() : a.getValue().toString());
                    }

                }
                if (validate) {
                    save(getBussinesEntity());
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se actualizó con exito " + bussinesEntity.getName(), ""));
                    RequestContext.getCurrentInstance().execute("editDlg.hide()"); //cerrar el popup si se grabo correctamente
                    setBussinesEntity(null); //liberar de memoria el objeto seleccionado
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Hay algunos valores requeridos vacios para " + bussinesEntity.getName() + ". Recuerde los campos con (*) son obligatorios", ""));
                }

            }

        } catch (Exception e) {
            System.out.println("saveBussinesEntity ERROR = " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
    }

    @Transactional
    public void deleteBussinessEntity() {
        try {
            if (getBussinesEntity() == null) {
                throw new NullPointerException("bussinessEntity is null");
            }

            if (getBussinesEntity().isPersistent()) {
                delete(getBussinesEntity());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Hemos completado con exito la operación de borrado de " + bussinesEntity.getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()"); //cerrar el popup si se grabo correctamente
                setBussinesEntity(null); //liberar de memoria el objeto seleccionado
            } else {
                //remover de la lista, si aún no esta persistido
                getInstance().getMembers().remove(this.getBussinesEntity());
                RequestContext.getCurrentInstance().execute("editDlg.hide()"); //cerrar el popup si se grabo correctamente
            }

        } catch (Exception e) {
            System.out.println("saveBussinesEntity ERROR = " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
    }

    public void onRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage("BussinesEntity Selected ", ((BussinesEntity) event.getObject()).getName());

        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage("BussinesEntity Unselected ", ((BussinesEntity) event.getObject()).getName());

        FacesContext.getCurrentInstance().addMessage(null, msg);
        this.setBussinesEntity(null);
    }

    private void loadProfileHome() {
        if (getProfileId() != null) {
            profileHome.setProfileId(profileId);
            profileHome.load();
        }
    }

    public Profile getProfile() {
        return profileHome.getInstance();
    }
}
