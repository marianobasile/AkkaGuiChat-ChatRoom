
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
import akka.actor.PoisonPill;
import akka.actor.Kill;

public class SubRemoteChatServiceActor extends UntypedActor {
	
	private ActorRef communicator1;
	private ActorRef subCommunicator1;
	private String nickname1;
	private ActorRef communicator2;
	private ActorRef subCommunicator2;
	private String nickname2;
	private ActorRef father;
	private Messages messages;
	
	/*CONSTRUCTOR*/
	public SubRemoteChatServiceActor(ActorRef communicator1, ActorRef subCommunicator1, String nickname1, ActorRef communicator2, String nickname2){
		this.communicator1 = communicator1;
		this.subCommunicator1 = subCommunicator1;
		this.nickname1 = nickname1;
		this.communicator2 = communicator2;
		this.nickname2 = nickname2;
		this.father = context().parent();
		this.messages = new Messages();
		
		/*ask communicator2 to start the chat1to1*/
		this.communicator2.tell( messages.new Request1To1( this.nickname1), getSelf() );
	}

	/*operation to do when receiving a poison Pill*/
	@Override
	public void postStop() {
		this.father.tell( messages.new LogMessage("CHANNEL CLOSED BETWEEN "+nickname1+ " "+nickname2), getSelf() );
	}
	
	/*BEHAVIOUR*/
	@Override
	public void onReceive(Object message) {
	  
	  switch(message.getClass().getSimpleName()) {

		case "AckRequest1To1": 
			/*estabilish 1to1 channel*/
			this.subCommunicator2 = getSender();
			this.father.tell( messages.new LogMessage("CHANNEL EXTABILISHED BETWEEN "+ this.nickname1 +" "+ this.nickname2), getSelf() );
			break;
			
		case "Chat1To1Message":
			handleChat1To1Message( ((Messages.Chat1To1Message)message).getContent() );
			break;
					
		case "DiscoverRequest":
			/*father ask me to discover the channel for a disconnectedCommunicator*/
			handleDiscoverRequest( ((Messages.DiscoverRequest)message).getDisconnectedCommunicator() );
			break;
		
		case "PoisonPill":
			//postStop()
			break;
		default:
			this.father.tell( messages.new LogMessage("UNKNOWN MESSAGES! ..."), getSelf() );
			break;				
		}
	}
		
	
	/*HANDLING MESSAGES*/
	public void handleChat1To1Message(String strMessage){

			if( (getSender().toString()).equals(this.subCommunicator1.toString()) ) {
				this.subCommunicator2.tell(  messages.new ToPrint1To1Message("<"+nickname1+">"+strMessage), getSelf());
				this.subCommunicator1.tell(  messages.new ToPrint1To1Message("<"+nickname1+">"+strMessage), getSelf());
			}
			else{
				this.subCommunicator2.tell(  messages.new ToPrint1To1Message("<"+nickname2+">"+strMessage), getSelf());
				this.subCommunicator1.tell(  messages.new ToPrint1To1Message("<"+nickname2+">"+strMessage), getSelf());
			}

	}
	public void handleDiscoverRequest(ActorRef disconnectedCommunicator){
		
		ActorRef discoveredCommunicator = null;
		ActorRef toKillSubCommunicator = null;
		
		if( disconnectedCommunicator.toString().equals(this.communicator1.toString()) ){
			discoveredCommunicator = this.communicator2;
			toKillSubCommunicator = this.subCommunicator2;
		}
		if( disconnectedCommunicator.toString().equals(this.communicator2.toString()) ){	
			discoveredCommunicator = this.communicator1;
			toKillSubCommunicator = this.subCommunicator1;
		}

		getSender().tell( messages.new DiscoverReply (discoveredCommunicator), getSelf());
		
		if(discoveredCommunicator != null)
			discoveredCommunicator.tell( messages.new ToKillMessage( toKillSubCommunicator ), getSelf() );
	}
}