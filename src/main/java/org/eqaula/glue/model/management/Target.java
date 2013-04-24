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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
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
import javax.persistence.Transient;
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
        EFFECT;         
        private Type() {
        }
    }
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Target.Type targetType;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "periodFrom", nullable = false)
    private Date from;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "periodTo", nullable = false)
    private Date to;
    private String unit;
    private BigDecimal value;
    private Long sequence;
    
    @ManyToOne
    private Measure measure;
    @OneToMany(mappedBy = "target", cascade = CascadeType.ALL)
    private List<Initiative> initiatives;
    @OneToMany(mappedBy = "target", cascade = CascadeType.ALL)
    private List<Method> methods;
    
    @OneToMany(mappedBy = "target", cascade = CascadeType.ALL)
    private List<TargetValue> values;

    public Type getTargetType() {
        return targetType;
    }

    public void setTargetType(Type targetType) {
        this.targetType = targetType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
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
    
    public boolean addMethod(Method method){
        method.setTarget(this);
        return this.getMethods().add(method);
    }
    
    public boolean removeMethod(Method method){
        return this.getMethods().remove(method);
    }

    public List<TargetValue> getValues() {
        return values;
    }

    public void setValues(List<TargetValue> values) {
        this.values = values;
    }

    @Transient
    public BigDecimal getCurrentValue(){
        BigDecimal _value = new BigDecimal(0);
        for (TargetValue tv : getValues()){
            if (Boolean.TRUE.equals(tv.getCurrent())){
                _value = tv.getValue();
                break;
            }
        }
        return _value;
    }
    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }
    
    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
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
    
    public List<Target.Type> getTargetTypes() {
        return Arrays.asList(Target.Type.values());
    }
}
