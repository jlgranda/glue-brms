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
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eqaula.glue.model.BussinesEntity;

/*
 * @author dianita
 */
@Entity
@Table(name = "Target")
@DiscriminatorValue(value = "TGT")
@PrimaryKeyJoinColumn(name = "id")
public class Target extends BussinesEntity implements Serializable {

    private static final long serialVersionUID = 449175027015485864L;

    public enum Type {

        CAUSE,
        EFECT; //Esquema contable         

        private Type() {
        }
    }
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Target.Type targetType;
    @ManyToOne
    private Measure measure;
    @OneToMany(mappedBy = "target", cascade = CascadeType.ALL)
    private List<Initiative> initiatives;
    @OneToMany(mappedBy = "target", cascade = CascadeType.ALL)
    private List<Method> methods;
    private Long sequence;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "periodFrom", nullable = false)
    private Date periodFrom;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "periodTo", nullable = false)
    private Date periodTo;

    public Type getTargetType() {
        return targetType;
    }

    public void setTargetType(Type targetType) {
        this.targetType = targetType;
    }

    public Measure getMeasure() {
        return measure;
    }

    public void setMeasure(Measure measure) {
        this.measure = measure;
    }

    public List<Initiative> getInitiatives() {
        return initiatives;
    }

    public void setInitiatives(List<Initiative> initiatives) {
        this.initiatives = initiatives;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public Date getPeriodFrom() {
        return periodFrom;
    }

    public void setPeriodFrom(Date periodFrom) {
        this.periodFrom = periodFrom;
    }

    public Date getPeriodTo() {
        return periodTo;
    }

    public void setPeriodTo(Date periodTo) {
        this.periodTo = periodTo;
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Target other = (Target) obj;
        return new EqualsBuilder().
                // if deriving: appendSuper(super.equals(obj)).
                append(getName(), other.getName()).
                append(getType(), other.getType()).
                isEquals();
    }

    @Override
    public String getCanonicalPath() {
        StringBuilder path = new StringBuilder();
        path.append(getMeasure().getCanonicalPath());
        path.append(BussinesEntity.SEPARATOR); //TODO hacer que sea personalizable
        path.append(getMeasure().getName());
        return path.toString();
    }

    @Override
    public String toString() {
        /*return "org.eqaula.glue.model.management.Target[ "
         + "id=" + getId() + ","
         + "name=" + getName() + ","
         + "type=" + getType() + ","
         + " ]";*/
        return getName();
    }
}
