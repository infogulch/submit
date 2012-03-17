package com.josephtaber.submit.userinterface;

public interface UserInterface
{
	public static final int INPUT_ECHO = 0;
	public static final int INPUT_NO_ECHO = 1;
	public static final int INPUT_FILE = 2;
	
	boolean promptUsername(String message);
	boolean promptPassword(String message);
	
	boolean promptCredentials();
	
	String promptInput(String message);
	String promptInput(String message, int type);
	String[] promptInputs(String instruction, String[] prompts, int[] type, String[] initial);
	
	boolean promptYesNo(String message);
	
	void showMessage(String message);
	
	void showInvalidInputMessage(String message);
	
	String getUsername();
	String getPassword();
	
	String[] getSubmission();
	boolean promptContinueSubmit();
	
	abstract void startProgressMonitor();
	abstract void progressUpdate(String fileName);
	abstract void stopProgressMonitor();
}
