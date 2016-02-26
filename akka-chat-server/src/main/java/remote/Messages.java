
package remote;

import java.io.Serializable;
import akka.actor.ActorRef;

public class Messages implements Serializable{
	
	//HANDSHAKE LOGIN CHAT
	class LoginMessage implements Serializable {
		
		private String nickname;

		public LoginMessage(String nickname) { 
			this.nickname = nickname;
		}

		public String getNickname() {
			return nickname;
		}	
	}
	class RejectLogin implements Serializable {
		
		private String cause;

		public RejectLogin(String cause) {
			this.cause = cause;
		}

		public String getCause() {
			return cause;
		}
	}
	class AckLogin implements Serializable {
		private String actualUsers;

		public AckLogin(String actualUsers) {
			this.actualUsers = actualUsers;
		}

		public String getActualUsers() {

			return this.actualUsers;

		}
	}
	//HANDLING MESSAGES
	class ChatMessage implements Serializable {
    	private String content;
    
    	public ChatMessage(String s) { 
    		content = s;
    	}

        public String getContent() {
        	return content;
        }	
	}
	class ToPrintMessage implements Serializable{
		private String content;
		
		public ToPrintMessage(String s){
			this.content = s;
		}

    	public String getContent(){
    		return this.content;
    	}
	}
	//HANDSHAKING LOGOUT
	class LogoutMessage implements Serializable {}

	class AckLogout implements Serializable {}
	
	class Bye implements Serializable {
		private String nickname;
		private String actualUsers;

		public Bye (String nickname, String actualUsers) {
			this.nickname = nickname;
			this.actualUsers = actualUsers;
		}

		public String getNickname() {
			return nickname;
		}

		public String getActualUsers() {
			return actualUsers;
		}

	}
	//UPDATING ACTIONS
	class UpdateUsrList implements Serializable {
		private String actualUsers;
        private String changedStatusUser;   
		//advert for the new logged user, or for a disconnected user

		public UpdateUsrList(String actualUsers, String changedStatusUser) {
			this.actualUsers = actualUsers;
                        this.changedStatusUser = changedStatusUser;
		}

		public String getActualUsers() {
                    
			return this.actualUsers;
                        
		}
                
        public String getChangedStatusUser(){
                    
            return this.changedStatusUser;
        }
	}
	//CHECKING CLIENTS CRASHES
	class Ping implements Serializable {}

	class Pong implements Serializable {}
	
//CHAT 1 to 1
	
	//ESTABILISHING THE CHANNEL	
	class CheckForUser implements Serializable {
		
		private String nickname;

		public CheckForUser(String nickname) { 
			this.nickname = nickname;
		}

		public String getNickname() {
			return nickname;
		}	
	}

	class FoundIt implements Serializable {
		private String nickname;

		public FoundIt(String nickname) {
			this.nickname = nickname;
		}

		public String getNickname() {
			return nickname;
		}
	}

	class NotFound implements Serializable {}
	
	class AckRequest1To1 implements Serializable {}

	class Login1To1 implements Serializable {  //Subcommunicator introduce itself to the server
		
		private String dest;
		private ActorRef father;

		public Login1To1(ActorRef father, String dest) {
			this.father = father;
			this.dest=dest;
		}

		public ActorRef getFather() {
			return father;
		}

		public String getDest() {
			return dest;
		}
	}
	
	class AckLogin1To1 implements Serializable {
		ActorRef SS;
		public AckLogin1To1 (ActorRef SS)
			{
				this.SS=SS;
			}
		public ActorRef getSubServer()
		{
			return SS;
		}
	}
	
	class Request1To1 implements Serializable {
		String nicknameSender;
		
		public Request1To1(String nicknameSender){
			this.nicknameSender = nicknameSender;
		}
		public String getNicknameSender(){
			return this.nicknameSender;
		}
	}
	
//HANDLING THE CHAT
	class Chat1To1Message implements Serializable{
		private String content;
    
    	public Chat1To1Message(String s) { 
    		content = s;
    	}

        public String getContent() {
        	return content;
        }
	}
	class ToPrint1To1Message implements Serializable{
		private String content;
    
    	public ToPrint1To1Message(String s) { 
    		content = s;
    	}

        public String getContent() {
        	return content;
        }
	}

	//LOG MESSAGE
	class LogMessage implements Serializable{
		private String log;

		public LogMessage(String log){
			this.log = log;
		}
		public String getLog(){
			return this.log;
		}
	}
	//HANDLING CHAT SWITCH BUTTONS
	class DiscoverRequest implements Serializable{
		private ActorRef disconnectedCommunicator;
		
		public DiscoverRequest(ActorRef disconnectedCommunicator){
			this.disconnectedCommunicator = disconnectedCommunicator;
		}
		public ActorRef getDisconnectedCommunicator(){
			return this.disconnectedCommunicator;
		}
	}
	class DiscoverReply implements Serializable{
		private ActorRef discoveredCommunicator;
		
		public DiscoverReply(ActorRef discoveredCommunicator){
			this.discoveredCommunicator = discoveredCommunicator;
		}
		public ActorRef getDiscoveredCommunicator(){
			return this.discoveredCommunicator;
		}
	}
	//REMOVING THE CHANNEL CHANNEL	
	class ToKillMessage implements Serializable {
		private ActorRef subCommToKill;
		
		public ToKillMessage(ActorRef subCommToKill){
			this.subCommToKill = subCommToKill;
		}
		public ActorRef getSubCommToKill(){
			return this.subCommToKill;
		}
	}
}
