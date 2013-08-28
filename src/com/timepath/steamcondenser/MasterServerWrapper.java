package com.timepath.steamcondenser;

import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
import com.github.koraktor.steamcondenser.steam.packets.A2M_GET_SERVERS_BATCH2_Paket;
import com.github.koraktor.steamcondenser.steam.packets.M2A_SERVER_BATCH_Paket;
import com.github.koraktor.steamcondenser.steam.servers.MasterServer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Vector;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extends MasterServer to allow for event driven applications
 *
 * @author timepath
 */
public class MasterServerWrapper extends MasterServer {

    public MasterServerWrapper(InetAddress address) throws SteamCondenserException {
        super(address);
    }

    public MasterServerWrapper(String address) throws SteamCondenserException {
        super(address);
    }

    public MasterServerWrapper(InetAddress address, Integer port) throws SteamCondenserException {
        super(address, port);
    }

    public MasterServerWrapper(String address, Integer port) throws SteamCondenserException {
        super(address, port);
    }

    public Vector<InetSocketAddress> getServers(byte regionCode, String filter, MasterServerListener listener) throws SteamCondenserException, TimeoutException {
        return this.getServers(regionCode, filter, false, listener);
    }

    /**
     * XXX: The only copy-paste code in this wrapper
     *
     * @param regionCode
     * @param filter
     * @param force
     * @param listeners
     * @return
     * @throws SteamCondenserException
     * @throws TimeoutException
     */
    public Vector<InetSocketAddress> getServers(byte regionCode, String filter, boolean force, MasterServerListener listener) throws SteamCondenserException, TimeoutException {
        int failCount = 0;
        boolean finished = false;
        int portNumber = 0;
        String hostName = "0.0.0.0";
        Vector<String> serverStringArray;
        Vector<InetSocketAddress> serverArray = new Vector<InetSocketAddress>();

        while (true) {
            try {
                failCount = 0;
                do {
                    this.socket.send(new A2M_GET_SERVERS_BATCH2_Paket(regionCode, hostName + ":" + portNumber, filter));
                    try {
                        serverStringArray = ((M2A_SERVER_BATCH_Paket) this.socket.getReply()).getServers();

                        for (String serverString : serverStringArray) {
                            hostName = serverString.substring(0, serverString.lastIndexOf(":"));
                            portNumber = Integer.valueOf(serverString.substring(serverString.lastIndexOf(":") + 1));

                            if (!hostName.equals("0.0.0.0") && portNumber != 0) {
                                InetSocketAddress inets = new InetSocketAddress(hostName, portNumber);
                                serverArray.add(inets);
                                if (listener != null) {
                                    listener.serverFound(inets);
                                }
                            } else {
                                finished = true;
                            }
                        }
                        failCount = 0;
                    } catch (TimeoutException e) {
                        failCount++;
                        if (failCount == retries) {
                            throw e;
                        }
                        Logger.getLogger("com.github.koraktor.steamcondenser").log(Level.INFO, "Request to master server {0} timed out, retrying...", this.ipAddress);
                    }
                } while (!finished);
                break;
            } catch (TimeoutException e) {
                if (force) {
                    break;
                } else if (this.rotateIp()) {
                    throw e;
                }
                Logger.getLogger("com.github.koraktor.steamcondenser").log(Level.INFO, "Request to master server failed, retrying {0}...", this.ipAddress);
            }
        }

        return serverArray;
    }

    public interface MasterServerListener {

        public void serverFound(InetSocketAddress addr);
    }
}
