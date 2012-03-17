package com.josephtaber.submit.fileinterface;

import java.io.File;
import java.io.InputStream;
import com.jcraft.jsch.*;
import com.josephtaber.submit.userinterface.UserInterface;

public class RemoteFileInterface extends FileInterface {
	private JSch jsch;
	private Session session;
	private ChannelSftp sftp;
	
	private static int uid = 0;
	private static java.util.List<Integer> groups = new java.util.LinkedList<Integer>();
	private static String home;
	
	public void connect(UserInterface p_ui) {
		ui = p_ui;
		ui.promptCredentials();

		jsch = new JSch();
		jsch.getHostKeyRepository().add(HOSTKEY, null);

		while (true) {
			try {
				session = jsch.getSession(ui.getUsername(), HOSTNAME, 22);
				session.setConfig("StrictHostKeyChecking", "yes");
				session.setPassword(ui.getPassword());
				// session.setConfig("compression...
				session.connect();

				sftp = (ChannelSftp) session.openChannel("sftp");
				sftp.connect();

				getPosixPermissions();
			} catch (Throwable t) {
				if (sftp != null && sftp.isConnected())
					sftp.disconnect();
				if (session != null && session.isConnected())
					session.disconnect();
				if (t.toString().endsWith("Auth fail")
						&& ui.promptYesNo("Invalid username/password. Continue?")
						&& ui.promptCredentials())
					continue;
				System.exit(0);
			}
			break;
		}
	}
	
	protected void finalize() {
		if (sftp != null && sftp.isConnected())
			sftp.disconnect();
		if (session != null && session.isConnected())
			session.disconnect();
	}
	
	private void getPosixPermissions() throws SftpException {
		uid = Integer.parseInt(exec("id -u").trim());

		for (String g : exec("id -G").split(" "))
			groups.add(Integer.parseInt(g.trim()));
		
		home = sftp.getHome();
	}
	
	private String exec(String command) {
		StringBuilder ret = new StringBuilder();
		ChannelExec exec = null;
		try {
			exec = (ChannelExec) session.openChannel("exec");
			exec.setCommand(command);
			exec.setInputStream(null);
			exec.setErrStream(System.err);
			InputStream in = exec.getInputStream();

			exec.connect();

			int len;
			byte[] buf = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					len = in.read(buf, 0, buf.length);
					if (len < 0)
						break;
					ret.append(new String(buf, 0, len));
				}
				if (exec.isClosed())
					break;
				Thread.sleep(100);
			}
			exec.disconnect();
		} catch (Throwable t) {
			if (exec != null && exec.isConnected())
				exec.disconnect();
			t.printStackTrace();
			return null;
		}
		return ret.toString();
	}
	
	private String expandFile(String path) {
		path = path.replace("~/", home + "/");
		path = path.replace("~", home.substring(0, home.lastIndexOf("/")) + "/");
		return path;
	}
	
	private SftpATTRS getAttrs(String path) {
		try {
			return sftp.lstat(expandFile(path));
		} catch (SftpException e) {
			e.printStackTrace();
			System.out.println("Get attrs: " + path + " Error:\n" + e.toString());
			return null;
		}
	}
	
	public String readFile(String file) {
		file = expandFile(file);
		
		SftpATTRS attr = getAttrs(file);
		if (attr == null)
			return null;
		
		StringBuilder ret = new StringBuilder((int) attr.getSize());
		try {
			int bufsz = 1024;
			byte[] buf = new byte[bufsz];
			InputStream is = sftp.get(file);
			int len;
			while ((len = is.read(buf, 0, bufsz)) > 0)
				ret.append(new String(buf, 0, len));
			is.close();
		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println("Reading file: '" + file + "' Error 2: \n" + e.toString());
			return null;
		}
		return ret.toString();
	}
	
	public boolean fileExists(String file) {
		return getAttrs(file) != null;
	}
	
	public boolean isDirectory(String file) {
		SftpATTRS attrs = getAttrs(file);
		return attrs != null && attrs.isDir();
	}
	
	public boolean canRead(String file) {
		SftpATTRS attrs = getAttrs(file);
		return attrs != null && havePermissions(attrs, "r");
	}
	
	public boolean canWrite(String file) {
		SftpATTRS attrs = getAttrs(file);
		return attrs != null && havePermissions(attrs, "w");
	}
	
	public boolean canExecute(String file) {
		SftpATTRS attrs = getAttrs(file);
		return attrs != null && havePermissions(attrs, "x");
	}
	
	private boolean havePermissions(SftpATTRS attrs, String rwx) {
		int ownid = attrs.getUId(), grpid = attrs.getGId(), b = 0, p = attrs.getPermissions();
		if (rwx.contains("r"))
			b += 4;
		if (rwx.contains("w"))
			b += 2;
		if (rwx.contains("x")) 
		    b += 1;
		return b != 0 && (ownid == uid && (p & b << 6) >> 6 == b || groups.contains(grpid) && (p & b << 3) >> 3 == b || (p & b) == b);
	}
	
	public void sendFile(File localFile, String remoteFile) throws SftpException {
		sftp.put(localFile.getAbsolutePath(), expandFile(remoteFile), null);
	}
	
	public void mkdir(String remoteDir) {
		remoteDir = expandFile(remoteDir);
		try {
			sftp.mkdir(remoteDir);
		} catch (SftpException e) {
			System.out.print("Error making dir '" + remoteDir + "':\n" + e.toString());
		}
	}
}
