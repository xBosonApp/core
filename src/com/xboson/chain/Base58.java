////////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
// 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
// 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
// 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
// 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
//
// 文件创建日期: 18-7-14 上午9:23
// 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/chain/Base58.java
// 授权说明版本: 1.1
//
// [ J.yanming - Q.412475540 ]
//
////////////////////////////////////////////////////////////////////////////////

package com.xboson.chain;

import com.xboson.been.XBosonException;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * The operations contained in this class are confined to their methods and are
 * entirely thread-safe. In fact, all of the methods
 * are implemented statically, and the instance-bound {@link #encode(byte[])}
 * and {@link #decode(String)} methods are non-static
 * simply to accommodate possible future compatibility with Apache Commons-Codec.
 *
 * @author Christopher Smith
 * @url https://github.com/Anujraval24/Base58Encoding
 *
 */
public class Base58 {

  public static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
  private static final char ENCODED_ZERO = ALPHABET[0];
  private static final int[] INDEXES = new int[128];

  static {
    Arrays.fill(INDEXES, -1);
    for (int i = 0; i < ALPHABET.length; i++) {
      INDEXES[ALPHABET[i]] = i;
    }
  }


  /**
   * Encodes the given bytes as a base58 string (no checksum is appended).
   *
   * @param input the bytes to encode
   * @return the base58-encoded string
   */
  public static String encode(byte[] input) {
    if (input.length == 0) {
      return "";
    }
    // Count leading zeros.
    int zeros = 0;
    while (zeros < input.length && input[zeros] == 0) {
      ++zeros;
    }
    // Convert base-256 digits to base-58 digits (plus conversion to ASCII characters)
    input = Arrays.copyOf(input, input.length); // since we modify it in-place
    char[] encoded = new char[input.length * 2]; // upper bound
    int outputStart = encoded.length;
    for (int inputStart = zeros; inputStart < input.length; ) {
      encoded[--outputStart] = ALPHABET[divmod(input, inputStart, 256, 58)];
      if (input[inputStart] == 0) {
        ++inputStart; // optimization - skip leading zeros
      }
    }
    // Preserve exactly as many leading encoded zeros in output as there were leading zeros in input.
    while (outputStart < encoded.length && encoded[outputStart] == ENCODED_ZERO) {
      ++outputStart;
    }
    while (--zeros >= 0) {
      encoded[--outputStart] = ENCODED_ZERO;
    }
    // Return encoded string (including encoded leading zeros).
    return new String(encoded, outputStart, encoded.length - outputStart);
  }


  /**
   * Decodes the given base58 string into the original data bytes.
   *
   * @param input the base58-encoded string to decode
   * @return the decoded data bytes
   */
  public static byte[] decode(String input) {
    if (input.length() == 0) {
      return new byte[0];
    }
    // Convert the base58-encoded ASCII chars to a base58 byte sequence (base58 digits).
    byte[] input58 = new byte[input.length()];
    for (int i = 0; i < input.length(); ++i) {
      char c = input.charAt(i);
      int digit = c < 128 ? INDEXES[c] : -1;
      if (digit < 0) {
        throw new XBosonException("char: "+ c +" index:"+ i);
      }
      input58[i] = (byte) digit;
    }
    // Count leading zeros.
    int zeros = 0;
    while (zeros < input58.length && input58[zeros] == 0) {
      ++zeros;
    }
    // Convert base-58 digits to base-256 digits.
    byte[] decoded = new byte[input.length()];
    int outputStart = decoded.length;
    for (int inputStart = zeros; inputStart < input58.length; ) {
      decoded[--outputStart] = divmod(input58, inputStart, 58, 256);
      if (input58[inputStart] == 0) {
        ++inputStart; // optimization - skip leading zeros
      }
    }
    // Ignore extra leading zeroes that were added during the calculation.
    while (outputStart < decoded.length && decoded[outputStart] == 0) {
      ++outputStart;
    }
    // Return decoded data (including original number of leading zeros).
    return Arrays.copyOfRange(decoded, outputStart - zeros, decoded.length);
  }


  public static BigInteger decodeToBigInteger(String input) {
    return new BigInteger(1, decode(input));
  }


  /**
   * Divides a number, represented as an array of bytes each containing a single digit
   * in the specified base, by the given divisor. The given number is modified in-place
   * to contain the quotient, and the return value is the remainder.
   *
   * @param number the number to divide
   * @param firstDigit the index within the array of the first non-zero digit
   *        (this is used for optimization by skipping the leading zeros)
   * @param base the base in which the number's digits are represented (up to 256)
   * @param divisor the number to divide by (up to 256)
   * @return the remainder of the division operation
   */
  private static byte divmod(byte[] number, int firstDigit, int base, int divisor) {
    // this is just long division which accounts for the base of the input digits
    int remainder = 0;
    for (int i = firstDigit; i < number.length; i++) {
      int digit = (int) number[i] & 0xFF;
      int temp = remainder * base + digit;
      number[i] = (byte) (temp / divisor);
      remainder = temp % divisor;
    }
    return (byte) remainder;
  }

}