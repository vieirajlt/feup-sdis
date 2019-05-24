package tests;

import connection.client.Client;
import connection.server.Server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ConnectionTest {
    public static void main(String[] args) {

        System.setProperty("java.security.policy","file:FILES/policy");
        System.setProperty("javax.net.ssl.trustStore","FILES/samplecacerts");
        System.setProperty("javax.net.ssl.trustStorePassword","changeit");
        System.setProperty("javax.net.ssl.keyStore", "FILES/testkeys");
        System.setProperty("javax.net.ssl.keyStorePassword", "passphrase");
        System.setProperty("javax.net.ssl.keyStoreType", "JKS");

        ScheduledThreadPoolExecutor executor;
        executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(10);
        System.out.println("test started");
        try (Server server = Server.create()) {
            System.out.println("server created");
            executor.schedule(server, 0, TimeUnit.MILLISECONDS);
            System.out.println("server started");
            Thread.sleep(1000);
            Client client = new Client(server.getLocalPort());
            System.out.println("client created");
            executor.schedule(client, 0, TimeUnit.MILLISECONDS);
            System.out.println("client started");
            client.write(client.message.getBytes());
            System.out.println("client wrote");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}