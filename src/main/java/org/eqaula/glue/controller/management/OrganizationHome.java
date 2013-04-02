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
package org.eqaula.glue.controller.management;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.MethodExpressionActionListener;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eqaula.glue.cdi.Current;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.BussinesEntityHome;
import org.eqaula.glue.model.BussinesEntity;
import org.eqaula.glue.model.BussinesEntityType;
import org.eqaula.glue.model.management.Initiative;
import org.eqaula.glue.model.management.Macroprocess;
import org.eqaula.glue.model.management.Measure;
import org.eqaula.glue.model.management.Method;
import org.eqaula.glue.model.management.Target;
import org.eqaula.glue.model.management.Objetive;
import org.eqaula.glue.model.management.Organization;
import org.eqaula.glue.model.management.Owner;
import org.eqaula.glue.model.management.Period;
import org.eqaula.glue.model.management.Resource;
import org.eqaula.glue.model.management.Theme;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.Dates;
import org.eqaula.glue.util.UI;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.component.menuitem.MenuItem;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultMenuModel;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.MenuModel;
import org.primefaces.model.TreeNode;

/**
 *
 * @author cesar
 */
@Named
@ViewScoped
public class OrganizationHome extends BussinesEntityHome<Organization> implements Serializable {

    private static final long serialVersionUID = 7632987414391869389L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(OrganizationHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Current
    @Inject
    private Profile profile;
    private TreeNode organizationNode;
    private TreeNode selectedNode;
    @Inject
    private NavigationHandler navigation;
    @Inject
    private FacesContext context;

    public OrganizationHome() {
    }

    public Long getOrganizationId() {
        return (Long) getId();
    }

    public void setOrganizationId(Long organizationId) {
        setId(organizationId);
    }

    public String getStructureName() {
        return getInstance().getName();
    }

    public TreeNode getOrganizationNode() {
        if (organizationNode == null) {
            buildTree();
        }
        return organizationNode;
    }

    public void setOrganizationNode(TreeNode organizationNode) {
        this.organizationNode = organizationNode;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    @TransactionAttribute
    public void load() {
        if (isIdDefined()) {
            wire();
        }
    }

    @PostConstruct
    public void init() {
        setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
    }

    @TransactionAttribute
    public void wire() {
        getInstance();
    }

    @Override
    protected Organization createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Organization.class.getName());
        Date now = Calendar.getInstance().getTime();
        Organization organization = new Organization();
        organization.setCode(UUID.randomUUID().toString());
        organization.setCreatedOn(now);
        organization.setLastUpdate(now);
        organization.setActivationTime(now);
        organization.setExpirationTime(Dates.addDays(now, 364));
        organization.setType(_type);
        organization.buildAttributes(bussinesEntityService);
        return organization;
    }

    @TransactionAttribute
    public String saveOrganization() {
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            if (this.profile != null && this.profile.isPersistent()) {
                getInstance().setAuthor(this.profile);
            }
            create(getInstance());
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public boolean isWired() {
        return true;
    }

    public Organization getDefinedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Organization> getEntityClass() {
        return Organization.class;
    }

    @Transactional
    public String deleteOrganization() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Organization is null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()"); //cerrar el popup si se grabo correctamente

            } else {
                //remover de la lista, si aún no esta persistido
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe una organización para ser borrada!", ""));
            }

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public TreeNode buildTree() {
        organizationNode = new DefaultTreeNode("organization", getInstance(), null);
        organizationNode.setExpanded(true);

        TreeNode macroprocessNode = null;
        TreeNode processNode = null;
        TreeNode themeNode = null;
        
        for (Theme theme : getInstance().getThemes()) {
            themeNode = new DefaultTreeNode("theme", theme, organizationNode);
            themeNode.setExpanded(true);
            for (Macroprocess macroprocess : theme.getMacroprocess()) {
                macroprocessNode = new DefaultTreeNode("macroprocess", macroprocess, themeNode);
                macroprocessNode.setExpanded(true);
                for (org.eqaula.glue.model.management.Process process : macroprocess.getProcess()) {
                    processNode = new DefaultTreeNode("process", process, macroprocessNode);
                    processNode.setExpanded(true);
                }
            }
        }
        return organizationNode;
    }

    public void onNodeSelect(NodeSelectEvent event) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.selectedBussinesEntity"), ((BussinesEntity) event.getTreeNode().getData()).getName());
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void redirecToAdd() {
        StringBuilder outcomeBuilder = new StringBuilder();
        BussinesEntity bussinesEntity = null;
        if (selectedNode != null) {
            bussinesEntity = (BussinesEntity) selectedNode.getData();
            if ("organization".equals(selectedNode.getType())) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.unimplemented"), ((BussinesEntity) selectedNode.getData()).getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else if ("theme".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/macroprocess/macroprocess.xhtml?");
                outcomeBuilder.append("themeId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/organization/viewTreeThemes");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("macroprocess".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/process/process.xhtml?");
                outcomeBuilder.append("&macroprocessId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/organization/viewTreeThemes");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("process".equals(selectedNode.getType())) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.unimplemented"), ((BussinesEntity) selectedNode.getData()).getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }

    public void redirecToEdit() {

        StringBuilder outcomeBuilder = new StringBuilder();
        if (selectedNode != null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Selected", selectedNode.getData().toString());
            FacesContext.getCurrentInstance().addMessage(null, message);
            BussinesEntity bussinesEntity = (BussinesEntity) selectedNode.getData();
            if ("organization".equals(selectedNode.getType())) {
                FacesMessage messag = new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.unimplemented"), ((BussinesEntity) selectedNode.getData()).getName());
                FacesContext.getCurrentInstance().addMessage(null, messag);
            } else if ("theme".equals(selectedNode.getType())) {
                FacesMessage messag = new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.unimplemented"), ((BussinesEntity) selectedNode.getData()).getName());
                FacesContext.getCurrentInstance().addMessage(null, messag);
            } else if ("macroprocess".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/macroprocess/macroprocess.xhtml?");
                outcomeBuilder.append("&macroprocessId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&themeId=").append(((Macroprocess) bussinesEntity).getTheme().getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/organization/viewTreeThemes");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("process".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/process/process.xhtml?");
                outcomeBuilder.append("&processId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&macroprocessId=").append(((org.eqaula.glue.model.management.Process) bussinesEntity).getMacroprocess().getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/organization/viewTreeThemes");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            }
        }
    }
}
