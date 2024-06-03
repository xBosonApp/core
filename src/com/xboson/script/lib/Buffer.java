////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 2017年11月9日 08:23
// 原始文件路径: xBoson/src/com/xboson/script/lib/Buffer.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.script.lib;

import com.xboson.script.JSObject;
import com.xboson.util.Tool;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Iterator;

/**
 * 全局静态方法
 */
public class Buffer {

  public final static String UTF8 = "utf8";
  public final static String BASE64 = "base64";
  public final static String DEFAULT_ENCODING = UTF8;


  public JsBuffer alloc(int size, int fill, String encoding) {
    JsBuffer buf = new JsBuffer(encoding);
    buf.buf = ByteBuffer.allocate(size);
    if (fill != 0) buf.fill((byte) fill);
    return buf;
  }


  public JsBuffer alloc(int size, int fill) {
    return alloc(size, fill, DEFAULT_ENCODING);
  }


  public JsBuffer alloc(int size) {
    return alloc(size, 0, DEFAULT_ENCODING);
  }


  /**
   * allocateDirect 创建的对象不支持 slice();
   * 所以 allocUnsafe 和 alloc 分配的对象没有区别
   * @see #alloc
   */
  public JsBuffer allocUnsafe(int size) {
    return alloc(size, 0, DEFAULT_ENCODING);
  }


  /**
   * @see #allocUnsafe
   */
  public JsBuffer allocUnsafeSlow(int size) {
    return alloc(size, 0, DEFAULT_ENCODING);
  }


  public int byteLength(String string, String encoding)
          throws UnsupportedEncodingException {
    return string.getBytes(encoding).length;
  }


  public int byteLength(String str) throws UnsupportedEncodingException {
    return byteLength(str, DEFAULT_ENCODING);
  }


  public int compare(JsBuffer a, JsBuffer b) {
    return a.compare(b);
  }


  public JsBuffer concat(JsBuffer[] list, int totalLength) {
    if (totalLength <= 0) {
      totalLength = 0;
      for (int i=0; i<list.length; ++i) {
        totalLength += list[i].getLength();
      }
    }

    JsBuffer ret = new JsBuffer(DEFAULT_ENCODING);
    ret.buf = ByteBuffer.allocate(totalLength);
    int pos = 0;

    for (int i=0; i<list.length; ++i) {
      list[i].buf.position(0);
      int len = list[i].getLength();
      while (len >= 0) {
        ret.buf.put(list[i].buf.get());
        --len;
        if (++pos >= totalLength) {
          return ret;
        }
      }
    }
    return ret;
  }


  public JsBuffer concat(JsBuffer[] list) {
    return concat(list, -1);
  }


  public JsBuffer from(byte[] array, int byteOffset, int length) {
    JsBuffer buf = new JsBuffer(DEFAULT_ENCODING);
    buf.buf = ByteBuffer.wrap(array, byteOffset, length);
    return buf;
  }


  public JsBuffer from(byte[] array, int byteOffset) {
    return from(array, byteOffset, array.length - byteOffset);
  }


  public JsBuffer from(byte[] array) {
    return from(array, 0, array.length);
  }


  public JsBuffer from(String string, String encoding) {
    byte[] array;
    if (encoding.equalsIgnoreCase(BASE64)) {
      array = Base64.getDecoder().decode(string);
    } else {
      array = string.getBytes(Charset.forName(encoding));
    }
    JsBuffer ret = new JsBuffer(encoding);
    ret.buf = ByteBuffer.wrap(array);
    return ret;
  }


  public JsBuffer from(String string) {
    return from(string, DEFAULT_ENCODING);
  }


  public JsBuffer from(JsBuffer other) {
    return other.clone();
  }


  public JsBuffer from(Object _ArrayBuffer) {
    try {
      ByteBuffer buf = JSObject.getUnderlyingBuffer(_ArrayBuffer);
      JsBuffer ret = new JsBuffer(DEFAULT_ENCODING);
      ret.buf = buf;

      return ret;
    } catch (Exception e) {
      e.printStackTrace();
    }

    throw new RuntimeException(
            "Buffer.from(ArrayBuffer) not implement.");
  }


  public boolean isBuffer(Object o) {
    return o instanceof JsBuffer;
  }


  public boolean isEncoding(String encoding) {
    return Charset.availableCharsets().containsKey(encoding);
  }


  /**
   * 实例对象
   */
  public class JsBuffer extends JSObject.Helper {
    private ByteBuffer buf;
    private String encoding;


    private JsBuffer(String encoding) {
      this.encoding = encoding;
      config(JSObject.ExportsFunction.class);
      config(new JSObject.ExportsAttribute("length"));
    }


    @Override
    public Object getSlot(int index) {
      return buf.get(index);
    }


    @Override
    public boolean hasSlot(int slot) {
      return slot >=0 && slot < buf.limit();
    }


    @Override
    public void setSlot(int index, Object value) {
      if (value instanceof Number) {
        byte v = ((Number) value).byteValue();
        buf.put(index, v);
      }
    }


    public int getLength() {
      return buf.limit();
    }


    public void setLength(int i) {
      // System.out.println("Buffer.length = "+ i +", do nothing");
    }


    public JsBuffer fill(byte value, int offset, int length) {
      for (int i=offset; i<length; ++i) {
        buf.put(i, value);
      }
      return this;
    }


    public JsBuffer fill(byte value, int offset) {
      return fill(value, offset, getLength() - offset);
    }


    public JsBuffer fill(byte value) {
      return fill(value, 0, buf.limit());
    }


    public JsBuffer fill(String str, int offset, int len, String encoding)
            throws UnsupportedEncodingException {
      return fill(str.getBytes(encoding)[0], offset, len);
    }


    public JsBuffer fill(String str, int offset, int len)
            throws UnsupportedEncodingException {
      return fill(str, offset, len, DEFAULT_ENCODING);
    }


    public JsBuffer fill(String str, int offset) throws UnsupportedEncodingException {
      return fill(str, offset, getLength()-offset, DEFAULT_ENCODING);
    }


    public JsBuffer fill(String str) throws UnsupportedEncodingException {
      return fill(str.getBytes(DEFAULT_ENCODING)[0], 0, getLength());
    }


    public int compare(JsBuffer target, int targetStart, int targetEnd,
                       int sourceStart, int sourceEnd) {
      if (target == this) return 0;

      int tlen = targetEnd - targetStart;
      int slen = sourceEnd - sourceStart;
      int len = Math.max(tlen, slen);

      for (int i=0; i<len; ++i) {
        int si = i + sourceStart;
        int ti = i + targetStart;
        if (si >= sourceEnd) return -1;
        if (ti >= targetEnd) return 1;

        byte s = this.buf.get(si);
        byte t = target.buf.get(ti);
        if (s > t) return 1;
        else if (s < t) return -1;
      }
      return 0;
    }


    public int compare(JsBuffer target) {
      return compare(target, 0, target.getLength(), 0, getLength());
    }


    public int copy(JsBuffer target, int targetStart,
                    int sourceStart, int sourceEnd) {
      int len = sourceEnd - sourceStart;
      for (int i = 0; i<len; ++i) {
        target.buf.put(i + targetStart, buf.get(i + sourceStart) );
      }
      return 0;
    }


    public int copy(JsBuffer t) {
      return copy(t, 0, 0, getLength());
    }


    public boolean equals(JsBuffer other) {
      buf.position(0);
      other.buf.position(0);
      return buf.equals(other.buf);
    }


    public boolean equals(Object[] other) {
      if (other.length != getLength())
        return false;

      try {
        for (int i = 0; i < other.length; ++i) {
          int a = (int) other[i];
          int b = Byte.toUnsignedInt(buf.get(i));
          if (a != b) {
            return false;
          }
        }
        return true;
      } catch(Exception e) {
        return false;
      }
    }


    public boolean equals(ScriptObjectMirror arr) {
      final int len = getLength();
      if (len != arr.size())
        return false;

      try {
        for (int i = 0; i < len; ++i) {
          int a = (int) arr.getSlot(i);
          int b = Byte.toUnsignedInt(buf.get(i));
          if (a != b) {
            return false;
          }
        }
        return true;
      } catch(Exception e) {
        return false;
      }
    }


    public JsBuffer clone() {
      JsBuffer newbuf = new JsBuffer(encoding);
      newbuf.buf = ByteBuffer.allocate(getLength());
      for (int i = getLength()-1; i>=0; --i) {
        newbuf.buf.put(i, buf.get(i));
      }
      return newbuf;
    }


    public String toString() {
      StringBuilder out = new StringBuilder();
      out.append("<Buffer");
      int len = Math.min(20, getLength());
      buf.position(0);
      while (--len >= 0) {
        out.append(" ");
        int d = Byte.toUnsignedInt(buf.get());
        out.append(Tool.hex(d >> 4));
        out.append(Tool.hex(d));
      }
      if (getLength() > 20) {
        out.append(" ..");
      }
      out.append('>');
      return out.toString();
    }


    public String toString(String encoding, int begin, int length)
            throws UnsupportedEncodingException {
      byte[] tmp = new byte[length];
      buf.position(begin);
      buf.get(tmp, 0, length);

      if (encoding.equalsIgnoreCase(BASE64)) {
        return Base64.getEncoder().encodeToString(tmp);
      }
      return new String(tmp, encoding);
    }


    public String toString(String encoding, int begin)
            throws UnsupportedEncodingException {
      return toString(encoding, begin, getLength() - begin);
    }


    public String toString(String encoding)
            throws UnsupportedEncodingException {
      return toString(encoding, 0, getLength());
    }


    public Iterator<?> entries() {
      return new Iterator<Object>() {
        int i = 0;
        public boolean hasNext() {
          return i < getLength();
        }
        public Object next() {
          return JSObject.createJSArray(i, buf.get(i++));
        }
      };
    }


    public Iterator<?> keys() {
      return new Iterator<Object>() {
        int i = 0;
        public boolean hasNext() {
          return i < getLength();
        }
        public Object next() {
          return i++;
        }
      };
    }


    public int indexOf(JsBuffer value, int offset) {
      int len = getLength();
      int vl = value.getLength();
      int vp = 0;

      for (int i=offset; i<len; ++i) {
        if (value.buf.get(vp) == buf.get(i)) {
          if (++vp >= vl) {
            return i + 1 - vl;
          }
        } else {
          vp = 0 ;
        }
      }
      return -1;
    }


    public int indexOf(JsBuffer value) {
      return indexOf(value, 0);
    }


    public int indexOf(String str, int offset, String encoding) {
      return indexOf(from(str, encoding), offset);
    }


    public int indexOf(String str, int offset) {
      return indexOf(str, offset, DEFAULT_ENCODING);
    }


    public int indexOf(String str) {
      return indexOf(str, 0, DEFAULT_ENCODING);
    }


    public int indexOf(int _i) {
      final byte b = (byte) _i;
      int len = getLength();
      for (int i=0; i<len; ++i) {
        if (b == buf.get(i)) {
          return i;
        }
      }
      return -1;
    }


    public int lastIndexOf(Object value) {
      throw new UnsupportedOperationException();
    }


    public boolean includes(String value, int byteOffset, String encoding) {
      return indexOf(value, byteOffset, encoding) >= 0;
    }


    public boolean includes(String value, int byteOffset) {
      return indexOf(value, byteOffset) >= 0;
    }


    public boolean includes(String value) {
      return indexOf(value) >= 0;
    }


    public boolean includes(JsBuffer value, int offset) {
      return indexOf(value, offset) >= 0;
    }


    public boolean includes(JsBuffer value) {
      return indexOf(value) >= 0;
    }


    public boolean includes(int c) {
      return indexOf(c) >= 0;
    }


    /**
     * allocUnsafe 创建的对象不支持 array() 方法
     */
    public JsBuffer slice(int start, int end) {
      try {
        byte[] inner = buf.array();
        JsBuffer ret = new JsBuffer(encoding);
        ret.buf = ByteBuffer.wrap(inner, start, end);

        return ret;
      } catch(UnsupportedOperationException e) {
        throw new UnsupportedOperationException(
                "cannot slice() use 'allocUnsafe' create Buffer", e);
      }
    }


    public JsBuffer slice(int start, double d) {
      return slice(start, (int) d);
    }


    public String toJSON() {
      byte[] data;
      if (buf.isDirect()) {
        data = new byte[getLength()];
        for (int i=0; i<data.length; ++i) {
          data[i] = buf.get(i);
        }
      } else {
        data = buf.array();
      }
      return "{\"type\":\"Buffer\",\"data\":"
              + Tool.getAdapter(byte[].class).toJson(data)
              + "}";
    }


    public JsBuffer swap16() {
      if (getLength() % 2 != 0 || getLength() < 2) {
        JSObject.throwJSRangeError(
                "Buffer size must be a multiple of 16-bits");
      }
      int len = getLength();
      byte t;
      for (int i=0; i<len; i+=2) {
        t = buf.get(i);
        buf.put(i, buf.get(i+1));
        buf.put(i+1, t);
      }
      return this;
    }


    public JsBuffer swap32() {
      if (getLength() % 4 != 0 || getLength() < 4) {
        JSObject.throwJSRangeError(
                "Buffer size must be a multiple of 32-bits");
      }
      int len = getLength();
      byte t;
      for (int i=0; i<len; i+=4) {
        t = buf.get(i);
        buf.put(i, buf.get(i+3));
        buf.put(i+3, t);
        t = buf.get(i+1);
        buf.put(i+1, buf.get(i+2));
        buf.put(i+2, t);
      }
      return this;
    }


    public JsBuffer swap64() {
      if (getLength() % 8 != 0 || getLength() < 8) {
        JSObject.throwJSRangeError(
                "Buffer size must be a multiple of 64-bits");
      }
      int len = getLength();
      byte t;
      for (int i=0; i<len; i+=8) {
        t = buf.get(i);
        buf.put(i, buf.get(i+7));
        buf.put(i+7, t);
        t = buf.get(i+1);
        buf.put(i+1, buf.get(i+6));
        buf.put(i+6, t);
        t = buf.get(i+2);
        buf.put(i+2, buf.get(i+5));
        buf.put(i+5, t);
        t = buf.get(i+3);
        buf.put(i+3, buf.get(i+4));
        buf.put(i+4, t);
      }
      return this;
    }


    public JsBuffer transcode(JsBuffer source, String fromEnc, String toEnc) {
      throw new UnsupportedOperationException();
    }


    public double readDoubleBE(int offset) {
      return buf.getDouble(offset);
    }


    public double readDoubleLE(int offset) {
      try {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf.getDouble(offset);
      } finally {
        buf.order(ByteOrder.BIG_ENDIAN);
      }
    }


    public float readFloatBE(int offset) {
      return buf.getFloat(offset);
    }


    public double readFloatLE(int offset) {
      try {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf.getFloat(offset);
      } finally {
        buf.order(ByteOrder.BIG_ENDIAN);
      }
    }


    public byte readInt8(int offset) {
      return buf.get(offset);
    }


    public short readInt16BE(int offset) {
      return buf.getShort(offset);
    }


    public short readInt16LE(int offset) {
      try {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf.getShort(offset);
      } finally {
        buf.order(ByteOrder.BIG_ENDIAN);
      }
    }


    public int readInt32BE(int offset) {
      return buf.getInt(offset);
    }


    public int readInt32LE(int offset) {
      try {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf.getInt(offset);
      } finally {
        buf.order(ByteOrder.BIG_ENDIAN);
      }
    }


    public int readIntBE(int offset, int byteLength) {
      throw new UnsupportedOperationException();
    }


    public int readIntLE(int offset, int byteLength) {
      throw new UnsupportedOperationException();
    }


    public int readUInt8(int offset) {
      return Byte.toUnsignedInt(readInt8(offset));
    }


    public int readUInt16BE(int offset) {
      return Short.toUnsignedInt(readInt16BE(offset));
    }


    public int readUInt16LE(int offset) {
      return Short.toUnsignedInt(readInt16LE(offset));
    }


    public Object readUInt32BE(int offset) {
      return wrap(Integer.toUnsignedLong(readInt32BE(offset)));
    }


    public Object readUInt32LE(int offset) {
      return wrap(Integer.toUnsignedLong(readInt32LE(offset)));
    }


    public Object readUIntBE(int offset, int byteLength) {
      return wrap(Integer.toUnsignedLong(readIntBE(offset, byteLength)));
    }


    public Object readUIntLE(int offset, int byteLength) {
      return wrap(Integer.toUnsignedLong(readIntLE(offset, byteLength)));
    }


    /**
     * jdk 8u111 之后 js 引擎不会将 long 自动转换为 Number;
     * 也不会将 Number 数字自动包装为 Long 传给 java 函数;
     */
    private Object wrap(long l) {
      return jdk.nashorn.internal.objects.NativeNumber.constructor(
              false, null, l);
    }


    public int write(String string, int offset, int length, String encoding) {
      byte[] bytes = string.getBytes(Charset.forName(encoding));
      int len = offset + length;

      if (len > getLength()) {
        len = getLength();
      }
      if (len > bytes.length) {
        len = bytes.length;
      }

      for (int i=offset; i<len; ++i) {
        buf.put(i, bytes[i-offset]);
      }
      return len - offset;
    }


    public int write(String string, int offset, int length) {
      return write(string, offset, length, DEFAULT_ENCODING);
    }


    public int write(String string, int offset) {
      return write(string, offset, getLength(), DEFAULT_ENCODING);
    }


    public int write(String string) {
      return write(string, 0, getLength(), DEFAULT_ENCODING);
    }


    public int writeDoubleBE(double value, int offset) {
      buf.putDouble(offset, value);
      return 8 + offset;
    }


    public int writeDoubleLE(double value, int offset) {
      try {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putDouble(offset, value);
        return 8 + offset;
      } finally {
        buf.order(ByteOrder.BIG_ENDIAN);
      }
    }


    public int writeFloatBE(float value, int offset) {
      buf.putFloat(offset, value);
      return 4 + offset;
    }


    public int writeFloatBE(double value, int offset) {
      return writeFloatBE((float) value, offset);
    }


    public int writeFloatLE(float value, int offset) {
      try {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putFloat(offset, value);
        return 4 + offset;
      } finally {
        buf.order(ByteOrder.BIG_ENDIAN);
      }
    }

    public int writeFloatLE(double value, int offset) {
      return writeFloatLE((float) value, offset);
    }


    public int writeInt8(int value, int offset) {
      buf.put(offset, (byte)value);
      return 1 + offset;
    }


    public int writeInt16BE(int value, int offset) {
      buf.putShort(offset, (short) value);
      return 2 + offset;
    }


    public int writeInt16LE(int value, int offset) {
      try {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putShort(offset, (short) value);
        return 2 + offset;
      } finally {
        buf.order(ByteOrder.BIG_ENDIAN);
      }
    }


    public int writeInt32BE(int value, int offset) {
      buf.putInt(offset, value);
      return 4 + offset;
    }


    public int writeInt32LE(int value, int offset) {
      try {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(offset, value);
        return 4 + offset;
      } finally {
        buf.order(ByteOrder.BIG_ENDIAN);
      }
    }


    public int writeIntBE(int value, int offset, int byteLength) {
      throw new UnsupportedOperationException();
    }


    public int writeIntLE(int value, int offset, int byteLength) {
      throw new UnsupportedOperationException();
    }


    public int writeUInt8(int value, int offset) {
      return writeInt8(value, offset);
    }


    public int writeUInt16BE(int value, int offset) {
      return writeInt16BE(value, offset);
    }


    public int writeUInt16LE(int value, int offset) {
      return writeInt16LE(value, offset);
    }


    public int writeUInt32BE(double value, int offset) {
      long l = (long) value;
      int low = (int)(0xFFFF & l);
      int hig = (int)((0xFFFF0000 & l) >> 16);
      writeInt16BE(hig, 0);
      writeInt16BE(low, 2);
      return 4 + offset;
    }


    public int writeUInt32LE(double value, int offset) {
      long l = (long) value;
      int low = (int)(0xFFFF & l);
      int hig = (int)((0xFFFF0000 & l) >> 16);
      writeInt16LE(hig, 2);
      writeInt16LE(low, 0);
      return 4 + offset;
    }


    public int writeUIntBE(int value, int offset, int byteLength) {
      throw new UnsupportedOperationException();
    }


    public int writeUIntLE(int value, int offset, int byteLength) {
      throw new UnsupportedOperationException();
    }


    public ByteBuffer _buffer() {
      return buf;
    }
  }
}
