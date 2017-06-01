package gscript.factory.database.cache;

import com.intersys.jdbc.CacheConnection;
import gscript.Factory;
import gscript.GroovyException;
import gscript.factory.database.GroovySQLConnection;

import java.sql.*;

public final class GroovyCacheConnection extends GroovySQLConnection {

    public static final String STORED_PROC_VERSION = "1";
    private static final int MAX_COMMAND_LENGTH = 1024;

    private final Factory factory;
    private final String host;
    private final int port;
    private final String namespace;
    private final String username;
    private final String password;
    private final Connection connection;

    /**
     * Create connection to Intersystems Cache database
     *
     * @param factory   factory
     * @param host      host
     * @param port      port
     * @param namespace namespace
     * @param username  username
     * @param password  password
     */
    public GroovyCacheConnection(Factory factory, String host, int port, String namespace, String username, String password) throws Exception {
        factory.registerAutoCloseable(this);

        this.factory = factory;
        this.host = host;
        this.port = port;
        this.namespace = namespace;
        this.username = username;
        this.password = password;

        Class.forName("com.intersys.jdbc.CacheDriver");
        connection = DriverManager.getConnection(
                "jdbc:Cache://" + host + ":" + port + "/" + namespace,
                username,
                password);

        if (!STORED_PROC_VERSION.equals(getStoredProcVersion())) {
            uninstallAll();
            installAll();
            setStoredProcVersion(STORED_PROC_VERSION);
        }
    }

    @Override
    public Factory getFactory() {
        return factory;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    public long getCacheJobID() throws SQLException {
        return ((CacheConnection) connection).getCacheJobID();
    }

    public String getServerLocale() {
        return ((CacheConnection) connection).getServerLocale();
    }

    public boolean isServerUnicode() {
        return ((CacheConnection) connection).isServerUnicode();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String read(String command) throws SQLException {
        if (command == null)
            throw new RuntimeException("Command not specified!");

        if (command.length() > MAX_COMMAND_LENGTH)
            throw new RuntimeException("Max command length is exceeded!");

        final CallableStatement callableStatement = connection.prepareCall("{? = call Groovy.Execute(?)}");
        callableStatement.setString(2, command);
        callableStatement.registerOutParameter(1, Types.VARCHAR);
        callableStatement.executeUpdate();
        return callableStatement.getString(1);
    }

    public void execute(String command) throws SQLException {
        if (command == null)
            throw new RuntimeException("Command not specified!");

        if (command.length() > MAX_COMMAND_LENGTH)
            throw new RuntimeException("Max command length is exceeded!");

        final CallableStatement callableStatement = connection.prepareCall("{call Groovy.ExecuteNoReturn(?)}");
        callableStatement.setString(1, command);
        callableStatement.executeUpdate();
    }

    @SuppressWarnings("SqlDialectInspection")
    private void installAll() throws SQLException {
        connection.prepareStatement(
                "create function Groovy.Execute(cmd varchar(" + MAX_COMMAND_LENGTH + ")) " +
                        "returns varchar(" + MAX_COMMAND_LENGTH + ") language cos {" +
                        " x \"set P0 = \"_cmd " +
                        " quit P0 " +
                        " }"
        ).executeUpdate();

        connection.prepareStatement(
                "create procedure Groovy.ExecuteNoReturn(cmd varchar(" + MAX_COMMAND_LENGTH + ")) " +
                        "language cos {" +
                        " x cmd " +
                        " }"
        ).executeUpdate();

        connection.prepareStatement(
                "create function Groovy.GetMD5(text varchar(300), original integer) RETURNS VARCHAR(32) LANGUAGE COS {\n" +
                        " set b=$system.Encryption.MD5Encode(text)\n" +
                        " if original = 0 set b=$ZCVT(b,\"L\")\n" +
                        " set result=\"\" \n" +
                        " set i=1\n" +
                        " while (i <= $L(b)) {\n" +
                        "  set result=result_$J($ZHEX($A(b,i)),2) \n" +
                        "  set i=i+1\n" +
                        " }\n" +
                        " set result=$TR(result,\" \",\"0\")\n" +
                        " quit result\n" +
                        " }"
        ).executeUpdate();

    }

    private void safeDropProc(String dml) {
        try {
            connection.prepareStatement(dml).executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() != 362)
                throw new GroovyException("DML execution error: " + dml + "\n" + e.getMessage(), e);
        }
    }

    private void uninstallAll() {
        safeDropProc("drop procedure Groovy.ExecuteNoReturn");
        safeDropProc("drop function Groovy.Execute");
        safeDropProc("drop function Groovy.GetMD5");
    }

    public String getStoredProcVersion() {
        try {
            return read("^Groovy(\"STORED_PROC_VERSION\")");
        } catch (Throwable e) {
            return null;
        }
    }

    public void setStoredProcVersion(String version) throws SQLException {
        execute("s ^Groovy(\"STORED_PROC_VERSION\")=" + version);
    }

    public boolean isMAPInstalled() {
        try {
            read("^Version(\"M-Apteka+\",\"Current\",\"Ver\")");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public int getMAPMajorVersion() {
        try {
            return Integer.parseInt(read("^Version(\"M-Apteka+\",\"Current\",\"Ver\")"));
        } catch (Throwable e) {
            throw new GroovyException("M-Apteka is not found");
        }
    }

    public int getMAPMinorVersion() {
        try {
            return Integer.parseInt(read("^Version(\"M-Apteka+\",\"Current\",\"Build\")"));
        } catch (Throwable e) {
            throw new GroovyException("M-Apteka is not found");
        }
    }

    @SuppressWarnings("SqlDialectInspection")
    public DBOwner getMAPDBOwner() {
        try (ResultSet resultSet = connection.prepareStatement(
                "select owner->firstobj->FullName, owner->firstobj->ExtCode " +
                        " from SprSubdiv " +
                        " where destroyed = 0 and isOut = 1"
        ).executeQuery()) {
            if (resultSet.next())
                return new DBOwner(resultSet.getString("FullName"), resultSet.getLong("ExtCode"));
        } catch (SQLException e) {
            throw new GroovyException("M-Apteka is not found");
        }

        return null;
    }

    public class DBOwner {
        private String name;
        private Long code;

        public DBOwner(String name, Long code) {
            this.name = name;
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public Long getCode() {
            return code;
        }
    }

}
