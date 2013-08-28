package com.timepath.steamcondenser;

import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
import com.github.koraktor.steamcondenser.steam.servers.MasterServer;
import com.timepath.steamcondenser.StreamingMasterServer.FriendlyServerListener;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

public class StreamingTest extends JFrame {

    private static final Logger LOG = Logger.getLogger(StreamingTest.class.getName());

    static {
        Logger l = Logger.getLogger("");
        for (Handler h : l.getHandlers()) {
            l.removeHandler(h);
        }
        Logger log = Logger.getLogger("com.timepath");
        log.addHandler(new java.util.logging.ConsoleHandler());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Unable to set look and feel", ex);
                }

                StreamingTest a = new StreamingTest();
                a.pack();
                a.setLocationRelativeTo(null);
                a.setVisible(true);
            }
        });
    }
    private final DefaultTableModel m;
    private final JTable table;

    public StreamingTest() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        table = new JTable();
        m = (DefaultTableModel) table.getModel();
        m.addColumn("Servers will populate here");

        this.add(new JScrollPane(table));
        new Thread(new Updater()).start();
    }

    private class Updater implements Runnable {

        public void run() {
            try {
                FriendlyServerListener sl = new FriendlyServerListener() {
                    private void add(final ArrayList<Server> buffer) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                for (final Server s : buffer) {
                                    m.addRow(new Object[]{s});
                                    m.fireTableRowsUpdated(0, m.getRowCount() - 1);
                                    table.getTableHeader().getColumnModel().getColumn(0).setHeaderValue("Servers (" + m.getRowCount() + ")");
                                    table.getTableHeader().repaint();
                                }
                            }
                        });
                    }
                    /**
                     * Set to true to only show servers whose info is resolved
                     */
                    boolean requireResolution = true;

                    public void found(final ArrayList<Server> buffer) {
                        if (!requireResolution) {
                            add(buffer);
                        }
                    }

                    public void resolved(ArrayList<Server> buffer) {
                        if (requireResolution) {
                            add(buffer);
                        }
                    }

                    public void timeout(ArrayList<Server> buffer) {
                        for (Server s : buffer) {
                            LOG.log(Level.WARNING, "Connection to {0} timed out", s);
                        }
                    }
                };
                long start = System.currentTimeMillis();
                StreamingMasterServer ms = new StreamingMasterServer(MasterServer.SOURCE_MASTER_SERVER);
                final int serverCount = ms.getServers(MasterServer.REGION_ALL, "\\gamedir\\tf", sl).size();
                LOG.log(Level.INFO, "{0} total servers. Took {1}ms", new Object[]{serverCount, System.currentTimeMillis() - start});
            } catch (SteamCondenserException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } catch (TimeoutException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }
}
