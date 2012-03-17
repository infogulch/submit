package com.josephtaber.submit.fileinterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;

import com.josephtaber.submit.userinterface.UserInterface;

public class LocalFileInterface extends FileInterface
{
	public boolean fileExists(String file)
	{
		return new File(file).exists();
	}
	
	public boolean canRead(String file)
	{
		return new File(file).canRead();
	}
	
	public boolean canWrite(String file)
	{
		return new File(file).canWrite();
	}

	public boolean canExecute(String file) {
		return new File(file).canExecute();
	}
	
	public boolean isDirectory(String file)
	{
		return new File(file).isDirectory();
	}
	
	public void connect(UserInterface p_ui)
	{
		ui = p_ui;
	}
	
	public String readFile(String fileName)
	{
		char[] buf = null;
		File file = new File(fileName);
		if (!file.exists())
			return null;
		try
		{
			buf = new char[(int) file.length()];
			new FileReader(file).read(buf);
		}
		catch (Throwable t)
		{
			System.out.println("Error reading file '" + fileName + "': " + t.toString());
			return null;
		}
		return new String(buf);
	}
	
	public void sendFile(File localFile, String remotePath) throws IOException {
		File remoteFile = new File(remotePath);
	    if(!remoteFile.exists()) {
	        remoteFile.createNewFile();
	    }

	    FileChannel source = null, destination = null;

	    try {
	        source = new FileInputStream(localFile).getChannel();
	        destination = new FileOutputStream(remoteFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null && source.isOpen())
	            source.close();
	        if(destination != null && source.isOpen())
	            destination.close();
	    }
	}
	
	public void mkdir(String remoteDir) {
		new File(remoteDir).mkdirs();
	}
}
