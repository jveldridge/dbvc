package dbvc;

public class Version {

	private DropboxFile _file;
	private String _dropboxRev;
	private int _versionNumber;
	private String _message;
	
	public Version(DropboxFile file, String dropboxRev, int versionNumber, String message) {
		_file = file;
		_dropboxRev = dropboxRev;
		_versionNumber = versionNumber;
		_message = message;
	}
	
	public DropboxFile getFile() {
		return _file;
	}
	
	public String getDropboxRev() {
		return _dropboxRev;
	}
	
	public int getVersionNumber() {
		return _versionNumber;
	}
	
	public String getMessage() {
		return _message;
	}
	
}
