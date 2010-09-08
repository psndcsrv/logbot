package org.jibble.logbot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogBotMain {

    private static Properties p;
    private static String server;
    private static String channel;
    private static String nick;
    private static String joinMessage;
    private static File outDir;
    private static String controlPassword;

    public static void main(String[] args) throws Exception {
        p = new Properties();
        p.load(new FileInputStream(new File("./config.ini")));

        server = p.getProperty("Server", "localhost");
        channel = p.getProperty("Channel", "#test");
        nick = p.getProperty("Nick", "LogBot");
        joinMessage = p.getProperty("JoinMessage", "This channel is logged.");
        outDir = new File(p.getProperty("OutputDir", "./output/"));
        controlPassword = p.getProperty("ControlPassword", UUID.randomUUID().toString());

        writePidFile();

        LogBot bot = new LogBot(nick, outDir, joinMessage, controlPassword);
        bot.connect(server);
        bot.join(channel);
    }

    /**
     * Write a pid file, if possible. Errors are non-fatal.
     * 
     * @throws Exception
     */
    private static void writePidFile() {
        try {
            String pid = getProcessId();
            if (pid == null) {
                throw new Exception("Cannot find current process id!");
            }

            File pidFile = new File(p.getProperty("PidFile", "./logbot.pid"));
            File parent = pidFile.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                throw new Exception("Couldn't make parent folder (" + parent + ")");
            }
            pidFile.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(pidFile));
            writer.write(pid);
            writer.flush();
            writer.close();
            
            pidFile.deleteOnExit();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    /**
     * A method of getting the current jvm's process id. Code from:
     * http://golesny.de/wiki/code:javahowtogetpid
     * 
     * tested on:
     *   - windows xp sp 2, java 1.5.0_13
     *   - mac os x 10.4.10, java 1.5.0
     *   - debian linux, java 1.5.0_13
     * all return pid@host, e.g 2204@antonius
     * 
     * @return a String representation of the system process id
     */
    private static String getProcessId() {
        RuntimeMXBean rtb = ManagementFactory.getRuntimeMXBean();
        String processName = rtb.getName();
        String result = null;

        Pattern pattern = Pattern.compile("^([0-9]+)@.+$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(processName);
        if (matcher.matches()) {
            result = matcher.group(1);
        }
        return result;
    }

}
