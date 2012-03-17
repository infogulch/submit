package com.josephtaber.submit.fileinterface;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.DatatypeConverter;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.SftpException;
import com.josephtaber.submit.userinterface.UserInterface;

public abstract class FileInterface
{
	static protected final String HOSTNAME = "foley.math-cs.ucmo.edu";
	static protected final HostKey HOSTKEY;
	
	static
	{
		HostKey hk = null;
		try
		{
			// public HostKey for foley.math-cs.ucmo.edu
			hk = new HostKey(
					HOSTNAME,
					HostKey.SSHRSA,
					DatatypeConverter.parseBase64Binary("AAAAB3NzaC1yc2EAAAABIwAAAIEApj3jWJcDdnFA7r+y0XSg+wA4sRc8RDyTmE/bPkjj00pTg8xrSkbVXhIPMkL2Km3qYZxaeZz3r10I2HSNx/pwmIuHOdBht7A0+z9ePoXTqgGZS0bj0scDoOlGlnPTVdHho/MXTqbkRWxK9eZIv6lEV8wE6jL1T1Zgv8IhZeYsBi8="));
		}
		catch (Throwable t)
		{
		}
		HOSTKEY = hk;
	}
	
	protected UserInterface ui;
	
	public abstract void connect(UserInterface ui);
	
	public abstract boolean fileExists(String file);
	
	public abstract boolean canRead(String file);
	
	public abstract boolean canWrite(String file);
	
	public abstract boolean canExecute(String file);
	
	public abstract boolean isDirectory(String file);
	
	public abstract String readFile(String file);
	
	public abstract void sendFile(File localFile, String remoteFile) throws SftpException, IOException;
	
	public abstract void mkdir(String remoteDir);
	
	public void submit(String submission[])
	{
		if (submission == null)
			return;
		
		if (submission.length != 3)
		{
			ui.showMessage("Invalid submission length.");
			return;
		}
		
		String course = submission[0].toLowerCase().trim()
		 , assignment = submission[1].toLowerCase().trim();
		
		if (!course.matches("^[a-z]+\\d+$"))
		{
			ui.showInvalidInputMessage("Invalid course name.");
			return;
		}
		
		if (!assignment.matches("^a\\d+$"))
		{
			ui.showInvalidInputMessage("Invalid assignment number. Must be in the form of \"aN\" where N is a number. E.g.: a5");
			return;
		}
		
		// Check that all entered files exist and can read them
		String[] filePaths = submission[2].split(";");
		File[] localFiles = new File[filePaths.length];
		for (int i = 0; i < filePaths.length; i++)
		{
			localFiles[i] = new File(filePaths[i].trim());
			if (!(localFiles[i].exists() && localFiles[i].canRead()))
			{
				ui.showInvalidInputMessage("Submitted file doesn't exist: " + filePaths[i]);
				return;
			}
		}
		
		String lastline = null, allLines = readFile("~/.submit");
		if (allLines == null)
		{
			ui.showMessage("Your '.submit' file is not set up. Contact your instructor.");
			return;
		}
		for (String line : allLines.split("\n")) // find the last entry with this course name
			if (line.trim().startsWith(course + " "))
				lastline = line.trim();
		if (lastline == null)
		{
			ui.showMessage("Course not set up to be submitted.");
			return;
		}
		
		// extract directory portion of .submit entry
		String submitDir = lastline.substring(course.length()+1).trim();
		
		while (submitDir.endsWith("/")) // trim trailing slash(es)
			submitDir = submitDir.substring(0, submitDir.length()-1);
		
		submitDir = submitDir + "/" + assignment;
		if (!(fileExists(submitDir) && isDirectory(submitDir) && canWrite(submitDir)))
		{
			ui.showMessage("Professor's assignment directory not found for: " + assignment);
			return;
		}
		
		submitDir += "/" + ui.getUsername();
		if (!fileExists(submitDir))
			mkdir(submitDir);
		else if (!isDirectory(submitDir))
		{
			ui.showMessage("User's submit dir is a file");
			return;
		}

		ui.startProgressMonitor();
		if (sendFiles(localFiles, submitDir))
			ui.showMessage("Submission completed sucessfully!");
		ui.stopProgressMonitor();
	}
	
	public boolean sendFiles(File[] localFiles, String remoteDir)
	{
		String remoteFile = "";
		File localFile = null;
		try {
			for (int i = 0; i < localFiles.length; i++)
			{
				localFile = localFiles[i];
				remoteFile = remoteDir + "/" + localFile.getName();
				if (localFile.isDirectory()) {
					ui.progressUpdate(localFile.getPath() + "/");
					mkdir(remoteFile);
					sendFiles(localFile.listFiles(), remoteFile); // recurse
				}
				else
				{
					ui.progressUpdate(localFile.getPath());
					sendFile(localFile, remoteFile);
				}
			}
		} catch (Throwable t) {
			String err = "Error copying '" + localFile.getPath() + "' to '" + remoteFile + "':\n" + t.toString();
			System.out.print(err);
			ui.showMessage(err);
			return false;
		}
		return true;
	}
	
	public static String getHostname() {
		return HOSTNAME;
	}
	
	public static HostKey getHostkey() {
		return HOSTKEY;
	}
	
	public UserInterface getUserInterface() {
		return ui;
	}
	
	public void setUserInterface(UserInterface ui) {
		this.ui = ui;
	}
}
