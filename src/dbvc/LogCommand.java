package dbvc;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.session.WebAuthSession;

import dbvc.DBVC.CLInteractor;

//dbvc log <filename>
public class LogCommand extends Command {
	
	private static final String NAME = "log";

	public LogCommand(File dropboxRoot, CLInteractor cli, DataStore ds, DropboxAPI<WebAuthSession> dbApi) throws IOException {
		super(dropboxRoot, cli, ds, dbApi);
	}
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void execute(List<String> args) throws SQLException, IOException {
		ParsedArgs parsedArgs = Command.parseArgs(args, 1, Collections.<Command.TerminalOption>emptyList());
		if (parsedArgs._allValid) {
			DropboxFile dropboxFile = new DropboxFile(_dropboxRoot, new File(parsedArgs._requiredArgs.get(0)));
			List<Version> versions = _ds.getVersions(dropboxFile);
			
			System.out.println("Rev\tMessage");
			for (Version v : versions) {
				System.out.println(v.getVersionNumber() + "\t" + v.getMessage());
			}
		}
	}

}