package remote;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import java.io.Serializable;
import akka.actor.Terminated;
import akka.actor.ReceiveTimeout;
import akka.japi.Procedure;
import javax.swing.JTextArea;
import java.awt.Font;
import java.awt.Color;
import javax.swing.text.DefaultCaret;
import akka.actor.ActorContext;
import java.util.Date;
import java.text.SimpleDateFormat;
import akka.actor.PoisonPill;
import akka.actor.Kill;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import java.awt.Color;

 public class SubCommunicator extends UntypedActor {
 	private String receiverName;
 	private ActorRef fatherRef;
 	private JTextArea myRoom;
 	private Messages messages;
 	private ActorRef serverRef;
 	private ActorRef subServer;
	private GuiChat chat;
	private boolean isFirstTime = true;
	
	JButton chatButton;
	Color buttonAlertColor;
	Color buttonNormalColor;

 	public SubCommunicator(String receiverName,ActorRef serverRef, GuiChat chat) {
 		this.receiverName = receiverName;
 		this.fatherRef = context().parent();
 		this.serverRef = serverRef;
		this.chat = chat;
 		messages =  new Messages();
 		myRoom = new JTextArea("");
		
		DefaultCaret caret = (DefaultCaret)myRoom.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		myRoom.setEditable(false);
		myRoom.setFont(new Font("Yu Gothic UI Semilight", Font.PLAIN, 15));
		
		//NOT SET VISIBLE, BUT ONLY WHEN CLICK ON BUTTON for comm2, instead for comm1 set visible when receive login1to1 messages!
		myRoom.setLineWrap(true);

		//CREATION BUTTON 
		buttonAlertColor = Color.GREEN;
		buttonNormalColor = Color.BLACK;
		
		chatButton = new JButton(receiverName);
		chatButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
				chatButton.setForeground(buttonNormalColor);
				(chat.getScrollPane()).setViewportView(myRoom);
				
				fatherRef.tell(messages.new ChangeActiveRoom(myRoom), getSelf());
				chat.getClientInput().requestFocus();
			}
		});							
		
		writeToRoom("YOU ARE TALKING WITH "+this.receiverName );
		fatherRef.tell(messages.new ForkSuccessed(receiverName,myRoom),getSelf());
 	}

   private void writeToRoom(String logMessage) {

    SimpleDateFormat sdf = new SimpleDateFormat();
    sdf.applyPattern("HH:mm:ss");
    String currTime = sdf.format(new Date());
    if(isFirstTime){
      myRoom.setText(myRoom.getText() + "[" + currTime +"]: " + logMessage);
      isFirstTime = ! isFirstTime;
    }
    else
      myRoom.setText(myRoom.getText() + "\n[" + currTime +"]: " + logMessage);

   }
	
    //remove button from toolbar
   @Override
   public void postStop() {
		chat.toolBar.remove(chatButton);
		chat.toolBar.revalidate();
		chat.toolBar.repaint();
		writeToRoom("SYSTEM: DISCONNETTING...");

   }
	
 	@Override
        public void onReceive(Object message) throws Exception {
        	switch(message.getClass().getSimpleName()) {

			case "Start1To1":
								chat.toolBar.add(chatButton);
								serverRef.tell(messages.new Login1To1(fatherRef,receiverName),getSelf());
								break;

            case "AckLogin1To1":								
								subServer = ((Messages.AckLogin1To1)message).getSubServer();
								break;

            case "AckComm1To1":	
								//add button to the Toolbar (set visible for comm2)
								chat.toolBar.add(chatButton);
								subServer = ((Messages.AckComm1To1)message).getSubServer();
								subServer.tell( messages.new AckRequest1To1(),getSelf());
								break;
						
            case "PoisonPill":
                                //postStop()
                                break;

            case "Chat1To1Message":	
									subServer.tell( message , getSelf() );
									break;

			case "ToPrint1To1Message":
								//CHANGE BUTTON COLOR!!
								String tmp = ((Messages.ToPrint1To1Message)message).getContent();
								tmp = tmp.substring(1, tmp.indexOf(">") );

								if( tmp.equals(receiverName) ) 
									chatButton.setForeground(buttonAlertColor);
								if ( tmp.equals(chat.getUsername().getText()) )
									chatButton.setForeground(buttonNormalColor);
								
								writeToRoom(((Messages.ToPrint1To1Message)message).getContent());
								break;
									
            case "ReceiveTimeout":
								// ignore
							    	break;
      		default:
        		    unhandled(message);
                    break;
      	 }
      }
}