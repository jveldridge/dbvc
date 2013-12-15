package dbvc;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.WebAuthSession;

import dbvc.DBVC.CLInteractor;

public abstract class Command {
	
	protected File _dropboxRoot;
	protected CLInteractor _cli;
	protected DataStore _ds;
	protected DropboxAPI<WebAuthSession> _dbApi;
	
	public Command(File dropboxRoot, CLInteractor cli, DataStore ds, DropboxAPI<WebAuthSession> dbApi) throws IOException {
		_dropboxRoot = dropboxRoot.getCanonicalFile();
		_cli = cli;
		_ds = ds;
		_dbApi = dbApi;
	}
	
	public abstract String getName();
		
	public abstract void execute(List<String> args) throws SQLException, IOException, DropboxException; 
	
	static class TerminalOption {

		private final String _shortOption, _longOption;
		private final boolean _required;
		private final int _minValues, _maxValues;

		TerminalOption(String shortOption, String longOption, boolean required, int minValues, int maxValues) {
			_shortOption = shortOption;
			_longOption = longOption;
			_required = required;
			_minValues = minValues;
			_maxValues = maxValues;
		}

		/**
		 * Parses the {@code terminalArg} returning a matching
		 * {@code TerminalOption} if one exists and {@code null} otherwise.
		 * 
		 * @param terminalArg
		 * @return
		 */
		static TerminalOption parse(String terminalArg, Iterable<TerminalOption> validOptions) {
			TerminalOption matchingOption = null;
			for (TerminalOption option : validOptions) {
				if (option.getShortOption().equals(terminalArg)
						|| option.getLongOption().equals(terminalArg)) {
					matchingOption = option;
					break;
				}
			}

			return matchingOption;
		}

		public String getShortOption() {
			return _shortOption;
		}

		public String getLongOption() {
			return _longOption;
		}

		boolean isRequired() {
			return _required;
		}

		int getMinValues() {
			return _minValues;
		}

		int getMaxValues() {
			return _maxValues;
		}
    }
	
	static class ParsedArgs {
		
		boolean _allValid;
		List<String> _requiredArgs;
		Map<TerminalOption, List<String>> _parsedOptions;
		
		public ParsedArgs(boolean allValid, List<String> requiredArgs, Map<TerminalOption, List<String>> parsedOptions) {
			_allValid = allValid;
			_requiredArgs = requiredArgs;
			_parsedOptions = parsedOptions;
		}
		
	}
	
	static ParsedArgs parseArgs(List<String> rawArgs, int requiredArgCount, Iterable<TerminalOption> supportedOptions) {
		Map<TerminalOption, List<String>> parsedOptions = new HashMap<TerminalOption, List<String>>();
		if (rawArgs.size() < requiredArgCount) {
			System.err.println("Missing argument");
			return new ParsedArgs(false, null, null);
		}
		List<String> requiredArgs = new ArrayList<String>(rawArgs.subList(0, requiredArgCount));
		rawArgs = rawArgs.subList(requiredArgCount, rawArgs.size());

		boolean optionsValid = true;

		// Parse arguments
		TerminalOption currOption = null;
		for (String arg : rawArgs) {
			TerminalOption option = TerminalOption.parse(arg, supportedOptions);
			if (option != null) {
				parsedOptions.put(option, new ArrayList<String>());
				currOption = option;
			}
			else if (currOption == null) {
				System.out.println("invalid option: " + arg);
				optionsValid = false;
			}
			else {
				parsedOptions.get(currOption).add(arg);
			}
		}

		// Validate arguments
		for (TerminalOption option : supportedOptions) {
			if (option.isRequired() && !parsedOptions.containsKey(option)) {
				System.out.println("required option not provided: "
						+ option.getShortOption());
				optionsValid = false;
			}
			else if (parsedOptions.containsKey(option)) {
				List<String> values = parsedOptions.get(option);
				if (values.size() < option.getMinValues()) {
					System.out.println(option.getShortOption()
							+ " requires at least " + option.getMinValues()
							+ " value(s)");
					optionsValid = false;
				}
				else if (values.size() > option.getMaxValues()) {
					System.out.println(option.getShortOption()
							+ " supports at most " + option.getMaxValues()
							+ " value(s)");
					optionsValid = false;
				}
			}
		}
		
		return new ParsedArgs(optionsValid, requiredArgs, parsedOptions);
	}

}