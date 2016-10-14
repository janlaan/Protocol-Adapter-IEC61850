/**
 * Copyright 2014-2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.openmuc.openiec61850.ServerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alliander.osgp.adapter.protocol.iec61850.application.mapping.Iec61850Mapper;
import com.alliander.osgp.adapter.protocol.iec61850.device.DeviceMessageStatus;
import com.alliander.osgp.adapter.protocol.iec61850.device.DeviceRequest;
import com.alliander.osgp.adapter.protocol.iec61850.device.DeviceResponseHandler;
import com.alliander.osgp.adapter.protocol.iec61850.device.ssld.SsldDeviceService;
import com.alliander.osgp.adapter.protocol.iec61850.device.ssld.requests.GetPowerUsageHistoryDeviceRequest;
import com.alliander.osgp.adapter.protocol.iec61850.device.ssld.requests.SetConfigurationDeviceRequest;
import com.alliander.osgp.adapter.protocol.iec61850.device.ssld.requests.SetEventNotificationsDeviceRequest;
import com.alliander.osgp.adapter.protocol.iec61850.device.ssld.requests.SetLightDeviceRequest;
import com.alliander.osgp.adapter.protocol.iec61850.device.ssld.requests.SetScheduleDeviceRequest;
import com.alliander.osgp.adapter.protocol.iec61850.device.ssld.requests.SetTransitionDeviceRequest;
import com.alliander.osgp.adapter.protocol.iec61850.device.ssld.requests.UpdateDeviceSslCertificationDeviceRequest;
import com.alliander.osgp.adapter.protocol.iec61850.device.ssld.requests.UpdateFirmwareDeviceRequest;
import com.alliander.osgp.adapter.protocol.iec61850.device.ssld.responses.EmptyDeviceResponse;
import com.alliander.osgp.adapter.protocol.iec61850.device.ssld.responses.GetConfigurationDeviceResponse;
import com.alliander.osgp.adapter.protocol.iec61850.device.ssld.responses.GetFirmwareVersionDeviceResponse;
import com.alliander.osgp.adapter.protocol.iec61850.device.ssld.responses.GetPowerUsageHistoryDeviceResponse;
import com.alliander.osgp.adapter.protocol.iec61850.device.ssld.responses.GetStatusDeviceResponse;
import com.alliander.osgp.adapter.protocol.iec61850.domain.valueobjects.EventType;
import com.alliander.osgp.adapter.protocol.iec61850.exceptions.ConnectionFailureException;
import com.alliander.osgp.adapter.protocol.iec61850.exceptions.ProtocolAdapterException;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.Iec61850Client;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.Iec61850ClientAssociation;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.Iec61850Connection;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.DeviceConnection;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.IED;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper.LogicalDevice;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850ClearReportCommand;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850EnableReportingCommand;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850GetConfigurationCommand;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850GetFirmwareVersionCommand;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850GetStatusCommand;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850PowerUsageHistoryCommand;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850RebootCommand;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850SetConfigurationCommand;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850SetEventNotificationFilterCommand;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850SetLightCommand;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850SetScheduleCommand;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850TransitionCommand;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850UpdateFirmwareCommand;
import com.alliander.osgp.adapter.protocol.iec61850.infra.networking.services.commands.Iec61850UpdateSslCertificateCommand;
import com.alliander.osgp.core.db.api.iec61850.application.services.SsldDataService;
import com.alliander.osgp.core.db.api.iec61850.entities.DeviceOutputSetting;
import com.alliander.osgp.core.db.api.iec61850.entities.Ssld;
import com.alliander.osgp.core.db.api.iec61850valueobjects.RelayType;
import com.alliander.osgp.dto.valueobjects.ConfigurationDto;
import com.alliander.osgp.dto.valueobjects.DeviceStatusDto;
import com.alliander.osgp.dto.valueobjects.EventNotificationTypeDto;
import com.alliander.osgp.dto.valueobjects.FirmwareVersionDto;
import com.alliander.osgp.dto.valueobjects.LightValueDto;
import com.alliander.osgp.dto.valueobjects.PowerUsageDataDto;
import com.alliander.osgp.shared.exceptionhandling.ComponentType;
import com.alliander.osgp.shared.exceptionhandling.FunctionalException;
import com.alliander.osgp.shared.exceptionhandling.FunctionalExceptionType;
import com.alliander.osgp.shared.exceptionhandling.TechnicalException;

@Component
public class Iec61850SsldDeviceService implements SsldDeviceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(Iec61850SsldDeviceService.class);

    @Autowired
    private Iec61850DeviceConnectionService iec61850DeviceConnectionService;

    @Autowired
    private SsldDataService ssldDataService;

    @Autowired
    private Iec61850Client iec61850Client;

    @Autowired
    private Iec61850Mapper mapper;

    // Timeout between the SetLight and getStatus during the device self-test
    @Autowired
    private int selftestTimeout;

    @Autowired
    private int disconnectDelay;

    @Override
    public void getStatus(final DeviceRequest deviceRequest, final DeviceResponseHandler deviceResponseHandler) {
        try {
            final DeviceConnection deviceConnection = this.connectToDevice(deviceRequest);

            // Getting the SSLD for the device output-settings.
            final Ssld ssld = this.ssldDataService.findDevice(deviceRequest.getDeviceIdentification());
            final DeviceStatusDto deviceStatus = new Iec61850GetStatusCommand().getStatusFromDevice(
                    this.iec61850Client, deviceConnection, ssld);

            final GetStatusDeviceResponse deviceResponse = new GetStatusDeviceResponse(
                    deviceRequest.getOrganisationIdentification(), deviceRequest.getDeviceIdentification(),
                    deviceRequest.getCorrelationUid(), deviceStatus);

            deviceResponseHandler.handleResponse(deviceResponse);
            this.iec61850DeviceConnectionService.disconnect(deviceRequest.getDeviceIdentification());
        } catch (final ConnectionFailureException se) {
            this.handleConnectionFailureException(deviceRequest, deviceResponseHandler, se);
        } catch (final Exception e) {
            this.handleException(deviceRequest, deviceResponseHandler, e);
        }
        this.iec61850DeviceConnectionService.disconnect(deviceRequest.getDeviceIdentification());
    }

    @Override
    public void getPowerUsageHistory(final GetPowerUsageHistoryDeviceRequest deviceRequest,
            final DeviceResponseHandler deviceResponseHandler) {
        try {
            final DeviceConnection deviceConnection = this.connectToDevice(deviceRequest);

            // Getting the SSLD for the device output-settings.
            final Ssld ssld = this.ssldDataService.findDevice(deviceRequest.getDeviceIdentification());
            final List<DeviceOutputSetting> deviceOutputSettingsLightRelays = this.ssldDataService.findByRelayType(
                    ssld, RelayType.LIGHT);

            final List<PowerUsageDataDto> powerUsageHistoryData = new Iec61850PowerUsageHistoryCommand()
                    .getPowerUsageHistoryDataFromDevice(this.iec61850Client, deviceConnection,
                            deviceRequest.getPowerUsageHistoryContainer(), deviceOutputSettingsLightRelays);

            final GetPowerUsageHistoryDeviceResponse deviceResponse = new GetPowerUsageHistoryDeviceResponse(
                    deviceRequest.getOrganisationIdentification(), deviceRequest.getDeviceIdentification(),
                    deviceRequest.getCorrelationUid(), DeviceMessageStatus.OK, powerUsageHistoryData);

            deviceResponseHandler.handleResponse(deviceResponse);
            this.iec61850DeviceConnectionService.disconnect(deviceRequest.getDeviceIdentification());
        } catch (final ConnectionFailureException se) {
            this.handleConnectionFailureException(deviceRequest, deviceResponseHandler, se);
        } catch (final Exception e) {
            this.handleException(deviceRequest, deviceResponseHandler, e);
        }
        this.iec61850DeviceConnectionService.disconnect(deviceRequest.getDeviceIdentification());
    }

    @Override
    public void setLight(final SetLightDeviceRequest deviceRequest, final DeviceResponseHandler deviceResponseHandler) {
        try {
            final DeviceConnection deviceConnection = this.connectToDevice(deviceRequest);

            // Getting the SSLD for the device output-settings.
            final Ssld ssld = this.ssldDataService.findDevice(deviceRequest.getDeviceIdentification());
            final Iec61850SetLightCommand iec61850SetLightCommand = new Iec61850SetLightCommand();

            for (final LightValueDto lightValue : deviceRequest.getLightValuesContainer().getLightValues()) {
                // for index 0, only devices LIGHT RelayTypes have to be
                // switched
                boolean switchAllLightRelays = false;
                if (lightValue == null) {
                    switchAllLightRelays = true;
                } else if (lightValue.getIndex() == null) {
                    switchAllLightRelays = true;
                } else if (lightValue.getIndex() == 0) {
                    switchAllLightRelays = true;
                }

                LOGGER.info("switchAllLightRelays: {}", switchAllLightRelays);

                if (switchAllLightRelays) {
                    for (final DeviceOutputSetting deviceOutputSetting : this.ssldDataService.findByRelayType(ssld,
                            RelayType.LIGHT)) {
                        iec61850SetLightCommand.switchLightRelay(this.iec61850Client, deviceConnection,
                                deviceOutputSetting.getInternalId(), lightValue.isOn());
                    }
                } else {

                    final DeviceOutputSetting deviceOutputSetting = this.ssldDataService
                            .getDeviceOutputSettingForExternalIndex(ssld, lightValue.getIndex());

                    if (deviceOutputSetting != null) {

                        // You can only switch LIGHT relays that are used
                        this.checkRelay(deviceOutputSetting.getRelayType(), RelayType.LIGHT,
                                deviceOutputSetting.getInternalId());

                        iec61850SetLightCommand.switchLightRelay(this.iec61850Client, deviceConnection,
                                deviceOutputSetting.getInternalId(), lightValue.isOn());
                    }
                }
            }
            this.createSuccessfulDefaultResponse(deviceRequest, deviceResponseHandler);
        } catch (final ConnectionFailureException se) {
            this.handleConnectionFailureException(deviceRequest, deviceResponseHandler, se);
        } catch (final Exception e) {
            this.handleException(deviceRequest, deviceResponseHandler, e);
        }
        this.iec61850DeviceConnectionService.disconnect(deviceRequest.getDeviceIdentification());
    }

    @Override
    public void setConfiguration(final SetConfigurationDeviceRequest deviceRequest,
            final DeviceResponseHandler deviceResponseHandler) {
        try {
            final DeviceConnection deviceConnection = this.connectToDevice(deviceRequest);
            final ConfigurationDto configuration = deviceRequest.getConfiguration();

            // Ignoring required, unused fields DALI-configuration, meterType,
            // shortTermHistoryIntervalMinutes, preferredLinkType,
            // longTermHistoryInterval and longTermHistoryIntervalType.
            new Iec61850SetConfigurationCommand().setConfigurationOnDevice(this.iec61850Client, deviceConnection,
                    configuration);

            this.createSuccessfulDefaultResponse(deviceRequest, deviceResponseHandler);
        } catch (final ConnectionFailureException se) {
            this.handleConnectionFailureException(deviceRequest, deviceResponseHandler, se);
        } catch (final Exception e) {
            this.handleException(deviceRequest, deviceResponseHandler, e);
        }
        this.iec61850DeviceConnectionService.disconnect(deviceRequest.getDeviceIdentification());
    }

    @Override
    public void getConfiguration(final DeviceRequest deviceRequest, final DeviceResponseHandler deviceResponseHandler) {
        try {
            final DeviceConnection deviceConnection = this.connectToDevice(deviceRequest);

            // Getting the SSLD for the device output-settings.
            final Ssld ssld = this.ssldDataService.findDevice(deviceRequest.getDeviceIdentification());

            final ConfigurationDto configuration = new Iec61850GetConfigurationCommand().getConfigurationFromDevice(
                    this.iec61850Client, deviceConnection, ssld, this.mapper);

            final GetConfigurationDeviceResponse response = new GetConfigurationDeviceResponse(
                    deviceRequest.getOrganisationIdentification(), deviceRequest.getDeviceIdentification(),
                    deviceRequest.getCorrelationUid(), DeviceMessageStatus.OK, configuration);

            deviceResponseHandler.handleResponse(response);
            this.iec61850DeviceConnectionService.disconnect(deviceRequest.getDeviceIdentification());
        } catch (final ConnectionFailureException se) {
            this.handleConnectionFailureException(deviceRequest, deviceResponseHandler, se);
        } catch (final Exception e) {
            this.handleException(deviceRequest, deviceResponseHandler, e);
        }
        this.iec61850DeviceConnectionService.disconnect(deviceRequest.getDeviceIdentification());
    }

    @Override
    public void setReboot(final DeviceRequest deviceRequest, final DeviceResponseHandler deviceResponseHandler) {
        try {
            final DeviceConnection deviceConnection = this.connectToDevice(deviceRequest);

            new Iec61850RebootCommand().rebootDevice(this.iec61850Client, deviceConnection);

            this.createSuccessfulDefaultResponse(deviceRequest, deviceResponseHandler);
        } catch (final ConnectionFailureException se) {
            this.handleConnectionFailureException(deviceRequest, deviceResponseHandler, se);
        } catch (final Exception e) {
            this.handleException(deviceRequest, deviceResponseHandler, e);
        }
        this.iec61850DeviceConnectionService.disconnect(deviceRequest.getDeviceIdentification());
    }

    @Override
    public void runSelfTest(final DeviceRequest deviceRequest, final DeviceResponseHandler deviceResponseHandler,
            final boolean startOfTest) {
        // Assuming all goes well.
        final DeviceMessageStatus status = DeviceMessageStatus.OK;

        try {
            final DeviceConnection deviceConnection = this.connectToDevice(deviceRequest);

            // Getting the SSLD for the device output-settings.
            final Ssld ssld = this.ssldDataService.findDevice(deviceRequest.getDeviceIdentification());

            // This list will contain the external indexes of all light relays.
            // It's used to interpret the deviceStatus data later on.
            final List<Integer> lightRelays = new ArrayList<>();

            LOGGER.info("Turning all lights relays {}", startOfTest ? "on" : "off");
            final Iec61850SetLightCommand iec61850SetLightCommand = new Iec61850SetLightCommand();

            // Turning all light relays on or off, depending on the value of
            // startOfTest.
            for (final DeviceOutputSetting deviceOutputSetting : this.ssldDataService.findByRelayType(ssld,
                    RelayType.LIGHT)) {
                lightRelays.add(deviceOutputSetting.getExternalId());
                iec61850SetLightCommand.switchLightRelay(this.iec61850Client, deviceConnection,
                        deviceOutputSetting.getInternalId(), startOfTest);
            }

            // Sleep and wait.
            try {
                LOGGER.info("Waiting {} seconds before getting the device status", this.selftestTimeout / 1000);
                Thread.sleep(this.selftestTimeout);
            } catch (final InterruptedException e) {
                LOGGER.error("An error occured during the device selftest timeout.", e);
                throw new TechnicalException(ComponentType.PROTOCOL_IEC61850,
                        "An error occured during the device selftest timeout.");
            }

            // Reconnecting to the device.
            this.iec61850DeviceConnectionService.connect(deviceRequest.getIpAddress(),
                    deviceRequest.getDeviceIdentification(), IED.FLEX_OVL, LogicalDevice.LIGHTING);

            // Getting the status.
            final DeviceStatusDto deviceStatus = new Iec61850GetStatusCommand().getStatusFromDevice(
                    this.iec61850Client, deviceConnection, ssld);

            LOGGER.info("Fetching and checking the devicestatus");

            // Checking to see if all light relays have the correct state.
            for (final LightValueDto lightValue : deviceStatus.getLightValues()) {
                if (lightRelays.contains(lightValue.getIndex()) && lightValue.isOn() != startOfTest) {
                    // One the the light relays is not in the correct state,
                    // request failed.
                    throw new ProtocolAdapterException("not all relays are ".concat(startOfTest ? "on" : "off"));
                }
            }

            LOGGER.info("All lights relays are {}, returning OK", startOfTest ? "on" : "off");

            this.createSuccessfulDefaultResponse(deviceRequest, deviceResponseHandler, status);
        } catch (final ConnectionFailureException se) {
            this.handleConnectionFailureException(deviceRequest, deviceResponseHandler, se);
        } catch (final Exception e) {
            this.handleException(deviceRequest, deviceResponseHandler, e);
        }
        this.iec61850DeviceConnectionService.disconnect(deviceRequest.getDeviceIdentification());
    }

    @Override
    public void setSchedule(final SetScheduleDeviceRequest deviceRequest,
            final DeviceResponseHandler deviceResponseHandler) {
        try {
            final DeviceConnection deviceConnection = this.connectToDevice(deviceRequest);

            // Getting the SSLD for the device output-settings.
            final Ssld ssld = this.ssldDataService.findDevice(deviceRequest.getDeviceIdentification());

            new Iec61850SetScheduleCommand().setScheduleOnDevice(this.iec61850Client, deviceConnection,
                    deviceRequest.getRelayType(), deviceRequest.getScheduleMessageDataContainer().getScheduleList(),
                    ssld, this.ssldDataService);

            this.createSuccessfulDefaultResponse(deviceRequest, deviceResponseHandler);
        } catch (final ConnectionFailureException se) {
            this.handleConnectionFailureException(deviceRequest, deviceResponseHandler, se);
        } catch (final ProtocolAdapterException e) {
            this.handleProtocolAdapterException(deviceRequest, deviceResponseHandler, e);
        } catch (final Exception e) {
            this.handleException(deviceRequest, deviceResponseHandler, e);
        }
        this.iec61850DeviceConnectionService.disconnect(deviceRequest.getDeviceIdentification());
    }

    @Override
    public void getFirmwareVersion(final DeviceRequest deviceRequest, final DeviceResponseHandler deviceResponseHandler) {
        try {
            final DeviceConnection deviceConnection = this.connectToDevice(deviceRequest);

            final List<FirmwareVersionDto> firmwareVersions = new Iec61850GetFirmwareVersionCommand()
            .getFirmwareVersionFromDevice(this.iec61850Client, deviceConnection);

            final GetFirmwareVersionDeviceResponse deviceResponse = new GetFirmwareVersionDeviceResponse(
                    deviceRequest.getOrganisationIdentification(), deviceRequest.getDeviceIdentification(),
                    deviceRequest.getCorrelationUid(), firmwareVersions);

            deviceResponseHandler.handleResponse(deviceResponse);
        } catch (final ConnectionFailureException se) {
            this.handleConnectionFailureException(deviceRequest, deviceResponseHandler, se);
        } catch (final Exception e) {
            this.handleException(deviceRequest, deviceResponseHandler, e);
        }
        this.iec61850DeviceConnectionService.disconnect(deviceRequest.getDeviceIdentification());
    }

    @Override
    public void setTransition(final SetTransitionDeviceRequest deviceRequest,
            final DeviceResponseHandler deviceResponseHandler) {
        try {
            final DeviceConnection deviceConnection = this.connectToDevice(deviceRequest);

            new Iec61850TransitionCommand().transitionDevice(this.iec61850Client, deviceConnection,
                    deviceRequest.getTransitionTypeContainer());

            this.createSuccessfulDefaultResponse(deviceRequest, deviceResponseHandler);

            // Enabling device reporting. This is placed here because this is
            // called twice a day.
            new Iec61850EnableReportingCommand().enableReportingOnDeviceWithoutUsingSequenceNumber(this.iec61850Client,
                    deviceConnection);
            // Don't disconnect now! The device should be able to send reports.
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        new Iec61850ClearReportCommand().clearReportOnDevice(deviceConnection);
                    } catch (final ProtocolAdapterException e) {
                        LOGGER.error("Unable to clear report for device: " + deviceRequest.getDeviceIdentification(), e);
                    }
                    Iec61850SsldDeviceService.this.iec61850DeviceConnectionService.disconnect(deviceRequest
                            .getDeviceIdentification());
                }
            }, this.disconnectDelay);
        } catch (final ConnectionFailureException se) {
            this.handleConnectionFailureException(deviceRequest, deviceResponseHandler, se);
        } catch (final Exception e) {
            this.handleException(deviceRequest, deviceResponseHandler, e);
        }
    }

    @Override
    public void updateFirmware(final UpdateFirmwareDeviceRequest deviceRequest,
            final DeviceResponseHandler deviceResponseHandler) {
        try {
            final DeviceConnection deviceConnection = this.connectToDevice(deviceRequest);

            new Iec61850UpdateFirmwareCommand().pushFirmwareToDevice(this.iec61850Client, deviceConnection,
                    deviceRequest.getFirmwareDomain().concat(deviceRequest.getFirmwareUrl()));

            this.createSuccessfulDefaultResponse(deviceRequest, deviceResponseHandler);
        } catch (final ConnectionFailureException se) {
            this.handleConnectionFailureException(deviceRequest, deviceResponseHandler, se);
        } catch (final Exception e) {
            this.handleException(deviceRequest, deviceResponseHandler, e);
        }
        this.iec61850DeviceConnectionService.disconnect(deviceRequest.getDeviceIdentification());
    }

    @Override
    public void updateDeviceSslCertification(final UpdateDeviceSslCertificationDeviceRequest deviceRequest,
            final DeviceResponseHandler deviceResponseHandler) {
        try {
            final DeviceConnection deviceConnection = this.connectToDevice(deviceRequest);

            new Iec61850UpdateSslCertificateCommand().pushSslCertificateToDevice(this.iec61850Client, deviceConnection,
                    deviceRequest.getCertification());

            this.createSuccessfulDefaultResponse(deviceRequest, deviceResponseHandler);
        } catch (final ConnectionFailureException se) {
            this.handleConnectionFailureException(deviceRequest, deviceResponseHandler, se);
        } catch (final Exception e) {
            this.handleException(deviceRequest, deviceResponseHandler, e);
        }
        this.iec61850DeviceConnectionService.disconnect(deviceRequest.getDeviceIdentification());
    }

    @Override
    public void setEventNotifications(final SetEventNotificationsDeviceRequest deviceRequest,
            final DeviceResponseHandler deviceResponseHandler) {
        LOGGER.info("Called setEventNotifications, doing nothing for now and returning OK");

        final List<EventNotificationTypeDto> eventNotifications = deviceRequest.getEventNotificationsContainer()
                .getEventNotifications();
        final String filter = EventType.getEventTypeFilterMaskForNotificationTypes(eventNotifications);

        try {
            final DeviceConnection deviceConnection = this.connectToDevice(deviceRequest);

            new Iec61850SetEventNotificationFilterCommand().setEventNotificationFilterOnDevice(this.iec61850Client,
                    deviceConnection, filter);

            this.createSuccessfulDefaultResponse(deviceRequest, deviceResponseHandler);
        } catch (final ConnectionFailureException se) {
            this.handleConnectionFailureException(deviceRequest, deviceResponseHandler, se);
        } catch (final Exception e) {
            this.handleException(deviceRequest, deviceResponseHandler, e);
        }
        this.iec61850DeviceConnectionService.disconnect(deviceRequest.getDeviceIdentification());
    }

    // ======================================
    // PRIVATE DEVICE COMMUNICATION METHODS =
    // ======================================

    private DeviceConnection connectToDevice(final DeviceRequest deviceRequest) throws ConnectionFailureException {
        this.iec61850DeviceConnectionService.connect(deviceRequest.getIpAddress(),
                deviceRequest.getDeviceIdentification(), IED.FLEX_OVL, LogicalDevice.LIGHTING);
        final ServerModel serverModel = this.iec61850DeviceConnectionService.getServerModel(deviceRequest
                .getDeviceIdentification());
        final Iec61850ClientAssociation iec61850ClientAssociation = this.iec61850DeviceConnectionService
                .getIec61850ClientAssociation(deviceRequest.getDeviceIdentification());
        return new DeviceConnection(new Iec61850Connection(iec61850ClientAssociation, serverModel),
                deviceRequest.getDeviceIdentification(), IED.FLEX_OVL);
    }

    // ========================
    // PRIVATE HELPER METHODS =
    // ========================

    private EmptyDeviceResponse createDefaultResponse(final DeviceRequest deviceRequest,
            final DeviceMessageStatus deviceMessageStatus) {
        return new EmptyDeviceResponse(deviceRequest.getOrganisationIdentification(),
                deviceRequest.getDeviceIdentification(), deviceRequest.getCorrelationUid(), deviceMessageStatus);
    }

    private void createSuccessfulDefaultResponse(final DeviceRequest deviceRequest,
            final DeviceResponseHandler deviceResponseHandler) {
        this.createSuccessfulDefaultResponse(deviceRequest, deviceResponseHandler, DeviceMessageStatus.OK);
    }

    private void createSuccessfulDefaultResponse(final DeviceRequest deviceRequest,
            final DeviceResponseHandler deviceResponseHandler, final DeviceMessageStatus deviceMessageStatus) {
        final EmptyDeviceResponse deviceResponse = this.createDefaultResponse(deviceRequest, deviceMessageStatus);
        deviceResponseHandler.handleResponse(deviceResponse);
    }

    private void handleConnectionFailureException(final DeviceRequest deviceRequest,
            final DeviceResponseHandler deviceResponseHandler,
            final ConnectionFailureException connectionFailureException) {
        LOGGER.error("Could not connect to device after all retries", connectionFailureException);
        final EmptyDeviceResponse deviceResponse = this.createDefaultResponse(deviceRequest,
                DeviceMessageStatus.FAILURE);
        deviceResponseHandler.handleException(connectionFailureException, deviceResponse, true);
    }

    private void handleProtocolAdapterException(final SetScheduleDeviceRequest deviceRequest,
            final DeviceResponseHandler deviceResponseHandler, final ProtocolAdapterException protocolAdapterException) {
        LOGGER.error(
                "Could complete the request: " + deviceRequest.getMessageType() + " for device: "
                        + deviceRequest.getDeviceIdentification(), protocolAdapterException);
        final EmptyDeviceResponse deviceResponse = this.createDefaultResponse(deviceRequest,
                DeviceMessageStatus.FAILURE);
        deviceResponseHandler.handleException(protocolAdapterException, deviceResponse, true);
    }

    private void handleException(final DeviceRequest deviceRequest, final DeviceResponseHandler deviceResponseHandler,
            final Exception exception) {
        LOGGER.error("Unexpected exception", exception);
        final EmptyDeviceResponse deviceResponse = this.createDefaultResponse(deviceRequest,
                DeviceMessageStatus.FAILURE);
        deviceResponseHandler.handleException(exception, deviceResponse, false);
    }

    // ========================
    // This method is duplicated in one of the command implementations. This
    // needs to be refactored. =
    // ========================

    /*
     * Checks to see if the relay has the correct type, throws an exception when
     * that't not the case
     */
    private void checkRelay(final RelayType actual, final RelayType expected, final Integer internalAddress)
            throws FunctionalException {
        if (!actual.equals(expected)) {
            if (RelayType.LIGHT.equals(expected)) {
                LOGGER.error("Relay with internal address: {} is not configured as light relay", internalAddress);
                throw new FunctionalException(FunctionalExceptionType.ACTION_NOT_ALLOWED_FOR_LIGHT_RELAY,
                        ComponentType.PROTOCOL_IEC61850);
            } else {
                LOGGER.error("Relay with internal address: {} is not configured as tariff relay", internalAddress);
                throw new FunctionalException(FunctionalExceptionType.ACTION_NOT_ALLOWED_FOR_TARIFF_RELAY,
                        ComponentType.PROTOCOL_IEC61850);
            }
        }
    }
}