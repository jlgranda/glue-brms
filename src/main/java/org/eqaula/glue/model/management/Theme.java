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
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eqaula.glue.model.BussinesEntity;

/**
 *
 * @author jlgranda
 */
@Entity
@Table(name = "Theme")
@DiscriminatorValue(value = "THM")
@PrimaryKeyJoinColumn(name = "id")
public class Theme extends BussinesEntity implements Serializable {
    
    private static final long serialVersionUID = -7436571688718703657L;
    
    @ManyToOne
    private Owner owner;
    
    @OneToMany(mappedBy = "theme", cascade = CascadeType.ALL)
    private List<Objetive> objetives;

    @ManyToOne
    private Perspective perspective;
    
    @ManyToOne
    private Organization organization;
    
    @OneToMany(mappedBy = "theme", cascade = CascadeType.ALL)
    private List<Macroprocess> macroprocess;

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public List<Objetive> getObjetives() {
        return objetives;
    }

    public void setObjetives(List<Objetive> objetives) {
        this.objetives = objetives;
    }

    public Perspective getPerspective() {
        return perspective;
    }

    public void setPerspective(Perspective perspective) {
        this.perspective = perspective;
    }

    public List<Macroprocess> getMacroprocess() {
        return macroprocess;
    }

    public void setMacroprocess(List<Macroprocess> macroprocess) {
        this.macroprocess = macroprocess;
    }

    @Override
    public Organization getOrganization() {
        return this.organization;
    }

    @Override
    public void setOrganization(Organization organization) {
        this.organization=organization;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).
                append(getName()).
                append(getType()).
                toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Theme other = (Theme) obj;
        return new EqualsBuilder().
                // if deriving: appendSuper(super.equals(obj)).
                append(getName(), other.getName()).
                append(getType(), other.getType()).
                isEquals();
    }
    
     @Override
    public String getCanonicalPath(){
        StringBuilder path = new StringBuilder();
        path.append(getPerspective().getCanonicalPath());
        path.append(BussinesEntity.SEPARATOR); //TODO hacer que sea personalizable
        path.append(getPerspective().getName());
        return path.toString();
    }
    
    @Override
    public String toString() {
        /*return "org.eqaula.glue.model.management.Theme[ "
                + "id=" + getId() + ","
                + "name=" + getName() + ","
                + "type=" + getType() + ","
                + " ]";*/
        return getName() + ":" + getDescription();
    }
}
