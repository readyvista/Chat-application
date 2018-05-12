package com.cauc.chat;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.omg.CORBA.PUBLIC_MEMBER;

import java.awt.GridLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import java.awt.FlowLayout;
import java.awt.CardLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.event.ActionEvent;

public class ImageShowTest extends JFrame {

	private JPanel contentPane;
	private JPanel panel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ImageShowTest frame = new ImageShowTest();
					frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ImageShowTest() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 703, 473);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		panel = new JPanel();
		scrollPane.setViewportView(panel);
		panel.setLayout(new GridLayout(0, 2, 0, 0));
		
		JButton btnNewButton = new JButton("New button");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser=new JFileChooser();
				fileChooser.showOpenDialog(null);
				File file=fileChooser.getSelectedFile();
				ImageIcon imageIcon=new ImageIcon(file.getPath());
				new Thread(new ImageShowing(imageIcon)).start();
				
			}
		});
		contentPane.add(btnNewButton, BorderLayout.SOUTH);
		
		
		
	}
	public void showImage(){
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				JLabel label=new JLabel(imageIcon);
				JLabel label_null=new JLabel();
				panel.add(label_null);
				panel.add(label);
			}
		});
	}
	class ImageShowing implements Runnable{
		ImageIcon imageIcon;
		public ImageShowing(ImageIcon imageIcon) {
			// TODO Auto-generated constructor stub
			this.imageIcon=imageIcon;
		}
		@Override
		public void run() {
			
		}
	}
}
