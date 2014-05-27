/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
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
package org.estatio.fixture;

import org.estatio.fixture.asset.PropertyForKal;
import org.estatio.fixture.asset.PropertyForOxf;
import org.estatio.fixture.financial.*;
import org.estatio.fixture.invoice.InvoiceForKalPoison001;
import org.estatio.fixture.invoice.InvoiceForOxfPoison003;
import org.estatio.fixture.lease.*;
import org.estatio.fixture.party.*;
import org.apache.isis.applib.fixturescripts.DiscoverableFixtureScript;

public class EstatioDemoFixture extends DiscoverableFixtureScript {

    public EstatioDemoFixture() {
        super(null, "demo");
    }

    @Override
    protected void execute(ExecutionContext executionContext) {
        execute(new EstatioBaseLineFixture(), executionContext);

        // execute("parties", new PersonsAndOrganisationsAndCommunicationChannelsForAll(), executionContext);
        execute(new OrganisationForAcme(), executionContext);
        execute(new OrganisationForHelloWorld(), executionContext);
        execute(new OrganisationForTopModel(), executionContext);
        execute(new OrganisationForMediaX(), executionContext);
        execute(new OrganisationForPoison(), executionContext);
        execute(new OrganisationForPret(), executionContext);
        execute(new OrganisationForMiracle(), executionContext);
        execute(new PersonForJohnDoe(), executionContext);
        execute(new PersonForLinusTorvalds(), executionContext);

        // execute("properties", new PropertiesAndUnitsForAll(), executionContext);
        execute(new PropertyForOxf(), executionContext);
        execute(new PropertyForKal(), executionContext);

        // execute("leases", new LeasesEtcForAll(), executionContext);
        execute(new LeasesEtcForOxfTopModel001(), executionContext);
        execute(new LeasesEtcForOxfMediax002(), executionContext);
        execute(new LeasesEtcForOxfPoison003(), executionContext);
        execute(new LeasesEtcForOxfPret004(), executionContext);
        execute(new LeasesEtcForOxfMiracl005(), executionContext);
        execute(new LeasesEtcForKalPoison001(), executionContext);

        //execute("invoices", new InvoicesAndInvoiceItemsForAll(), executionContext);
        execute(new InvoiceForOxfPoison003(), executionContext);
        execute(new InvoiceForKalPoison001(), executionContext);

        //execute("bank-accounts", new BankAccountsAndMandatesForAll(), executionContext);
        execute(new BankAccountAndMandateForAcme(), executionContext);
        execute(new BankAccountAndMandateForHelloWorld(), executionContext);
        execute(new BankAccountAndMandateForMediaX(), executionContext);
        execute(new BankAccountAndMandateForMiracle(), executionContext);
        execute(new BankAccountAndMandateForPoison(), executionContext);
        execute(new BankAccountAndMandateForPret(), executionContext);
        execute(new BankAccountAndMandateForTopModel(), executionContext);

    }
}