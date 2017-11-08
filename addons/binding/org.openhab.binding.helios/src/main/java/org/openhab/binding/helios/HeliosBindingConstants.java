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

    // Custom Properties
    public final static String PROPERTY_HOSTNAME = "hostname";
    public final static String PROPERTY_REFRESH_INTERVALL = "refreshInterval";
    public final static String PROPERTY_PORT = "port";
    public final static String PROPERTY_UNIT = "unit";
    public final static String PROPERTY_START_ADRESS = "startAdress";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "sample");

    // List of all Channel ids
    public static final String CHANNEL_1 = "channel1";
    public static final String ARTIKEL_DESC = "articleDesc";
    public static final String REF_NO = "refNo";
    public static final String MAC_ADRESS = "macAdress";
    public static final String LANGUAGE = "language";
    public static final String DATE = "date";
    public static final String DATE_TIME = "dateTime";
    public static final String SUMMER_WINTER = "summerWinter";
}
