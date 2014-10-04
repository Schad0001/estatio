/*
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
package org.estatio.dom.party.relationship;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.ViewModel;
import org.apache.isis.applib.annotation.Where;

import org.estatio.app.EstatioViewModel;
import org.estatio.dom.party.Party;

@ViewModel
public class PartyRelationshipView extends EstatioViewModel {

    public PartyRelationshipView() {
    }

    public PartyRelationshipView(PartyRelationship partyRelationship, Party fromParty) {
        if (fromParty.equals(partyRelationship.getFrom())) {
            setFrom(partyRelationship.getFrom());
            setTo(partyRelationship.getTo());
        } else {
            setFrom(partyRelationship.getTo());
            setTo(partyRelationship.getFrom());
        }
        setRelationshipType(partyRelationship.getRelationshipType());
        setStartDate(partyRelationship.getStartDate());
        setEndDate(partyRelationship.getEndDate());
        setDescription(partyRelationship.getDescription());
    }

    // //////////////////////////////////////

    public String title() {
        return String.format("%s is %s of %s",
                getFrom().getName(),
                getRelationshipType().fromTitle(),
                getTo().getName());
    }

    // //////////////////////////////////////

    private Party from;

    @Hidden(where = Where.REFERENCES_PARENT)
    @MemberOrder(sequence = "1")
    public Party getFrom() {
        return from;
    }

    public void setFrom(Party from) {
        this.from = from;
    }

    // //////////////////////////////////////

    private Party to;

    @MemberOrder(sequence = "2")
    public Party getTo() {
        return to;
    }

    public void setTo(Party to) {
        this.to = to;
    }

    // //////////////////////////////////////

    private PartyRelationshipType relationshipType;

    @MemberOrder(sequence = "3")
    @Hidden()
    public PartyRelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(PartyRelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }

    @Named("Title")
    public String getRelationshipToTitle() {
        return getRelationshipType().toTitle();
    }

    @Named("Title")
    @Hidden(where = Where.ALL_TABLES)
    public String getRelationshipFromTitle() {
        return getRelationshipType().toTitle();
    }

    // //////////////////////////////////////

    private LocalDate startDate;

    @MemberOrder(sequence = "4")
    @Hidden(where = Where.PARENTED_TABLES)
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    // //////////////////////////////////////

    private LocalDate endDate;

    @MemberOrder(sequence = "5")
    @Hidden(where = Where.PARENTED_TABLES)
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    // //////////////////////////////////////

    private String description;

    @MemberOrder(sequence = "6")
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

}
