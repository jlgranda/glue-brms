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
import org.eqaula.glue.cdi.Current;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.BussinesEntityHome;
import org.eqaula.glue.model.BussinesEntity;
import org.eqaula.glue.model.BussinesEntityType;
import org.eqaula.glue.model.management.BalancedScorecard;
import org.eqaula.glue.model.management.Initiative;
import org.eqaula.glue.model.management.Measure;
import org.eqaula.glue.model.management.Method;
import org.eqaula.glue.model.management.Objetive;
import org.eqaula.glue.model.management.Organization;
import org.eqaula.glue.model.management.Owner;
import org.eqaula.glue.model.management.Period;
import org.eqaula.glue.model.management.Perspective;
import org.eqaula.glue.model.management.Target;
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
 * @author dianita
 */
@Named
@ViewScoped
public class BalancedScorecardHome extends BussinesEntityHome<BalancedScorecard> implements Serializable {

    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(org.eqaula.glue.controller.management.OwnerHome.class);
    private static final long serialVersionUID = -8602961273211681365L;
    @Inject
    @Web
    private EntityManager em;
    @Current
    @Inject
    private Profile profile;
    private TreeNode bscNode;
    private TreeNode selectedNode;
    @Inject
    private NavigationHandler navigation;
    @Inject
    private FacesContext context;

    public BalancedScorecardHome() {
    }

    public Long getBalancedScorecardId() {
        return (Long) getId();
    }

    public void setBalancedScorecardId(Long balancedScorecardId) {
        setId(balancedScorecardId);
    }

    public String getBalancedScorecardName() {
        return getInstance().getName();
    }

    public TreeNode getBscNode() {
        if (bscNode == null) {
            buildTree();
        }
        return bscNode;
    }

    public void setBscNode(TreeNode bscNode) {
        this.bscNode = bscNode;
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

    @TransactionAttribute
    public void wire() {
        getInstance();
    }

    @PostConstruct
    public void init() {
        setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
        organizationService.setEntityManager(em);
    }

    @Override
    protected BalancedScorecard createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(BalancedScorecard.class.getName());
        Date now = Calendar.getInstance().getTime();
        BalancedScorecard balancedScorecard = new BalancedScorecard();
        balancedScorecard.setCode(UUID.randomUUID().toString());
        balancedScorecard.setCreatedOn(now);
        balancedScorecard.setLastUpdate(now);
        balancedScorecard.setActivationTime(now);
        balancedScorecard.setExpirationTime(Dates.addDays(now, 364));
        balancedScorecard.setType(_type);
        balancedScorecard.buildAttributes(bussinesEntityService);
        return balancedScorecard;
    }

    @TransactionAttribute
    public String saveBalancedScorecard() {
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            getInstance().setAuthor(this.profile);
            getInstance().setOrganization(getOrganization());
            create(getInstance());
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public boolean isWired() {
        return true;
    }

    public BalancedScorecard getDefiniedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<BalancedScorecard> getEntityClass() {
        return BalancedScorecard.class;
    }

    @Transactional
    public String deleteBalancedScorecard() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("BalancedScorecard is Null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe un cuadro de mando integral para ser borrado!", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }
        if (getBalancedScorecardId() != null) {

            return getOutcome() + "?balancedScorecardId=" + getBalancedScorecardId() + "&faces-redirect=true&includeViewParams=true";
        }

        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public TreeNode buildTree() {
        bscNode = new DefaultTreeNode("bsc", getInstance(), null);
        TreeNode perspectiveNode = null;
        TreeNode themeNode = null;
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

        bscNode.setExpanded(true);
        for (Perspective perspective : getInstance().getPerspectives()) {
            perspectiveNode = new DefaultTreeNode("perspective", perspective, bscNode);
            perspectiveNode.setExpanded(true);
            for (Theme theme : perspective.getThemes()) {
                themeNode = new DefaultTreeNode("theme", theme, perspectiveNode);
                themeNode.setExpanded(true);
                for (Objetive objetive : theme.getObjetives()) {
                    objetiveNode = new DefaultTreeNode("objetive", objetive, themeNode);
                    objetiveNode.setExpanded(true);
                    for (Measure measure : objetive.getMeasures()) {
                        measureNode = new DefaultTreeNode("measure", measure, objetiveNode);
                        measureNode.setExpanded(true);
                        targetMasterNode = new DefaultTreeNode("targets", UI.getMessages("common.targets"), measureNode);
                        periodMasterNode = new DefaultTreeNode("periods", UI.getMessages("common.periods"), measureNode);
                        initiativeMasterNode = new DefaultTreeNode("initiatives", UI.getMessages("common.initiatives"), measureNode);
                        methodMasterNode = new DefaultTreeNode("methods", UI.getMessages("common.methods"), measureNode);

                        targetMasterNode.setExpanded(true);
                        periodMasterNode.setExpanded(true);
                        initiativeMasterNode.setExpanded(true);
                        methodMasterNode.setExpanded(true);
                        
                        for (Target target : measure.getTargets()) {
                            targetNode = new DefaultTreeNode("target", target, targetMasterNode);
                            targetNode.setExpanded(true);
                        }
                        for (Period period : measure.getPeriods()) {
                            periodNode = new DefaultTreeNode("period", period, periodMasterNode);
                            periodNode.setExpanded(true);
                        }
                        for (Initiative initiative : measure.getInitiatives()) {
                            initiativeNode = new DefaultTreeNode("initiative", initiative, initiativeMasterNode);
                            initiativeNode.setExpanded(true);
                        }
                        for (Method method : measure.getMethods()) {
                            methodNode = new DefaultTreeNode("method", method, methodMasterNode);
                            methodNode.setExpanded(true);
                        }
                    }
                }
            }
        }

        return bscNode;
    }

    public void onNodeSelect(NodeSelectEvent event) {

        if (event.getTreeNode().getData().toString().equals(UI.getMessages("common.targets"))
                || event.getTreeNode().getData().toString().equals(UI.getMessages("common.periods"))
                || event.getTreeNode().getData().toString().equals(UI.getMessages("common.initiatives"))
                || event.getTreeNode().getData().toString().equals(UI.getMessages("common.methods"))) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.selectedBussinesEntity"), event.getTreeNode().getData().toString());
            FacesContext.getCurrentInstance().addMessage(null, message);
        } else {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.selectedBussinesEntity"), ((BussinesEntity) event.getTreeNode().getData()).getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void redirecToAdd() {
        StringBuilder outcomeBuilder = new StringBuilder();
        String masterNodoName = "";
        BussinesEntity bussinesEntity = null;
        if (selectedNode != null) {
            if (selectedNode.getData().toString().equals(UI.getMessages("common.targets"))
                    || selectedNode.getData().toString().equals(UI.getMessages("common.periods"))
                    || selectedNode.getData().toString().equals(UI.getMessages("common.initiatives"))
                    || selectedNode.getData().toString().equals(UI.getMessages("common.methods"))) {
                masterNodoName = selectedNode.getData().toString();
            } else {
                bussinesEntity = (BussinesEntity) selectedNode.getData();
            }

            if ("bsc".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/perspective/perspective.xhtml?");
                outcomeBuilder.append("balancedScorecardId=").append(getBalancedScorecardId());
                outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("perspective".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/theme/theme.xhtml?");
                outcomeBuilder.append("&perspectiveId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("theme".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/objetive/objetive.xhtml?");
                outcomeBuilder.append("&themeId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("objetive".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/measure/measure.xhtml?");
                outcomeBuilder.append("&objetiveId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("measure".equals(selectedNode.getType())) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.unimplemented"), ((BussinesEntity) selectedNode.getData()).getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else if (masterNodoName.equals(UI.getMessages("common.targets"))) {
                outcomeBuilder.append("/pages/management/targets/target.xhtml?");
                outcomeBuilder.append("&measureId=").append(((BussinesEntity) selectedNode.getParent().getData()).getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("target".equals(selectedNode.getType())) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.unimplemented"), ((BussinesEntity) selectedNode.getData()).getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else if (masterNodoName.equals(UI.getMessages("common.periods"))) {
                outcomeBuilder.append("/pages/management/period/period.xhtml?");
                outcomeBuilder.append("&measureId=").append(((BussinesEntity) selectedNode.getParent().getData()).getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("period".equals(selectedNode.getType())) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.unimplemented"), ((BussinesEntity) selectedNode.getData()).getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else if (masterNodoName.equals(UI.getMessages("common.initiatives"))) {
                outcomeBuilder.append("/pages/management/initiative/initiative.xhtml?");
                outcomeBuilder.append("&measureId=").append(((BussinesEntity) selectedNode.getParent().getData()).getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("initiative".equals(selectedNode.getType())) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.unimplemented"), ((BussinesEntity) selectedNode.getData()).getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else if (masterNodoName.equals(UI.getMessages("common.methods"))) {
                outcomeBuilder.append("/pages/management/method/method.xhtml?");
                outcomeBuilder.append("&measureId=").append(((BussinesEntity) selectedNode.getParent().getData()).getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
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
            if ("bsc".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/balancedscorecard/balancedscorecard.xhtml?");
                outcomeBuilder.append("balancedScorecardId=").append(getBalancedScorecardId());
                outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");

            } else if ("perspective".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/perspective/perspective.xhtml?");
                outcomeBuilder.append("&balancedscorecardId=").append(getBalancedScorecardId());
                outcomeBuilder.append("&perspectiveId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("theme".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/theme/theme.xhtml?");
                outcomeBuilder.append("&perspectiveId=").append(((Theme)bussinesEntity).getPerspective().getId());
                outcomeBuilder.append("&themeId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("objetive".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/objetive/objetive.xhtml?");
                outcomeBuilder.append("&objetiveId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&themeId=").append(((Objetive) bussinesEntity).getTheme().getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("measure".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/measure/measure.xhtml?");
                outcomeBuilder.append("&measureId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&objetiveId=").append(((Measure) bussinesEntity).getObjetive().getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("target".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/targets/target.xhtml?");
                outcomeBuilder.append("&targetId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&measureId=").append(((Target) bussinesEntity).getMeasure().getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("period".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/period/period.xhtml?");
                outcomeBuilder.append("&periodId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&measureId=").append(((Period) bussinesEntity).getMeasure().getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("initiative".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/initiative/initiative.xhtml?");
                outcomeBuilder.append("&initiativeId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&measureId=").append(((Initiative) bussinesEntity).getMeasure().getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("method".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/method/method.xhtml?");
                outcomeBuilder.append("&methodId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&measureId=").append(((Method) bussinesEntity).getMeasure().getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
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
            item.addActionListener(createMethodActionListener("#{balancedScorecardHome.redirecToAdd}"));
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
