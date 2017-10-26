package client.forex.input;

import server.forex.protocol.MDMessage;
import java.util.Map;


public class ForexData {
  private Map<String, String> coreData;

  public ForexData(MDMessage mdMessage) {
    this.coreData = mdMessage.coreData();
  }

  @Override
  public String toString() {
    StringBuilder info = new StringBuilder();
    for (Map.Entry<String, String> entry : this.coreData.entrySet()) {
      info.append(String.format("%s: %s\n", entry.getKey(), entry.getValue()));
    }
    return info.toString();
  }
}
