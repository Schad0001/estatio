/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.estatio.dom.classification;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.NatureOfService;

import org.incode.module.classification.dom.impl.classification.Classification;
import org.incode.module.classification.dom.impl.classification.ClassificationRepository;
import org.incode.module.classification.dom.impl.classification.T_classifications;
import org.incode.module.classification.dom.impl.classification.T_classify;
import org.incode.module.classification.dom.impl.classification.T_unclassify;
import org.incode.module.classification.dom.spi.ApplicationTenancyService;

import org.estatio.dom.asset.Property;

@javax.jdo.annotations.PersistenceCapable(
        identityType= IdentityType.DATASTORE,
        schema = "dbo" // Isis' ObjectSpecId inferred from @DomainObject#objectType
)
@javax.jdo.annotations.Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
@DomainObject(
        objectType = "org.estatio.dom.classification.ClassificationForProperty"
)
public class ClassificationForProperty extends Classification {

    private Property property;

    @Column(allowsNull = "false", name = "propertyId")
    @org.apache.isis.applib.annotation.Property(editing = Editing.DISABLED)
    public Property getProperty() {
        return property;
    }

    public void setProperty(final Property property) {
        this.property = property;
    }

    @Override
    public Object getClassified() {
        return getProperty();
    }

    @Override
    protected void setClassified(final Object classified) {
        setProperty((Property) classified);
    }

    //region > ApplicationTenancyService SPI implementation
    @DomainService(nature = NatureOfService.DOMAIN)
    public static class ApplicationTenancyServiceForProperty implements ApplicationTenancyService {
        @Override
        public String atPathFor(final Object domainObjectToClassify) {
            if(domainObjectToClassify instanceof Property) {
                return ((Property) domainObjectToClassify).getApplicationTenancy().getPath();
            }
            return null;
        }
    }
    //endregion

    //region > SubtypeProvider SPI implementation
    @DomainService(nature = NatureOfService.DOMAIN)
    public static class SubtypeProvider extends ClassificationRepository.SubtypeProviderAbstract {
        public SubtypeProvider() {
            super(Property.class, ClassificationForProperty.class);
        }
    }
    //endregion

    //region > mixins

    @Mixin
    public static class _classifications extends T_classifications<Property> {
        public _classifications(final Property classified) {
            super(classified);
        }
    }

    @Mixin
    public static class _classify extends T_classify<Property> {
        public _classify(final Property classified) {
            super(classified);
        }
    }

    @Mixin
    public static class _unclassify extends T_unclassify<Property> {
        public _unclassify(final Property classified) {
            super(classified);
        }
    }

    //endregion

}
