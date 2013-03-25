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
import org.eqaula.glue.model.management.Measure;
import org.eqaula.glue.model.management.Method;
import org.eqaula.glue.model.management.Target;
import org.eqaula.glue.model.management.Objetive;
import org.eqaula.glue.model.management.Organization;
import org.eqaula.glue.model.management.Owner;
import org.eqaula.glue.model.management.Period;
import org.eqaula.glue.model.management.Resource;
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
        TreeNode ownerNode = null;
        TreeNode objetiveNode = null;
        TreeNode measureNode = null;
        TreeNode targetNode = null;
        TreeNode periodNode = null;
        TreeNode initiativeNode = null;
        TreeNode methodNode = null;
        TreeNode targetMasterNode = null;
        TreeNode periodMasterNode = null;
        TreeNode initiativeMasterNode = null;
        TreeNode methodMasterNode = null;

        organizationNode.setExpanded(true);
        for (Owner owner : getInstance().getOwners()) {
            ownerNode = new DefaultTreeNode("owner", owner, organizationNode);
            ownerNode.setExpanded(true);
//            for (Objetive objetive : owner.getObjetives()) {
//                objetiveNode = new DefaultTreeNode("objetive", objetive, ownerNode);
//                objetiveNode.setExpanded(true);
//                for (Measure measure : objetive.getMeasures()) {
//                    measureNode = new DefaultTreeNode("measure", measure, objetiveNode);
//                    measureNode.setExpanded(false);
//
//                    targetMasterNode = new DefaultTreeNode("targets", UI.getMessages("common.targets"), measureNode);
//                    periodMasterNode = new DefaultTreeNode("periods", UI.getMessages("common.periods"), measureNode);
//                    initiativeMasterNode = new DefaultTreeNode("initiatives", UI.getMessages("common.initiatives"), measureNode);
//                    methodMasterNode = new DefaultTreeNode("methods", UI.getMessages("common.methods"), measureNode);
//
//                    for (Target target : measure.getTargets()) {
//                        targetNode = new DefaultTreeNode("target", target, targetMasterNode);
//                        targetNode.setExpanded(false);
//                    }
//                    for (Period period : measure.getPeriods()) {
//                        periodNode = new DefaultTreeNode("period", period, periodMasterNode);
//                        periodNode.setExpanded(false);
//                    }
//                    for (Initiative initiative : measure.getInitiatives()) {
//                        initiativeNode = new DefaultTreeNode("initiative", initiative, initiativeMasterNode);
//                        initiativeNode.setExpanded(false);
//                    }
//                    for (Method method : measure.getMethods()) {
//                        methodNode = new DefaultTreeNode("method", method, methodMasterNode);
//                        methodNode.setExpanded(false);
//                    }
//                }
//            }
        }
        return organizationNode;
    }

    public void onNodeSelect(NodeSelectEvent event) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.selectedBussinesEntity"), ((BussinesEntity) event.getTreeNode().getData()).getName());
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void redirecToAdd() {

        StringBuilder outcomeBuilder = new StringBuilder();
        String masterNodoName = "";
        if (selectedNode != null) {
            if (selectedNode.getData().toString().equals(UI.getMessages("common.targets"))||
                    selectedNode.getData().toString().equals(UI.getMessages("common.periods"))||
                    selectedNode.getData().toString().equals(UI.getMessages("common.initiatives"))||
                    selectedNode.getData().toString().equals(UI.getMessages("common.methods"))) {
                masterNodoName = selectedNode.getData().toString();
              
            } else {
                BussinesEntity bussinesEntity = (BussinesEntity) selectedNode.getData();
            }

            if ("organization".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/owner/owner.xhtml?");
                outcomeBuilder.append("organizationId=").append(getOrganizationId());
                outcomeBuilder.append("&outcome=" + "/pages/management/organization/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("owner".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/objetive/objetive.xhtml?");
                outcomeBuilder.append("&ownerId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/organization/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("objetive".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/measure/measure.xhtml?");
                outcomeBuilder.append("&objetiveId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/organization/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("measure".equals(selectedNode.getType())) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.unimplemented"), ((BussinesEntity) selectedNode.getData()).getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else if (masterNodoName.equals(UI.getMessages("common.targets"))) {
                outcomeBuilder.append("/pages/management/targets/target.xhtml?");
                outcomeBuilder.append("&measureId=").append(((BussinesEntity) selectedNode.getParent().getData()).getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/organization/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("target".equals(selectedNode.getType())) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.unimplemented"), ((BussinesEntity) selectedNode.getData()).getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else if (masterNodoName.equals(UI.getMessages("common.periods"))) {
                outcomeBuilder.append("/pages/management/period/period.xhtml?");
                outcomeBuilder.append("&measureId=").append(((BussinesEntity) selectedNode.getParent().getData()).getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/organization/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("period".equals(selectedNode.getType())) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.unimplemented"), ((BussinesEntity) selectedNode.getData()).getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else if (masterNodoName.equals(UI.getMessages("common.initiatives"))) {
                outcomeBuilder.append("/pages/management/initiative/initiative.xhtml?");
                outcomeBuilder.append("&measureId=").append(((BussinesEntity) selectedNode.getParent().getData()).getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/organization/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("initiative".equals(selectedNode.getType())) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.unimplemented"), ((BussinesEntity) selectedNode.getData()).getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else if (masterNodoName.equals(UI.getMessages("common.methods"))) {
                outcomeBuilder.append("/pages/management/method/method.xhtml?");
                outcomeBuilder.append("&measureId=").append(((BussinesEntity) selectedNode.getParent().getData()).getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/organization/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("method".equals(selectedNode.getType())) {
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
                outcomeBuilder.append("/pages/management/organization/organization.xhtml?");
                outcomeBuilder.append("organizationId=").append(getOrganizationId());
                outcomeBuilder.append("&outcome=" + "/pages/management/organization/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("owner".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/owner/owner.xhtml?");
                outcomeBuilder.append("&organizationId=").append(getOrganizationId());
                outcomeBuilder.append("&ownerId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/organization/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("objetive".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/objetive/objetive.xhtml?");
                outcomeBuilder.append("&objetiveId=").append(bussinesEntity.getId());
                //outcomeBuilder.append("&ownerId=").append(((Objetive) bussinesEntity).getOwner().getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/organization/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("measure".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/measure/measure.xhtml?");
                outcomeBuilder.append("&measureId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&objetiveId=").append(((Measure) bussinesEntity).getObjetive().getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/organization/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("target".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/targets/target.xhtml?");
                outcomeBuilder.append("&targetId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&measureId=").append(((Target) bussinesEntity).getMeasure().getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/organization/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("period".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/period/period.xhtml?");
                outcomeBuilder.append("&periodId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&measureId=").append(((Period) bussinesEntity).getMeasure().getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/organization/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("initiative".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/initiative/initiative.xhtml?");
                outcomeBuilder.append("&initiativeId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&measureId=").append(((Initiative) bussinesEntity).getMeasure().getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/organization/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("method".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/method/method.xhtml?");
                outcomeBuilder.append("&methodId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&measureId=").append(((Method) bussinesEntity).getMeasure().getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/organization/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            }
        }
    }
    /*
     * UI management
     */
    private MenuModel model = null;
    private String lastNodeType = "";

    public MenuModel getMenuModel() {
        model = new DefaultMenuModel();

        log.debug("node=<" + selectedNode + ">");
        if (selectedNode != null && !lastNodeType.equalsIgnoreCase(selectedNode.getType())) {
            lastNodeType = selectedNode.getType();
            BussinesEntity info = (BussinesEntity) selectedNode.getData();
            MenuItem item = new MenuItem();
            item.setValue(UI.getMessages("common.add") + " " + UI.getMessages("common.in") + " " + info.getName());
            item.setIcon("ui-icon-plus");
            item.setUpdate(":messages");
            item.addActionListener(createMethodActionListener("#{organizationHome.redirecToAdd}"));
            model.addMenuItem(item);
        }
        return model;
    }

    public ActionListener createMethodActionListener(String menuAction) {
        ExpressionFactory factory = FacesContext.getCurrentInstance().getApplication().getExpressionFactory();
        MethodExpression methodsexpression = factory.createMethodExpression(FacesContext.getCurrentInstance().getELContext(), menuAction, null, new Class[]{ActionEvent.class});
        MethodExpressionActionListener actionListener = new MethodExpressionActionListener(methodsexpression);
        return actionListener;
    }
}
