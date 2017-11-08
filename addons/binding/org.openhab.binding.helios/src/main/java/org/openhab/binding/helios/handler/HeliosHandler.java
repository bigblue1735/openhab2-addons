/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.helios.handler;

import static org.openhab.binding.helios.HeliosBindingConstants.DATE;

import java.math.BigDecimal;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.helios.HeliosBindingConstants;
import org.openhab.binding.helios.internal.HeliosCommunicator;
import org.openhab.binding.helios.internal.HeliosConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeliosHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Susi Loma - Initial contribution
 */
public class HeliosHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HeliosHandler.class);

    private HeliosCommunicator heliosCom;

    public HeliosHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (getThing().getStatus() != ThingStatus.ONLINE || heliosCom == null || !heliosCom.isOnline()) {
            logger.error("Not able to get info, connection invalid or plugin offline");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Connect to helios failed for some reason");
            return;
        }

        if (channelUID.getId().equals(DATE)) {
            try {
                heliosCom.getValue(HeliosConstants.DATE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (channelUID.getId().equals(DATE)) {

        } else if (channelUID.getId().equals(DATE)) {

        } else if (channelUID.getId().equals(DATE)) {

        } else if (channelUID.getId().equals(DATE)) {

        } else {
            logger.error(String.format("not able to map channel %s", channelUID));
        }

    }

    @Override
    public void initialize() {
        Configuration config = getThing().getConfiguration();
        String host = (String) config.get(HeliosBindingConstants.PROPERTY_HOSTNAME);
        host = "192.168.99.37";

        int port = ((BigDecimal) config.get(HeliosBindingConstants.PROPERTY_PORT)).intValue();
        int unit = ((BigDecimal) config.get(HeliosBindingConstants.PROPERTY_UNIT)).intValue();
        int startAddress = ((BigDecimal) config.get(HeliosBindingConstants.PROPERTY_START_ADRESS)).intValue();

        heliosCom = new HeliosCommunicator(host, port, unit, startAddress);

        if (heliosCom == null || !heliosCom.isOnline() || heliosCom.getErrorMessage() != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    String.format("Error while connect to helios: %s", heliosCom.getErrorMessage()));
            return;
        }
        updateStatus(ThingStatus.ONLINE);
    }
}
