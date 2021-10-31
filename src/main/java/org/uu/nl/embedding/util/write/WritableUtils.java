package org.uu.nl.embedding.util.write;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** Taken from org.apache.hadoop.commons and modified */
public final class WritableUtils {

  /**
   * Serializes a long to a binary stream with zero-compressed encoding. For -112 <= i <= 127, only
   * one byte is used with the actual value. For other values of i, the first byte value indicates
   * whether the long is positive or negative, and the number of bytes that follow. If the first
   * byte value v is between -113 and -120, the following long is positive, with number of bytes
   * that follow are -(v+112). If the first byte value v is between -121 and -128, the following
   * long is negative, with number of bytes that follow are -(v+120). Bytes are stored in the
   * high-non-zero-byte-first order.
   *
   * @param stream Binary output stream
   * @param i Long to be serialized
   * @throws java.io.IOException If an I/O error occurs
   */
  public static void writeVLong(DataOutput stream, long i) throws IOException {
    if (i >= -112 && i <= 127) {
      stream.writeByte((byte) i);
      return;
    }

    int len = -112;
    if (i < 0) {
      i ^= -1L; // take one's complement'
      len = -120;
    }

    long tmp = i;
    while (tmp != 0) {
      tmp = tmp >> 8;
      len--;
    }

    stream.writeByte((byte) len);

    len = len < -120 ? -(len + 120) : -(len + 112);

    for (int idx = len; idx != 0; idx--) {
      int shiftbits = (idx - 1) * 8;
      long mask = 0xFFL << shiftbits;
      stream.writeByte((byte) ((i & mask) >> shiftbits));
    }
  }

  /**
   * Reads a zero-compressed encoded long from input stream and returns it.
   *
   * @param stream Binary input stream
   * @throws java.io.IOException If an I/O error occurs
   * @return deserialized long from stream.
   */
  public static long readVLong(DataInput stream) throws IOException {
    byte firstByte = stream.readByte();
    int len = decodeVIntSize(firstByte);
    if (len == 1) {
      return firstByte;
    }
    long i = 0;
    for (int idx = 0; idx < len - 1; idx++) {
      byte b = stream.readByte();
      i = i << 8;
      i = i | b & 0xFF;
    }
    return isNegativeVInt(firstByte) ? ~i : i;
  }

  /**
   * Parse the first byte of a vint/vlong to determine the number of bytes
   *
   * @param value the first byte of the vint/vlong
   * @return the total number of bytes (1 to 9)
   */
  private static int decodeVIntSize(byte value) {
    if (value >= -112) {
      return 1;
    } else if (value < -120) {
      return -119 - value;
    }
    return -111 - value;
  }

  /**
   * Given the first byte of a vint/vlong, determine the sign
   *
   * @param value the first byte
   * @return is the value negative
   */
  private static boolean isNegativeVInt(byte value) {
    return value < -120 || value >= -112 && value < 0;
  }


}