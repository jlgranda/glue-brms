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
package org.eqaula.glue.model.management;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import org.eqaula.glue.model.BussinesEntity;

/**
 *
 * @author jlgranda
 */
@Entity
@Table(name = "Organization")
@DiscriminatorValue(value = "ORG")
@PrimaryKeyJoinColumn(name = "id")
public class Organization extends BussinesEntity implements Serializable {

    private static final long serialVersionUID = 3095488521256724258L;
    private String ruc;
    private String initials;
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Owner> owners = new ArrayList<Owner>();
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<Theme> themes = new ArrayList<Theme>();
    //Philosophical definition
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mission> missions = new org.apache.commons.collections.list.TreeList();
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vision> vissions = new org.apache.commons.collections.list.TreeList();
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Principle> principles = new org.apache.commons.collections.list.TreeList();

    public enum Type {

        GOVERMENT,
        PUBLIC,
        PRIVATE;

        private Type() {
        }
    }
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Organization.Type organizationType;

    public Type getOrganizationType() {
        return organizationType;
    }

    public void setOrganizationType(Type organizationType) {
        this.organizationType = organizationType;
    }

    
    

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public List<Owner> getOwners() {
        return owners;
    }

    public void setOwners(List<Owner> owners) {
        this.owners = owners;
    }

    public boolean addOwner(Owner owner) {
        owner.setOrganization(this);
        return getOwners().add(owner);
    }

    public boolean removeOwner(Owner owner) {
        owner.setOrganization(null);
        return getOwners().remove(owner);
    }

    public List<Theme> getThemes() {
        return themes;
    }

    public void setThemes(List<Theme> themes) {
        this.themes = themes;
    }

    public List<Mission> getMissions() {
        return missions;
    }

    public void setMissions(List<Mission> missions) {
        this.missions = missions;
    }

    public List<Vision> getVissions() {
        return vissions;
    }

    public void setVissions(List<Vision> vissions) {
        this.vissions = vissions;
    }

    public List<Principle> getPrinciples() {
        return principles;
    }

    public void setPrinciples(List<Principle> principles) {
        this.principles = principles;
    }

    @Override
    public String getCanonicalPath() {
        StringBuilder path = new StringBuilder();
        path.append(getName());
        return path.toString();
    }

    @Override
    public String toString() {
        return getName();
    }

    public boolean addMission(Mission mission) {
        if (!this.getMissions().contains(mission)) {
            mission.setOrganization(this);
            return this.getMissions().add(mission);
        }
        return false;
    }

    public boolean addVision(Vision vision) {
        if (!this.getVissions().contains(vision)) {
            vision.setOrganization(this);
            return this.getVissions().add(vision);
        }
        return false;
    }

    public boolean addPrinciple(Principle principle) {
        if (!this.getPrinciples().contains(principle)) {
            principle.setOrganization(this);
            return this.getPrinciples().add(principle);
        }
        return false;
    }

    public boolean removeMission(Mission mission) {
        mission.setOrganization(null);
        return this.getMissions().remove(mission);
    }

    public boolean removeVision(Vision vision) {
        vision.setOrganization(null);
        return this.getVissions().remove(vision);
    }

    public boolean removePrinciple(Principle principle) {
        principle.setOrganization(null);
        return this.getPrinciples().remove(principle);
    }

    public List<Organization.Type> getOrganizationTypes() {
        return Arrays.asList(Organization.Type.values());
    }
}
