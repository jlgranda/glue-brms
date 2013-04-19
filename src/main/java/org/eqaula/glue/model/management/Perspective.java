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
@Table(name = "Perspective")
@DiscriminatorValue(value = "PTV")
@PrimaryKeyJoinColumn(name = "id")
public class Perspective extends BussinesEntity implements Serializable {
    
    private static final long serialVersionUID = -4535279430871708301L;
    
    private Long sequence;
            
    @OneToMany(mappedBy = "perspective", cascade = CascadeType.ALL)
    private  List<Theme> themes;

    @ManyToOne
    private BalancedScorecard balancedScorecard;
    
    public List<Theme> getThemes() {
        return themes;
    }

    public void setThemes(List<Theme> themes) {
        this.themes = themes;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public BalancedScorecard getBalancedScorecard() {
        return balancedScorecard;
    }

    public void setBalancedScorecard(BalancedScorecard balancedScorecard) {
        this.balancedScorecard = balancedScorecard;
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
        Perspective other = (Perspective) obj;
        return new EqualsBuilder().
                // if deriving: appendSuper(super.equals(obj)).
                append(getName(), other.getName()).
                append(getType(), other.getType()).
                isEquals();
    }
    
    @Override
    public String getCanonicalPath(){
        StringBuilder path = new StringBuilder();
        path.append(getBalancedScorecard().getCanonicalPath());
        path.append("/"); //TODO hacer que sea personalizable
        path.append(getName());
        return path.toString();
    }
    
    @Override
    public String toString() {
        /*return "org.eqaula.glue.model.management.Perspective[ "
                + "id=" + getId() + ","
                + "name=" + getName() + ","
                + "type=" + getType() + ","
                + " ]";*/
        return getName();
    }
}
