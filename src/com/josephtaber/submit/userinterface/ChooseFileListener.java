package com.josephtaber.submit.userinterface;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JTextField;

public class ChooseFileListener implements ActionListener
{
	private JTextField field;
	private Component parent;

	public ChooseFileListener(JTextField jTextField, Component p_parent)
	{
		field = jTextField;
		parent = p_parent;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		JFileChooser chooser = new JFileChooser();
		chooser.changeToParentDirectory();
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(true);
		
		chooser.showDialog(parent, "Select");

		// separate with semicolons, hopefully there aren't semicolons in the filenames
		String all = "";
		for (File file : chooser.getSelectedFiles())
			all += ";" + file.getAbsolutePath();
		field.setText(all.length() > 0 ? all.substring(1) : "");
	}
}
