package games.strategy.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * A Java Implementation of the MD5Crypt function.
 * Modified from the GANYMEDE network directory management system
 * released under the GNU General Public License
 * by the University of Texas at Austin
 * http://tools.arlut.utexas.edu/gash2/
 * Original version from :Jonathan Abbey, jonabbey@arlut.utexas.edu
 * Modified by: Vladimir Silva, vladimir_silva@yahoo.com
 * Modification history:
 * 9/2005
 * - Removed dependencies on a MD5 private implementation
 * - Added built-in java.security.MessageDigest (MD5) support
 * - Code cleanup
 * <br>
 *
 * @deprecated Use SHA512(fast) or BCrypt(secure) in the future instead
 *             (kept for backwards compatibility)
 */
@Deprecated // MD5 is not secure, use BCrypt or SHA512 (fast instead
public class MD5Crypt {
  public static final String MAGIC = "$1$";
  // Character set allowed for the salt string
  private static final String SALTCHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
  // Character set of the encrypted password: A-Za-z0-9./
  private static final String itoa64 = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

  /**
   * Function to return a string from the set: A-Za-z0-9./
   *
   * @param size
   *        Length of the string
   * @param v
   *        value to be converted
   * @return A string of size (size) from the set A-Za-z0-9./
   */
  private static String to64(long v, int size) {
    final StringBuilder result = new StringBuilder();
    while (--size >= 0) {
      result.append(itoa64.charAt((int) (v & 0x3f)));
      v >>>= 6;
    }
    return result.toString();
  }

  private static void clearbits(final byte[] bits) {
    for (int i = 0; i < bits.length; i++) {
      bits[i] = 0;
    }
  }

  /**
   * convert an encoded unsigned byte value
   * into a int with the unsigned value.
   */
  private static int bytes2u(final byte inp) {
    return inp & 0xff;
  }

  /**
   * LINUX/BSD MD5Crypt function.
   *
   * @param password
   *        Password to be encrypted
   * @return The encrypted password as an MD5 hash
   */
  public static String crypt(final String password) {
    return crypt(password, newSalt());
  }

  /**
   * LINUX/BSD MD5Crypt function.
   *
   * @param salt
   *        Random string used to initialize the MD5 engine
   * @param password
   *        Password to be encrypted
   * @return The encrypted password as an MD5 hash
   */
  public static String crypt(final String password, String salt) {
    if (password == null) {
      throw new IllegalArgumentException("Null password!");
    }
    if (salt == null) {
      throw new IllegalArgumentException("Null salt!");
    }
    /*
     * Two MD5 hashes are used
     */
    final MessageDigest ctx;
    MessageDigest ctx1;
    try {
      ctx = MessageDigest.getInstance("md5");
      ctx1 = MessageDigest.getInstance("md5");
    } catch (final NoSuchAlgorithmException ex) {
      ex.printStackTrace();
      return null;
    }
    /* Refine the Salt first */
    /* If it starts with the magic string, then skip that */
    if (salt.startsWith(MAGIC)) {
      salt = salt.substring(MAGIC.length());
    }
    /* It stops at the first '$', max 8 chars */
    if (salt.indexOf('$') != -1) {
      salt = salt.substring(0, salt.indexOf('$'));
    }
    if (salt.length() > 8) {
      salt = salt.substring(0, 8);
    }
    /*
     * Transformation set #1:
     * The password first, since that is what is most unknown
     * Magic string
     * Raw salt
     */
    ctx.update(password.getBytes());
    ctx.update(MAGIC.getBytes());
    ctx.update(salt.getBytes());
    /* Then just as many characters of the MD5(pw,salt,pw) */
    ctx1.update(password.getBytes());
    ctx1.update(salt.getBytes());
    ctx1.update(password.getBytes());
    // ctx1.Final();
    byte[] finalState = ctx1.digest();
    for (int pl = password.length(); pl > 0; pl -= 16) {
      ctx.update(finalState, 0, pl > 16 ? 16 : pl);
    }
    /*
     * the original code claimed that finalState was being cleared
     * to keep dangerous bits out of memory,
     * but doing this is also required in order to get the right output.
     */
    clearbits(finalState);
    /* Then something really weird... */
    for (int i = password.length(); i != 0; i >>>= 1) {
      if ((i & 1) != 0) {
        ctx.update(finalState, 0, 1);
      } else {
        ctx.update(password.getBytes(), 0, 1);
      }
    }
    finalState = ctx.digest();
    /*
     * and now, just to make sure things don't run too fast
     * On a 60 Mhz Pentium this takes 34 msec, so you would
     * need 30 seconds to build a 1000 entry dictionary...
     * (The above timings from the C version)
     */
    // TODO: we are WAY faster than 60Mhz, this operation is likely nanoseconds.. (not even micro seconds, nano!)
    for (int i = 0; i < 1000; i++) {
      try {
        ctx1 = MessageDigest.getInstance("md5");
      } catch (final NoSuchAlgorithmException e0) {
        return null;
      }
      if ((i & 1) != 0) {
        ctx1.update(password.getBytes());
      } else {
        ctx1.update(finalState, 0, 16);
      }
      if ((i % 3) != 0) {
        ctx1.update(salt.getBytes());
      }
      if ((i % 7) != 0) {
        ctx1.update(password.getBytes());
      }
      if ((i & 1) != 0) {
        ctx1.update(finalState, 0, 16);
      } else {
        ctx1.update(password.getBytes());
      }
      // Final();
      finalState = ctx1.digest();
    }
    /* Now make the output string */
    final StringBuilder result = new StringBuilder();
    result.append(MAGIC);
    result.append(salt);
    result.append("$");
    /*
     * Build a 22 byte output string from the set: A-Za-z0-9./
     */
    long l = (bytes2u(finalState[0]) << 16) | (bytes2u(finalState[6]) << 8) | bytes2u(finalState[12]);
    result.append(to64(l, 4));
    l = (bytes2u(finalState[1]) << 16) | (bytes2u(finalState[7]) << 8) | bytes2u(finalState[13]);
    result.append(to64(l, 4));
    l = (bytes2u(finalState[2]) << 16) | (bytes2u(finalState[8]) << 8) | bytes2u(finalState[14]);
    result.append(to64(l, 4));
    l = (bytes2u(finalState[3]) << 16) | (bytes2u(finalState[9]) << 8) | bytes2u(finalState[15]);
    result.append(to64(l, 4));
    l = (bytes2u(finalState[4]) << 16) | (bytes2u(finalState[10]) << 8) | bytes2u(finalState[5]);
    result.append(to64(l, 4));
    l = bytes2u(finalState[11]);
    result.append(to64(l, 2));
    /* Don't leave anything around in vm they could use. */
    clearbits(finalState);
    return result.toString();
  }

  /**
   * Creates a new random salt that can be passed to {@link #crypt(String, String)}.
   *
   * @return A new random salt; never {@code null}.
   */
  public static String newSalt() {
    final StringBuilder salt = new StringBuilder();
    final Random rnd = new Random();
    // build a random 8 chars salt
    while (salt.length() < 8) {
      final int index = (int) (rnd.nextFloat() * SALTCHARS.length());
      salt.append(SALTCHARS.substring(index, index + 1));
    }
    return salt.toString();
  }

  public static String getSalt(final String encrypted) {
    final String valNoMagic = encrypted.substring(MAGIC.length());
    return valNoMagic.substring(0, valNoMagic.indexOf("$"));
  }
}
