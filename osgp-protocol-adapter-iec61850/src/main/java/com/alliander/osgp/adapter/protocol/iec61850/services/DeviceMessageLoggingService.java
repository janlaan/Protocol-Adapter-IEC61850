/**
 * Copyright 2017 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.protocol.iec61850.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alliander.osgp.adapter.protocol.iec61850.device.DeviceRequest;
import com.alliander.osgp.adapter.protocol.iec61850.domain.valueobjects.DeviceMessageLog;
import com.alliander.osgp.adapter.protocol.iec61850.infra.messaging.Iec61850LogItemRequestMessage;
import com.alliander.osgp.adapter.protocol.iec61850.infra.messaging.Iec61850LogItemRequestMessageSender;

@Service
public class DeviceMessageLoggingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceMessageLoggingService.class);

    private static Iec61850LogItemRequestMessageSender iec61850LogItemRequestMessageSender;

    @Autowired
    public DeviceMessageLoggingService(final Iec61850LogItemRequestMessageSender iec61850LogItemRequestMessageSender) {
        DeviceMessageLoggingService.iec61850LogItemRequestMessageSender = iec61850LogItemRequestMessageSender;
    }

    public static void logMessage(final DeviceRequest deviceRequest, final boolean incoming, final boolean valid,
            final String message, final int size) {

        final String deviceIdentification = deviceRequest.getDeviceIdentification();
        final String organisationIdentification = deviceRequest.getOrganisationIdentification();
        final String command = deviceRequest.getClass().getSimpleName();

        final Iec61850LogItemRequestMessage iec61850LogItemRequestMessage = new Iec61850LogItemRequestMessage(
                deviceIdentification, organisationIdentification, incoming, valid, command + " - " + message, size);

        LOGGER.info("Sending iec61850LogItemRequestMessage for device: {}", deviceIdentification);
        iec61850LogItemRequestMessageSender.send(iec61850LogItemRequestMessage);
    }

    public static void logMessage(final DeviceMessageLog deviceMessageLog, final String deviceIdentification,
            final String organisationIdentification, final boolean incoming) {

        final Iec61850LogItemRequestMessage iec61850LogItemRequestMessage = new Iec61850LogItemRequestMessage(
                deviceIdentification, organisationIdentification, incoming, true, deviceMessageLog.getMessage(), 0);

        LOGGER.info("Sending iec61850LogItemRequestMessage for device: {}", deviceIdentification);
        iec61850LogItemRequestMessageSender.send(iec61850LogItemRequestMessage);
    }
}
