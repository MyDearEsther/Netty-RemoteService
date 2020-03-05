package com.weikun.server.server.service;

import com.weikun.server.common.*;
import com.weikun.server.common.annotation.Service;
import com.weikun.server.common.annotation.ServiceMethod;
import com.weikun.server.common.entity.ClientToken;
import com.weikun.server.server.db.DbHelper;
import com.weikun.server.server.db.ServerLog;

import java.net.SocketAddress;

/**
 * 用户服务 实现类
 * */
@Service(MsgType.SERVICE_USER)
public class UserService{

    @ServiceMethod(value = "requestLogin",verify = false)
    public static String requestLogin(SocketAddress address,String name) throws Throwable{
        boolean userExist = DbHelper.isUserNameExist(name);
        if (!userExist){
            throw new Exception("user not exist");
        }
        MsgKey key = CodeUtil.generateKeyPair();
        //存储私钥1
        boolean ret = DbHelper.savePrivateKey(name,key.getPvKey());
        if (!ret){
            throw new Exception("private key save failed");
        }else {
            LogUtil.i(name+" "+address.toString()+"  request login");
            ServerLog.saveUserLog(address,name,MsgType.OP_LOGIN,"request login");
            return key.getPbKey();
        }
    }


    /**
     * 登录验证
     * */
    @ServiceMethod(value = "login",verify = false)
    public static byte[][] login(SocketAddress address,String name,byte[] code,String pbKey) throws Throwable{
        String pvKey = DbHelper.getPrivateKey(name);
        //解密 密码
        byte[] md5Pwd = CodeUtil.decryptText(code,pvKey);
        if (md5Pwd==null){
            throw new Throwable("decode failed");
        }
        //校验用户密码
        if (!DbHelper.verifyUser(name,CodeUtil.base64Encode(md5Pwd))){
            throw new Throwable("name or password incorrect");
        }
        //生成长期token
        Token refreshToken = Token.generateRefreshToken(name);
        //生成短期token
        Token accessToken = Token.generateAccessToken(name);
        //base64编码 长期token
        String encodedRefreshToken = refreshToken.getEncodedToken();
        //base64编码 短期token
        String encodedAccessToken = accessToken.getEncodedToken();
        //存储token base64编码
        if(!DbHelper.saveToken(name,encodedRefreshToken,0)
                ||!DbHelper.saveToken(name,encodedAccessToken,1)){
            return null;
        }
        //使用客户端的公钥2加密token
        byte[] encryptedRefreshToken = CodeUtil.encryptText(encodedRefreshToken.getBytes(),pbKey);
        byte[] encryptedAccessToken = CodeUtil.encryptText(encodedAccessToken.getBytes(),pbKey);
        byte[][] tokens = new byte[2][];
        tokens[0] = encryptedRefreshToken;
        tokens[1] = encryptedAccessToken;
        LogUtil.i(name+" "+address.toString()+"  login success");
        ServerLog.saveUserLog(address,name,MsgType.OP_LOGIN,"login success");
        //发放token
        return tokens;
    }

    @ServiceMethod(value = "checkToken",verify = false)
    public static int checkToken(SocketAddress address,String accessToken,String refreshToken)throws Throwable{
        int ret = DbHelper.checkToken(accessToken,refreshToken);
        LogUtil.i(address.toString()+" check token:"+ret);
        return ret;
    }

    @ServiceMethod(value = "refreshToken",verify = false)
    public static String refreshToken(SocketAddress address,String accessToken,String refreshToken)throws Throwable{
        LogUtil.i(address.toString()+" refresh token");
        int ret = checkToken(address,accessToken,refreshToken);
        if (ret==MsgType.TOKEN_AVAILABLE||ret==MsgType.TOKEN_ACCESS_INVALID){
            return DbHelper.refreshAccessToken(accessToken);
        }
        return null;
    }

    /**
     * 检查token是否有效
     * */
    public static boolean isTokenValid(ClientToken token){
        int ret = -1;
        try {
            ret = DbHelper.checkToken(token.getAccessToken(),token.getRefreshToken());
        }catch (Throwable t){
            t.printStackTrace();
        }
        return ret==0;
    }

    @ServiceMethod(value = "registerUser",verify = false)
    public static void registerUser(String name,String pwd){
        try {
            String code = CodeUtil.md5Encode(pwd);
            String base64Code = CodeUtil.base64Encode(code.getBytes());
            DbHelper.registerUser(name,base64Code);
            LogUtil.i("register user "+name+" success");
        }catch (Throwable t){
            LogUtil.i("register user "+name+" failed:"+t.getMessage());
        }
    }

//    @ServiceMethod(value = "removeUser",verify = false)
//    public static void removeUser(String name){
//        try {
//            DbHelper.removeUser(name);
//        }catch (Throwable t){
//            t.printStackTrace();
//        }
//    }
}
