package test;

import java.io.File;
import java.sql.*;
import org.junit.*;
import static org.junit.Assert.*;

/** These tests check whether access to files is woring correctly and
 *  some Connection.close() cases. */
public class ConnectionTest
{
    @BeforeClass public static void forName() throws Exception {
        Class.forName("org.sqlite.JDBC");
    }

    @Test public void openMemory() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:");
        conn.close();
    }

    @Test public void isClosed() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:");
        assertFalse(conn.isReadOnly());
        conn.close();
        assertTrue(conn.isClosed());
    }

    @Test public void openFile() throws SQLException {
        File testdb = new File("test.db");
        if (testdb.exists()) testdb.delete();

        assertFalse(testdb.exists());
        Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
        assertFalse(conn.isReadOnly());
        conn.close();

        assertTrue(testdb.exists());
        conn = DriverManager.getConnection("jdbc:sqlite:test.db");
        assertFalse(conn.isReadOnly());
        conn.close();

        assertTrue(testdb.exists());
        testdb.delete();
    }

    @Test(expected= SQLException.class)
    public void closeTest() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:");
        PreparedStatement prep = conn.prepareStatement("select null;");
        ResultSet rs = prep.executeQuery();
        conn.close();
        prep.clearParameters();
    }
    
    @Test public void sqlCipherTest() throws SQLException {
        File testdb = new File("cipher.db");
        if (testdb.exists()) testdb.delete();

        Connection conn = DriverManager.getConnection("jdbc:sqlite:cipher.db");
        PreparedStatement prep = conn.prepareStatement("pragma key='123';");
        prep.executeUpdate();
        prep.close();

        prep = conn.prepareStatement("create table foo (bar integer);");
        prep.executeUpdate();
        prep.close();

        prep = conn.prepareStatement("insert into foo (bar) values (1);");
        prep.executeUpdate();
        prep.close();

        conn.close();

        conn = DriverManager.getConnection("jdbc:sqlite:cipher.db");

        ResultSet rs = null;

        try {
            prep = conn.prepareStatement("select bar from foo");
            rs = prep.executeQuery();
            assertTrue(false);
        } 
        catch (java.sql.SQLException e)
        {
            prep.close();
            assertTrue(true);
        }
        
        conn = DriverManager.getConnection("jdbc:sqlite:cipher.db");
        
        prep = conn.prepareStatement("pragma key='123';");
        prep.executeUpdate();
        prep.close();

        prep = conn.prepareStatement("select bar from foo");
        rs = prep.executeQuery();

        assertTrue(rs.next());
        assertEquals(rs.getInt(1), 1);

        prep.close();
        conn.close();
    }
}
