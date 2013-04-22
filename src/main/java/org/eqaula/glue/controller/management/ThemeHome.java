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
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.eqaula.glue.cdi.Current;
import org.eqaula.glue.cdi.Web;
import org.eqaula.glue.controller.BussinesEntityHome;
import org.eqaula.glue.model.BussinesEntity;
import org.eqaula.glue.model.BussinesEntityType;
import org.eqaula.glue.model.management.Macroprocess;
import org.eqaula.glue.model.management.Organization;
import org.eqaula.glue.model.management.Process;
import org.eqaula.glue.model.management.Owner;
import org.eqaula.glue.model.management.Perspective;
import org.eqaula.glue.model.management.Theme;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.service.OwnerService;
import org.eqaula.glue.service.PerspectiveService;
import org.eqaula.glue.util.Dates;
import org.eqaula.glue.util.UI;
import org.jboss.seam.transaction.Transactional;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/*
 * @author dianita
 */
@Named
@ViewScoped
public class ThemeHome extends BussinesEntityHome<Theme> implements Serializable {

    private static final long serialVersionUID = 6941666069724371093L;
    private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(OwnerHome.class);
    @Inject
    @Web
    private EntityManager em;
    @Current
    @Inject
    private Profile profile;
    private Owner owner;
    private Long ownerId;
    @Inject
    private OwnerService ownerService;
    private Perspective perspective;
    private Long perspectiveId;
    @Inject
    private PerspectiveService perspectiveService;
    private TreeNode themeNode;
    private TreeNode selectedNode;
    @Inject
    private NavigationHandler navigation;
    @Inject
    private FacesContext context;
    private String outcomeOther = "";

    public ThemeHome() {
    }

    public Long getThemeId() {
        return (Long) getId();
    }

    public void setThemeId(Long themeId) {
        setId(themeId);
    }

    public String getStructureName() {
        return getInstance().getName();
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public TreeNode getThemeNode() {
        if (themeNode == null) {
            buildTree();
        }
        return themeNode;
    }

    public void setThemeNode(TreeNode themeNode) {
        this.themeNode = themeNode;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public String getOutcomeOther() {
        return outcomeOther;
    }

    public void setOutcomeOther(String outcomeOther) {
        this.outcomeOther = outcomeOther;
    }

    @Transactional
    public Owner getOwner() {
        if (owner == null) {
            if (ownerId == null) {
                owner = null;
            } else {
                owner = ownerService.find(getOwnerId());
            }
        }
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Long getPerspectiveId() {
        return perspectiveId;
    }

    public void setPerspectiveId(Long perspectiveId) {
        this.perspectiveId = perspectiveId;
    }

    @Transactional
    public Perspective getPerspective() {
        if (perspective == null) {
            if (perspectiveId == null) {
                perspective = null;
            } else {
                perspective = perspectiveService.find(getPerspectiveId());
            }
        }
        return perspective;
    }

    public void setPerspective(Perspective perspective) {
        this.perspective = perspective;
    }

    @Override
    public Organization getOrganization() {
        if (getOrganizationId() == null && isManaged()) {
            super.setOrganization(getInstance().getPerspective().getOrganization());
        }
        return super.getOrganization();
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
        ownerService.setEntityManager(em);
        perspectiveService.setEntityManager(em);
        bussinesEntityService.setEntityManager(em);
    }

    @Override
    protected Theme createInstance() {
        BussinesEntityType _type = bussinesEntityService.findBussinesEntityTypeByName(Theme.class.getName());
        Date now = Calendar.getInstance().getTime();
        Theme theme = new Theme();
        theme.setCode(UUID.randomUUID().toString());
        theme.setCreatedOn(now);
        theme.setLastUpdate(now);
        theme.setActivationTime(now);
        theme.setExpirationTime(Dates.addDays(now, 364));
        theme.setType(_type);
        theme.setPerspective(getPerspective());
        theme.setOrganization(getPerspective().getBalancedScorecard().getOrganization());
        theme.buildAttributes(bussinesEntityService);
        return theme;
    }

    @TransactionAttribute
    public String saveTheme() {
        Date now = Calendar.getInstance().getTime();
        getInstance().setLastUpdate(now);
        if (getInstance().isPersistent()) {
            save(getInstance());
        } else {
            getInstance().setAuthor(this.profile);
            create(getInstance());
        }
        if (getInstance().getPerspective().getId() != null) {
            if (getOutcomeOther().isEmpty()) {
                return getOutcome() + "?balancedScorecardId=" + getInstance().getPerspective().getBalancedScorecard().getId() + "&faces-redirect=true&includeViewParams=true";
            } else {
                return getOutcomeOther() + "?faces-redirect=true&includeViewParams=true";
            }
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    @TransactionAttribute
    public String saveAndAddOther() {
        setOutcomeOther("/pages/management/theme/theme");
        return saveTheme();
    }

    public boolean isWired() {
        return true;
    }

    public Theme getDefiniedInstance() {
        return isIdDefined() ? getInstance() : null;
    }

    @Override
    public Class<Theme> getEntityClass() {
        return Theme.class;
    }

    @Transactional
    public String deleteTheme() {
        try {
            if (getInstance() == null) {
                throw new NullPointerException("Theme is Null");
            }
            if (getInstance().isPersistent()) {
                delete(getInstance());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se borró exitosamente:  " + getInstance().getName(), ""));
                RequestContext.getCurrentInstance().execute("editDlg.hide()");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "¡No existe un tema para ser borrado!", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERRORE", e.toString()));
        }

        if (getInstance().getPerspective().getId() != null) {
            return getOutcome() + "?balancedScorecardId=" + getInstance().getPerspective().getBalancedScorecard().getId() + "&faces-redirect=true&includeViewParams=true";
        }
        return getOutcome() + "?faces-redirect=true&includeViewParams=true";
    }

    public TreeNode buildTree() {
        themeNode = new DefaultTreeNode("theme", getInstance(), null);
        TreeNode macroprocessNode = null;
        TreeNode processNode = null;
        themeNode.setExpanded(true);
        for (Macroprocess macroprocess : getInstance().getMacroprocess()) {
            macroprocessNode = new DefaultTreeNode("macroprocess", macroprocess, themeNode);
            macroprocessNode.setExpanded(true);
            for (Process process : macroprocess.getProcess()) {
                processNode = new DefaultTreeNode("process", process, macroprocessNode);
                processNode.setExpanded(true);
            }
        }
        return themeNode;
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
            if ("theme".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/macroprocess/macroprocess.xhtml?");
                outcomeBuilder.append("themeId=").append(getThemeId());
                outcomeBuilder.append("&outcome=" + "/pages/management/theme/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("macroprocess".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/process/process.xhtml?");
                outcomeBuilder.append("&macroprocessId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/theme/view");
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
            if ("theme".equals(selectedNode.getType())) {
                FacesMessage messag = new FacesMessage(FacesMessage.SEVERITY_INFO, UI.getMessages("common.unimplemented"), ((BussinesEntity) selectedNode.getData()).getName());
                FacesContext.getCurrentInstance().addMessage(null, messag);
                //outcomeBuilder.append("/pages/management/theme/theme.xhtml?");
                //outcomeBuilder.append("themeId=").append(getThemeId());
                //outcomeBuilder.append("&outcome=" + "/pages/management/theme/view");
                //navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("macroprocess".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/macroprocess/macroprocess.xhtml?");
                outcomeBuilder.append("&macroprocessId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&themeId=").append(((Macroprocess) bussinesEntity).getTheme().getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/theme/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            } else if ("process".equals(selectedNode.getType())) {
                outcomeBuilder.append("/pages/management/process/process.xhtml?");
                outcomeBuilder.append("&processId=").append(bussinesEntity.getId());
                outcomeBuilder.append("&macroprocessId=").append(((Process) bussinesEntity).getMacroprocess().getId());
                outcomeBuilder.append("&outcome=" + "/pages/management/theme/view");
                navigation.handleNavigation(context, null, outcomeBuilder.toString() + "&faces-redirect=true");
            }
        }
    }   
}
