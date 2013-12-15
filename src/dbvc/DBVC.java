package dbvc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;
import com.dropbox.client2.session.WebAuthSession.WebAuthInfo;

public class DBVC {
	
	private static final String CONFIG_FILE_NAME = "config.txt";
	
	private File _dropboxRoot;
	private HelpCommand _helpCommand;
	private Set<Command> _commands;
	
	public DBVC(CLInteractor cli, String dropboxRoot) throws DropboxException, IOException, SQLException {
		_dropboxRoot = new File(dropboxRoot).getCanonicalFile();
		DropboxAPI<WebAuthSession> dbApi = this.getDropboxAPI();
		DataStore ds = new DatabaseImpl(getDbvcDatabase());
		
		_helpCommand = new HelpCommand(this, _dropboxRoot, cli, ds, dbApi);
		_commands = new HashSet<Command>();
		_commands.add(_helpCommand);
		_commands.add(new SaveCommand(_dropboxRoot, cli, ds, dbApi));
		_commands.add(new LogCommand(_dropboxRoot, cli, ds, dbApi));
		_commands.add(new RestoreCommand(_dropboxRoot, cli, ds, dbApi));
	}
	
	public void run(List<String> args) throws SQLException, IOException, DropboxException {
		if (args.isEmpty()) {
			_helpCommand.execute(Collections.<String>emptyList());
		}
		else {
			String cmdArg = args.get(0);
			for (Command cmd : _commands) {
				if (cmd.getName().equalsIgnoreCase(cmdArg)) {
					cmd.execute(args.subList(1, args.size()));
				}
			}
		}
	}
	
	Set<Command> getCommands() {
		return Collections.unmodifiableSet(_commands);
	}
	
	public File getDbvcDatabase() throws IOException {
		return new File(this.getDbvcFolder(), "dbvc.sqlite");
	}
	
	public File getDbvcFolder() throws IOException {
		return new File(this.getDropboxPath(), ".dbvc");
	}
	
	public File getDropboxPath() {
		return _dropboxRoot;
	}
	
	private DropboxAPI<WebAuthSession> getDropboxAPI() throws IOException, DropboxException {
		File configFile = new File(this.getDbvcFolder(), CONFIG_FILE_NAME);
		BufferedReader reader = new BufferedReader(new FileReader(configFile));
		String appKey = reader.readLine();
		String appSecret = reader.readLine();
		
		DropboxAPI<WebAuthSession> dbApi = new DropboxAPI<WebAuthSession>(new WebAuthSession(new AppKeyPair(appKey, appSecret), AccessType.DROPBOX));
		WebAuthSession session = dbApi.getSession();
		
		String atpKey = null, atpSecret = null;
		atpKey = reader.readLine();
		if (atpKey != null) {
			atpSecret = reader.readLine();
			reader.close();
		}
		else {
			reader.close();
			
			WebAuthInfo authInfo = session.getAuthInfo();
			System.out.println("URL to go to: " + authInfo.url);
			System.out.println("Press enter when complete");
			new BufferedReader(new InputStreamReader(System.in)).readLine();
			
			session.retrieveWebAccessToken(authInfo.requestTokenPair);
			atpKey = session.getAccessTokenPair().key;
			atpSecret = session.getAccessTokenPair().secret;
			
			BufferedWriter w = new BufferedWriter(new FileWriter(configFile));
			w.write(atpKey + '\n');
			w.write(atpSecret + '\n');
			w.close();
		}
		
		session.setAccessTokenPair(new AccessTokenPair(atpKey, atpSecret));
		
		return dbApi;
	}
	
	static interface CLInteractor {
		public void println(String msg);
		
		public void print(String msg);
	}
	
	public static class DefaultCLInteractor implements CLInteractor {
		@Override
		public void println(String msg) {
			System.out.println(msg);
		}
		
		@Override
		public void print(String msg) {
			System.out.print(msg);
		}
	}
	
	public static void main(String[] argv) throws DropboxException, IOException, SQLException {
		//assume that jar is located in a .dbvc folder in the root of the user's Dropbox directory
		File jarFile = new File(System.getProperty("java.class.path")).getAbsoluteFile();
		String dropboxRoot = jarFile.getParentFile().getParent();
		new DBVC(new DefaultCLInteractor(), dropboxRoot).run(Arrays.asList(argv));
	}

}
