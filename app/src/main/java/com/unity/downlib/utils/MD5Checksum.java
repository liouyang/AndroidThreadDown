package com.unity.downlib.utils;

/**
 * User: 1241734684@qq.com
 * Description:
 * Date:2024-08-27 21
 * Time:11
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Checksum {

    // 计算文件的 MD5 值
    public static String getMD5Checksum(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md5Digest = MessageDigest.getInstance("MD5");

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[1024];
            int bytesRead;

            // 读取文件并更新 md5Digest
            while ((bytesRead = fis.read(byteArray)) != -1) {
                md5Digest.update(byteArray, 0, bytesRead);
            }
        }

        // 计算哈希值
        byte[] md5Bytes = md5Digest.digest();

        // 将字节数组转换为十六进制字符串
        StringBuilder sb = new StringBuilder();
        for (byte b : md5Bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    // 比较两个文件的 MD5 值
    public static boolean compareFiles(String md5Str, File file2) {
        try {
            String md5File1 =md5Str;
            String md5File2 = getMD5Checksum(file2);

            System.out.println("MD5 of File 1: " + md5File1);
            System.out.println("MD5 of File 2: " + md5File2);

            return md5File1.equalsIgnoreCase(md5File2);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }


}

