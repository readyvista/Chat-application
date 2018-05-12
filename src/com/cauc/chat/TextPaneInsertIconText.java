package com.cauc.chat;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet.FontAttribute;
import javax.swing.text.StyledDocument;

import org.omg.CORBA.PRIVATE_MEMBER;

import javax.swing.JTextPane;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.io.File;
import java.security.KeyStore.Entry.Attribute;
import java.awt.event.ActionEvent;

public class TextPaneInsertIconText extends JFrame {
    private StyledDocument doc=null;
    private JPanel contentPane;
    private JTextPane textPane;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TextPaneInsertIconText frame = new TextPaneInsertIconText();
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
	public TextPaneInsertIconText() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		textPane = new JTextPane();
		contentPane.add(textPane, BorderLayout.CENTER);
		
		JButton btnInsertImage = new JButton("\u5411TextPane\u4E2D\u63D2\u5165\u56FE\u7247");
		btnInsertImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doc=textPane.getStyledDocument();
				JFileChooser jChooser=new JFileChooser();
				jChooser.showOpenDialog(null);
				insertIcon(jChooser.getSelectedFile());
				
			}
		});
		contentPane.add(btnInsertImage, BorderLayout.SOUTH);
		
		
		
	}
	
	private void insertIcon(File file) {
		textPane.setCaretPosition(doc.getLength());
		textPane.insertIcon(new ImageIcon(file.getPath()));
	}
	
	
}
