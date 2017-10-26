package server.forex.protocol;

import java.util.ArrayList;
import java.util.List;

public class MDMessageContainer {
  private List<MDMessage> messages;
  private int timeStamp = 0;
  private int msgType = 0;

  public MDMessageContainer() {
    this.messages = new ArrayList<>();
  }

  public MDMessageContainer(int msgType, int size) {
    this.msgType = msgType;
    this.messages = new ArrayList<>(size);
  }

  public MDMessageContainer(MDMessage mdMessage) {
    this.messages = new ArrayList<>(1);
    this.messages.add(mdMessage);
    this.msgType = mdMessage.getMsgType();
  }

  public void setMsgType(int msgType) {
    this.msgType = msgType;
  }

  public int msgType() {
    return this.msgType;
  }

  public int add(MDMessage message) {
    this.messages.add(message);
    return this.messages.size();
  }

  public int getTimeStamp() {
    return this.timeStamp;
  }

  public void setTimeStamp(int timeStamp) {
    this.timeStamp = timeStamp;
  }

  public int size() {
    return this.messages.size();
  }

  public MDMessage get(int index) {
    return (MDMessage)this.messages.get(index);
  }

  public void clear() {
    this.messages.clear();
  }

  public List<MDMessage> getMessages() {
    return this.messages;
  }

  public void setMessages(List<MDMessage> messages) {
    this.messages = messages;
  }
}

