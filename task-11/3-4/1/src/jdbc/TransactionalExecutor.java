package jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionalExecutor {

    public static <T> T executeInTransaction(TransactionCallback<T> callback) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            T result = callback.execute(conn);

            conn.commit();
            return result;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw new RuntimeException("Ошибка транзакции", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FunctionalInterface
    public interface TransactionCallback<T> {
        T execute(Connection conn) throws Exception;
    }
}