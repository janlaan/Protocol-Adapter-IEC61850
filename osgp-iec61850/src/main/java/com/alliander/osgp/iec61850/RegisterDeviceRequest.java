/**
 * Copyright 2014-2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.iec61850;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import org.apache.cxf.common.util.StringUtils;

public class RegisterDeviceRequest implements Serializable {

    private static final long serialVersionUID = 5163567467800583964L;

    /**
     * The elements inside the IEC61850 Register Device Request expressed in
     * bytes separated by a comma (0x2C).
     */
    private static final byte SEPARATOR = ',';

    private final String serialNumber;
    private final String ipAddress;

    public RegisterDeviceRequest(final byte[] bytes) {
        final int splitIndex = this.getSeparatorPos(bytes);
        this.serialNumber = new String(bytes, 0, splitIndex, StandardCharsets.US_ASCII);
        this.ipAddress = new String(bytes, splitIndex + 1, bytes.length - splitIndex - 1, StandardCharsets.US_ASCII);
    }

    public RegisterDeviceRequest(final String serialNumber, final String ipAddress) {
        this.serialNumber = serialNumber;
        this.ipAddress = ipAddress;
    }

    private int getSeparatorPos(final byte[] bytes) {
        int index = -1;
        for (int i = 0; i < bytes.length; i++) {
            if (SEPARATOR == bytes[i]) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new IllegalArgumentException("Bytes should contain separator '" + (char) SEPARATOR + "'");
        }
        if (index == 0 || index == bytes.length - 1) {
            throw new IllegalArgumentException("Bytes should contain data before and after separator '"
                    + (char) SEPARATOR + "'");
        }
        return index;
    }

    public boolean isValid() {
        return !StringUtils.isEmpty(this.serialNumber) && !StringUtils.isEmpty(this.ipAddress);
    }

    @Override
    public String toString() {
        return String.format("RegisterDeviceRequest[serialNumber=%s, ipAddress=%s]", this.serialNumber, this.ipAddress);
    }

    public byte[] toByteArray() {
        return new StringBuilder().append(this.serialNumber).append((char) SEPARATOR).append(this.ipAddress).toString()
                .getBytes(StandardCharsets.US_ASCII);
    }

    public int getSize() {
        return this.serialNumber.length() + 1 + this.ipAddress.length();
    }

    public String getSerialNumber() {
        return this.serialNumber;
    }

    public String getDeviceIdentification() {
        /*
         * IEC61850 devices for now are only Kaifa devices. The device
         * identification is the serial number as indicated by Kaifa, prefixed
         * by "KAI-".
         */
        return "KAI-" + this.serialNumber;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }
}
