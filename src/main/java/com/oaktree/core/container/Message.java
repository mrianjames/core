package com.oaktree.core.container;

import java.util.logging.Level;

import com.oaktree.core.utils.Text;

/**
 * Stock implementation of a message. @see IMessage
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public class Message implements IMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean sendToUnavailableTarget = false;
	
    private long transactTime;
    
	/**
	 * publisher of the message and who responses should go back to normally.
	 */
	private transient IMessageSender sender;
	/**
	 * payload; anything you wish to send to a component
	 */
	private Object contents;
	/**
	 * target component type; if NAMED then targetSubType will be used for direct routing.
	 */
	private ComponentType targetComponentType;
	/**
	 * target sub id; a specialisation of the component type or even a specific name if componenttype
	 * is set to NAMED.
	 */
	private String targetSubtype;
	
	/**
	 * Reason for sending the message
	 */
	private String reason;
	
	/**
	 * The unique message id
	 */
	private String id;
	
	/**
	 * A general type of this message so clients can easily filter and delegate the message to a specific
	 * handler without needing to examine the pay-load.
	 */
	private MessageType type;
	
	/**
	 * A key that can be used by optional IDispatchers in the receiver or router.
	 */
	private String dispatchId;
	
	/**
	 * Our priority of this message; defaults to normal (5)
	 */
	private int priority = IMessage.NORMAL_PRIORITY;
	
	/**
	 * A level this message is intended to be "at". This allows listeners that only want certain types of messages to be heard can
	 * filter in a low cost manor. Akin to the concept of logging levels, but can be used for any listener type.
	 */
	private int level = Level.FINEST.intValue();
	
	public Message() {
            transactTime = System.currentTimeMillis();
        }

	public Message(String id, MessageType type,ComponentType target,String subtype,Object message, IMessageSender sender, String reason, int priority) {
		this(null,id,type,target,subtype,message,sender,reason,priority);
	}
	
	public Message(String dispatchId,String id, MessageType type,ComponentType target,String subtype,Object message, IMessageSender sender, String reason, int priority) {
                this();
		this.dispatchId = dispatchId;
		this.id = id;
		this.targetComponentType = target;
		this.targetSubtype = subtype;
		this.contents = message;
		this.sender = sender;
		this.reason = reason;
		this.type = type;		
		this.priority = priority;
	}
	
	public Message(String dispatchId,String id, MessageType type,ComponentType target,String subtype,Object message, IMessageSender sender, String reason, int level, int priority) {
		this(dispatchId,id,type,target,subtype,message,sender,reason,priority);
		this.level = level;
		
	}
	
	public String toString() {
		String s = sender != null ? sender.getName() : "";
		String ctype = this.type != null ? this.type.name() : ""; 
		return this.dispatchId+"," +this.id + " " + ctype + ", "+ contents + " from " + s + ": " + reason +". Priority: " + this.priority;
	}
	
	@Override
	public Object getMessageContents() {
		return this.contents;
	}

	@Override
	public String getMessageId() {
		return this.id;
	}

	@Override
	public IMessageSender getMessageSender() {
		return this.sender;
	}

	@Override
	public MessageType getMessageType() {
		return this.type;
	}

	@Override
	public String getReason() {
		return this.reason;
	}

	@Override
	public String getTargetSubtype() {
		return this.targetSubtype;
	}

	@Override
	public ComponentType getTargetComponentType() {
		return this.targetComponentType;
	}

	@Override
	public void setMessageContents(Object msg) {
		this.contents = msg;
	}

	@Override
	public void setMessageId(String id) {
		this.id = id;
	}

	@Override
	public void setMessageSender(IMessageSender sender) {
		this.sender = sender;
	}

	@Override
	public void setMessageType(MessageType type) {
		this.type = type;
	}

	@Override
	public void setReason(String reason) {
		this.reason = reason;
	}

	@Override
	public void setSubtype(String subtype) {
		this.targetSubtype = subtype;
	}

	@Override
	public void setTargetComponentType(ComponentType type) {
		this.targetComponentType = type;
	}
	public String getDispatchId() {
		return dispatchId;
	}
	public void setDispatchId(String dispatchId) {
		this.dispatchId = dispatchId;
	}
	@Override
	public int getLevel() {
		return this.level;
	}
	@Override
	public void setLevel(int level) {
		this.level = level;
	}
	@Override
	public int getPriority() {
		return this.priority;
	}
	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}

        @Override
        public long getTransactTime(){
            return this.transactTime;
        }

        @Override
        public void setTransactTime(long t){
            this.transactTime = t;
        }

        @Override
        public String getDescription() {
            StringBuilder b = new StringBuilder(100);
            b.append("ID: ");
            b.append(this.id);
            b.append(Text.SPACE);
            b.append(" from ");
            b.append(this.getMessageSender() != null ? this.getMessageSender().getName() : "Unknown");
            b.append(Text.SPACE);
            b.append(" to " );
            b.append(this.getTargetComponentType().name());
            b.append(Text.PERIOD);
            b.append(this.getTargetSubtype());
            b.append(" priority ");
            b.append(this.priority);
            b.append(" msgtype ");
            b.append(this.type);
            b.append(" on dispatchid ");
            b.append(this.dispatchId);
            b.append(Text.PERIOD);
            b.append("Contents: ");
            b.append(this.toString());
            return b.toString();
        }

		@Override
		public boolean isSendToUnavailableTarget() {
			return this.sendToUnavailableTarget;
		}

		@Override
		public void setSendToUnavailableTarget(boolean send) {
			this.sendToUnavailableTarget = send;
		}
}
