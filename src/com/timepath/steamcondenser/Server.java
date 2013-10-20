package com.timepath.steamcondenser;

import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
import com.github.koraktor.steamcondenser.steam.SteamPlayer;
import com.github.koraktor.steamcondenser.steam.servers.SourceServer;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author TimePath
 */
public class Server {

    private static final Logger LOG = Logger.getLogger(Server.class.getName());
    private final InetSocketAddress sock;
    String name;

        public Server(InetSocketAddress a) {
            this.sock = a;
            name = (a.getAddress().getHostAddress() + ":" + a.getPort());
        }

    public void update() throws SteamCondenserException, TimeoutException {
        SourceServer ss = new SourceServer(sock.getAddress(), sock.getPort());

        Map<String, Object> info = ss.getServerInfo();
        Map<String, String> rules = ss.getRules();
        Map<String, SteamPlayer> players = ss.getPlayers();

        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("ping", ss.getPing());
        maps.put("info", info);
        maps.put("rules", ss.getRules());
        maps.put("players", ss.getPlayers());

        StringBuilder sb = new StringBuilder().append("\n");
        String ln = "------------------------------------------------------------------------\n";
        sb.append(ln);
        sb.append(this.toString().toUpperCase()).append("\n");
        sb.append(ln);

        for (Map.Entry<String, Object> entry : maps.entrySet()) {
            sb.append(ln);
            String k = entry.getKey();
            Object v = entry.getValue();
            if (v instanceof Map) {
                Map<?, ?> m = ((Map<?, ?>) v);
                sb.append(entry.getKey().toUpperCase()).append("\n");
                sb.append(ln);
                for (Map.Entry<?, ?> subEntry : m.entrySet()) {
                    sb.append(subEntry.getKey()).append(" = ").append(subEntry.getValue()).append("\n");
                }
                sb.append(ln);
            } else {
                sb.append(k).append(" = ").append(v).append("\n");
            }
            sb.append("\n");
        }
        LOG.log(Level.FINE, sb.toString());
        this.name = (String) info.get("serverName");
    }

    @Override
    public String toString() {
        return name;
    }
}
