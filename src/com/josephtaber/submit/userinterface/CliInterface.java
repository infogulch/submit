package com.josephtaber.submit.userinterface;

import com.josephtaber.submit.userinterface.console.TextDevice;

public class CliInterface implements UserInterface
{
	private String username;
	private String password;
	
	private String[] args;
	private TextDevice textdevice = TextDevice.defaultTextDevice();
	
	private final boolean isInteractive;
	
	public CliInterface(String[] args)
	{
		if (args[0].equalsIgnoreCase("--help") || args[0].equalsIgnoreCase("-h"))
		{
			printUsage();
			System.exit(0);
		}
		isInteractive = args.length > 0 && args[0].equalsIgnoreCase("-i");
		this.args = args;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public boolean promptCredentials()
	{
		return promptUsername("Enter username") && promptPassword("Enter password");
	}
	
	public String[] getSubmission()
	{
		String[] ret = null;
		if (isInteractive)
			ret = promptInputs("", new String[] {"Course", "Assignment", "Files"}, new int[] {0, 0, 0}, null);
		else if (args.length >= 3)
		{
			String files = "";
			for (int i = 2; i < args.length; i++)
				files += ";" + args[i];
			args[2] = files.substring(1);
			args = java.util.Arrays.copyOfRange(args, 0, 3);
			ret = args;
		}
		else 
			printUsage();
		return ret;
	}
	
	public String promptInput(String message)
	{
		return promptInput(message, UserInterface.INPUT_ECHO);
	}
	
	public String promptInput(String message, int type)
	{
		textdevice.printf(message + ": ");
		if (type == UserInterface.INPUT_NO_ECHO)
			return new String(textdevice.readPassword());
		else
			return textdevice.readLine();
	}
	
	public String[] promptInputs(String instruction, String[] prompts, int[] types, String[] initial)
	{
		if (!instruction.isEmpty())
			showMessage(instruction);
		String[] result = new String[prompts.length];
		for (int i = 0; i < prompts.length; i++)
			result[i] = promptInput(prompts[i], types[i]);
		return result;
	}
	
	public boolean promptPassword(String message)
	{
		password = promptInput(message, UserInterface.INPUT_NO_ECHO);
		return true;
	}
	
	public boolean promptUsername(String message)
	{
		username = promptInput(message);
		return true;
	}
	
	public boolean promptYesNo(String message)
	{
		textdevice.printf(message + ": ");
		String result = textdevice.readLine();;
		if (result.equals("yes") || result.equals("y"))
			return true;
		return false;
	}
	
	public void showMessage(String message)
	{
		textdevice.printf(message + "\n");
	}
	
	public boolean promptContinueSubmit()
	{
		return isInteractive && promptYesNo("Submit again?"); // short-circuits if not interactive
	}

	public void showInvalidInputMessage(String message) {
		showMessage(message);
		if (!isInteractive)
			printUsage();
	}
	
	private void printUsage() {
		textdevice.printf("Usage: submit [<nothing> | -i | <coursename> <assignmentname> <files>]"
				+ "\n\nExamples:"
				+ "\nStart in gui mode:"
				+ "\n\tsubmit"
				+ "\nStart in interactive mode:"
				+ "\n\tsubmit -i"
				+ "\nStart in command line mode (still asks for username and password):"
				+ "\n\tsubmit cs1100 a0 file1.java file2.jpg directory"
				+ "\nShow this message:"
				+ "\n\tsubmit --help"
				+ "\n");
	}
	
	public void startProgressMonitor() {
		textdevice.printf("Starting submission...\n");
	}
	
	public void progressUpdate(String fileName) {
		textdevice.printf("submitting " + fileName + "\n");
	}
	
	public void stopProgressMonitor() { }
}
