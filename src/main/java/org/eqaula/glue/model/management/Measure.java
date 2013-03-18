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

/*
 * @author dianita
 */
@Entity
@Table(name = "Measure")
@DiscriminatorValue(value = "MSR")
@PrimaryKeyJoinColumn(name = "id")
public class Measure extends BussinesEntity implements Serializable {

    private static final long serialVersionUID = -6935266135527132362L;
    private Long frequency;
    
    @OneToMany(mappedBy = "measure", cascade = CascadeType.ALL)
    private List<Target> targets;
   
    @ManyToOne
    private Objetive objetive;

    public Long getFrequency() {
        return frequency;
    }

    public void setFrequency(Long frequency) {
        this.frequency = frequency;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public void setTargets(List<Target> targets) {
        this.targets = targets;
    }

    public Objetive getObjetive() {
        return objetive;
    }

    public void setObjetive(Objetive objetive) {
        this.objetive = objetive;
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
        Measure other = (Measure) obj;
        return new EqualsBuilder().
                // if deriving: appendSuper(super.equals(obj)).
                append(getName(), other.getName()).
                append(getType(), other.getType()).
                isEquals();
    }

    @Override
    public String toString() {
        return "org.eqaula.glue.model.management.Theme[ "
                + "id=" + getId() + ","
                + "name=" + getName() + ","
                + "type=" + getType() + ","
                + " ]";
    }
}
