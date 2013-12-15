package dbvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.dropbox.client2.exception.DropboxException;

import dbvc.DBVC.CLInteractor;

public class DatabaseTest {
	
	@Rule
	public TemporaryFolder _fakeRoot = new TemporaryFolder();
		
	private DataStore _testDb;
	
	static class DoNothingCLInteractor implements CLInteractor {
		@Override
		public void println(String msg) {}

		@Override
		public void print(String msg) {}
	}
	
	@Before
	public void setUp() throws SQLException, IOException, DropboxException {
		System.out.println(_fakeRoot.getRoot());
		_testDb = new DatabaseImpl(new File(_fakeRoot.getRoot(), "test.sqlite"));
		_testDb.resetDatabase();
	}
	
	@Test
	public void getNoVersions() throws SQLException, IOException {
		List<Version> versions = _testDb.getVersions(new DropboxFile(_fakeRoot.getRoot(), "doesNotExist"));
		assertEquals(0, versions.size());
	}
	
	@Test
	public void saveFirstVersion() throws SQLException, IOException {
		DropboxFile fileToSave = new DropboxFile(_fakeRoot.getRoot(), new File("fileToSave"));
		String message = "This is the first version";
		String dropboxRev = UUID.randomUUID().toString().substring(0, 7);
		_testDb.saveVersion(fileToSave, dropboxRev, message);
		
		//there should be one version for the file we just saved a version for
		List<Version> versions = _testDb.getVersions(fileToSave);
		assertEquals(1, versions.size());
		
		//its path and message should match what was set, and its version number should be 1
		Version version = versions.get(0);
		assertEquals(fileToSave.getDbvcCanonicalPath(), version.getFile().getDbvcCanonicalPath());
		assertEquals(message, version.getMessage());
		assertEquals(dropboxRev, version.getDropboxRev());
		assertEquals(1, version.getVersionNumber());
		
		//there should still be no versions for any other path
		versions = _testDb.getVersions(new DropboxFile(_fakeRoot.getRoot(), "stillDoesNotExist"));
		assertEquals(0, versions.size());
	}
	
	@Test
	public void saveTwoVersions() throws SQLException, IOException {
		DropboxFile fileToSave = new DropboxFile(_fakeRoot.getRoot(), new File("fileToSave"));
		String firstMessage = "This is the first version";
		String firstDbRev = UUID.randomUUID().toString().substring(0, 7);
		_testDb.saveVersion(fileToSave, firstDbRev, firstMessage);
		
		//there should be one version for the file we just saved a version for
		List<Version> versions = _testDb.getVersions(fileToSave);
		assertEquals(1, versions.size());
		
		//its path and message should match what was set, and its version number should be 1
		Version version = versions.get(0);
		assertEquals(fileToSave.getDbvcCanonicalPath(), version.getFile().getDbvcCanonicalPath());
		assertEquals(firstMessage, version.getMessage());
		assertEquals(firstDbRev, version.getDropboxRev());
		assertEquals(1, version.getVersionNumber());
		
		//after adding the second version...
		String secondMessage = "This is the second version";
		String secondDbRev = UUID.randomUUID().toString().substring(0, 7);
		_testDb.saveVersion(fileToSave, secondDbRev, secondMessage);
		
		//there should be two versions for the file, and they should be in increasing order by version number
		versions = _testDb.getVersions(fileToSave);
		assertEquals(2, versions.size());
		assertTrue(versions.get(0).getVersionNumber() < versions.get(1).getVersionNumber());
		
		//the path and message of the second version should match what was set, and its version number should be 2
		version = versions.get(1);
		assertEquals(fileToSave.getDbvcCanonicalPath(), version.getFile().getDbvcCanonicalPath());
		assertEquals(secondMessage, version.getMessage());
		assertEquals(2, version.getVersionNumber());
	}
	
	@Test
	public void saveVersionWithNoMessage() throws SQLException, IOException {
		DropboxFile fileToSave = new DropboxFile(_fakeRoot.getRoot(), new File("fileToSave"));
		String dropboxRev = UUID.randomUUID().toString().substring(0, 7);
		_testDb.saveVersion(fileToSave, dropboxRev, null);
		
		//there should be one version for the file we just saved a version for
		List<Version> versions = _testDb.getVersions(fileToSave);
		assertEquals(1, versions.size());
		
		//its path and message should match what was set, and its version number should be 1
		Version version = versions.get(0);
		assertEquals(fileToSave.getDbvcCanonicalPath(), version.getFile().getDbvcCanonicalPath());
		assertNull(version.getMessage());
		assertEquals(dropboxRev, version.getDropboxRev());
		assertEquals(1, version.getVersionNumber());
	}

}
