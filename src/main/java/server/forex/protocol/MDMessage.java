package server.forex.protocol;

import com.em.mdsserver.protocol.codec.TypeCodec;
//import com.em.mdsserver.protocol.dic.EMDictonary;
import server.forex.protocol.EMDictionary;
import com.em.mdsserver.protocol.types.AsciiStringValue;
import com.em.mdsserver.protocol.types.ByteVectorValue;
import com.em.mdsserver.protocol.types.IntegerValue;
import com.em.mdsserver.protocol.types.LongValue;
import com.em.mdsserver.protocol.types.ScalarValue;
import com.em.mdsserver.protocol.types.StringValue;
import com.em.mdsserver.protocol.types.UnicodeStringValue;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MDMessage {
  private static Logger LOG = LoggerFactory.getLogger(MDMessage.class);
  private static final Map<Integer, String> indexToName;

  private String emCode;
  private int msgType = 0;
  private HashMap<Integer, ScalarValue> fields = new HashMap<>();
  private ByteArrayOutputStream buffer;

  static {
    indexToName = new HashMap<>();
    indexToName.put(1, "date");
    indexToName.put(2, "time");
    indexToName.put(6, "mean");
    indexToName.put(10, "bid(buy)");
    indexToName.put(12, "ask(sell)");
  }

  public void setFields(HashMap<Integer, ScalarValue> fields) {
    this.fields = fields;
  }

  public HashMap<Integer, ScalarValue> getFields() {
    return this.fields;
  }

  public int getMsgType() {
    return this.msgType;
  }

  public void setMsgType(int msgType) {
    this.msgType = msgType;
  }

  public MDMessage(String emCode) {
    this.emCode = emCode;
  }

  public void clear() {
    this.fields.clear();
    try {
      if (this.buffer != null) {
        this.buffer.flush();
        this.buffer.close();
      }
    } catch (Exception var2) {
      var2.printStackTrace();
    }
  }

  public String emCode() {
    return this.emCode;
  }

  public void addField(int index, ScalarValue value) {
    this.fields.put(index, value);
  }

  public ScalarValue getField(int index) {
    return (ScalarValue) this.fields.get(index);
  }

  public void setBytes(int index, byte[] bytes) {
    ByteVectorValue value = new ByteVectorValue(bytes);
    this.addField(index, value);
  }

  public void setInt(int index, int value) {
    IntegerValue intValue = new IntegerValue(value);
    this.addField(index, intValue);
  }

  public void setDouble(int index, double value) {
    BigDecimal b1 = new BigDecimal(value);
    int dp = EMDictionary.getInstance().get(this.msgType, index).getDecimalPlaces();
    BigDecimal b2 = new BigDecimal(Math.pow(10.0D, (double) (dp + 1)));
    BigDecimal b3 = b1.multiply(b2);
    BigDecimal b4 = b3.setScale(0, 4);
    BigDecimal b5 = b4.divide(new BigDecimal(10)).setScale(0, 4);
    if (b5.compareTo(new BigDecimal(9223372036854775807L)) == 1) {
      LOG.info("index:" + index + ",value:" + value + ",OUT OF LONG");
    } else {
      LongValue longValue = new LongValue(b5.longValue());
      this.addField(index, longValue);
    }
  }

  public double getDouble(int index) {
    long lv = ((ScalarValue) this.fields.get(index)).toLong();
    int dp = EMDictionary.getInstance().get(this.msgType, index).getDecimalPlaces();
    BigDecimal b1 = new BigDecimal(lv);
    BigDecimal b2 = new BigDecimal(Math.pow(10.0D, (double) dp));
    BigDecimal b3 = b1.divide(b2);
    return b3.doubleValue();
  }

  public void setFloat(int index, float value) {
    BigDecimal b1 = new BigDecimal((double) value);
    int dp = EMDictionary.getInstance().get(this.msgType, index).getDecimalPlaces();
    BigDecimal b2 = new BigDecimal(Math.pow(10.0D, (double) (dp + 1)));
    BigDecimal b3 = b1.multiply(b2);
    BigDecimal b4 = b3.setScale(0, 4);
    BigDecimal b5 = b4.divide(new BigDecimal(10)).setScale(0, 4);
    if (b5.compareTo(new BigDecimal(9223372036854775807L)) == 1) {
      LOG.info("index:" + index + ",value:" + value + ",OUT OF LONG");
    } else {
      LongValue longValue = new LongValue(b5.longValue());
      this.addField(index, longValue);
    }
  }

  public float getFloat(int index) {
    long lv = ((ScalarValue) this.fields.get(index)).toLong();
    int dp = EMDictionary.getInstance().get(this.msgType, index).getDecimalPlaces();
    BigDecimal b1 = new BigDecimal(lv);
    BigDecimal b2 = new BigDecimal(Math.pow(10.0D, (double) dp));
    BigDecimal b3 = b1.divide(b2);
    return b3.floatValue();
  }

  public void setLong(int index, long value) {
    LongValue longValue = new LongValue(value);
    this.addField(index, longValue);
  }

  public void setString(int index, String value) {
    AsciiStringValue asciiStringValue = new AsciiStringValue(value);
    this.addField(index, asciiStringValue);
  }

  public void setUnicodeString(int index, String value) {
    UnicodeStringValue unicodeStringValue = new UnicodeStringValue(value);
    this.addField(index, unicodeStringValue);
  }

  public int size() {
    return this.fields.size();
  }

  public boolean ifIndexExist(int index) {
    boolean b = false;
    if (this.fields.get(index) != null) {
      b = true;
    }

    return b;
  }

  public int getInt(int index) {
    return ((ScalarValue) this.fields.get(index)).toInt();
  }

  public long getLong(int index) {
    return ((ScalarValue) this.fields.get(index)).toLong();
  }

  public String getString(int index) {
    return ((ScalarValue) this.fields.get(index)).toString();
  }

  public byte[] encode() {
    try {
      if (this.buffer == null) {
        this.buffer = new ByteArrayOutputStream();
      }
      this.buffer.reset();
      byte[] encoding = TypeCodec.ASCII.encode(new StringValue(this.emCode));
      this.buffer.write(encoding);
      encoding = TypeCodec.UINT.encode(new IntegerValue(this.fields.size()));
      this.buffer.write(encoding);
      for (Entry entry : this.fields.entrySet()) {
        Integer index = (Integer) entry.getKey();
        ScalarValue field = (ScalarValue) entry.getValue();
        encoding = TypeCodec.UINT.encode(new IntegerValue(index));
        this.buffer.write(encoding);
        encoding = field.encode();
        this.buffer.write(encoding);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return this.buffer.toByteArray();
  }

  @Override
  public String toString() {
    StringBuilder messageInfo = new StringBuilder(
        String.format("MDMessage: code: %s, messageType: %d\n", this.emCode, this.msgType)
    );
    for (Map.Entry<Integer, ScalarValue> entry : this.fields.entrySet()) {
      String name = MDMessage.indexToName.get(entry.getKey());
      if (name != null) {
        messageInfo.append(String.format("%s: %s\n", name, entry.getValue().toString()));
      } else {
        messageInfo.append(String.format("Unknown index: %s\n", entry.getKey()));
      }
    }
    return messageInfo.toString();
  }

  public Map<String, String> coreData() {
    Map<String, String> coreData = new HashMap<>();
    coreData.put("code", this.emCode);
    for (Map.Entry<Integer, ScalarValue> entry : this.fields.entrySet()) {
      String name = MDMessage.indexToName.get(entry.getKey());
      if (name != null) {
        coreData.put(name, entry.getValue().toString());
      }
    }
    return coreData;
  }
}

