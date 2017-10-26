package server.forex.protocol;


public class ForexProtocol {
  public static final int VERSION_0 = 0;
  public static final int VERSION_8 = 8;
  public static final int VERSION_16 = 16;
//  split:
//  Separator: 0
  private byte split;
  private int packLen;
//  type:
//  1: push.
//  2: subscribe.
//  3: request.
//  4: response.
  private byte type;
  private byte topicCount;
  private short[] topic;
  private byte[] content;

  public ForexProtocol() {
  }

  public ForexProtocol(byte split, int packLen, byte type, short[] topic, byte[] content) {
    this.split = split;
    this.packLen = packLen;
    this.type = type;
    this.topic = topic;
    this.topicCount = (byte) topic.length;
    this.content = content;
  }

  public byte getSplit() {
    return this.split;
  }

  public void setSplit(byte split) {
    this.split = split;
  }

  public int getPackLen() {
    return this.packLen;
  }

  public void setPackLen(int packLen) {
    this.packLen = packLen;
  }

  public byte getType() {
    return this.type;
  }

  public void setType(byte type) {
    this.type = type;
  }

  public byte getTopicCount() {
    return this.topicCount;
  }

  public void setTopicCount(byte topicCount) {
    this.topicCount = topicCount;
  }

  public short[] getTopic() {
    return this.topic;
  }

  public void setTopic(short[] topic) {
    this.topic = topic;
  }

  public byte[] getContent() {
    return this.content;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }

  @Override
  public String toString() {
    return String.format("packlen: %d, type: %s topic: %d content.length: %s",
        this.packLen, this.type, this.topic[0], this.content.length);
  }
}
