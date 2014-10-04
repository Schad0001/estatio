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
package org.estatio.app;

import org.apache.isis.applib.AbstractContainedObject;
import org.apache.isis.applib.services.eventbus.EventBusService;

import org.estatio.services.clock.ClockService;

public abstract class EstatioViewModel extends AbstractContainedObject {

    private ClockService clockService;

    protected ClockService getClockService() {
        return clockService;
    }

    public final void injectClockService(final ClockService clockService) {
        this.clockService = clockService;
    }

    // //////////////////////////////////////

    /**
     * a default value is used to prevent null pointers for objects
     * being initialized where the service has not yet been injected into.
     */
    private EventBusService eventBusService = EventBusService.NOOP;

    protected EventBusService getEventBusService() {
        return eventBusService;
    }

    /**
     * Unlike domain services, domain objects are NOT automatically registered
     * with the {@link EventBusService}; Isis makes no guarantees as to whether
     * a subscribing domain object is in memory or not to receive the event.
     */
    public final void injectEventBusService(final EventBusService eventBusService) {
        this.eventBusService = eventBusService;
    }
}
