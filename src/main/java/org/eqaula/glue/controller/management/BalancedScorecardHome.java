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
import java.util.ArrayList;
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
import org.eqaula.glue.drools.RuleRunner;
import org.eqaula.glue.model.BussinesEntity;
import org.eqaula.glue.model.BussinesEntityType;
import org.eqaula.glue.model.management.BalancedScorecard;
import org.eqaula.glue.model.management.Measure;
import org.eqaula.glue.model.management.Method;
import org.eqaula.glue.model.management.Objetive;
import org.eqaula.glue.model.management.Organization;
import org.eqaula.glue.model.management.Perspective;
import org.eqaula.glue.model.management.Target;
import org.eqaula.glue.model.management.Theme;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.util.Dates;
import org.eqaula.glue.util.UI;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.menu.MenuModel;
import org.primefaces.model.TreeNode;

/*
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
    private TreeNode selectedNode;
    private TreeNode rootNode;
    @Inject
    private NavigationHandler navigation;
    @Inject
    private FacesContext context;
    private boolean toHaveChildren;

    public boolean getToHaveChildren(TreeNode node) {
        if (node.getChildren().isEmpty()) {
            toHaveChildren = false;
        } else {
            toHaveChildren = true;
        }
        return toHaveChildren;
    }

    public void setToHaveChildren(boolean toHaveChildren) {
        this.toHaveChildren = toHaveChildren;
    }

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

    public TreeNode getRootNode() {
        if (rootNode == null || rootNode.getChildCount() == 0) {
            buildTree();
        }
        return rootNode;
    }

    public void setRootNode(TreeNode rootNode) {
        this.rootNode = rootNode;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    @Override
    public Organization getOrganization() {
        if (getOrganizationId() == null && isManaged()) {
            super.setOrganization(getInstance().getOrganization());
        }
        return super.getOrganization();
    }

    @TransactionAttribute
    public void load() {
        if (isIdDefined()) {
            wire();
            rootNode = new DefaultTreeNode("bsc", getInstance(), null);
            setSelectedNode(rootNode);
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
        balancedScorecard.setOrganization(getOrganization());
        balancedScorecard.buildAttributes(bussinesEntityService);
        return balancedScorecard;
    }

    protected Perspective createInstancePerspective() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Perspective.class.getName());
        Date now = Calendar.getInstance().getTime();
        Perspective perspective = new Perspective();
        perspective.setCode(UUID.randomUUID().toString());
        perspective.setCreatedOn(now);
        perspective.setLastUpdate(now);
        perspective.setActivationTime(now);
        perspective.setExpirationTime(Dates.addDays(now, 364));
        perspective.setType(_type);
        perspective.buildAttributes(bussinesEntityService);
        return perspective;
    }

    @TransactionAttribute
    public String saveBalancedScorecard() {
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            getInstance().setAuthor(this.profile);
            create(getInstance());
            createDefaultPerspectives(getInstance());
            return getOutcome() + "?organizationId=" + getInstance().getOrganization().getId() + "&faces-redirect=true&includeViewParams=true";
        }

        if (getOutcome() == null) {
            return null;
        }
        if (getOrganizationId() != null) {
            return getOutcome() + "?organizationId=" + getInstance().getOrganization().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public void createDefaultPerspectives(BalancedScorecard balancedScorecard) {

        ArrayList<String> messagesPerspectives = new ArrayList();
        Organization.Type organizationType = getInstance().getOrganization().getOrganizationType();

        switch (organizationType) {
            case GOVERMENT:
                
                messagesPerspectives.add(UI.getMessages("common.perspective.StrategicDirection"));
                messagesPerspectives.add(UI.getMessages("common.perspective.GovernmentForResults"));
                messagesPerspectives.add(UI.getMessages("common.perspective.Process"));
                messagesPerspectives.add(UI.getMessages("common.perspective.HumanTalent"));
                break;
            case PRIVATE:
                
                messagesPerspectives.add(UI.getMessages("common.perspective.Financial"));
                messagesPerspectives.add(UI.getMessages("common.perspective.Customer"));
                messagesPerspectives.add(UI.getMessages("common.perspective.Internal"));
                messagesPerspectives.add(UI.getMessages("common.perspective.InnovationAndLearning"));
                break;
            case PUBLIC:
                
                messagesPerspectives.add(UI.getMessages("common.perspective.StrategicDirection"));
                messagesPerspectives.add(UI.getMessages("common.perspective.GovernmentForResults"));
                messagesPerspectives.add(UI.getMessages("common.perspective.Process"));
                messagesPerspectives.add(UI.getMessages("common.perspective.HumanTalent"));
                break;
            default:
                ;
        }

        for (String createPerspectives : messagesPerspectives) {
            Date now = Calendar.getInstance().getTime();
            Perspective perspective = createInstancePerspective();
            perspective.setName(createPerspectives);
            perspective.setLastUpdate(now);
            perspective.setAuthor(profile);
            perspective.setBalancedScorecard(balancedScorecard);
            create(perspective);
        }
        messagesPerspectives.clear();
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

        return "/pages/management/balancedscorecard/list.xhtml?organizationId=" + getInstance().getOrganization().getId() + "&faces-redirect=true&includeViewParams=true";

    }

    public TreeNode buildTree() {

        //TreeNode bscNode = new DefaultTreeNode("bsc", getInstance(), rootNode);
        
        TreeNode perspectiveNode = null;
        TreeNode themeNode = null;
        TreeNode objetiveNode = null;
        TreeNode measureNode = null;
        TreeNode targetNode = null;

        rootNode.setExpanded(true);
        for (Perspective perspective : getInstance().getPerspectives()) {
            perspectiveNode = new DefaultTreeNode("perspective", perspective, rootNode);
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
                        for (Target target : measure.getTargets()) {
                            targetNode = new DefaultTreeNode("target", target, measureNode);
                            targetNode.setExpanded(true);
                        }
                    }
                }
            }
        }

        return rootNode;
    }

    public void onNodeSelect(NodeSelectEvent event) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.selectedBussinesEntity"), ((BussinesEntity) event.getTreeNode().getData()).getName());
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    @Inject
    private ThemeHome themeHome;
    @Inject
    private PerspectiveHome perspectiveHome;
    @Inject
    private ObjetiveHome objetiveHome;
    @Inject
    private MeasureHome measureHome;

    public void redirecToAdd() {
        StringBuilder outcomeBuilder = new StringBuilder();
        BussinesEntity bussinesEntity = null;
        if (selectedNode != null) {
            bussinesEntity = (BussinesEntity) selectedNode.getData();
            if ("bsc".equals(selectedNode.getType())) {
                perspectiveHome.setBalancedScorecardId(getBalancedScorecardId());
                perspectiveHome.createNewPerspective();
                RequestContext.getCurrentInstance().execute("perspectiveEditDlg.show()");
                //dialogBean.showDialog();
                //outcomeBuilder.append("/pages/management/perspective/perspective.xhtml?");
                //outcomeBuilder.append("balancedScorecardId=").append(getBalancedScorecardId());
                //outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                //navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("perspective".equals(selectedNode.getType())) {
                themeHome.setPerspectiveId(bussinesEntity.getId());
                themeHome.createNewTheme();
                RequestContext.getCurrentInstance().execute("themeEditDlg.show()");
                //outcomeBuilder.append("/pages/management/theme/theme.xhtml?");
                //outcomeBuilder.append("&perspectiveId=").append(bussinesEntity.getId());
                //outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                //navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("theme".equals(selectedNode.getType())) {
                objetiveHome.setThemeId(bussinesEntity.getId());
                objetiveHome.createNewObjetive();
                RequestContext.getCurrentInstance().execute("objetiveEditDlg.show()");
                //outcomeBuilder.append("/pages/management/objetive/objetive.xhtml?");
                //outcomeBuilder.append("&themeId=").append(bussinesEntity.getId());
                //outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                //navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("objetive".equals(selectedNode.getType())) {
                measureHome.setObjetiveId(bussinesEntity.getId());
                measureHome.createNewMeasure();
                RequestContext.getCurrentInstance().execute("measureEditDlg.show()");
                //outcomeBuilder.append("/pages/management/measure/measure.xhtml?");
                //outcomeBuilder.append("&objetiveId=").append(bussinesEntity.getId());
                //outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                //navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("measure".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/targets/target.xhtml?");
                outcomeBuilder.append("&measureId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("target".equals(selectedNode.getType())) {
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
                //outcomeBuilder.append("/pages/management/balancedscorecard/balancedscorecard.xhtml?");
                //outcomeBuilder.append("balancedScorecardId=").append(getBalancedScorecardId());
                //outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                //navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
                FacesMessage messag = new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.unimplemented"), ((BussinesEntity) selectedNode.getData()).getName());
                FacesContext.getCurrentInstance().addMessage(null, messag);

            } else if ("perspective".equals(selectedNode.getType())) {
                perspectiveHome.editPerspective(bussinesEntity.getId());
                RequestContext.getCurrentInstance().execute("perspectiveEditDlg.show()");
                //outcomeBuilder.append("/pages/management/perspective/perspective.xhtml");
                //outcomeBuilder.append("&balancedscorecardId=").append(getBalancedScorecardId());
                //outcomeBuilder.append("&perspectiveId=").append(bussinesEntity.getId());
                //outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                //navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("theme".equals(selectedNode.getType())) {
                themeHome.editTheme(bussinesEntity.getId());
                RequestContext.getCurrentInstance().execute("themeEditDlg.show()");
                //outcomeBuilder.append("/pages/management/theme/theme.xhtml?");
                //outcomeBuilder.append("&perspectiveId=").append(((Theme) bussinesEntity).getPerspective().getId());
                //outcomeBuilder.append("&themeId=").append(bussinesEntity.getId());
                //outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                //navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("objetive".equals(selectedNode.getType())) {
                objetiveHome.editObjetive(bussinesEntity.getId());
                RequestContext.getCurrentInstance().execute("objetiveEditDlg.show()");
                //outcomeBuilder.append("/pages/management/objetive/objetive.xhtml?");
                //outcomeBuilder.append("&objetiveId=").append(bussinesEntity.getId());
                //outcomeBuilder.append("&themeId=").append(((Objetive) bussinesEntity).getTheme().getId());
                //outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                //navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("measure".equals(selectedNode.getType())) {
                measureHome.editMeasure(bussinesEntity.getId());
                RequestContext.getCurrentInstance().execute("measureEditDlg.show()");
                //outcomeBuilder.append("/pages/management/measure/measure.xhtml?");
                //outcomeBuilder.append("&measureId=").append(bussinesEntity.getId());
                //outcomeBuilder.append("&objetiveId=").append(((Measure) bussinesEntity).getObjetive().getId());
                //outcomeBuilder.append("&outcome=" + "/pages/management/balancedscorecard/view");
                //navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("target".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/targets/target.xhtml?");
                outcomeBuilder.append("&targetId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&measureId=").append(((Target) bussinesEntity).getMeasure().getId());
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

    /* public MenuModel getMenuModel() {
     model = new DefaultMenuModel();

     log.debug("node=<" + selectedNode + ">");
     if (selectedNode != null && !lastNodeType.equalsIgnoreCase(selectedNode.getType())) {
     lastNodeType = selectedNode.getType();
     BussinesEntity info = (BussinesEntity) selectedNode.getData();
     MenuItem item = new BaseMenuModel();
     item.setValue(UI.getMessages("common.add") + " " + UI.getMessages("common.in") + " " + info.getName());
     item.setIcon("ui-icon-plus");
     item.setUpdate(":messages");
     item.addActionListener(createMethodActionListener("#{balancedScorecardHome.redirecToAdd}"));
     model.addMenuItem(item);
     }
     return model;
     }*/
    public ActionListener createMethodActionListener(String menuAction) {
        ExpressionFactory factory = FacesContext.getCurrentInstance().getApplication().getExpressionFactory();
        MethodExpression methodsexpression = factory.createMethodExpression(FacesContext.getCurrentInstance().getELContext(), menuAction, null, new Class[]{ActionEvent.class});
        MethodExpressionActionListener actionListener = new MethodExpressionActionListener(methodsexpression);
        return actionListener;
    }

    public Method runMethod(Method method) throws Exception {
        RuleRunner runner = new RuleRunner();
        runner.runRules(method);
        return method;
    }

    public void createNewBalancedScoreCard() {
        setId(null);
        setInstance(null);
        load();
    }

    public void editBalancedScoreCard(Long id) {
        setId(id);
        load();
    }

    @Transactional
    public String saveBalancedScoreCardDialog() {
        saveBalancedScorecard();
        return null;
    }
}
