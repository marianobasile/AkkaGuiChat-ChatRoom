
package remote;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import java.util.HashMap;
import java.util.Date;
import java.util.Arrays;
import java.text.SimpleDateFormat;
import akka.actor.Props;
import scala.concurrent.duration.Duration;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Iterator;
import akka.actor.ActorContext;
import akka.actor.Kill;
import akka.actor.PoisonPill;
import java.util.LinkedList;
import java.lang.Iterable; 
import java.util.ListIterator;

public class RemoteChatServiceActor extends UntypedActor {
  
	private GuiServer chat;
	private HashMap <ActorRef,Client> users;
	private Messages messages;
	private HashMap <ActorRef,LinkedList<ActorRef>> subServers;
	private int nextSS;
	
	/*CONSTRUCTOR*/
	public RemoteChatServiceActor(GuiServer chat) {
		this.chat = chat;
		messages = new Messages();
		users = new HashMap<>();
		subServers = new HashMap<>();
		nextSS=0;

		ping();
	}

	/*CLIENT CRASH DETECTION SERVICE*/
	private void ping() {
		Timer timer =  new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				ActorRef crashed=null;
				
				/*Update available clients --> it controls if a client requested the ping*/
				Iterator <Map.Entry<ActorRef,Client>> iterator = users.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<ActorRef,Client> entry = iterator.next();
					boolean alive = entry.getValue().getStatus();
					String clientName = entry.getValue().getClientName();
					
					if( alive == false ){
						crashed=entry.getKey();
						writeToLog(clientName + " SUDDENLY CRASHED "); 
						iterator.remove();

						/*Adverting subServers that a client has crashed sending a discover request to obtain all the clients that were talking with it*/
						if(subServers.containsKey(crashed) && subServers.get(crashed)!=null){
							
							ListIterator<ActorRef> i = subServers.get(crashed).listIterator();
							while(i.hasNext()){
								i.next().tell(messages.new DiscoverRequest(crashed), getSelf());
							}
							/*remove from the hashMap the subserver list refered to the crashed client*/
							subServers.remove(crashed);
						}
						/*Tell all the clients that the crashed client is logged out*/
						for (ActorRef user : users.keySet())
							user.tell(messages.new Bye(clientName, getAvailableUsers().trim()),getSelf());
					}   
				}
				/*start a new ping-pong session*/
				for (Client client : users.values())
					   client.setStatus(false);
				for (ActorRef user : users.keySet())
						user.tell(messages.new Ping(),getSelf());
			}
		}, 0, 20000); 
	}

	/*BEHAVIOUR*/
	@Override
	public void onReceive(Object message) {
	  
	  switch(message.getClass().getSimpleName()) {

		case "LoginMessage": 
							handleLoginMessage(message); 
							break;

		case "ChatMessage": 
							handleChatMessage(message);  
							break;
		
		case "CheckForUser":/*seatch if the an user is logged in*/
							handleCheckForUser(message);
							break;
						
		case "Login1To1":
							handleLogin1To1(message);
							break;
				
		case "LogoutMessage":
							handleLogoutMessage(message);
							break;		  

		case "DiscoverReply":/*receive the communicator reference that belongs to a chat 1to1 that is gone down*/
							handleDiscoverReply(message);
							break;
							
		case "Pong":/*reply that the user is not crashed*/
							handlePongMessage(message);
							break;
					
		case "LogMessage":/*subserver log message*/
							handleLogMessage(message);
							break;	

		default: 
							writeToLog("Messaggio non riconosciuto!...");
							break;						
	  }
	}
		
	/*UTILITY FUNCTIONS*/
	private void writeToLog(String logMessage) {
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern("HH:mm:ss");
		String currTime = sdf.format(new Date());
		this.chat.getLog().setText(this.chat.getLog().getText() + "\n[" + currTime +"]: "+ logMessage);
	}
	
	/*Handling User-Hashmap*/
	private String[] makeAvailableUser(){
	  
		String[] availableUsers = new String[users.size()];
		int i=0;
		for (Client connectedUser : users.values())
			availableUsers[i++] = connectedUser.getClientName();
		  
		return availableUsers;
	}

	private String getAvailableUsers() {

		String userList = new String();
		String[] availableUsers = makeAvailableUser();
		Arrays.sort(availableUsers);

		for (String userName : availableUsers)
			userList += "<" + userName + ">" +"\n";

		return userList;
	}
	
	/*HANDLING MESSAGES*/
	private void handleLoginMessage(Object message) {
	  
		String name = ((Messages.LoginMessage)message).getNickname();
		String[] availableUsers = makeAvailableUser();

		if(Arrays.asList(availableUsers).contains(name)){ 
			writeToLog("NICKNAME ALREADY USED "+name);
			getSender().tell(messages.new RejectLogin("<SYSTEM>: NICKNAME ALREADY USED!"),getSelf());
		}
		else{
			writeToLog(name+" LOGGED IN");
			users.put(getSender(),new Client(name,true));
			getSender().tell(messages.new AckLogin(getAvailableUsers().trim()),getSelf());
			for (ActorRef user : users.keySet()){
				if ( user == getSender())
					continue;
				user.tell(messages.new UpdateUsrList(getAvailableUsers().trim(), "<SYSTEM>: <"+users.get(getSender()).getClientName()+"> LOGGED IN!" ),getSelf());
			}	
		}
	}
	
	private void handleChatMessage(Object message){
	 
		writeToLog(users.get(getSender()).getClientName()+" SENT A MESSAGE");

		Messages.ToPrintMessage toPrint = messages.new ToPrintMessage("<" + users.get(getSender()).getClientName()+">: "+((Messages.ChatMessage)message).getContent().trim());

		for(ActorRef user : users.keySet())
			user.tell(toPrint, getSelf()); 
	}

	private void handleCheckForUser(Object message){
		
		boolean found=false;
		/*check if the users that comm1 wants to contact is logged!*/
		for (Client connectedUser : users.values())
			
			if( connectedUser.getClientName().equals( ((Messages.CheckForUser)message).getNickname() ) ){
				
				getSender().tell( messages.new FoundIt( connectedUser.getClientName() ), getSelf() );
				found=true;
				break;								
				}
		if(!found)
			getSender().tell(messages. new NotFound(), getSelf() );
	}
	
	private void handleLogin1To1(Object message) {
				
		ActorRef father = ((Messages.Login1To1)message).getFather();	
		String 	dest = ((Messages.Login1To1)message).getDest();
		ActorRef destCommunicator = null;
		ActorRef SubServer = null;
		boolean allow = true;
		
		  /*research destCommunicator to pass it to the subServer*/
		for (ActorRef tmpComm : users.keySet())
			if(users.get(tmpComm).getClientName().equals(dest)) {
				destCommunicator = tmpComm;
				break;	
			}
			
		/*check if the connection is yet in creation --> very unluky case in witch 2 client require the chat in the "same" moment*/
		if(subServers.get(destCommunicator)!=null && subServers.get(getSender())!=null){
			
			Iterator<ActorRef> i = subServers.get(getSender()).iterator(); 			//iterator on senderCommunicato subServers List
			Iterator<ActorRef> k = subServers.get(destCommunicator).iterator();		//iterator on destCommunicator subServers List
		
			while(k.hasNext())
				while(i.hasNext())
					if(i.next() == k.next() ){
						allow=false;
						break;
					}
		}
		/*SubServer Creation*/
		if(allow) {
			
			writeToLog("<SYSTEM>: "+users.get(father).getClientName() + " CONNECTED WITH " + dest );
      		SubServer = getContext().actorOf(Props.create(
											SubRemoteChatServiceActor.class, father, getSender(), users.get(father).getClientName(), destCommunicator, dest 
											),"SubServer"+ nextSS++);
			
			writeToLog("<SYSTEM>: SUBSERVER CREATED!");
			
			/*Update SubServers hashmap*/
			if ( !subServers.containsKey( father ) )				//if not exist create an enter
				subServers.put(father,new LinkedList<ActorRef>());

			subServers.get(father).add(SubServer);

			if( !subServers.containsKey(destCommunicator) )			//if not exist create an enter
				subServers.put( destCommunicator, new LinkedList<ActorRef>());
				
			subServers.get(destCommunicator).add(SubServer);

			/*acklogin to subCommunicator1 to introduce the subServer*/
			getSender().tell(messages.new AckLogin1To1(SubServer),getSelf());
		}
	}

	private void handleLogoutMessage(Object message) { 
	
		String nickname = users.get(getSender()).getClientName();
		writeToLog(nickname + " LOGGED OUT");
	
		users.remove(getSender());
		getSender().tell(messages.new AckLogout(),getSelf());	
		
		for (ActorRef user : users.keySet())
			user.tell(messages.new Bye(nickname, getAvailableUsers().trim()),getSelf());
		
		/*Send discover request to handle the subServers List of the clients that were talking with the disconnected user!*/
		if(subServers.containsKey(getSender())) {
			
			ListIterator<ActorRef> i = subServers.get(getSender()).listIterator();
			while(i.hasNext()) {
					i.next().tell(messages.new DiscoverRequest(getSender()), getSelf());
				}
			subServers.remove(getSender());
		}
}
	
	private void handleDiscoverReply(Object message){
		
		/*finding the subservers list refered to the discovered communicator and remove the reference to the subserver --> because the channel 1to1 is gone down!*/	
		ListIterator<ActorRef> discoverdCommSubServerList = subServers.get(((Messages.DiscoverReply)message).getDiscoveredCommunicator()).listIterator();
		
		while(discoverdCommSubServerList.hasNext())
			if(discoverdCommSubServerList.next().toString().equals(getSender().toString()) ) {
				discoverdCommSubServerList.remove();
				break;			
			}
		getSender().tell( PoisonPill.getInstance(),null);	
	}
	
	private void handlePongMessage(Object message) {

		users.get(getSender()).setStatus(true);
	}


	private void handleLogMessage(Object message) {
		writeToLog( ((Messages.LogMessage)message).getLog() );
	}


	/*MAIN: Start chatting service*/
	public static void main(String[] args) {

		GuiServer frame = new GuiServer();
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		frame.setTitle("Server - Akka Chat");

		final ActorSystem system = ActorSystem.create("ChatSystem", ConfigFactory.load(("chat")));

		final ActorRef remoteActor = system.actorOf(Props.create(RemoteChatServiceActor.class,frame), "remoteActor");

		frame.setActorReference(remoteActor);
		frame.printBootstrapMessage();
	}

}
/*Client Class*/
class Client {
	private String nickname;
	private boolean status;

	public Client(String nickname,boolean status) {
		this.nickname = nickname;
		this.status = status;
	}

	public String getClientName() {
		return nickname;
	}

	public boolean getStatus() {
		return status;
	}
	
	public void setStatus(boolean state) {
		status = state;
	}  
}
