package com.josephtaber.submit;

import com.josephtaber.submit.fileinterface.*;
import com.josephtaber.submit.userinterface.*;

/*
 * Command line usage:
 * 
 * Command line:
 *  - java submit cs1110 a9 file1 file2 ... fileN
 * 
 * Interactive:
 *  - java submit -i
 * 
 * Gui: (no args)
 *  - java
 */

/*
 *  .submit file syntax:
 *  [course] [submission directory]
 *  
 *  Example:
 *  cs6789 ~yue/cs6789/assgns/2012s/
 *  cs2800 ~curtisc/cs2800/assgns/2012f/
 *  
 */

public class Submit
{
	public static void main(String[] args)
	{
		FileInterface fi;
		UserInterface ui;

		// The check file is a read-only hidden file in my user folder.
		// If it exists then we must be logged in already.
		if (new java.io.File("/home/nfs/student/JAT58150/.submitCheckFile_14860_14134_27133").exists())
			fi = new LocalFileInterface();
		else
			fi = new RemoteFileInterface();
		
		if (args.length != 0 || java.awt.GraphicsEnvironment.isHeadless())
			ui = new CliInterface(args);
		else
			ui = new GuiInterface();
		
		fi.connect(ui);
		
		do
			fi.submit(ui.getSubmission());
		while (ui.promptContinueSubmit());
		
		System.exit(0);
	}
}
