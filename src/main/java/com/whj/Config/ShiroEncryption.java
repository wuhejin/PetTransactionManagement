package com.whj.Config;

import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.util.ByteSource;

public class ShiroEncryption {

    public static String shiroencryption(String password , String salt){

        Md5Hash md5Hash1 = new Md5Hash(password,
                ByteSource.Util.bytes(salt),2);

        return md5Hash1.toString();
    }

}
