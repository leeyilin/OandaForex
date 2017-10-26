package server.forex.protocol;

import com.em.mdsserver.message.MDMessageBinaryContainer;
import server.forex.config.ServerConfig;
import com.em.mdsserver.protocol.codec.TypeCodec;
import com.em.mdsserver.protocol.types.IntegerValue;
import com.em.mdsserver.protocol.util.ZipUtil;

import java.io.ByteArrayOutputStream;
//import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MDEncoder {
  private static Logger LOG = LoggerFactory.getLogger(MDEncoder.class);
  private static final Byte TAILBYTE = 22;
  private static int COMPRESS_THRESHOLD;

  private MDMessageContainer context;
  private MDMessageBinaryContainer binaryContext;
  private VSSHeader vssHeader;

  public MDEncoder(ServerConfig serverConfig) {
    MDEncoder.COMPRESS_THRESHOLD = serverConfig.getThreshold();
    this.vssHeader = new VSSHeader();
  }

  public void setHeader(int sequenceID, int dataSourceID, int marketID, int protocolVersion,
                        int compressType, int messageType) {
    this.vssHeader = new VSSHeader(sequenceID, dataSourceID, marketID, protocolVersion, compressType, messageType);
  }

  public byte[] encode(MDMessageContainer context) {
    ByteArrayOutputStream buffer = null;
    this.context = context;
    if (this.vssHeader.getProtocolVersion() == 0) {
      buffer = new ByteArrayOutputStream();
      byte[] codeCounts = TypeCodec.UINT.encode(new IntegerValue(context.size()));
      byte[][] fieldEncodings = new byte[context.size()][];
      for (int j = 0; j < context.size(); ++j) {
        MDMessage message = context.get(j);
        fieldEncodings[j] = message.encode();
      }

      int checkSum = 0;
      byte[] msgTypes = TypeCodec.UINT.encode(new IntegerValue(this.vssHeader.getMessageType()));
      int bodyLen = msgTypes.length;
      for (byte b : msgTypes) {
        checkSum += b;
      }

      for (byte b : codeCounts) {
        checkSum += b;
        bodyLen = codeCounts.length;
      }

      for (int j = 0; j < context.size(); ++j) {
//        for(i = 0; i < fieldEncodings[i].length; ++i) {
//          sum += fieldEncodings[i][i];
//        }
        bodyLen += fieldEncodings[j].length;
      }
      try {
        if (this.vssHeader.getCompressType() != 0) {
          this.vssHeader.setCompressType(0);
        }
        this.vssHeader.setBodyLength(bodyLen);
        buffer.write(this.vssHeader.toByteArray());
        buffer.write(codeCounts);
        for (int j = 0; j < context.size(); ++j) {
          buffer.write(fieldEncodings[j]);
        }
        byte[] dests = TypeCodec.UINT.encode(new IntegerValue(checkSum % 256));
        buffer.write(dests);
        buffer.write(TAILBYTE);
      } catch (Exception e) {
        e.printStackTrace();
        LOG.error(e.toString());
      }
    } else if (this.vssHeader.getProtocolVersion() == 8) {
      try {
        ByteArrayOutputStream dataBuf = new ByteArrayOutputStream();
        byte[] codeCounts = TypeCodec.UINT.encode(new IntegerValue(this.vssHeader.getMessageType()));
        dataBuf.write(codeCounts);
        byte[] counts = TypeCodec.UINT.encode(new IntegerValue(context.size()));
        dataBuf.write(counts);
        for (int j = 0; j < context.size(); ++j) {
          MDMessage message = context.get(j);
          dataBuf.write(message.encode());
        }
        int sum = 0;
        int bodyLen = 0;
        byte[] datas = dataBuf.toByteArray();
        for (byte b : datas) {
          sum += b;
        }
        bodyLen += datas.length;
        if (bodyLen < COMPRESS_THRESHOLD) {
          buffer = new ByteArrayOutputStream(bodyLen + 16);
          this.vssHeader.setCompressType(0);
          this.vssHeader.setBodyLength(bodyLen);
          buffer.write(this.vssHeader.toByteArray());
          buffer.write(datas);
          buffer.write(TypeCodec.UINT.encode(new IntegerValue(sum > 32767 ? sum % 256 : sum)));
          buffer.write(TAILBYTE);
        } else {
          byte[] dests = null;
          if (this.vssHeader.getCompressType() == 2) {
            dests = ZipUtil.compress(datas);
          } else {
            LOG.error("compressType:{} not support!", this.vssHeader.getCompressType());
          }

          buffer = new ByteArrayOutputStream(dests == null ? 10 : dests.length + 16);
//          this.initHead(buffer);
          if (dests != null) {
            this.vssHeader.setBodyLength(dests.length);
            buffer.write(this.vssHeader.toByteArray());
//            buffer.write(TypeCodec.UINT.encode(new IntegerValue(dests.length)));
            buffer.write(dests);
          } else {
            this.vssHeader.setBodyLength(0);
            buffer.write(this.vssHeader.toByteArray());
//            buffer.write(TypeCodec.UINT.encode(new IntegerValue(0)));
            buffer.write(new byte[0]);
          }
          buffer.write(TypeCodec.INTEGER.encode(new IntegerValue(sum <= 32767 && sum >= -32768 ? sum : sum % 256)));
          buffer.write(TAILBYTE);
        }
      } catch (Exception e) {
        LOG.error(e.toString());
      }
    } else {
      System.out.println("Unknown protocol version: " + this.vssHeader.getProtocolVersion());
    }
    return buffer.toByteArray();
  }

//  public byte[] encode(MDMessageBinaryContainer bianryContext) {
//    ByteArrayOutputStream buffer = null;
//    this.binaryContext = bianryContext;
//    this.msgType = this.binaryContext.msgType();
//    byte[] codeCounts;
//    int sum;
//    int bodyLen;
////    int i;
//    byte[] dests;
//    if (this.protocolVersion == 0) {
//      buffer = new ByteArrayOutputStream();
//      byte[] msgTypes = TypeCodec.UINT.encode(new IntegerValue(this.msgType));
//      codeCounts = TypeCodec.UINT.encode(new IntegerValue(this.binaryContext.size()));
//      byte[][] fieldEncodings = new byte[this.binaryContext.size()][];
//
//      for (sum = 0; sum < this.binaryContext.size(); ++sum) {
//        fieldEncodings[sum] = this.binaryContext.get(sum);
//      }
//
//      sum = 0;
//      bodyLen = 0;
//
//      for (int i = 0; i < msgTypes.length; ++i) {
//        sum += msgTypes[i];
//        bodyLen = msgTypes.length;
//      }
//
//      for (int i = 0; i < codeCounts.length; ++i) {
//        sum += codeCounts[i];
//        bodyLen = codeCounts.length;
//      }
//
//      for (int i = 0; i < this.binaryContext.size(); ++i) {
//        for (i = 0; i < fieldEncodings[i].length; ++i) {
//          sum += fieldEncodings[i][i];
//        }
//
//        bodyLen += fieldEncodings[i].length;
//      }
//
//      dests = TypeCodec.UINT.encode(new IntegerValue(sum % 256));
//
//      try {
//        if (this.compressType != 0) {
//          this.compressType = 0;
//        }
//
//        this.initHead(buffer);
//        buffer.write(TypeCodec.UINT.encode(new IntegerValue(bodyLen)));
//        buffer.write(msgTypes);
//        buffer.write(codeCounts);
//
//        for (int i = 0; i < this.binaryContext.size(); ++i) {
//          buffer.write(fieldEncodings[i]);
//        }
//
//        buffer.write(dests);
//        buffer.write(TAILBYTE.byteValue());
//      } catch (Exception var15) {
//        LOG.error(var15.toString());
//      }
//    } else if (this.protocolVersion == 8) {
//      ByteArrayOutputStream dataBuf = new ByteArrayOutputStream();
//      codeCounts = TypeCodec.UINT.encode(new IntegerValue(this.msgType));
//
//      try {
//        dataBuf.write(codeCounts);
//      } catch (IOException var13) {
//        var13.printStackTrace();
//      }
//
//      byte[] counts = TypeCodec.UINT.encode(new IntegerValue(this.context.size()));
//
//      try {
//        dataBuf.write(counts);
//      } catch (IOException var12) {
//        var12.printStackTrace();
//      }
//
//      for (sum = 0; sum < this.binaryContext.size(); ++sum) {
//        try {
//          dataBuf.write(this.binaryContext.get(sum));
//        } catch (IOException var11) {
//          var11.printStackTrace();
//        }
//      }
//
//      sum = 0;
//      bodyLen = 0;
//      byte[] datas = dataBuf.toByteArray();
//
//      for (int i = 0; i < datas.length; ++i) {
//        sum += datas[i];
//      }
//
//      bodyLen = bodyLen + datas.length;
//
//      try {
//        if (bodyLen < COMPRESS_THRESHOLD) {
//          buffer = new ByteArrayOutputStream(bodyLen + 16);
//          this.compressType = 0;
//          this.initHead(buffer);
//          buffer.write(TypeCodec.UINT.encode(new IntegerValue(bodyLen)));
//          buffer.write(datas);
//          buffer.write(TypeCodec.UINT.encode(new IntegerValue(sum > 32767 ? sum % 256 : sum)));
//          buffer.write(TAILBYTE.byteValue());
//        } else {
//          dests = null;
//          if (this.compressType == 2) {
//            dests = ZipUtil.compress(datas);
//          } else {
//            LOG.error("compressType:{} not support!", this.compressType);
//          }
//
//          buffer = new ByteArrayOutputStream(dests == null ? 10 : dests.length + 16);
//          this.initHead(buffer);
//          if (dests != null) {
//            buffer.write(TypeCodec.UINT.encode(new IntegerValue(dests.length)));
//            buffer.write(dests);
//          } else {
//            buffer.write(TypeCodec.UINT.encode(new IntegerValue(0)));
//            buffer.write(new byte[0]);
//          }
//
//          buffer.write(TypeCodec.INTEGER.encode(new IntegerValue(sum <= 32767 && sum >= -32768 ? sum : sum % 256)));
//          buffer.write(TAILBYTE.byteValue());
//        }
//      } catch (Exception var14) {
//        LOG.error(var14.toString());
//      }
//    }
//
//    return buffer.toByteArray();
//  }

//  private void initHead(ByteArrayOutputStream buffer) {
//    try {
//      buffer.write(HEADBYTE);
//      buffer.write(HEADBYTE);
//      long curtime = DateUtil.getCuryyyyyyyyMMddHHmmssL();
//      byte[] encoding = TypeCodec.UINT.encode(new IntegerValue((int) (curtime / 1000000L)));
//      buffer.write(encoding);
//      encoding = TypeCodec.UINT.encode(new IntegerValue((int) (curtime % 1000000L)));
//      buffer.write(encoding);
//      encoding = TypeCodec.UINT.encode(new IntegerValue(this.sid));
//      buffer.write(encoding);
//      encoding = TypeCodec.UINT.encode(new IntegerValue(this.dataSourceID));
//      buffer.write(encoding);
//      encoding = TypeCodec.UINT.encode(new IntegerValue(this.marketID));
//      buffer.write(encoding);
//      encoding = TypeCodec.UINT.encode(new IntegerValue(this.protocolVersion));
//      buffer.write(encoding);
//      encoding = TypeCodec.UINT.encode(new IntegerValue(this.compressType));
//      buffer.write(encoding);
//    } catch (Exception e) {
//      LOG.error(e.toString());
//    }
//  }

  public static void main(String[] args) {
    byte[] codeCounts = TypeCodec.UINT.encode(new IntegerValue(20150915));
    System.out.println(codeCounts.length);
  }
}

