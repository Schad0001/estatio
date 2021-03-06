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
package org.estatio.dom.invoice;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.Indices;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.VersionStrategy;

import com.google.common.base.Predicate;

import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.InvokeOn;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.repository.RepositoryService;

import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;

import org.incode.module.communications.dom.impl.commchannel.CommunicationChannel;
import org.incode.module.document.dom.impl.paperclips.PaperclipRepository;
import org.incode.module.document.dom.types.AtPathType;

import org.estatio.dom.UdoDomainObject2;
import org.estatio.dom.apptenancy.WithApplicationTenancyAny;
import org.estatio.dom.apptenancy.WithApplicationTenancyPathPersisted;
import org.estatio.dom.bankmandate.BankMandate;
import org.estatio.dom.currency.Currency;
import org.estatio.dom.party.Party;

import lombok.Getter;
import lombok.Setter;

@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.DATASTORE
        ,schema = "dbo"   // Isis' ObjectSpecId inferred from @DomainObject#objectType
)
@javax.jdo.annotations.DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE,
        column = "id")
@javax.jdo.annotations.Discriminator(
        strategy = DiscriminatorStrategy.VALUE_MAP,
        column = "discriminator",
        value = "org.estatio.dom.invoice.InvoiceAbstract" // dummy value required because the InvoiceForLease subclass uses this class' FQCN (for backward compatibility; see EST-1084)
)
@javax.jdo.annotations.Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")
@javax.jdo.annotations.Queries({
        @javax.jdo.annotations.Query(
                name = "findByStatus", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.dom.invoice.Invoice " +
                        "WHERE status == :status " +
                        "ORDER BY invoiceNumber"),
        @javax.jdo.annotations.Query(
                name = "findByBuyer", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.dom.invoice.Invoice " +
                        "WHERE buyer == :buyer " +
                        "ORDER BY invoiceDate DESC"),
        @javax.jdo.annotations.Query(
                name = "findBySeller", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.dom.invoice.Invoice " +
                        "WHERE seller == :seller " +
                        "ORDER BY invoiceDate DESC"),
        @javax.jdo.annotations.Query(
                name = "findByInvoiceNumber", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.dom.invoice.Invoice " +
                        "WHERE invoiceNumber.matches(:invoiceNumber) "
                        + "ORDER BY invoiceDate DESC")
})
@Indices({
        @Index(name = "Invoice_invoiceNumber_IDX",
                members = { "invoiceNumber" })
        ,@Index(name = "Invoice_sendTo_IDX",
                members = { "sendTo" })
})
@DomainObject(
        editing = Editing.DISABLED
)
@DomainObjectLayout(bookmarking = BookmarkPolicy.AS_ROOT)
public abstract class Invoice<T extends Invoice<T>>
        extends UdoDomainObject2<T>
        implements WithApplicationTenancyAny, WithApplicationTenancyPathPersisted {

    public Invoice(final String keyProperties) {
        super(keyProperties);
    }


    @Property(hidden = Where.EVERYWHERE, optionality = Optionality.OPTIONAL)
    @Getter @Setter
    private String uuid;

    @javax.jdo.annotations.Column(
            length = AtPathType.Meta.MAX_LEN,
            allowsNull = "false",
            name = "atPath"
    )
    @Property(hidden = Where.EVERYWHERE)
    @Getter @Setter
    private String applicationTenancyPath;

    @PropertyLayout(
            named = "Application Level",
            describedAs = "Determines those users for whom this object is available to view and/or modify."
    )
    public ApplicationTenancy getApplicationTenancy() {
        return securityApplicationTenancyRepository.findByPathCached(getApplicationTenancyPath());
    }

    // //////////////////////////////////////

    public String title() {
        if (getInvoiceNumber() != null) {
            return String.format("Invoice %s", getInvoiceNumber());
        }
        if (getCollectionNumber() != null) {
            return String.format("Collection %s", getCollectionNumber());
        }
        return String.format("Temp *%08d", Integer.parseInt(getId()));
    }

    // //////////////////////////////////////

    @Property(hidden = Where.OBJECT_FORMS)
    public String getNumber() {
        return ObjectUtils.firstNonNull(
                getInvoiceNumber(),
                getCollectionNumber(),
                title());
    }

    // //////////////////////////////////////

    @javax.jdo.annotations.Column(name = "buyerPartyId", allowsNull = "false")
    @Getter @Setter
    private Party buyer;

    @javax.jdo.annotations.Column(name = "sellerPartyId", allowsNull = "false")
    @Property(hidden = Where.ALL_TABLES)
    @Getter @Setter
    private Party seller;

    @javax.jdo.annotations.Column(allowsNull = "true", length = InvoiceNumberType.Meta.MAX_LEN)
    @Property(hidden = Where.ALL_TABLES)
    @Getter @Setter
    private String collectionNumber;

    @javax.jdo.annotations.Column(allowsNull = "true", length = InvoiceNumberType.Meta.MAX_LEN)
    @Property(hidden = Where.ALL_TABLES)
    @Getter @Setter
    private String invoiceNumber;

    @javax.jdo.annotations.Column(allowsNull = "true")
    @javax.jdo.annotations.Persistent
    @Getter @Setter
    private LocalDate invoiceDate;

    @javax.jdo.annotations.Column(name = "sendToCommunicationChannelId", allowsNull = "true")
    @javax.jdo.annotations.Persistent
    @Getter @Setter
    private CommunicationChannel sendTo;

    @javax.jdo.annotations.Column(allowsNull = "false")
    @javax.jdo.annotations.Persistent
    @Getter @Setter
    private LocalDate dueDate;

    @javax.jdo.annotations.Column(allowsNull = "true", length = Invoice.DescriptionType.Meta.MAX_LEN)
    @Property(editing = Editing.ENABLED) // TODO: this doesn't work, ISIS-1478
    @PropertyLayout(multiLine = Invoice.DescriptionType.Meta.MULTI_LINE)
    @Getter @Setter
    private String description;

    public String disableDescription() {
        if (isImmutable()) {
            return "Invoice can't be changed";
        }
        return null;
    }


    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public Invoice changeDescription(
            final @ParameterLayout(multiLine = 3) String description) {
        setDescription(description);
        return this;
    }

    public String default0ChangeDescription() {
        return getDescription();
    }

    public String disableChangeDescription(
            final String description) {
        return disableDescription();
    }


    @Mixin
    public static class _changeDueDate {

        private final Invoice invoice;

        public _changeDueDate(final Invoice invoice) {
            this.invoice = invoice;
        }

        @Action(semantics = SemanticsOf.IDEMPOTENT)
        @ActionLayout(contributed = Contributed.AS_ACTION)
        public void $$(
                final LocalDate dueDate) {
            invoice.setDueDate(dueDate);
        }

        public LocalDate default0$$(
                final LocalDate dueDate) {
            return invoice.getDueDate();
        }

        public String disable$$(
                final LocalDate dueDate) {
            if (invoice.isImmutable()) {
                return "Due date can't be changed";
            }
            return null;
        }

    }


    @javax.jdo.annotations.Column(allowsNull = "false", length = InvoiceStatus.Meta.MAX_LEN)
    @Getter @Setter
    private InvoiceStatus status;


    // REVIEW: invoice generation is not populating this field.
    @javax.jdo.annotations.Column(name = "currencyId", allowsNull = "true")
    @Property(hidden = Where.ALL_TABLES)
    @Getter @Setter
    private Currency currency;


    @javax.jdo.annotations.Column(allowsNull = "false", length = PaymentMethod.Meta.MAX_LEN)
    @Getter @Setter
    private PaymentMethod paymentMethod;


    @Mixin
    public static class _changePaymentMethod {

        private final Invoice invoice;

        public _changePaymentMethod(final Invoice invoice) {
            this.invoice = invoice;
        }

        @Action(semantics = SemanticsOf.IDEMPOTENT)
        @ActionLayout(contributed = Contributed.AS_ACTION)
        public Invoice $$(
                final PaymentMethod paymentMethod,
                @ParameterLayout(describedAs = "Not currently used")
                final String reason) {
            invoice.setPaymentMethod(paymentMethod);
            return invoice;
        }

        public PaymentMethod default0$$() {
            return invoice.getPaymentMethod();
        }

        public String disable$$(
                final PaymentMethod paymentMethod,
                final String reason) {
            return invoice.getStatus().invoiceIsChangable() ? null : "Invoice cannot be changed";
        }
    }

    @CollectionLayout(defaultView = "table")
    @javax.jdo.annotations.Persistent(mappedBy = "invoice")
    @Getter @Setter
    private SortedSet<InvoiceItem> items = new TreeSet<>();


    @javax.jdo.annotations.Column(allowsNull = "true")
    @Property(hidden = Where.EVERYWHERE)
    @Persistent
    @Getter @Setter
    private BigInteger lastItemSequence;

    @Programmatic
    public BigInteger nextItemSequence() {
        BigInteger nextItemSequence = getLastItemSequence() == null
                ? BigInteger.ONE
                : getLastItemSequence().add(BigInteger.ONE);
        setLastItemSequence(nextItemSequence);
        return nextItemSequence;
    }


    @Property(notPersisted = true)
    public BigDecimal getNetAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceItem item : getItems()) {
            total = total.add(item.getNetAmount());
        }
        return total;
    }

    @Property(notPersisted = true, hidden = Where.ALL_TABLES)
    public BigDecimal getVatAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceItem item : getItems()) {
            total = total.add(item.getVatAmount());
        }
        return total;
    }

    @Property(notPersisted = true)
    public BigDecimal getGrossAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceItem item : getItems()) {
            total = total.add(item.getGrossAmount());
        }
        return total;
    }


    // //////////////////////////////////////

    @Programmatic
    public void createPaymentTerms() {

    }


    // //////////////////////////////////////

    protected boolean isImmutable() {
        return !getStatus().invoiceIsChangable();
    }

    // //////////////////////////////////////


    @Property(hidden = Where.ALL_TABLES)
    @javax.jdo.annotations.Column(allowsNull = "true", name = "paidByBankMandateId")
    @Getter @Setter
    private BankMandate paidBy;


    // //////////////////////////////////////



    @Mixin
    public static class _remove {

        private final Invoice<?> invoice;

        public _remove(final Invoice invoice) {
            this.invoice = invoice;
        }

        @Action(invokeOn = InvokeOn.OBJECT_AND_COLLECTION, semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE)
        @ActionLayout(contributed = Contributed.AS_ACTION)
        public void $$() {
            // Can be called as bulk so have a safeguard
            if (disable$$() == null) {
                for (InvoiceItem item : invoice.getItems()) {
                    item.remove();
                }
                paperclipRepository.deleteIfAttachedTo(invoice, PaperclipRepository.Policy.PAPERCLIPS_AND_DOCUMENTS_IF_ORPHANED);
                repositoryService.remove(invoice);
            }
        }

        public String disable$$() {
            if (!invoice.getStatus().invoiceIsChangable()) {
                return "Only invoices with status New can be removed.";
            }
            return null;
        }

        @javax.inject.Inject
        PaperclipRepository paperclipRepository;

        @javax.inject.Inject
        RepositoryService repositoryService;

    }



    public static class Predicates {

        public static Predicate<Invoice> isChangeable() {
            return invoice -> invoice.getStatus().invoiceIsChangable();
        }

        public static Predicate<Invoice> noLongerChangeable() {
            return com.google.common.base.Predicates.not(Invoice.Predicates.isChangeable());
        }

    }


    public static class InvoiceNumberType {

        private InvoiceNumberType() {}

        public static class Meta {

            /**
             * TODO: review
             */
            public static final int MAX_LEN = 16;

            private Meta() {}

        }

    }


    public static class DescriptionType {

        private DescriptionType() {}

        public static class Meta {

            public static final int MAX_LEN = org.incode.module.base.dom.types.DescriptionType.Meta.MAX_LEN;
            public static final int MULTI_LINE = org.incode.module.base.dom.types.DescriptionType.Meta.MULTI_LINE;

            private Meta() {}

        }

    }

}
