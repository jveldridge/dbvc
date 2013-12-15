package dbvc;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.sqlite.SQLiteConfig;

//TODO fix testability in case where dropbox isn't authorized

public class DatabaseImpl implements DataStore {
	
	private ConnectionProvider _connProvider;
    
    public DatabaseImpl(final File dbFile) throws IOException, SQLException {
        _connProvider = new ConnectionProvider() {

            @Override
            public Connection createConnection() throws SQLException {
                try {
                    Class.forName("org.sqlite.JDBC");
                    SQLiteConfig config = new SQLiteConfig();
                    config.enforceForeignKeys(true);
                    
                    return DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath(), config.toProperties());
                } catch (ClassNotFoundException e) {
                    throw new SQLException("Could not open a connection to the DB.", e);
                }
            }

            @Override
            public void closeConnection(Connection c) throws SQLException {
                if (c != null) {
                    c.close();
                }
            }
        };
        
        this.createDatabaseIfNecessary(dbFile);
    }
    
	private void createDatabaseIfNecessary(File databaseFile) throws IOException, SQLException {
		if (!databaseFile.exists()) {
			databaseFile.createNewFile();
			this.resetDatabase();
		}
	}
    
    /**
     * opens a new connection to the DB
     */
    private Connection openConnection() throws SQLException {
        return _connProvider.createConnection();
    }
    
    private void closeConnection(Connection c) throws SQLException {
        _connProvider.closeConnection(c);
    }
    
    @Override
    public void saveVersion(DropboxFile file, String dropboxRev, String message) throws SQLException {
    	Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT version FROM versions WHERE path = ? ORDER BY version DESC");
            ps.setString(1, file.getDbvcCanonicalPath());
            
            ResultSet rs = ps.executeQuery();
            int lastVersion = 0;
            if (rs.next()) {
            	lastVersion = rs.getInt("version");
            }
            lastVersion++;
            
            ps = conn.prepareStatement("INSERT INTO versions(path, dropboxrev, version, message) VALUES (?, ?, ?, ?)");
            ps.setString(1, file.getDbvcCanonicalPath());
            ps.setString(2, dropboxRev);
            ps.setInt(3, lastVersion);
            ps.setString(4, message);
            ps.executeUpdate();
            
        } finally {
            this.closeConnection(conn);
        }
    }
	
    @Override
	public List<Version> getVersions(DropboxFile file) throws SQLException {
    	List<Version> versions = new ArrayList<Version>();
    	
    	Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT version, dropboxrev, message FROM versions WHERE path = ? ORDER BY version ASC");
            ps.setString(1, file.getDbvcCanonicalPath());
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	versions.add(new Version(file, rs.getString("dropboxrev"), rs.getInt("version"), rs.getString("message")));
            }
        } finally {
            this.closeConnection(conn);
        }
    	
		return versions;
	}
    
    @Override
    public Version getVersion(DropboxFile file, int versionNumber) throws SQLException {
    	Version version = null;
    	
    	Connection conn = this.openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT dropboxrev, message FROM versions WHERE path = ? AND version = ?");
            ps.setString(1, file.getDbvcCanonicalPath());
            ps.setInt(2, versionNumber);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
            	version = new Version(file, rs.getString("dropboxrev"), versionNumber, rs.getString("message"));
            }
        } finally {
            this.closeConnection(conn);
        }
        
        return version;
    }
    
    @Override
    public void resetDatabase() throws SQLException {
        Connection conn = this.openConnection();
        try {
            conn.setAutoCommit(false);
            //DROP all tables in DB
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS versions");
            
            //CREATE all DB tables
            conn.createStatement().executeUpdate("CREATE TABLE versions ("
            		+ "path VARCHAR NOT NULL,"
            		+ "dropboxrev VARCHAR NOT NULL,"
            		+ "version INTEGER NOT NULL,"
                    + "message VARCHAR,"
                    + "CONSTRAINT versionsunique UNIQUE (path, version) ON CONFLICT ROLLBACK)");

            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            this.closeConnection(conn);
        }
    }

    /**
     * ConnectionProviders encapsulate the creation of DB connections. Used by the DBWrapper to allow it to work with both
     * sqlite file DB and the sqlite in memory test DB.
     *
     * @author aunger
     */
    public interface ConnectionProvider {

        /**
         * Opens a new connection to the same DB as the last connection.
         *
         * @return a connection to the DB
         * @throws SQLException
         */
        public Connection createConnection() throws SQLException;

        /**
         * Closes a connection that was opened by the createConnection method.
         *
         * @param connection the connection to close
         * @throws SQLException
         */
        public void closeConnection(Connection connection) throws SQLException;
    }
    
}
