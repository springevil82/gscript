package gscript.factory.proxy;

import gscript.Factory;

public final class GroovyProxyFactory {

    private final Factory factory;
    private Proxy proxy;

    public GroovyProxyFactory(Factory factory) {
        this.factory = factory;
    }

    /**
     * Инициализировать прокси, для работы internet сервисов скрипта
     *
     * @param server server
     * @param port   port
     */
    public void initProxy(String server, int port) {
        this.proxy = new Proxy(server, port, null, null);
    }

    /**
     * Инициализировать прокси, для работы internet сервисов скрипта
     *
     * @param server   server
     * @param port     port
     * @param username username
     * @param password password
     */
    public void initProxy(String server, int port, String username, String password) {
        this.proxy = new Proxy(server, port, username, password);
    }

    public Proxy getProxy() {
        return proxy;
    }

    public final class Proxy {

        private final String server;
        private final int port;
        private final String username;
        private final String password;

        public Proxy(String server, int port, String username, String password) {
            this.server = server;
            this.port = port;
            this.username = username;
            this.password = password;
        }

        public String getServer() {
            return server;
        }

        public int getPort() {
            return port;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

}
