package dbvc;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.WebAuthSession;

import dbvc.DBVC.CLInteractor;

//dbvc save <filename> -m "message"
public class SaveCommand extends Command {
	
	private static final String NAME = "save";
	private static final TerminalOption MESSAGE_OPTION = new TerminalOption("-m", "--message", false, 0, 1);
	private static final Iterable<TerminalOption> SAVE_OPTIONS = new ArrayList<TerminalOption>(Arrays.asList(new TerminalOption[]{MESSAGE_OPTION}));

	public SaveCommand(File dropboxRoot, CLInteractor cli, DataStore ds, DropboxAPI<WebAuthSession> dbApi) throws IOException {
		super(dropboxRoot, cli, ds, dbApi);
	}
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public void execute(List<String> args) throws SQLException, IOException, DropboxException {
		ParsedArgs parsedArgs = Command.parseArgs(args, 1, SAVE_OPTIONS);
		if (parsedArgs._allValid) {
			DropboxFile dropboxFile = new DropboxFile(_dropboxRoot, new File(parsedArgs._requiredArgs.get(0)));
			System.out.println("dbvc danonical path: " + dropboxFile.getDbvcCanonicalPath());
			//TODO explicitly wait until dropbox has finished syncing to ensure that the rev number of the server version is correct
			Entry entry = _dbApi.metadata(dropboxFile.getDbvcCanonicalPath(), 1, null, false, null);
			System.out.println(entry.rev);
			_ds.saveVersion(dropboxFile, entry.rev, parsedArgs._parsedOptions.get(MESSAGE_OPTION).get(0));
		}
	}

}