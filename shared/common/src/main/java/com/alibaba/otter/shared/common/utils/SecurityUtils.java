package com.alibaba.otter.shared.common.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 类SecurityUtils.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2011-11-14 上午10:48:03
 */
public class SecurityUtils {

    /**
     * MD5 加密
     */
    public static String getMD5Str(String str) {
        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance("MD5");

            messageDigest.reset();

            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException caught!");
            System.exit(-1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] byteArray = messageDigest.digest();

        StringBuffer md5StrBuff = new StringBuffer();

        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }

        return md5StrBuff.toString();
    }

    public static String getPassword(String str) {
        StringBuffer password = new StringBuffer();
        String md5Str = getMD5Str(str);
        password.append(md5Str.substring(26, 32));
        password.append(md5Str.substring(10, 17));
        password.append(md5Str.substring(15, 22));
        return password.toString();
    }

    // public static void main(String[] args) {
    // System.out.println(getMD5Str("operator"));
    // System.out.println(getPassword("operator"));
    // }

}
