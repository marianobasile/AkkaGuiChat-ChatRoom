package remote;
//====== GUI Packages ======
import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.SpringLayout;
import javax.swing.JTextArea;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import java.awt.Color;
import javax.swing.SwingConstants;
import javax.swing.ScrollPaneConstants;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.DefaultCaret;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JToolBar;

//====== Handling events ====== 
import java.awt.event.*;

//====== Mapping (GuiChat - Actor) ====== 
import akka.actor.ActorRef;

public class GuiChat extends JFrame {

	private JPanel contentPane;
	private JTextField username;
	private JLabel lblOnlineUsers;
	private JButton btnLogin;
	private JButton btnDisconnect;
	private JScrollPane scrollPane;
	private JTextArea room;
	private JScrollPane scrollPane_1;
	private JTextArea usersList;
	private JScrollPane scrollPane_2;
	private JTextArea clientInput;
	private JButton btnSend;
	private ActorRef communicator;
	private Messages messages;
	
	//toolbar for buttons
	JScrollPane scrollPane_3;
	JToolBar toolBar;
	
	public void setFocusOnUsername() {
		this.username.requestFocus();	
	}

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public JTextArea getRoom () {
		return this.room;
	}

	public JTextArea getUsersList() {
		return this.usersList;
	}

	public JTextField getUsername() {
		return this.username;
	}

	public JTextArea getClientInput() {
		return this.clientInput;
	}

	public void setBtnSend (boolean b) {
		this.btnSend.setEnabled(b);
	}

	public void setBtnDisconnect (boolean b) {
		this.btnDisconnect.setEnabled(b);
	}

	public void setBtnLogin (boolean b) {
		this.btnLogin.setEnabled(b);
	}

	public void setClientInput (boolean b) {
		this.clientInput.setEnabled(b);
	}

	public void setUsername (boolean b) {
		this.username.setEnabled(b);
	}

	public void setActorReference(ActorRef communicator) {
	    this.communicator = communicator;
	}

	public void setLoginFields (boolean b) {
		this.btnLogin.setEnabled(b);
		this.username.setEnabled(b);
	}

	private boolean corruptedInput(String content,int distinguish) {
		//A non-whitespace character
		//Both invoke on username field and on clientInput.
		//Used a distinguish parameter to avoid space in the username and new line at the beginnning in the clientInput.
     	String pattern = "\\s";
     	Pattern r = Pattern.compile(pattern);
     	Matcher m = r.matcher(content);

     	if(distinguish == 0) {
     		if (m.find() || content.equals("")) {
     			this.communicator.tell(messages.new InvalidInput(),null);
     			return true;
     		}
     	} else {
     		pattern = "^(\\s)";
     		r = Pattern.compile(pattern);
     		m = r.matcher(content);
     		if (content.equals("") || m.find()) {
     			this.communicator.tell(messages.new InvalidInput(),null);
     			return true;
     		}
     	}
      		return false; 	
	}

	private void login() {
				if(corruptedInput(username.getText(),0) )
					return;

      			if( this.username.getText().length() > 20 ) {
      				String name = this.username.getText().substring(0,20);
					this.username.setText(name);
      				this.communicator.tell(messages.new TrunckedUsername(name),null);
					
      			}
      			else
   					this.communicator.tell(messages.new LoginMessage(this.username.getText()),null);

   					this.btnLogin.setEnabled(false);
					this.username.setEnabled(false);		
	}

	public void initializeComponents() {
		usersList.setText("");
		Timer timer =  new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				username.setText("");				
				setClientInput(false);
				room.setText("");
					setLoginFields(true);
					room.setText("<SYSTEM>: CONNECTION ESTABLISHED!\nAVAILABLE COMMANDS:\nPress /query username to start a new Chat with the user having that username");					
  			}
		}, 3000);		
	}

	public void initializeComponentsNoTimeout() {

		usersList.setText("");	
		room.setText("");
		username.setText("");				
		setClientInput(false);
		room.setText("");
		setLoginFields(false);		
	}

	public GuiChat() {

		messages =  new Messages(); 
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Tell the server to disconnect the user
		this.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
						if(communicator == null)	System.exit(0);
                        communicator.tell(messages.new LogoutMessage(),null);
                }
                public void windowOpened( WindowEvent e ){
        				username.requestFocus();
    			}
        });

		setBounds(100, 100, 729, 742);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
		
		JLabel lblUsername = new JLabel("Username");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblUsername, 15, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblUsername, 10, SpringLayout.WEST, contentPane);
		lblUsername.setFont(new Font("Yu Gothic UI Semilight", Font.PLAIN, 15));
		contentPane.add(lblUsername);
		
		username = new JTextField("");
		username.setEnabled(false);
		username.setHorizontalAlignment(SwingConstants.CENTER);
		username.setFont(new Font("Yu Gothic UI Semilight", Font.PLAIN, 12));
		sl_contentPane.putConstraint(SpringLayout.EAST, lblUsername, -5, SpringLayout.WEST, username);
		sl_contentPane.putConstraint(SpringLayout.NORTH, username, 2, SpringLayout.NORTH, lblUsername);
		sl_contentPane.putConstraint(SpringLayout.WEST, username, 97, SpringLayout.WEST, contentPane);
		
		username.addActionListener( new ActionListener() {

			public void actionPerformed(ActionEvent e) {
					login();
			}
		});

		contentPane.add(username);
		username.setColumns(10);
		
		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.setEnabled(false);
		btnDisconnect.setForeground(new Color(255, 69, 0));
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnDisconnect, 12, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnDisconnect, 332, SpringLayout.WEST, contentPane);
		btnDisconnect.setFont(new Font("Yu Gothic UI Semilight", Font.BOLD, 14));

		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				clientInput.setEnabled(false);
				btnSend.setEnabled(false);
				room.setText(room.getText()+"\nDISCONNECTING...");
				communicator.tell(messages.new LogoutMessage(),null);
			}
		});

		contentPane.add(btnDisconnect);
		
		lblOnlineUsers = new JLabel("Online Users");
		sl_contentPane.putConstraint(SpringLayout.EAST, lblOnlineUsers, -30, SpringLayout.EAST, contentPane);
		lblOnlineUsers.setForeground(new Color(0, 0, 0));
		lblOnlineUsers.setFont(new Font("Yu Gothic UI Semilight", Font.BOLD, 14));
		contentPane.add(lblOnlineUsers);
		
		btnLogin = new JButton("Sign in");
		btnLogin.setEnabled(false);
		btnLogin.setForeground(new Color(50, 205, 50));
		sl_contentPane.putConstraint(SpringLayout.EAST, username, -6, SpringLayout.WEST, btnLogin);
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLogin, 12, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnLogin, -6, SpringLayout.WEST, btnDisconnect);
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				login();
			}
		});
		btnLogin.setFont(new Font("Yu Gothic UI Semilight", Font.BOLD, 14));
		contentPane.add(btnLogin);
		
		scrollPane = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 19, SpringLayout.SOUTH, btnDisconnect);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -150, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, -146, SpringLayout.EAST, contentPane);
		//no scroll horiz
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		contentPane.add(scrollPane);
		
		room = new JTextArea("");
		DefaultCaret caret = (DefaultCaret)room.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		room.setEditable(false);
		room.setFont(new Font("Yu Gothic UI Semilight", Font.PLAIN, 15));
		//a capo automatico
		room.setLineWrap(true);
		scrollPane.setViewportView(room);

		
		scrollPane_1 = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.SOUTH, lblOnlineUsers, -1, SpringLayout.NORTH, scrollPane_1);
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane_1, 0, SpringLayout.NORTH, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane_1, -5, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane_1, 6, SpringLayout.EAST, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane_1, -10, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane_1);
		
		usersList = new JTextArea("");
		//a capo automatico
		usersList.setLineWrap(true);
		usersList.setEditable(false);
		usersList.setFont(new Font("Yu Gothic UI Semilight", Font.BOLD, 13));
		scrollPane_1.setViewportView(usersList);
		
		//toolbar for JButtons
		JScrollPane scrollPane_3 = new JScrollPane();
		scrollPane_3.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		scrollPane_3.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane_3, 6, SpringLayout.SOUTH, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane_3, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane_3, 62, SpringLayout.SOUTH, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane_3, -146, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane_3);
		
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		scrollPane_3.setViewportView(toolBar);
		
		scrollPane_2 = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane_2, 6, SpringLayout.SOUTH, scrollPane_3);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane_2, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane_2, 0, SpringLayout.SOUTH, scrollPane_1);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane_2, -227, SpringLayout.EAST, contentPane);
		//no scroll horiz
		scrollPane_2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		contentPane.add(scrollPane_2);
		
		clientInput = new JTextArea("");
		clientInput.setFont(new Font("Yu Gothic UI Semilight", Font.PLAIN, 14));
		clientInput.setLineWrap(true);
		scrollPane_2.setViewportView(clientInput);
		//textAreaInput
		clientInput.setEnabled(false);
		clientInput.addKeyListener(new KeyListener() {
		@Override
  			public void keyPressed(KeyEvent evt) {
    			if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
    				evt.consume();
    				if(corruptedInput(clientInput.getText(),1) ){
    					return;
    				}
					String pattern = "/query ";
					if(clientInput.getText().toLowerCase().contains(pattern.toLowerCase()) && clientInput.getText().indexOf('/') == 0 ) {
						String nickname = clientInput.getText().substring(7).trim();
						if(!username.getText().equals(nickname))
							communicator.tell(messages.new Query(nickname),null);
						else 
							communicator.tell(messages.new ToPrintWarningMessage("<SYSTEM>: CAN'T START A CHAT WITH YOURSELF"), null);
					}
					else
						communicator.tell( messages.new RoutingMessage(clientInput.getText().trim()) , null);
						clientInput.setText("");
				}
  			}
  			public void keyReleased(KeyEvent keyEvent) {}
  			public void keyTyped(KeyEvent keyEvent) {}

		});
	
		btnSend = new JButton("Send");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnSend, 6, SpringLayout.SOUTH, scrollPane_3);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnSend, 0, SpringLayout.EAST, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnSend, 6, SpringLayout.EAST, scrollPane_2);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnSend, 82, SpringLayout.SOUTH, scrollPane_3);
		btnSend.setForeground(new Color(50, 205, 50));
		btnSend.setFont(new Font("Yu Gothic UI Semilight", Font.BOLD, 13));
		btnSend.setEnabled(false);
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
    				if(corruptedInput(clientInput.getText(),1) ){
    					return;
    				}
					String pattern = "/query ";
					if(clientInput.getText().toLowerCase().contains(pattern.toLowerCase()) && clientInput.getText().indexOf('/') == 0 ) {
						String nickname = clientInput.getText().substring(7).trim();
						if(!username.getText().equals(nickname))
							communicator.tell(messages.new Query(nickname),null);
						else 
							communicator.tell(messages.new ToPrintWarningMessage("<SYSTEM>: CAN'T START A CHAT WITH YOURSELF"), null);
					}
					else
						communicator.tell( messages.new RoutingMessage(clientInput.getText().trim()) , null);
						clientInput.setText("");
			}
		});
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnSend, 6, SpringLayout.SOUTH, scrollPane_3);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnSend, 0, SpringLayout.EAST, scrollPane);
		contentPane.add(btnSend);
	}
}

