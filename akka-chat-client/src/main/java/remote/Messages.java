package remote;
import java.io.Serializable;
import akka.actor.ActorRef;
import javax.swing.JTextArea;

public class Messages implements Serializable{
	
	class ChatMessage implements Serializable {
    	private String content;
    
    	public ChatMessage(String s) { 
    		content = s;
    	}

        public String getContent() {
        	return content;
        }	
	}

	class RoutingMessage implements Serializable {
    	private String content;
    
    	public RoutingMessage(String s) { 
    		content = s;
    	}

        public String getContent() {
        	return content;
        }	
	}

	class LoginMessage implements Serializable {
		
		private String nickname;

		public LoginMessage(String nickname) { 
			this.nickname = nickname;
		}

		public String getNickname() {
			return nickname;
		}	
	}

	class TrunckedUsername implements Serializable {
		
		private String nickname;

		public TrunckedUsername(String nickname) { 
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

	class UpdateUsrList implements Serializable {
		private String actualUsers;
        private String changedStatusUser;               //advert for the new logged user, or for a disconnected user

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

	class ToPrintMessage implements Serializable{
		private String content;
		
		public ToPrintMessage(String s){
			this.content = s;
		}

    	public String getContent(){
    		return this.content;
    	}
	}

	class ToPrintWarningMessage implements Serializable{
		private String content;
		
		public ToPrintWarningMessage(String s){
			this.content = s;
		}

    	public String getContent(){
    		return this.content;
    	}
	}

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

	class Query implements Serializable {
		
		private String nickname;

		public Query(String nickname) { 
			this.nickname = nickname;
		}

		public String getNickname() {
			return nickname;
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

	class ForkSuccessed implements Serializable {
		private JTextArea myRoom;
		private String receiverName;

		public ForkSuccessed(String receiverName, JTextArea myRoom) {
			this.myRoom = myRoom;
			this.receiverName = receiverName;
		}

		public JTextArea getSubCommRoom() {
			return myRoom;
		}

		public String getOpponentName() {
			return receiverName;
		}

	}

	class Login1To1 implements Serializable {
		
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

		public AckLogin1To1 (ActorRef SS) {
				this.SS = SS;
		}

		public ActorRef getSubServer() {
			return SS;
		}
	}
	
	class AckComm1To1 implements Serializable {
	ActorRef SS;

		public AckComm1To1 (ActorRef SS) {
				this.SS = SS;
		}

		public ActorRef getSubServer() {
			return SS;
		}
	}

	class AckRequest1To1 implements Serializable {}
	
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

	class ToKillMessage implements Serializable {
		private ActorRef subCommToKill;
		
		public ToKillMessage(ActorRef subCommToKill){
			this.subCommToKill = subCommToKill;
		}
		public ActorRef getSubCommToKill(){
			return this.subCommToKill;
		}
	}
	
	class ChangeActiveRoom implements Serializable{
		private JTextArea myRoom;
		
		public ChangeActiveRoom(JTextArea myRoom){
			this.myRoom = myRoom;
		}
		public JTextArea getChatRoom(){
			return this.myRoom;
		}
	}
	class InvalidInput implements Serializable {}
	
	class LogoutMessage implements Serializable {}

	class AckLogout implements Serializable {}

	class Ping implements Serializable {}

	class Pong implements Serializable {}
	
	class NotFound implements Serializable {}
	
	class Start1To1 implements Serializable{}
}

