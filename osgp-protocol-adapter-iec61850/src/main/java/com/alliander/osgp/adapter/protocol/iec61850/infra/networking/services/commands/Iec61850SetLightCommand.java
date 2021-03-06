/**
 * Copyright 2014-2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openmuc.openiec61850.BdaBoolean;
import org.openmuc.openiec61850.Fc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alliander.osgp.adapter.protocol.iec61850.domain.valueobjects.DeviceMessageLog;
import com.alliander.osgp.adapter.protocol.iec61850.exceptions.NodeException;
import com.alliander.osgp.adapter.protocol.iec61850.exceptions.ProtocolAdapterException;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.Iec61850Client;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.DataAttribute;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.DeviceConnection;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.Function;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.LogicalDevice;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.LogicalNode;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.NodeContainer;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.SubDataAttribute;
import com.alliander.osgp.adapter.protocol.iec61850.services.DeviceMessageLoggingService;
import com.alliander.osgp.dto.valueobjects.LightValueDto;

public class Iec61850SetLightCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(Iec61850SetLightCommand.class);

    /**
     * Switch one or more light relays of a switching device.
     *
     * @param iec61850Client
     *            The {@link Iec61850Client} instance.
     * @param deviceConnection
     *            The {@link DeviceConnection} instance.
     * @param relaysWithInternalIdToSwitch
     *            List of {@link LightValueDto}'s which represent the relays to
     *            switch on or off.
     * @param functionName
     *            The name of the function reported to {@link DeviceMessageLog}.
     *
     * @throws ProtocolAdapterException
     *             In case the switch action(s) for light relays fail.
     */
    public void switchLightRelays(final Iec61850Client iec61850Client, final DeviceConnection deviceConnection,
            final List<LightValueDto> relaysWithInternalIdToSwitch, final String functionName)
            throws ProtocolAdapterException {

        final Function<Void> function = new Function<Void>() {

            @Override
            public Void apply(final DeviceMessageLog deviceMessageLog) throws ProtocolAdapterException {

                for (final LightValueDto relayWithInternalIdToSwitch : relaysWithInternalIdToSwitch) {
                    final int index = relayWithInternalIdToSwitch.getIndex();
                    final boolean on = relayWithInternalIdToSwitch.isOn();
                    final String deviceIdentification = deviceConnection.getDeviceIdentification();

                    LOGGER.info("Trying to switch light relay with internal index: {} on: {} for device: {}", index, on,
                            deviceIdentification);

                    try {
                        Iec61850SetLightCommand.this.switchLightRelay(iec61850Client, deviceConnection,
                                deviceMessageLog, index, on);
                    } catch (final Exception e) {
                        LOGGER.error("Exception during switchLightRelay()", e);
                        throw new ProtocolAdapterException(String.format(
                                "Failed to switch light relay with internal index: %d for device: %s",
                                relayWithInternalIdToSwitch.getIndex(), deviceConnection.getDeviceIdentification()));
                    }
                }

                DeviceMessageLoggingService.logMessage(deviceMessageLog, deviceConnection.getDeviceIdentification(),
                        deviceConnection.getOrganisationIdentification(), false);

                return null;
            }
        };

        iec61850Client.sendCommandWithRetry(function, StringUtils.isEmpty(functionName) ? "SetLight" : functionName,
                deviceConnection.getDeviceIdentification());
    }

    private void switchLightRelay(final Iec61850Client iec61850Client, final DeviceConnection deviceConnection,
            final DeviceMessageLog deviceMessageLog, final int index, final boolean on) throws NodeException {

        final LogicalNode logicalNode = LogicalNode.getSwitchComponentByIndex(index);

        // Check if CfSt.enbOper [CF] is set to true. If it is
        // not set to true, the relay can not be operated.
        final NodeContainer masterControl = deviceConnection.getFcModelNode(LogicalDevice.LIGHTING, logicalNode,
                DataAttribute.MASTER_CONTROL, Fc.CF);
        iec61850Client.readNodeDataValues(deviceConnection.getConnection().getClientAssociation(),
                masterControl.getFcmodelNode());

        final BdaBoolean enbOper = masterControl.getBoolean(SubDataAttribute.ENABLE_OPERATION);
        if (enbOper.getValue()) {
            LOGGER.info("masterControl.enbOper is true, switching of relay {} is enabled", index);
        } else {
            LOGGER.info("masterControl.enbOper is false, switching of relay {} is disabled", index);
            // Set the value to true.
            masterControl.writeBoolean(SubDataAttribute.ENABLE_OPERATION, true);
            LOGGER.info("set masterControl.enbOper to true to enable switching of relay {}", index);

            deviceMessageLog.addVariable(logicalNode, DataAttribute.MASTER_CONTROL, Fc.CF,
                    SubDataAttribute.ENABLE_OPERATION, Boolean.toString(true));
        }

        // Switch the relay using Pos.Oper.ctlVal [CO].
        final NodeContainer position = deviceConnection.getFcModelNode(LogicalDevice.LIGHTING, logicalNode,
                DataAttribute.POSITION, Fc.CO);
        iec61850Client.readNodeDataValues(deviceConnection.getConnection().getClientAssociation(),
                position.getFcmodelNode());

        final NodeContainer operation = position.getChild(SubDataAttribute.OPERATION);

        final BdaBoolean controlValue = operation.getBoolean(SubDataAttribute.CONTROL_VALUE);

        LOGGER.info(String.format("Switching relay %d %s", index, on ? "on" : "off"));
        controlValue.setValue(on);
        operation.write();

        deviceMessageLog.addVariable(logicalNode, DataAttribute.POSITION, Fc.CO, SubDataAttribute.OPERATION,
                SubDataAttribute.CONTROL_VALUE, Boolean.toString(on));
    }
}
