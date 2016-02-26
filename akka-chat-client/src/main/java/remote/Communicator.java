package remote;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import java.io.Serializable;
import akka.actor.ActorIdentity;
import akka.actor.Identify;
import akka.actor.Terminated;
import akka.actor.ReceiveTimeout;
import akka.japi.Procedure;
import static java.util.concurrent.TimeUnit.SECONDS;
import scala.concurrent.duration.Duration;
import com.typesafe.config.ConfigFactory;
import java.util.Date;
import java.text.SimpleDateFormat;
import akka.actor.ActorContext;
import java.util.HashMap;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.util.Map;
import java.util.Iterator;
import akka.actor.PoisonPill;
import akka.actor.Kill;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;

    public class Communicator extends UntypedActor {

        private GuiChat chat;
        private final String path;
        private ActorRef remoteActor = null;
        private HashMap <JTextArea,Room> rooms;
        private JTextArea activeRoom;
        private int idNumber;
        private JTextArea chatRoom;
		
		private JButton roomButton;
		Color buttonAlertColor;
		Color buttonNormalColor;
		boolean startedComm;
		
        public Communicator(String path, GuiChat chat, ActorSystem system) {
            this.chat = chat;
            this.path = path;
            chatRoom = this.chat.getRoom();
            this.rooms = new HashMap<JTextArea,Room>();
            activeRoom = chatRoom;
            idNumber = 0;			
			
			buttonAlertColor = Color.GREEN;
			buttonNormalColor = Color.BLACK;
			startedComm = false;
            sendIdentifyRequest();
        }

        private void sendIdentifyRequest() {
            getContext().actorSelection(path).tell(new Identify(path), getSelf());
            getContext().system().scheduler().scheduleOnce(Duration.create(3, SECONDS), getSelf(),ReceiveTimeout.getInstance(), getContext().dispatcher(), getSelf());
        }
        
        private void writeToRoom(String logMessage) {
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("HH:mm:ss");
            String currTime = sdf.format(new Date());
            this.chat.getRoom().setText(this.chat.getRoom().getText() + "\n[" + currTime +"]: " + logMessage);
        }

        private void writeToUsersList(String list) {
            this.chat.getUsersList().setText(list);
        }

        private String mark1To1User(String actualUsers) {
            String[] users = actualUsers.split("\n");        

            for(int i = 0; i<users.length;i++){
                for(Room r : rooms.values()){
                    if( ("<"+r.getOpponentNickname()+">").equals(users[i])){
                        users[i] = users[i] + " [C]";
                        
                        break; 
                    }
                }
                    
            }             
            String temp = new String();
                for(String user : users)
                    temp += user+"\n";     
            
            return temp;

        }

		private void finalizeLogout(boolean crashed){
			
			activeRoom = chatRoom;
			JScrollPane scrollP = chat.getScrollPane();
			scrollP.setViewportView(activeRoom); 
			chat.setBtnDisconnect(false);
            chat.getUsername().requestFocus();
			if(crashed)
				chat.initializeComponentsNoTimeout();
			else
				chat.initializeComponents();
			
			//remove button from toolbar
			chat.toolBar.remove(roomButton);
			chat.toolBar.revalidate();
			chat.toolBar.repaint();
										
			ActorRef child;
			Iterator <Map.Entry<JTextArea,Room>> it = rooms.entrySet().iterator();
			
			while (it.hasNext()) {
				
				Map.Entry<JTextArea,Room> entry = it.next();
				child = entry.getValue().getSubCommRef();
				it.remove();

				child.tell(PoisonPill.getInstance(), null);
			}	
		}
		
        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof ActorIdentity) {
                remoteActor = ((ActorIdentity) message).getRef();
                if (remoteActor == null) {
                    System.out.println("Remote actor not available: " + path);
                } else {
                    this.chat.getRoom().setText("<SYSTEM>: CONNECTION ESTABLISHED!\nAVAILABLE COMMANDS:\nPress \"/query username\" to start a new Chat with the user");
                    this.chat.setLoginFields(true);
                    getContext().watch(remoteActor);
                    getContext().become(active, true);
                 }
            } else if (message instanceof ReceiveTimeout) {
                sendIdentifyRequest();
            } else {
                writeToRoom("Not ready yet");
            }
        }

        Procedure<Object> active = new Procedure<Object>() {
        @Override
            public void apply(Object message) {
              
                switch(message.getClass().getSimpleName()) {

                    case "LoginMessage": 
										remoteActor.tell(message,getSelf());
										break;
               
                    case "RejectLogin": 
                                        writeToRoom(((Messages.RejectLogin)message).getCause());
                                        chat.setBtnLogin(true);
                                        chat.setUsername(true);
                                        chat.setFocusOnUsername();
                                        break;

                    case "AckLogin": 
										writeToRoom("<SYSTEM>: LOGGED IN!");
										chat.setClientInput(true);
										chat.setBtnSend(true);
										chat.setBtnDisconnect(true);
										String actualUsers = ((Messages.AckLogin)message).getActualUsers();
										chat.getClientInput().requestFocus();
										writeToUsersList(actualUsers);

										//room button
										roomButton = new JButton("Room");
										roomButton.addActionListener(new ActionListener() {
											public void actionPerformed(ActionEvent e) {
												activeRoom = chatRoom;
												roomButton.setForeground(buttonNormalColor);
												JScrollPane scrollPane = chat.getScrollPane();
												scrollPane.setViewportView(activeRoom);
												chat.getClientInput().requestFocus();
											}
										});
										chat.toolBar.add(roomButton);
										break;

                    case "UpdateUsrList":
										String availableUsers = ((Messages.UpdateUsrList)message).getActualUsers();
										writeToUsersList(mark1To1User(availableUsers));
										writeToRoom(((Messages.UpdateUsrList)message).getChangedStatusUser());
										break;

                    case "TrunckedUsername":
										String nickname = ((Messages.TrunckedUsername)message).getNickname();
										writeToRoom("<SYSTEM>: REACHED USERNAME MAXLENGTH.\nYOUR USERNAME: "+nickname);
										remoteActor.tell(new Messages().new LoginMessage(nickname),getSelf());
										break;
											
                    case "InvalidInput":
                                        //writeToRoom("<SYSTEM>: INVALID INPUT! PLEASE TRY AGAIN.");
                                        break; 
										
                    case "Query":   
										String destName = ((Messages.Query)message).getNickname();
										boolean found = false;
										
										if(destName.equals("room")){
											activeRoom = chatRoom;
											JScrollPane scrollPane = chat.getScrollPane();
											scrollPane.setViewportView(activeRoom);
											break;
										}	
										else {
											 Iterator <Map.Entry<JTextArea,Room>> iterator = rooms.entrySet().iterator();
											 while (iterator.hasNext()) {
												Map.Entry<JTextArea,Room> entry = iterator.next();
												if(entry.getValue().getOpponentNickname().equals(destName)){
													activeRoom = entry.getKey();
													JScrollPane scrollPan = chat.getScrollPane();
													scrollPan.setViewportView(activeRoom);
													found = true;
													break;
												}
											}
										}   
										if(!found)
										remoteActor.tell(new Messages().new CheckForUser(destName),getSelf());                                  
                                        break;
										
                    case "NotFound":
										activeRoom = chatRoom;
										JScrollPane s = chat.getScrollPane();
										s.setViewportView(activeRoom); 
										writeToRoom("<SYSTEM>: USER NOT AVAILABLE.");
										break;

                    case "FoundIt":
										String receivName = ((Messages.FoundIt)message).getNickname();
										ActorRef mySubCom = getContext().actorOf(Props.create(SubCommunicator.class,receivName,remoteActor,chat), "SubCommunicator"+idNumber++);
										writeToUsersList(mark1To1User(chat.getUsersList().getText()));
										mySubCom.tell( new Messages().new Start1To1(), getSelf());
										//comm1 is starting a chat
										startedComm=true;
										break;

                    case "ForkSuccessed":   
										String receiverName = ((Messages.ForkSuccessed)message).getOpponentName();
										JTextArea subCommRoom = ((Messages.ForkSuccessed)message).getSubCommRoom();
										ActorRef subComm = getSender();
										rooms.put(subCommRoom,new Room(subComm,receiverName));
										writeToUsersList(mark1To1User(chat.getUsersList().getText()));
										//switch only if comm1!
										if(startedComm){
											activeRoom = subCommRoom;
											JScrollPane scrollPane = chat.getScrollPane();
											scrollPane.setViewportView(activeRoom); 
											startedComm = false;
										}
										break;

                    case "Request1To1": 
                                        String senderName = ((Messages.Request1To1)message).getNicknameSender();
                                        ActorRef myChild = getContext().actorOf(Props.create(SubCommunicator.class, senderName, remoteActor, chat), "SubCommunicator"+idNumber++);
										//initialize subserver
										myChild.tell(new Messages().new AckComm1To1( getSender() ), getSelf());
                                        break; 
					
											
					case "ChangeActiveRoom": //switch the activeRoom when click on a chatButton!
										activeRoom = ((Messages.ChangeActiveRoom)message).getChatRoom();
										break;
										
                    case "RoutingMessage":
										String userMsg = ((Messages.RoutingMessage)message).getContent();
										
										if(activeRoom == chatRoom)
											getSelf().tell(new Messages().new ChatMessage(userMsg),getSelf());
										else {
											ActorRef targetSubCommRef = rooms.get(activeRoom).getSubCommRef();
											targetSubCommRef.tell(new Messages().new Chat1To1Message(userMsg),getSelf());
										}   
										break;
                    case "ChatMessage":
										remoteActor.tell(message, getSelf());
                                        break;

                    case "ToPrintMessage":
                                        String msgToPrint = ((Messages.ToPrintMessage)message).getContent();
                                        String  tmp = msgToPrint.substring(1, msgToPrint.indexOf(">") );
                                        
                                        if ( tmp.equals(chat.getUsername().getText()) )
                                            roomButton.setForeground(buttonNormalColor);
                                        else
                                            roomButton.setForeground(buttonAlertColor);

                                        writeToRoom(msgToPrint);
                                        break;

                    case "ToPrintWarningMessage":
										activeRoom = chatRoom;
										JScrollPane p = chat.getScrollPane();
										p.setViewportView(activeRoom); 
										String wrnmsg = ((Messages.ToPrintWarningMessage)message).getContent();
										writeToRoom(wrnmsg);
										break;

                    case "LogoutMessage":   
                                        remoteActor.tell(message,getSelf());
                                        break;                        
										
                    case "ToKillMessage":   
										ActorRef subCommToKill = ((Messages.ToKillMessage)message).getSubCommToKill();

										Iterator <Map.Entry<JTextArea,Room>> iteratatore = rooms.entrySet().iterator();
										while (iteratatore.hasNext()) {
											Map.Entry<JTextArea,Room> entry = iteratatore.next();
											if(entry.getValue().getSubCommRef() == subCommToKill) {
											   iteratatore.remove();
												break;
											}
										}
										activeRoom = chatRoom;
										JScrollPane scrolP = chat.getScrollPane();
										scrolP.setViewportView(activeRoom); 
										
										subCommToKill.tell(PoisonPill.getInstance(), null);
										break;  

                    case "AckLogout":	
                                        finalizeLogout(false);	//logout (not crashed)
										break;

                    case "Bye":
										String leftUser =((Messages.Bye)message).getNickname();
										writeToRoom("<SYSTEM>: "+ leftUser + " HAS LEFT THE CHAT");
										String connectedUsers = ((Messages.Bye)message).getActualUsers();
										writeToUsersList(mark1To1User(connectedUsers));
										break;

                    case "Ping":    
										remoteActor.tell(new Messages().new Pong(),getSelf());
										break;

                    case "Terminated":
										finalizeLogout(true);	//crashed
                                        writeToRoom("<SYSTEM>: SERVER CRASHED!!!");
                                        chat.setBtnDisconnect(false);
                                        chat.setClientInput(false);
                                        chat.setBtnSend(false);
                                        writeToUsersList("");
                                        sendIdentifyRequest();
                                        getContext().unbecome();
                                        break;

                    case "ReceiveTimeout":
                                        //ignore
                                        break;     
                                
                    default: 
										unhandled(message);
										break;    
                }    
            }
    };
    	public static void main(String[] args) {

            GuiChat frame = new GuiChat();
            frame.setSize(900,600);
            frame.setMinimumSize(new Dimension(500, 400));
		    frame.setVisible(true);
		    frame.setLocationRelativeTo(null);
		    frame.setTitle("Remote Chat with Akka");
		    

            final ActorSystem system = ActorSystem.create("ClientChatSystem", ConfigFactory.load("remotelookup"));

            final String path = "akka.tcp://ChatSystem@127.0.0.1:2552/user/remoteActor";
            //MARIANO
            final ActorRef communicator = system.actorOf(Props.create(Communicator.class, path,frame,system), "Communicator");
            
            frame.setActorReference(communicator);

    }
}

class Room {
  private ActorRef subCommRef;
  private String opponentNickname;

  public Room(ActorRef subCommRef,String opponentNickname) {
    this.subCommRef = subCommRef;
    this.opponentNickname = opponentNickname;
  }

  public ActorRef getSubCommRef() {
    return subCommRef;
  }

  public String getOpponentNickname() {
    return opponentNickname;
  }
}

