/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.helios;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link HeliosBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Susi Loma - Initial contribution
 */
public class HeliosBindingConstants {

    private static final String BINDING_ID = "helios";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "helios");

    // Custom Properties
    public static final String PROPERTY_HOSTNAME = "hostname";
    public static final String PROPERTY_REFRESH_INTERVALL = "refreshInterval";
    public static final String PROPERTY_PORT = "port";
    public static final String PROPERTY_UNIT = "unit";
    public static final String PROPERTY_START_ADRESS = "startAdress";
    public static final String PROPERTY_RETRY_COUNT = "retryCount";
}
