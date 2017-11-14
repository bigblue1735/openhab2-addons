/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.helios.internal;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.msg.WriteMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;

/**
 * This class is responsible for communicating with the Helios modbus.
 *
 * @author Bernhard Bauer
 * @since 1.8.0
 */
public class HeliosCommunicator {

    private String host;
    private int port;
    private int unit;
    private int startAddress;
    private HeliosVariableMap vMap;
    private String errorMessage;
    private TCPMasterConnection conn;

    /**
     * Constructor to set the member variables
     *
     * @param host IP Address
     * @param port Port (502)
     * @param address Modbus address (180)
     * @param startAddress Start address (1)
     */
    public HeliosCommunicator(String host, int port, int unit, int startAddress) {
        this.vMap = new HeliosVariableMap();
        this.setHost(host);
        this.setPort(port);
        this.unit = unit;
        this.startAddress = startAddress;
        try {
            this.conn = new TCPMasterConnection(InetAddress.getByName(host));
            this.conn.setPort(port);
            this.conn.connect();
        } catch (Exception e) {
            errorMessage = new String("Error while create helios connection:" + e.toString());
        }
    }

    /**
     * Sets a variable in the Helios device
     *
     * @param variableName The variable name
     * @param value The new value
     * @return The value if the transaction succeeded, <tt>null</tt> otherwise
     * @throws HeliosException
     */
    public String setValue(String variableName, String value) throws Exception {

        HeliosVariable v = this.vMap.getVariable(variableName);

        // check range if applicable
        if ((v.getAccess() == HeliosVariable.ACCESS_W) || (v.getAccess() == HeliosVariable.ACCESS_RW)) { // changing
                                                                                                         // value is
                                                                                                         // allowed

            boolean inAllowedRange = false;

            if (v.getMinVal() != null) { // min and max value are set
                if (v.getMinVal() instanceof Integer) {
                    inAllowedRange = (((Integer) v.getMinVal()).intValue() <= Integer.parseInt(value));
                    if (v.getMaxVal() instanceof Integer) {
                        inAllowedRange = inAllowedRange
                                && (((Integer) v.getMaxVal()).intValue() >= Integer.parseInt(value));
                    } else { // Long
                        inAllowedRange = inAllowedRange
                                && (((Long) v.getMaxVal()).longValue() >= Long.parseLong(value));
                    }
                } else if (v.getMinVal() instanceof Double) {
                    inAllowedRange = (((Double) v.getMinVal()).doubleValue() <= Double.parseDouble(value))
                            && (((Double) v.getMaxVal()).doubleValue() >= Double.parseDouble(value));
                }
            } else {
                inAllowedRange = true; // no range to check
            }

            if (inAllowedRange) {
                String payload = v.getVariableString() + "=" + value;

                // create request
                WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(this.startAddress,
                        this.preparePayload(payload));
                request.setUnitID(this.unit);

                // communicate with modbus
                try {
                    ModbusTCPTransaction trans = new ModbusTCPTransaction(this.conn);

                    // send request
                    trans.setRequest(request);
                    trans.setReconnecting(true);
                    trans.execute();

                    return value;

                } catch (Exception e) {
                    throw new RuntimeException("Communication with Helios device failed");
                }
            } else {
                throw new RuntimeException("Value is outside of allowed range");
            }
        } else {
            throw new RuntimeException("Variable is read-only");
        }
    }

    /**
     * Read a variable from the Helios device
     *
     * @param variableName The variable name
     * @return The value
     * @throws HeliosException
     */
    public synchronized String getValue(String variableName) throws Exception {
        if (!isOnline()) {
            this.conn.connect();
        }

        HeliosVariable v = this.vMap.getVariable(variableName);
        if (v == null) {
            System.err.println(String.format("Not able to find variable with name: %s", variableName));
            return null;
        }
        String payload = v.getVariableString();
        // create request 1
        WriteMultipleRegistersRequest request1 = new WriteMultipleRegistersRequest(this.startAddress,
                this.preparePayload(payload));
        request1.setUnitID(this.unit);

        // create request 2
        ReadMultipleRegistersRequest request2 = new ReadMultipleRegistersRequest(this.startAddress, v.getCount());
        request2.setUnitID(this.unit);

        // communicate with modbus
        ModbusTCPTransaction trans = new ModbusTCPTransaction(this.conn);

        // send request 1
        trans.setRequest(request1);
        trans.setReconnecting(true);
        trans.execute();

        // receive response
        // WriteMultipleRegistersResponse response1 = (WriteMultipleRegistersResponse) trans.getResponse();

        // send request 2
        trans.setRequest(request2);
        trans.setReconnecting(true);
        trans.execute();
        //
        // // receive response
        ReadMultipleRegistersResponse response2 = (ReadMultipleRegistersResponse) trans.getResponse();
        return decodeResponse(response2.getRegisters());
    }

    /**
     * Prepares the payload for the request
     *
     * @param payload The String representation of the payload
     * @return The Register representation of the payload
     */
    private Register[] preparePayload(String payload) {

        List<Register> returnList = new ArrayList<Register>();
        byte[] temp = payload.getBytes();
        for (int i = 0; i < temp.length - 1; i++) {
            byte b1 = temp[i];
            i++;
            byte b2 = temp[i];
            Register simpleReg = new SimpleRegister(b1, b2);
            returnList.add(simpleReg);
        }
        SimpleRegister emptyREg = new SimpleRegister(0);
        returnList.add(emptyREg);

        Register[] registerArryay = new Register[returnList.size()];
        for (int i = 0; i < returnList.size(); i++) {
            registerArryay[i] = returnList.get(i);
        }

        return registerArryay;

    }

    /**
     * Decodes the Helios device' response and returns the actual value of the variable
     *
     * @param response The registers received from the Helios device
     * @return The value or <tt>null</tt> if an error occurred
     */
    private String decodeResponse(Register[] response) {
        byte[] b = new byte[response.length * 2];
        int actSize = 0; // track the actual size of the useable array (excluding any 0x00 characters)
        for (int i = 0; i < response.length; i++) {
            byte[] reg = response[i].toBytes();
            if (reg.length == 2) { // only add to the array if it's a useable character
                if (reg[0] != 0x00) {
                    b[actSize++] = reg[0];
                }
                if (reg[1] != 0x00) {
                    b[actSize++] = reg[1];
                }
            }
        }
        b = Arrays.copyOf(b, actSize); // before creating a string of it the array needs to be truncated
        String r = new String(b);
        String[] parts = r.split("="); // remove the part "vXXXX=" from the string
        if (parts.length == 2) {
            return parts[1];
        } else {
            return null;
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isOnline() {
        return this.conn.isConnected();
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
