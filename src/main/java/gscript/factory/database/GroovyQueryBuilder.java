package gscript.factory.database;

import gscript.factory.database.hsqldb.GroovyHSQLDBTable;
import gscript.factory.format.GroovyStringJoiner;

public final class GroovyQueryBuilder {

    private final String baseTable;

    private String selectStatement = "";
    private String fromStatement = "";
    private String joinStatement = "";
    private String whereStatement = "";
    private String orderByStatement = "";

    public GroovyQueryBuilder(String baseTable) {
        this.baseTable = baseTable;
        fromStatement = baseTable;
    }

    public GroovyQueryBuilder selectAll() {
        selectStatement = "select *";
        return this;
    }

    public GroovyQueryBuilder select(String... columns) {
        final GroovyStringJoiner stringJoiner = new GroovyStringJoiner(", ");
        for (String columnn : columns)
            stringJoiner.add(columnn);

        selectStatement = "select " + stringJoiner.toString();
        return this;
    }

    public GroovyQueryBuilder leftJoin(GroovyHSQLDBTable table, String thisColumnName, String joinColumnName) {
        joinStatement = " left join " + table.getName() + " on " + baseTable + "." + thisColumnName + " = " + table.getName() + "." + joinColumnName + " ";
        return this;
    }

    public GroovyQueryBuilder where(String condition) {
        whereStatement = condition;
        return this;
    }

    public GroovyQueryBuilder orderBy(String condition) {
        orderByStatement = condition;
        return this;
    }

    public String getQuery() {
        final StringBuilder stringBuilder = new StringBuilder();

        if ("".equals(selectStatement))
            stringBuilder.append(" select * ");
        else
            stringBuilder.append(selectStatement);

        stringBuilder.append(" from ").append(fromStatement);

        if (!"".equals(joinStatement))
            stringBuilder.append(joinStatement);

        if (!"".equals(whereStatement))
            stringBuilder.append(" where ").append(whereStatement);

        if (!"".equals(orderByStatement))
            stringBuilder.append(" order by ").append(orderByStatement);

        return stringBuilder.toString().trim().replaceAll("\\s+", " ");
    }

}
