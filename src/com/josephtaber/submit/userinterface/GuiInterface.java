package com.josephtaber.submit.userinterface;

import java.awt.Dimension;

import javax.swing.*;

public class GuiInterface implements UserInterface
{
	private String password;
	private String username;
	
	public String getPassword()
	{
		return password;
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public String[] getSubmission()
	{
		return promptInputs("Submission"
				, new String[] {"Course", "Assignment", "Files, separated by ';'"}
				, new int[] {UserInterface.INPUT_ECHO, UserInterface.INPUT_ECHO, UserInterface.INPUT_FILE}
				, new String[] {"", "", ""});
	}
	
	public boolean promptCredentials()
	{
		String[] x = promptInputs("Enter credentials", new String[] { "Username", "Password" }, new int[] { UserInterface.INPUT_ECHO, UserInterface.INPUT_NO_ECHO }, new String[] { username, "" });
		if (x == null)
			return false;
		username = x[0];
		password = x[1];
		return true;
	}
	
	public String promptInput(String message)
	{
		return promptInput(message, UserInterface.INPUT_ECHO);
	}
	
	public String promptInput(String message, int type)
	{
		return promptInputs(message, new String[] { "" }, new int[] { type }, new String[] { "" })[0];
	}
	
	public String[] promptInputs(String instruction, String[] prompts, int[] type, String[] initial)
	{
		JPanel panel  = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JTextField[] fields = new JTextField[prompts.length];
		int fieldColumns = 20;
		for (int i = 0; i < prompts.length; i++)
		{
			JPanel subp = new JPanel();
			subp.setLayout(new BoxLayout(subp, BoxLayout.LINE_AXIS));
			subp.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
			
			JLabel label = new JLabel(prompts[i] + ":");
			fields[i] = (type[i] == UserInterface.INPUT_NO_ECHO ? new JPasswordField(fieldColumns) : new JTextField(fieldColumns));
			fields[i].setText(initial[i]);
			fields[i].setMaximumSize(fields[i].getPreferredSize());
			
			subp.add(label);
			subp.add(Box.createHorizontalGlue());
			subp.add(Box.createRigidArea(new Dimension(5, 0)));
			subp.add(fields[i]);
			if (type[i] == UserInterface.INPUT_FILE)
			{
				JButton button = new JButton("...");
				fields[i].setColumns(fieldColumns - 3);
				button.addActionListener(new ChooseFileListener(fields[i], panel));
				button.setPreferredSize(new Dimension(new JTextField(fieldColumns).getPreferredSize().width - fields[i].getPreferredSize().width - 6, fields[i].getMaximumSize().height));
				subp.add(Box.createRigidArea(new Dimension(5, 0)));
				subp.add(button);
			}
			panel.add(subp);
		}
		fields[0].addAncestorListener(new RequestFocusListener());
		int result = JOptionPane.showConfirmDialog(null, panel, instruction, JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION)
			return null;
		String[] strings = new String[prompts.length];
		for (int i = 0; i < prompts.length; i++)
			strings[i] = fields[i].getText();
		return strings;
	}
	
	public boolean promptPassword(String message)
	{
		String ret = promptInput(message, UserInterface.INPUT_NO_ECHO);
		if (ret == null)
			return false;
		password = ret;
		return true;
	}
	
	public boolean promptUsername(String message)
	{
		String ret = promptInput(message, UserInterface.INPUT_ECHO);
		if (ret == null)
			return false;
		username = ret;
		return true;
	}
	
	public boolean promptYesNo(String str)
	{
		Object[] options = { "Yes", "No" };
		return 0 == JOptionPane.showOptionDialog(null, str, "", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
	}
	
	public void showMessage(String message)
	{
		JOptionPane.showMessageDialog(null, message);
	}
	
	public boolean promptContinueSubmit()
	{
		return promptYesNo("Submit another assignment?");
	}

	public void showInvalidInputMessage(String message) {
		showMessage(message);
	}

	public void startProgressMonitor() {
		// TODO Auto-generated method stub
	}
	
	public void progressUpdate(String fileName) {
		// TODO
	}

	public void stopProgressMonitor() {
		// TODO Auto-generated method stub
	}
}