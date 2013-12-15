package dbvc;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.WebAuthSession;

import dbvc.DBVC.CLInteractor;

//dbvc restore <filename> <versionNumber> [-o] [-n]
public class RestoreCommand extends Command {

	private static final String NAME = "restore";
	private static final TerminalOption OVERWRITE_OPTION = new TerminalOption("-o", "--overwrite", false, 0, 0);
	private static final Iterable<TerminalOption> RESTORE_OPTIONS = new ArrayList<TerminalOption>(Arrays.asList(new TerminalOption[]{OVERWRITE_OPTION}));
	
	public RestoreCommand(File dropboxRoot, CLInteractor cli, DataStore ds, DropboxAPI<WebAuthSession> dbApi) throws IOException {
		super(dropboxRoot, cli, ds, dbApi);
	}
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void execute(List<String> args) throws SQLException, IOException, DropboxException {
		ParsedArgs parsedArgs = Command.parseArgs(args, 2, RESTORE_OPTIONS);
		if (parsedArgs._allValid) {
			DropboxFile dropboxFile = new DropboxFile(_dropboxRoot, new File(parsedArgs._requiredArgs.get(0)));
			Integer versionNumber = null;
			Version version = null;
			try {
				versionNumber = Integer.parseInt(parsedArgs._requiredArgs.get(1));
			} catch (NumberFormatException ex) {
				//TODO handle this
			};
			if (versionNumber != null) {
				version = _ds.getVersion(dropboxFile, versionNumber);
				System.out.println("version message: " + version.getMessage() + "; db rev: " + version.getDropboxRev());
				_dbApi.restore(dropboxFile.getDbvcCanonicalPath(), version.getDropboxRev());
				System.out.println("restored successfully");
				//return true;
			}
			
			if (version == null) {
				System.err.println("No such version '" + parsedArgs._requiredArgs.get(1) + "'");
				//return false;
			}
		}
		//return false;
	}
	
}
