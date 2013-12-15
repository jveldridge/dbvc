package dbvc;

import java.io.File;
import java.io.IOException;

public class DropboxFile {
	
	private File _file;
	private String _dbvcCanonicalPath;

	public DropboxFile(File dropboxRoot, String pathname) throws IOException {
		this(dropboxRoot, new File(pathname));
	}
	
	public DropboxFile(File dropboxRoot, File file) throws IOException {
		if (file.isDirectory()) {
			throw new IllegalArgumentException("DropboxFile cannot be a directory");
		}
		_file = file.getCanonicalFile();
		_dbvcCanonicalPath = _file.getCanonicalPath().replaceFirst(dropboxRoot.getCanonicalPath(), "/");
	}
	
	/**
	 * @return the canonical path to the file, excluding the location of the Dropbox folder
	 */
	public String getDbvcCanonicalPath() {
		return _dbvcCanonicalPath;
	}
	
}
