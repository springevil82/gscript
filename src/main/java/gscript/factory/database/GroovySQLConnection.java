package gscript.factory.database;

import gscript.Factory;
import gscript.GroovyException;
import gscript.factory.document.GroovyMultilineDocument;
import gscript.factory.format.GroovyDBTablePrinter;
import org.apache.commons.lang3.ArrayUtils;

import java.sql.*;

public abstract class GroovySQLConnection implements AutoCloseable {

    private String lastError;

    public abstract Factory getFactory();

    public abstract Connection getConnection();

    @Override
    public void close() throws Exception {
        getConnection().close();
    }

    /**
     * Получить название и версию БД
     */
    public String getDatabaseVersion() throws SQLException {
        return getConnection().getMetaData().getDatabaseProductVersion();
    }

    /**
     * Начать новую транзакцию
     */
    public void startTransaction() throws SQLException {
        getConnection().setAutoCommit(false);
    }

    /**
     * Комит текущей транзакции
     *
     * @throws SQLException
     */
    public void commit() throws SQLException {
        getConnection().commit();
        getConnection().setAutoCommit(true);
    }

    /**
     * Роллбэк текущей транзакции
     *
     * @throws SQLException
     */
    public void rollback() throws SQLException {
        getConnection().rollback();
        getConnection().setAutoCommit(true);
    }

    /**
     * Получение последней ошибки
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * Выполнить команду sql (DML) для обновления данных (не возвращающую ResultSet; типа INSERT, UPDATE или DELETE)
     *
     * @param sql SQL
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing
     * @throws SQLException
     */
    public int executeSQL(String sql) throws SQLException {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            return preparedStatement.executeUpdate();
        }
    }

    /**
     * Выполнить команду sql (DML) для обновления данных (не возвращающую ResultSet; типа INSERT, UPDATE или DELETE).
     * Если команда не выполнена успешно - ошибки не будет (будет вернут код возврата -1). Ошибку можно забрать используя getLastError()
     *
     * @param sql SQL
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing
     * @throws SQLException
     */
    public int executeSQLSafe(String sql) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            return -1;
        }
    }

    /**
     * Удалить таблицу без выброса исключений
     *
     * @param tableName имя таблицы
     * @return true - удалено успешно, false - при удалении возникли ошибки (например ее нет или контстрейнты). Ошибку можно посмотреть getLastError
     */
    public boolean dropTableSafe(String tableName) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("drop table " + tableName)) {
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            lastError = e.getMessage();
            return false;
        }
    }

    /**
     * Запросить ResultSet с одной ячейкой и вернуть ее значение
     *
     * @return значение
     */
    public Object queryObject(String sql) throws SQLException {
        try (ResultSet resultSet = getConnection().prepareStatement(sql).executeQuery()) {
            if (resultSet.next())
                return resultSet.getObject(1);
            else
                throw new GroovyException("No one row found");
        }
    }

    /**
     * Запросить ResultSet с одной записью и вернуть массив значений
     *
     * @return массив значений
     */
    public Object[] queryRow(String sql) throws SQLException {
        Object[] objects = new Object[0];
        try (ResultSet resultSet = getConnection().prepareStatement(sql).executeQuery()) {
            if (resultSet.next()) {

                for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++)
                    objects = ArrayUtils.add(objects, resultSet.getObject(i + 1));

                return objects;
            } else
                throw new GroovyException("No one row found");
        }
    }

    /**
     * Выполнить команду sql возвращающую ResultSet
     *
     * @param sql SQL
     * @return ResultSet
     * @throws SQLException
     */
    public GroovyResultSet queryResultSet(String sql) throws SQLException {
        long currentTime = System.currentTimeMillis();
        final ResultSet resultSet = getConnection().prepareStatement(sql).executeQuery();
        long timeTaken = System.currentTimeMillis() - currentTime;

        return new GroovyResultSet(getFactory(), resultSet, timeTaken);
    }

    /**
     * Распечатать полученный resultset в консоль
     *
     * @param sql запрос
     * @throws Exception
     */
    public void printResultSet(String sql) throws Exception {
        try (ResultSet resultSet = getConnection().prepareStatement(sql).executeQuery()) {
            new GroovyDBTablePrinter().printResultSet(resultSet);
        }
    }

    /**
     * Получить данные запроса и загрузить их в новый документ
     *
     * @param sql запрос
     * @return документ
     * @throws Exception
     */
    public GroovyMultilineDocument resultSetToDocument(String sql) throws Exception {
        final GroovyMultilineDocument document = getFactory().document.createDocument();

        document.put("query", sql);

        final long startTime = System.currentTimeMillis();
        try (ResultSet resultSet = getConnection().prepareStatement(sql).executeQuery()) {
            final ResultSetMetaData metaData = resultSet.getMetaData();

            int rowCount = 0;
            while (resultSet.next()) {
                final GroovyMultilineDocument.Line line = document.createLine();
                for (int i = 0; i < metaData.getColumnCount(); i++)
                    line.put(metaData.getColumnName(i + 1), resultSet.getObject(i + 1));

                rowCount++;
            }

            document.put("rowsFetched", rowCount);
        }

        document.put("executeTimeInMilis", System.currentTimeMillis() - startTime);

        return document;
    }

}
