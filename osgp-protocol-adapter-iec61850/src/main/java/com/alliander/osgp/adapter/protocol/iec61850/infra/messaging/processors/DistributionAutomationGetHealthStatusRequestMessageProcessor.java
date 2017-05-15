/**
 * Copyright 2017 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.protocol.iec61850.infra.messaging.processors;

import com.alliander.osgp.adapter.protocol.iec61850.device.da.rtu.DaDeviceRequest;
import com.alliander.osgp.adapter.protocol.iec61850.infra.messaging.DaRtuDeviceRequestMessageProcessor;
import com.alliander.osgp.adapter.protocol.iec61850.infra.messaging.DeviceRequestMessageType;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.Iec61850Client;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.DataAttribute;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.DeviceConnection;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.Function;

import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.Health;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.NodeContainer;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.SubDataAttribute;
import org.openmuc.openiec61850.Fc;
import org.springframework.stereotype.Component;

import org.osgpfoundation.osgp.dto.da.GetHealthStatusResponseDto;

/**
 * Class for processing distribution automation get health status request messages
 */
@Component("iec61850DistributionAutomationGetHealthStatusRequestMessageProcessor")
public class DistributionAutomationGetHealthStatusRequestMessageProcessor extends DaRtuDeviceRequestMessageProcessor {
    public DistributionAutomationGetHealthStatusRequestMessageProcessor() {
        super(DeviceRequestMessageType.GET_HEALTH_STATUS);
    }

    @Override
    public Function<GetHealthStatusResponseDto> getDataFunction(final Iec61850Client client, final DeviceConnection connection, final DaDeviceRequest deviceRequest) {
        return () -> {
            final int logicalDeviceIndex = 1;
            final NodeContainer containingNode = connection.getFcModelNode(
                    com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.LogicalDevice.RTU, logicalDeviceIndex,
                    com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.LogicalNode.PHYSICAL_DEVICE_ONE,
                    DataAttribute.PHYSICAL_HEALTH, Fc.ST);
            client.readNodeDataValues(connection.getConnection().getClientAssociation(), containingNode.getFcmodelNode());
            return new GetHealthStatusResponseDto(Health.fromByte(containingNode.getByte(SubDataAttribute.STATE).getValue()));
        };
    }
}