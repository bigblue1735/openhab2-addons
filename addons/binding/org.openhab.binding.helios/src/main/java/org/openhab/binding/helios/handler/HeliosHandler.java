/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.helios.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.helios.HeliosBindingConstants;
import org.openhab.binding.helios.internal.HeliosCommunicator;
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
    BigDecimal refreshRate;

    ScheduledFuture<?> refreshJob;

    public HeliosHandler(Thing thing) {
        super(thing);
    }

    List<Command> commandList;

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (getThing().getStatus() != ThingStatus.ONLINE || heliosCom == null) {
            logger.error("Not able to get info, connection invalid or plugin offline");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Connect to helios failed for some reason");
            return;
        }

        try {
            refreshJob = scheduler.scheduleAtFixedRate(() -> {
                try {
                    updateState(channelUID, new StringType(heliosCom.getValue(channelUID.getId())));
                } catch (Exception e) {
                    logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                }
            }, 0, refreshRate.intValue(), TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("not able to update Element");
        }
    }

    @Override
    public void initialize() {
        Configuration config = getThing().getConfiguration();
        String host = (String) config.get(HeliosBindingConstants.PROPERTY_HOSTNAME);

        int port = ((BigDecimal) config.get(HeliosBindingConstants.PROPERTY_PORT)).intValue();
        int unit = ((BigDecimal) config.get(HeliosBindingConstants.PROPERTY_UNIT)).intValue();
        int startAddress = ((BigDecimal) config.get(HeliosBindingConstants.PROPERTY_START_ADRESS)).intValue();
        refreshRate = (BigDecimal) config.get(HeliosBindingConstants.PROPERTY_REFRESH_INTERVALL);

        if (refreshRate == null) {
            refreshRate = new BigDecimal(60);
        }

        heliosCom = new HeliosCommunicator(host, port, unit, startAddress);

        if (heliosCom == null || !heliosCom.isOnline() || heliosCom.getErrorMessage() != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    String.format("Error while connect to helios: %s", heliosCom.getErrorMessage()));
            return;
        }
        commandList = new ArrayList<>();
        updateStatus(ThingStatus.ONLINE);

    }

    @Override
    public void dispose() {
        super.dispose();
        refreshJob.cancel(true);
    }
}
