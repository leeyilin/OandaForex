package server.forex.output;

import server.forex.protocol.ForexProtocol;
import server.forex.register.DataPusher;


public class MessagePusher {
  private static MessagePusher ourInstance = new MessagePusher();

  private MessagePusher() {
  }

  public static MessagePusher getInstance() {
    return ourInstance;
  }

  public void writeData(ForexProtocol message) {
    DataPusher.getInstance().send(message);
  }
}

