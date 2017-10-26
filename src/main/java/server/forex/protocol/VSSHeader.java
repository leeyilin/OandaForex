package server.forex.protocol;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
//import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.em.mdsserver.protocol.codec.TypeCodec;
import com.em.mdsserver.protocol.types.IntegerValue;
import server.forex.util.datetime.DateUtil;

class VSSHeader {
  private static Logger LOG = LoggerFactory.getLogger(VSSHeader.class);
  //  Double-check this value when unpacking packages.
  private static final Byte HEADBYTE = -17;

  private byte[] headBytes = new byte[2];
  private int sequenceID;
  private int dataSourceID;
  private int marketID;
  private int protocolVersion;
  private int compressType;
  private int messageType;
  private int bodyLength;
  private int dateStamp;
  private int timeStamp;

  VSSHeader() {
  }

  VSSHeader(int sequenceID, int dataSourceID, int marketID, int protocolVersion, int compressType,
            int messageType) {
    this.sequenceID = sequenceID;
    this.dataSourceID = dataSourceID;
    this.marketID = marketID;
    this.protocolVersion = protocolVersion;
    this.compressType = compressType;
    this.messageType = messageType;
  }

  void setBodyLength(int bodyLength) {
    this.bodyLength = bodyLength;
  }

  int getMessageType() {
    return this.messageType;
  }

  int getProtocolVersion() {
    return this.protocolVersion;
  }

  int getCompressType() {
    return this.compressType;
  }

  void setCompressType(int compressType) {
    this.compressType = compressType;
  }

  byte[] toByteArray() {
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      buffer.write(VSSHeader.HEADBYTE);
      buffer.write(VSSHeader.HEADBYTE);
      long currenttime = DateUtil.getCuryyyyyyyyMMddHHmmssL();
      this.dateStamp = (int) (currenttime / 1000000L);
      this.timeStamp = (int) (currenttime % 1000000L);
      buffer.write(TypeCodec.UINT.encode(new IntegerValue(this.dateStamp)));
      buffer.write(TypeCodec.UINT.encode(new IntegerValue(this.timeStamp)));
      buffer.write(TypeCodec.UINT.encode(new IntegerValue(this.sequenceID)));
      buffer.write(TypeCodec.UINT.encode(new IntegerValue(this.dataSourceID)));
      buffer.write(TypeCodec.UINT.encode(new IntegerValue(this.marketID)));
      buffer.write(TypeCodec.UINT.encode(new IntegerValue(this.protocolVersion)));
      buffer.write(TypeCodec.UINT.encode(new IntegerValue(this.compressType)));
      buffer.write(TypeCodec.UINT.encode(new IntegerValue(this.bodyLength)));
      buffer.write(TypeCodec.UINT.encode(new IntegerValue(this.messageType)));
      return buffer.toByteArray();
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error(e.toString());
      return new byte[1];
    }
  }

  //  I need to compare in.available() and bodyLength.
  boolean fromInputStream(InputStream in) {
    boolean result = false;
    try {
      if (in.available() >= 38) {
        in.read(this.headBytes);
        if ((this.headBytes[0] == -17) && (this.headBytes[1] == -17)) {
          this.dateStamp = TypeCodec.UINT.decode(in).toInt();
          this.timeStamp = TypeCodec.UINT.decode(in).toInt();
          this.sequenceID = TypeCodec.UINT.decode(in).toInt();
          this.dataSourceID = TypeCodec.UINT.decode(in).toInt();
          this.marketID = TypeCodec.UINT.decode(in).toInt();
          this.protocolVersion = TypeCodec.UINT.decode(in).toInt();
          this.compressType = TypeCodec.UINT.decode(in).toInt();
          this.bodyLength = TypeCodec.UINT.decode(in).toInt();
          this.messageType = TypeCodec.UINT.decode(in).toInt();
          if (LOG.isDebugEnabled()) {
            LOG.debug("[MDHeader] dateStamp:{},timeStamp:{},sid:{},dataSourceID:{},marketID:{},protocolVersion:{},compressType:{}",
                new Object[]{dateStamp, timeStamp, sequenceID, dataSourceID, marketID, protocolVersion, compressType});
          }
          result = true;
        } else {
//          Handle it later.
          System.out.println("Receive incorrect head bytes, prepare to drop these bytes!");
          result = false;
        }
      } else {
        result = false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error(e.toString());
      result = false;
    }
    return result;
  }
}
