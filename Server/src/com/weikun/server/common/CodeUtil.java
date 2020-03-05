package com.weikun.server.common;


import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
//import java.util.Base64;

/**
 * 加密/解密工具类
 *
 * @author weikun
 */
public class CodeUtil {

    /**
     * 计算文件MD5值
     */
    public static String getFileMd5(File file) {
        FileInputStream fileInputStream = null;
        try {
            MessageDigest MD5 = MessageDigest.getInstance("MD5");
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                MD5.update(buffer, 0, length);
            }
            return base64Encode(MD5.digest());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * MD5加密
     */
    public static String md5Encode(String inStr) throws Throwable {
        MessageDigest md5 = null;
        md5 = MessageDigest.getInstance("MD5");
        byte[] byteArray = inStr.getBytes("UTF-8");
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuilder hexValue = new StringBuilder();
        for (byte b : md5Bytes) {
            int val = ((int) b) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    /**
     * 加密位数
     */
    private static final int KEYSIZE = 1024;

    /**
     * 加密算法
     */
    private static final String ALGORITHM = "RSA";

    /**
     * 生成密钥对
     */
    public static MsgKey generateKeyPair() throws Throwable {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        keyPairGenerator.initialize(KEYSIZE);
        //生成密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return new MsgKey(keyPair);
    }

    /**
     * 提取公钥
     */
    public static RSAPublicKey loadPublicKey(String publicKeyStr) throws Throwable {
        byte[] buffer = base64Decode(publicKeyStr);
        if (buffer == null) {
            return null;
        }
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    /**
     * 提取私钥
     */
    public static RSAPrivateKey loadPrivateKey(String privateKeyStr) throws Throwable {
        byte[] buffer = base64Decode(privateKeyStr);
        if (buffer == null) {
            return null;
        }
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

//    /**
//     * Base64编码
//     */
//    public static String base64Encode(byte[] data) {
//        Base64.Encoder encoder = Base64.getMimeEncoder();
//        return encoder.encodeToString(data);
//    }

    /**
     * Base64编码
     */
    public static String base64Encode(byte[] data) {
        return new BASE64Encoder().encode(data);
    }

//    /**
//     * Base64解码
//     */
//    public static byte[] base64Decode(String data) {
//        Base64.Decoder decoder = Base64.getMimeDecoder();
//        return decoder.decode(data);
//    }

    /**
     * Base64解码
     */
    public static byte[] base64Decode(String data) {
        try {
            return new BASE64Decoder().decodeBuffer(data);
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 公钥加密
     */
    public static byte[] encryptText(byte[] data, String pbKey) throws Throwable {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        RSAPublicKey key = loadPublicKey(pbKey);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }


    /**
     * 私钥解密
     */
    public static byte[] decryptText(byte[] data, String pvKey) throws Throwable {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        RSAPrivateKey key = loadPrivateKey(pvKey);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }
}
