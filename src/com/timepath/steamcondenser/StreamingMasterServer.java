package com.timepath.steamcondenser;

import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author TimePath
 */
public class StreamingMasterServer extends MasterServerWrapper {

    private static final Logger LOG = Logger.getLogger(StreamingMasterServer.class.getName());
    final int quanta = 50;
    final int frequency = 5;
    final int threads = quanta * frequency;
    final int period = 1000 / frequency;
    final int limit = quanta / frequency;
    final ExecutorService service = Executors.newFixedThreadPool(threads);

    public StreamingMasterServer(InetAddress address) throws SteamCondenserException {
        super(address);
    }

    public StreamingMasterServer(String address) throws SteamCondenserException {
        super(address);
    }

    public StreamingMasterServer(InetAddress address, Integer port) throws SteamCondenserException {
        super(address, port);
    }

    public StreamingMasterServer(String address, Integer port) throws SteamCondenserException {
        super(address, port);
    }

    public Vector<InetSocketAddress> getServers(byte regionCode, String filter, FriendlyServerListener listener) throws SteamCondenserException, TimeoutException {
        return this.getServers(regionCode, filter, false, listener);
    }

    public Vector<InetSocketAddress> getServers(byte regionCode, String filter, boolean force, final FriendlyServerListener listener) throws SteamCondenserException, TimeoutException {
        MasterServerListener sl = new MasterServerListener() {
            RateLimitedBuffer<Server> rb = new RateLimitedBuffer<Server>(period, limit) {
                @Override
                void publish(final ArrayList<Server> buffer) {
                    for (final Server s : buffer) {
                        service.submit(new Runnable() {
                            public void run() {
                                ArrayList<Server> al = new ArrayList<Server>();
                                al.add(s);
                                try {
                                    s.update();
                                    listener.resolved(al);
                                } catch (SteamCondenserException ex) {
                                    LOG.log(Level.WARNING, "Steam condenser exception", ex);
                                } catch (TimeoutException ex) {
                                    listener.timeout(al);
                                }
                            }
                        });
                    }
                    listener.found(buffer);
                }
            };

            public void serverFound(InetSocketAddress addr) {
                rb.put(new Server(addr));
            }
        };
        return super.getServers(regionCode, filter, force, sl);
    }

    public interface FriendlyServerListener {

        public void found(ArrayList<Server> buffer);

        public void timeout(ArrayList<Server> buffer);

        public void resolved(ArrayList<Server> buffer);
    }
}
