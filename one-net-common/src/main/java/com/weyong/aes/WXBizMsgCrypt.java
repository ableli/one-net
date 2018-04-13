package com.weyong.aes;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;


@Slf4j
public class WXBizMsgCrypt {
    static Charset CHARSET = Charset.forName("utf-8");
    private static Cipher defaultEncryptCipher;
    private static Cipher defaultDecryptCipher;

    static {
        byte[] key = Base64.decodeBase64("woshihaogewoshihaogewoshihaogewoshihaogehao");
        try {
            defaultEncryptCipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec iv = new IvParameterSpec(key, 0, 16);
            defaultEncryptCipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
            defaultDecryptCipher = Cipher.getInstance("AES/CBC/NoPadding");
            defaultDecryptCipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
        } catch (Exception e) {

        }
    }

    Base64 base64 = new Base64();
    byte[] aesKey;
    String token;
    String appId;

    public WXBizMsgCrypt(String token, String encodingAesKey, String appId) throws AesException {
        if (encodingAesKey.length() != 43) {
            throw new AesException(AesException.IllegalAesKey);
        }

        this.token = token;
        this.appId = appId;
        aesKey = Base64.decodeBase64(encodingAesKey + "=");
    }

    public static byte[] getEncryptBytes(byte[] unencrypted, byte[] key) {
        try {
            Cipher cipher = null;
            return cipher.doFinal(unencrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encrypt Exception:" + e.getMessage());
        }
    }

    public static byte[] getDecryptBytes(byte[] encryptByte) {
        byte[] original;
        try {
            original = defaultDecryptCipher.doFinal(encryptByte);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
        return PKCS7Encoder.decode(original);
    }

    public static byte[] getEncryptBytes(byte[] unencrypted) {
        try {
            ByteGroup byteCollector = new ByteGroup();
            byteCollector.addBytes(unencrypted);
            byte[] padBytes = PKCS7Encoder.encode(byteCollector.size());
            byteCollector.addBytes(padBytes);
            return defaultEncryptCipher.doFinal(byteCollector.toBytes());
        } catch (Exception e) {
            log.error("Encrypt Exception:" + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public static byte[] getDecryptBytes(byte[] encryptByte, byte[] key) {
        byte[] original;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec key_spec = new SecretKeySpec(key, "AES");
            IvParameterSpec iv = new IvParameterSpec(Arrays.copyOfRange(key, 0, 16));
            cipher.init(Cipher.DECRYPT_MODE, key_spec, iv);

            original = cipher.doFinal(encryptByte);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
        return original;
    }

    byte[] getNetworkBytesOrder(int sourceNumber) {
        byte[] orderBytes = new byte[4];
        orderBytes[3] = (byte) (sourceNumber & 0xFF);
        orderBytes[2] = (byte) (sourceNumber >> 8 & 0xFF);
        orderBytes[1] = (byte) (sourceNumber >> 16 & 0xFF);
        orderBytes[0] = (byte) (sourceNumber >> 24 & 0xFF);
        return orderBytes;
    }

    int recoverNetworkBytesOrder(byte[] orderBytes) {
        int sourceNumber = 0;
        for (int i = 0; i < 4; i++) {
            sourceNumber <<= 8;
            sourceNumber |= orderBytes[i] & 0xff;
        }
        return sourceNumber;
    }

    String getRandomStr() {
        String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 16; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    String encrypt(String randomStr, String text) throws AesException {
        ByteGroup byteCollector = new ByteGroup();
        byte[] randomStrBytes = randomStr.getBytes(CHARSET);
        byte[] textBytes = text.getBytes(CHARSET);
        byte[] networkBytesOrder = getNetworkBytesOrder(textBytes.length);
        byte[] appidBytes = appId.getBytes(CHARSET);

        byteCollector.addBytes(randomStrBytes);
        byteCollector.addBytes(networkBytesOrder);
        byteCollector.addBytes(textBytes);
        byteCollector.addBytes(appidBytes);

        byte[] padBytes = PKCS7Encoder.encode(byteCollector.size());
        byteCollector.addBytes(padBytes);

        byte[] unencrypted = byteCollector.toBytes();

        try {
            byte[] encrypted = getEncryptBytes(unencrypted, aesKey);
            String base64Encrypted = base64.encodeToString(encrypted);

            return base64Encrypted;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AesException(AesException.EncryptAESError);
        }
    }

    String decrypt(String text) throws AesException {
        byte[] encrypted = Base64.decodeBase64(text);
        byte[] original = getDecryptBytes(encrypted, aesKey);

        String xmlContent, from_appid;
        try {
            byte[] bytes = PKCS7Encoder.decode(original);

            byte[] networkOrder = Arrays.copyOfRange(bytes, 16, 20);

            int xmlLength = recoverNetworkBytesOrder(networkOrder);

            xmlContent = new String(Arrays.copyOfRange(bytes, 20, 20 + xmlLength), CHARSET);
            from_appid = new String(Arrays.copyOfRange(bytes, 20 + xmlLength, bytes.length),
                    CHARSET);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AesException(AesException.IllegalBuffer);
        }

        if (!from_appid.equals(appId)) {
            throw new AesException(AesException.ValidateAppidError);
        }
        return xmlContent;

    }

    public String encryptMsg(String replyMsg, String timeStamp, String nonce) throws AesException {
        String encrypt = encrypt(getRandomStr(), replyMsg);

        if (timeStamp == "") {
            timeStamp = Long.toString(System.currentTimeMillis());
        }

        String signature = SHA1.getSHA1(token, timeStamp, nonce, encrypt);

        String result = XMLParse.generate(encrypt, signature, timeStamp, nonce);
        return result;
    }

    public String decryptMsg(String msgSignature, String timeStamp, String nonce, String postData)
            throws AesException {

        Object[] encrypt = XMLParse.extract(postData);

        String signature = SHA1.getSHA1(token, timeStamp, nonce, encrypt[1].toString());

        if (!signature.equals(msgSignature)) {
            throw new AesException(AesException.ValidateSignatureError);
        }

        String result = decrypt(encrypt[1].toString());
        return result;
    }

    public String verifyUrl(String msgSignature, String timeStamp, String nonce, String echoStr)
            throws AesException {
        String signature = SHA1.getSHA1(token, timeStamp, nonce, echoStr);

        if (!signature.equals(msgSignature)) {
            throw new AesException(AesException.ValidateSignatureError);
        }

        String result = decrypt(echoStr);
        return result;
    }

}