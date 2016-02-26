package remote;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Color;
import javax.swing.text.DefaultCaret;
import java.awt.event.*;
import akka.actor.ActorRef;

public class GuiServer extends JFrame {

	private JPanel contentPane;
	private JTextArea log;
	private ActorRef serverActor;
	private Messages messages;

	//UTILITY FUNCTIONS
	public void printBootstrapMessage() {
		log.setText("CHAT SERVICE HAS BEEN SUCCESSUFULY STARTED\n");
	}

	public JTextArea getLog() {
		return this.log;
	}

	public void setActorReference(ActorRef serverActor) {
	    this.serverActor = serverActor;
	}

	//GUI CREATION
	public GuiServer() {
		messages = new Messages();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 596, 601);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
		
		JLabel lblStatus = new JLabel("STATUS");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblStatus, 0, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblStatus, 255, SpringLayout.WEST, contentPane);
		lblStatus.setForeground(new Color(50, 205, 50));
		lblStatus.setFont(new Font("Yu Gothic UI Semilight", Font.BOLD, 15));
		contentPane.add(lblStatus);
		
		JScrollPane scrollPane = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 4, SpringLayout.SOUTH, lblStatus);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 15, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -5, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, -15, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane);
		
		log = new JTextArea();
		log.setLineWrap(true);
		DefaultCaret caret = (DefaultCaret)log.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		log.setEditable(false);
		log.setFont(new Font("Yu Gothic UI Semilight", Font.PLAIN, 15));
		log.setForeground(Color.BLACK);
		scrollPane.setViewportView(log);
	}
}
