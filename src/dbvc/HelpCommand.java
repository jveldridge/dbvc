package dbvc;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.WebAuthSession;

import dbvc.DBVC.CLInteractor;

public class HelpCommand extends Command {
	
	private DBVC _dbvc;
	
	public HelpCommand(DBVC dbvc, File dropboxRoot, CLInteractor cli, DataStore ds, DropboxAPI<WebAuthSession> dbApi) throws IOException {
		super(dropboxRoot, cli, ds, dbApi);
		_dbvc = dbvc;
	}

	private static final String NAME = "help";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void execute(List<String> args) throws SQLException, IOException, DropboxException {
		_cli.println("Error: no command provided.  Valid commands are: ");
		
		for (Iterator<Command> cmdIterator = _dbvc.getCommands().iterator(); cmdIterator.hasNext(); cmdIterator.next()) {
			_cli.print(cmdIterator.next().getName());
			
			if (cmdIterator.hasNext()) {
				_cli.print(", ");
			}
		}
		_cli.print("\n");
	}

}
