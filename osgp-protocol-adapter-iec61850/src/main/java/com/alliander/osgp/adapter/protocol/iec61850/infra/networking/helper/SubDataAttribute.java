/**
 * Copyright 2014-2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.protocol.iec61850.infra.networking.helper;

import com.alliander.osgp.adapter.protocol.iec61850.domain.valueobjects.ScheduleWeekday;
import com.alliander.osgp.adapter.protocol.iec61850.domain.valueobjects.TriggerType;
import com.alliander.osgp.domain.core.valueobjects.LightType;

/**
 * Contains a list of all sub data attributes of the IEC61850 Device.
 */
public enum SubDataAttribute {
    /**
     * Attribute of Property SWCf, used to read the value of the offset from
     * astronomic sunrise.
     */
    ASTRONOMIC_SUNRISE_OFFSET("adRiseOft"),
    /**
     * Attribute of Property SWCf, used to read the value of the offset from
     * astronomic sunset.
     */
    ASTRONOMIC_SUNSET_OFFSET("adSetOft"),
    /**
     * Attribute of Property Clock, used to enable daylights savings.
     */
    AUTOMATIC_SUMMER_TIMING_ENABLED("enbDst"),
    /**
     * Attribute of several Properties (like Oper for example), used to control
     * functions of the device.
     */
    CONTROL_VALUE("ctlVal"),
    /**
     * Attribute of both firmware configuration nodes, used to read the value of
     * the current firmware version
     */
    CURRENT_VERSION("curVer"),
    /**
     * Attribute of Property Sensor, used to transition the device between day
     * and night transition.
     */
    DAY("day"),
    /**
     * Attribute of Property Reg, used to enable or disable the device
     * registration.
     */
    DEVICE_REGISTRATION_ENABLED("ntfEnb"),
    /**
     * Attribute of Property SWCf, used to enable DHCP.
     */
    ENABLE_DHCP("enbDHCP"),
    /**
     * Attribute of Property CfSt, enbOper, enables the operation of a relay.
     */
    ENABLE_OPERATION("enbOper"),
    /**
     * Property of LLN0's Node, evn_rpn01. Enables reporting. This boolean is
     * reset to false by the device once the reports are sent.
     */
    ENABLE_REPORTING("RptEna"),
    /**
     * Attribute of the CSLC Event Buffer configuration, filter for enabled
     * event types.
     */
    EVENT_BUFFER_FILTER("enbEvnType"),
    /**
     * Attribute of Property SWCf, used to read the value of fixed ip address'
     * gateway.
     */
    GATEWAY("gateway"),
    /**
     * Attribute of Property OnItvB, itv (itv followed by an index from 1 ..
     * 60).
     */
    INTERVAL("itv"),
    /**
     * Attribute of Property SWCf, used to read the value of fixed IP address.
     */
    IP_ADDRESS("ipAddr"),
    /**
     * Attribute of Property OnItvB, lastIdx.
     */
    LAST_INDEX("lastIdx"),
    /**
     * Attribute of Property SWCf, used to read the value of the
     * {@link LightType}
     */
    LIGHT_TYPE("LT"),
    /**
     * Minimum time the lights have to remain on. Used by the device to decide
     * whether or not schedule entries should be ignored. Property of sche node,
     * which is a node of the schedule node of the XSWC Node.
     */
    MINIMUM_TIME_ON("minOnPer"),
    /**
     * Attribute of Property SWCf, used to read the value of fixed IP address'
     * net-mask.
     */
    NETMASK("netmask"),
    /**
     * Attribute of several Logical Nodes, for example Logical Node RbOper.Oper,
     * used to reboot the device.
     */
    OPERATION("Oper"),
    /**
     * Attribute of Property Clock, contains the time zone value
     */
    TIME_ZONE("tZ"),
    /**
     * Schedule day of the week. see {@link ScheduleWeekday}. Property of sche
     * node, which is a node of the schedule node of the XSWC Node.
     */
    SCHEDULE_DAY("day"),
    /**
     * Schedule entry of a schedule.
     */
    SCHEDULE_ENTRY("sche"),
    /**
     * Schedule day of the week. see {@link ScheduleWeekday}. Property of sche
     * node, which is a node of the schedule node of the XSWC Node.
     */
    SCHEDULE_ENABLE("enable"),
    /**
     * Time off Property of sche node, which is a node of the schedule node of
     * the XSWC Node.
     */
    SCHEDULE_TIME_OFF("tOff"),
    /**
     * Time off type. see {@link TriggerType}. Property of sche node, which is a
     * node of the schedule node of the XSWC Node.
     */
    SCHEDULE_TIME_OFF_TYPE("tOffT"),
    /**
     * Time on. Property of sche node, which is a node of the schedule node of
     * the XSWC Node.
     */
    SCHEDULE_TIME_ON("tOn"),
    /**
     * Time on type. see {@link TriggerType}. Property of sche node, which is a
     * node of the schedule node of the XSWC Node.
     */
    SCHEDULE_TIME_ON_TYPE("tOnT"),
    /**
     * Minutes after of the trigger window. Property of sche node, which is a
     * node of the schedule node of the XSWC Node.
     */
    SCHEDULE_TRIGGER_MINUTES_AFTER("srAftWd"),
    /**
     * Minutes before of the trigger window. Property of sche node, which is a
     * node of the schedule node of the XSWC Node.
     */
    SCHEDULE_TRIGGER_MINUTES_BEFORE("srBefWd"),
    /**
     * Attribute of Property Reg, used to read or set the IP address of the
     * platform.
     */
    SERVER_ADDRESS("svrAddr"),
    /**
     * Attribute of Property Reg, used to read or set the port number of the
     * platform.
     */
    SERVER_PORT("svrPort"),
    /**
     * Property of LLN0's Node, evn_rpn01. Next SqNum for a report not yet
     * reported. This value is updated by the device when newer reports have
     * been sent.
     */
    SEQUENCE_NUMBER("SqNum"),
    /**
     * Attribute of both firmware configuration nodes and the certificate
     * authority replacement node, used to read the value of the startTime of
     * the download.
     */
    START_TIME("startT"),
    /**
     * Attribute of Property SwType, used to read the type (tariff=0/light=1) of
     * the switch.
     */
    STATE("stVal"),
    /**
     * Begin of summer time.
     */
    SUMMER_TIME_DETAILS("dstBegT"),
    /**
     * Attribute of Property Clock, used to read the value of time sync period.
     */
    TIME_SYNC_FREQUENCY("syncPer"),
    /**
     * Attribute of both firmware configuration nodes and the certificate
     * authority replacement node, this URL will be the location of the file to
     * download.
     */
    URL("url"),
    /**
     * End of summer time.
     */
    WINTER_TIME_DETAILS("dstEndT"),
    /**
     * Magnitude.
     */
    MAGITUDE("mag"),
    /**
     * Instantaneous magnitude.
     */
    MAGITUDE_INSTANTANEOUS("instMag"),
    /**
     * Setpoint for magnitude.
     */
    MAGITUDE_SETPOINT("setMag"),
    /**
     * Quality indicator
     */
    QUALITY("q"),
    /**
     * Timestamp
     */
    TIME("t"),
    /**
     * Float
     */
    FLOAT("f");

    private String description;

    private SubDataAttribute(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
