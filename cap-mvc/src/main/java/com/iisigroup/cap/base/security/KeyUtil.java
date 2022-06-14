package com.iisigroup.cap.base.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;

import javax.crypto.Cipher;

public class KeyUtil {
    
    @Deprecated
    public static final String RSA_CBC_TRANSFORMATION = "RSA/CBC/PKCS5Padding";
    //Weak Encryption: Insecure Mode of Operation
    public static final String RSA_OAEP_TRANSFORMATION = "RSA/ECB/OAEPWithSHA256AndMGF1Padding";
    public static final String AES_ALGORITHM = "AES";
    public static final String AES_CBC_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    
    public static void main(String[] args) throws Exception {
        System.out.println(verifyKey(new File("../../../tcb/prvt.key")
        		, new File("../../../tcb/pblc.key"), "RSA"));
    }

    public static boolean verifyKey(File publicKeyFile, File privateKeyFile, String algorithm) throws Exception {
        String testStr = "emosdnah-si-kram";
        byte[] testBytes = testStr.getBytes(StandardCharsets.UTF_8);
        byte[] encrypt = encrypt(testBytes, getKey(publicKeyFile), algorithm);
        byte[] decrypt = decrypt(encrypt, getKey(privateKeyFile), algorithm);
        //Privacy Violation: Heap Inspection
        ByteBuffer bbb = ByteBuffer.allocate(decrypt.length);
        bbb.put(testBytes).flip();
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = cs.decode(bbb);
        boolean result = testStr.equals(cb.toString());
        encrypt = null;
        decrypt = null;
        cb = null;
        return result;
    }

    public static byte[] encrypt(byte[] source, Key publicKey, String algorithm) throws Exception {
        //Weak Encryption: Inadequate RSA Padding
        //Cipher cipher = Cipher.getInstance(algorithm);
        Cipher cipher = Cipher.getInstance(RSA_OAEP_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(source);
    }

    public static byte[] decrypt(byte[] encrypt, Key privateKey, String algorithm) throws Exception {
        //Weak Encryption: Inadequate RSA Padding
        //Cipher cipher = Cipher.getInstance(algorithm);
        Cipher cipher = Cipher.getInstance(RSA_OAEP_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encrypt);
    }

    private static Key getKey(File keyFile) throws Exception {
        Key key;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(keyFile))) {
            key = (Key) ois.readObject();
        }
        return key;
    }
}
