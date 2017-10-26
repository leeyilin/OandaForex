package server.forex.protocol;

//import com.em.mdsserver.protocol.codec.BitVector;
import com.em.mdsserver.protocol.codec.SequenceCodec;
import com.em.mdsserver.protocol.codec.TypeCodec;
import server.forex.protocol.EMDictionary;
//import com.em.mdsserver.protocol.types.BitVectorValue;
//import com.em.mdsserver.protocol.util.LZ4Util;
//import com.em.mdsserver.protocol.util.ZipUtil;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * User: HuCC
 * Date: 2015-09-23
 * Time: 18:06
 */
public class MDDecoder {
  private static Logger LOG = LoggerFactory.getLogger(MDDecoder.class);

  private InputStream in;
  private VSSHeader vssHeader;

  public MDDecoder() {
    vssHeader = new VSSHeader();
  }

  public void setInputStream(InputStream in) {
    this.in = in;
  }

  public boolean decode(MDMessageContainer context) {
    if (this.vssHeader.fromInputStream(this.in)) {
      if (this.vssHeader.getProtocolVersion() == ForexProtocol.VERSION_0) {
        if (this.vssHeader.getCompressType() != 0) {
          LOG.error("protocolVersion:0 compress not support!");
          return false;
        } else {
          context.setMsgType(this.vssHeader.getMessageType());
          return readBody(context);
        }
      } else if (this.vssHeader.getProtocolVersion() == ForexProtocol.VERSION_8) {
//      try {
//        if (compressType == 0) {
//          readBody(in, context);
//          //获取校验和(不压缩不做校验)
//          TypeCodec.UINT.decode(in).toInt();
//          //获取包尾
//          in.read(pt);
//        } else {
//
//          byte[] zDataBody = new byte[bodyLength];
//          in.read(zDataBody);
//          //decompress
//          byte[] bodyData = null;
//          if (compressType == 2) {
//            try {
//              bodyData = ZipUtil.decompress(zDataBody);
//            } catch (Exception e) {
//              LOG.error(e.getMessage());
//            }
//          } else {
//            LOG.error("compressType:{} not support!", compressType);
//          }
//          //获取校验和
//          int checksum = TypeCodec.INTEGER.decode(in).toInt();
//
//          if (bodyData != null) {
//            int sum = 0;
//            for (int i = 0; i < bodyData.length; i++) {
//              sum = sum + bodyData[i];
//            }
//            if (checksum == ((sum > Short.MAX_VALUE || sum < Short.MIN_VALUE) ? (sum % 256) : sum)) {
//              readBody(new ByteArrayInputStream(bodyData), context);
//            } else {
//              LOG.error("checksum error..." + (checksum + " " + ((sum > Short.MAX_VALUE || sum < Short.MIN_VALUE) ? (sum % 256) : sum)));
//
//            }
//          }
//          //获取包尾
//          in.read(pt);
//        }
//      } catch (Exception e) {
//        LOG.error(e.getMessage(), e);
//      }
      } else if (this.vssHeader.getProtocolVersion() == ForexProtocol.VERSION_16) {
//      try {
//        if (compressType == 0) {
//          readBodyV16(in, bodyLength, context);
//          //获取校验和(不压缩不做校验)
//          TypeCodec.UINT.decode(in).toInt();
//          //获取包尾
//          in.read(pt);
//        } else {
//
//          //decompress
//          byte[] bodyData = null;
//          if (compressType == 2) {
//            byte[] zDataBody = new byte[bodyLength];
//            //
//            in.read(zDataBody);
//
//            try {
//              bodyData = ZipUtil.decompress(zDataBody);
//            } catch (Exception e) {
//              LOG.error(e.getMessage());
//            }
//          } else if (compressType == 3) {
//            //
//            int tmpAvai = in.available();
//            int srcLen = TypeCodec.UINT.decode(in).toInt();
//            byte[] zDataBody = new byte[bodyLength - (tmpAvai - in.available())];
//            //
//            in.read(zDataBody);
//            try {
//              bodyData = LZ4Util.decompress(zDataBody, srcLen);
//            } catch (Exception e) {
//              LOG.error(e.getMessage());
//            }
//          } else {
//            LOG.error("compressType:{} not support!", compressType);
//          }
//          //获取校验和
//          int checksum = TypeCodec.INTEGER.decode(in).toInt();
//
//          if (bodyData != null) {
//            int sum = 0;
//            for (int i = 0; i < bodyData.length; i++) {
//              sum = sum + bodyData[i];
//            }
//            if (checksum == ((sum > Short.MAX_VALUE || sum < Short.MIN_VALUE) ? (sum % 256) : sum)) {
//              readBodyV16(new ByteArrayInputStream(bodyData), bodyData.length, context);
//            } else {
//              LOG.error("checksum error..." + (checksum + " " + ((sum > Short.MAX_VALUE || sum < Short.MIN_VALUE) ? (sum % 256) : sum)));
//
//            }
//          }
//          //获取包尾
//          in.read(pt);
//        }
//      } catch (Exception e) {
//        LOG.error(e.getMessage(), e);
//      }
      } else {
        System.out.println("Unknown protocol version: " + this.vssHeader.getProtocolVersion());
      }
    } else {
      return false;
    }
    return false;
  }

  private boolean readBody(MDMessageContainer context) {
    int codeCount = TypeCodec.UINT.decode(in).toInt();
    try {
      for (int i = 0; i < codeCount; ++i) {
        String emCode = TypeCodec.ASCII.decode(in).toString();
        MDMessage mdMessage = new MDMessage(emCode);
        mdMessage.setMsgType(this.vssHeader.getMessageType());
        int indexCount = TypeCodec.UINT.decode(in).toInt();
        for (int j = 0; j < indexCount; ++j) {
          int index = TypeCodec.UINT.decode(in).toInt();
          TypeCodec codec = EMDictionary.getInstance().get(this.vssHeader.getMessageType(), index).getCodec();
          if (codec instanceof SequenceCodec) {
            ((SequenceCodec) codec).setMsgType(this.vssHeader.getMessageType());
            ((SequenceCodec) codec).setSeqIndex(index);
            mdMessage.addField(index, codec.decode(in));
          } else {
            mdMessage.addField(index, codec.decode(in));
          }
        }
        context.add(mdMessage);
      }
      int checksum = TypeCodec.UINT.decode(in).toInt();
      byte[] trailBytes = new byte[1];
      in.read(trailBytes);
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

//  private void readBodyV16(InputStream bodyIn, int bodyLen, MDMessageContainer context) throws IOException {
//    int readedLen = 0;
//    int available = bodyIn.available();
//
//    while (readedLen < bodyLen) {
//      short msgType = 0;
//      int msgLen = 0;
//      byte pmap = 0;
//      int seq = 0;
//      int indexCount = 0;
//      try {
//        //
//        msgLen = TypeCodec.UINT.decode(bodyIn).toInt();
//        //
//        msgType = TypeCodec.UINT.decode(bodyIn).toShort();
//        //pmap
//        pmap = EMDictonary.getInstance().pmaps(msgType);
//        //seq
//        seq = TypeCodec.UINT.decode(bodyIn).toInt();
//        //index
//        indexCount = TypeCodec.UINT.decode(bodyIn).toInt();
//        Collection<Integer> msgIndexs = new ArrayDeque<>();
//        for (int i = 1; i <= indexCount; i++) {
//          msgIndexs.add(TypeCodec.UINT.decode(bodyIn).toInt());
//        }
//        //codeCount
//        int codeCount = TypeCodec.UINT.decode(bodyIn).toInt();
//
//        if (protocolVersion > 0) {
//          //PackMiss
//          Integer lastSeq = seqMap.get(msgType);
//          if (lastSeq != null) {
//            if (ServerInfo.getInstance().isPackMissStat() && seq != 0 && (seq - lastSeq != 1)) {
//              LOG.error("PackMiss[SEQ{}]: {}->{}", new Object[]{msgType, seq, lastSeq});
//            }
//          }
//          if (LOG.isDebugEnabled()) {
//            LOG.debug("SEQ[{}]: {}", new Object[]{msgType, seq});
//          }
//          seqMap.put(msgType, seq);
//        }
//
//        //
//        int j = 0;
//        for (int i = 0; i < codeCount; i++) {
//          //读取emCode
//          String emCode = TypeCodec.ASCII.decode(bodyIn).toString();
//          MDMessage mdMessage = new MDMessage(emCode, msgType);
//
//          BitVector pmapBit = null;
//          if (pmap == 1) {
//            pmapBit = ((BitVectorValue) TypeCodec.BIT_VECTOR.decode(bodyIn)).getValue();
//          } else if (pmap == 2) {
//            //nothing
//          }
//          j = 0;
//          for (int msgIndex : msgIndexs) {
//            try {
//              if ((pmap == 1 && pmapBit.isSet(j++))
//                  || pmap == 2) {
//                TypeCodec codec = EMDictonary.getInstance().get(msgType, msgIndex).getCodec();
//                if (codec instanceof SequenceCodec) {
//                  SequenceCodec sequenceCodec = (SequenceCodec) codec;
//                  sequenceCodec.setMsgType(msgType);
//                  sequenceCodec.setSeqIndex(msgIndex);
//                  byte subPmap = EMDictonary.getInstance().get(msgType, msgIndex).getPmap();
//                  mdMessage.addField(msgIndex, ((SequenceCodec) codec).decode(bodyIn, protocolVersion, msgType, msgIndex, subPmap));
//                } else {
//                  mdMessage.addField(msgIndex, codec.decode(bodyIn));
//                }
//
//              } else {
//                if (pmap != 1 && pmap != 2) {
//                  LOG.error("MsgType:" + msgType + ", pmap:" + pmap);
//                }
//              }
//
//            } catch (Exception e) {
//              LOG.error("MsgType:" + msgType + ":" + msgIndex + ":" + e.getMessage(), e);
//
//            }
//          }
//
//          context.add(mdMessage);
//        }
//      } catch (Exception e) {
////                LOG.error("MsgType:" + msgType + ":"  + e.getMessage(), e);
//        LOG.error("MsgType:{},msgLen:{},pmap:{},seq:{},indexCount:{}:errmsg:{}:erro{}",
//            new Object[]{msgType, msgLen, pmap, seq, indexCount, e.getMessage(), e});
//      }
//      readedLen += available - bodyIn.available();
//      available = bodyIn.available();
//    }
//  }
}


