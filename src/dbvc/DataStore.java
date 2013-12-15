package dbvc;

import java.sql.SQLException;
import java.util.List;

public interface DataStore {

	//TODO store timestamp and expiration time
	public void saveVersion(DropboxFile file, String dropboxRev, String message) throws SQLException;
	
	public List<Version> getVersions(DropboxFile file) throws SQLException;
	
	public Version getVersion(DropboxFile file, int versionNumber) throws SQLException;
	
	public void resetDatabase() throws SQLException;
	
}
