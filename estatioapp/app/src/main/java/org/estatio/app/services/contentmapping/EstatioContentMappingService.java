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
package org.estatio.app.services.contentmapping;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.rendering.service.conmap.ContentMappingService;

import org.estatio.canonical.bankmandate.v1.BankMandateDtoFactory;
import org.estatio.canonical.financial.v1.BankAccountDtoFactory;
import org.estatio.canonical.invoice.InvoiceDtoFactory;
import org.estatio.canonical.party.PartyDtoFactory;
import org.estatio.dom.bankmandate.BankMandate;
import org.estatio.dom.financial.bankaccount.BankAccount;
import org.estatio.dom.invoice.Invoice;
import org.estatio.dom.party.Party;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class EstatioContentMappingService implements ContentMappingService {

    @Programmatic
    @Override
    public Object map(
            final Object object,
            final List<MediaType> acceptableMediaTypes,
            final RepresentationType representationType) {

        if(object instanceof Party) {
            return partyDtoFactory.newDto((Party)object);
        }
        if(object instanceof BankAccount) {
            return bankAccountDtoFactory.newDto((BankAccount)object);
        }
        if(object instanceof BankMandate) {
            return bankMandateDtoFactory.newDto((BankMandate)object);
        }
        if(object instanceof Invoice) {
            return invoiceDtoFactory.newDto((Invoice)object);
        }

        return null;
    }

    @javax.inject.Inject
    BankAccountDtoFactory bankAccountDtoFactory;

    @javax.inject.Inject
    BankMandateDtoFactory bankMandateDtoFactory;

    @javax.inject.Inject
    PartyDtoFactory partyDtoFactory;

    @javax.inject.Inject
    InvoiceDtoFactory invoiceDtoFactory;

}