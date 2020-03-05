package com.weikun.server.server.db;

import com.weikun.server.common.CodeUtil;
import com.weikun.server.common.MsgType;
import com.weikun.server.common.DateUtil;
import com.weikun.server.common.Token;

import java.sql.*;

/**
 * MySQL操作
 * @author weikun
 * */
public class DbHelper {
    /**
     * MySQL地址
     * */
    private static final String URL = "jdbc:mysql://localhost:3306/root?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";

    /**
     * 建立连接
     * @return SQL连接对象
     * */
    public static Connection connect() throws Throwable{
        //加载驱动
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new Throwable("连接错误");
        }

        String username = "root";
        String password = "password";
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (connection==null){
            throw new Throwable("连接失败");
        }
        return connection;
    }



    /**
     * 注册用户
     * @param name 用户名
     * @param code MD5加密用户密码
     * @return 注册成功
     * */
    public static boolean registerUser(String name,String code)throws Throwable{
        if (isUserNameExist(name)){
            return false;
        }
        Connection connection = connect();
        String base64Code = CodeUtil.base64Encode(code.getBytes());
        String sql = "INSERT INTO user (user_name,user_code) VALUES ('"+name+"','"+base64Code+"')";
        try {
            Statement st = connection.createStatement();
            int row = st.executeUpdate(sql);
            // 释放
            st.close();
            connection.close();
            return row > 0;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 检测用户名是否存在
     * @param name 用户名
     * */
    public static boolean isUserNameExist(String name) throws Throwable{
        Connection connection = connect();
        String sql = "SELECT user_id FROM user WHERE user_name = '"+name+"'";
        try {
            int id = -1;
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                id = rs.getInt("user_id");
            }
            // 释放
            st.close();
            connection.close();
            return id > 0;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 验证用户
     * @param name 用户名
     * @param code 用户MD5密码
     * @return 验证有效
     */
    public static boolean verifyUser(String name, String code)throws Throwable{
        Connection connection = connect();
        String sql = "SELECT user_code FROM user WHERE user_name='" + name + "'";
        try {
            String ret = null;
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                ret = rs.getString("user_code");
            }
            // 释放
            st.close();
            connection.close();
            return code.equals(ret);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 存储令牌
     * @param name  用户名
     * @param token 令牌
     * @param type 令牌类型 0长期 1短期
     * @return 存储成功
     */
    public static boolean saveToken(String name, String token,int type)throws Throwable{
        Connection connection = connect();
        String sql = "UPDATE user SET "+(type==0?"refresh_token":"access_token")+" = '" + token + "' WHERE user_name='" + name + "'";
        try {
            Statement st = connection.createStatement();
            int row = st.executeUpdate(sql);
            // 释放
            st.close();
            connection.close();
            return row > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 存储用户私钥1
     * @param name 用户名
     * @param pvKey 私钥
     * */
    public static boolean savePrivateKey(String name,String pvKey)throws Throwable{
        Connection connection = connect();
        String sql = "UPDATE user SET private_key = '"+pvKey+"' WHERE user_name = '"+name+"'";
        try {
            Statement st = connection.createStatement();
            int row = st.executeUpdate(sql);
            // 释放
            st.close();
            connection.close();
            return row > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getPrivateKey(String name)throws Throwable{
        Connection connection = connect();
        String sql = "SELECT private_key FROM user WHERE user_name = '"+name+"'";
        try {
            String key = null;
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                key = rs.getString("private_key");
            }
            // 释放
            st.close();
            connection.close();
            if (key==null) {
                return null;
            }
            return key;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 检查短期令牌 获取长期令牌
     * @param token 短期令牌
     * */
    private static String getRefreshToken(String token)throws Throwable{
        Connection connection = connect();
        String sql = "SELECT refresh_token FROM user WHERE access_token = '"+token+"'";
        try {
            String refreshToken = null;
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                refreshToken = rs.getString("refresh_token");
            }
            // 释放
            st.close();
            connection.close();
            return refreshToken;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 检查令牌(短期及长期)有效性
     * @param accessToken 短期令牌
     * @param refreshToken 长期令牌
     * @return
     * 0 有效
     * 1 短期令牌失效
     * 2 长期令牌失效
     * -1 令token无效
     * -2 解析异常
     * */
    public static int checkToken(String accessToken,String refreshToken)throws Throwable{
        //检查长期令牌
        String temp  = getRefreshToken(accessToken);
        if (!refreshToken.equals(temp)){
            return MsgType.TOKEN_INVALID;
        }
        //解析成token
        Token token1 = Token.getToken(refreshToken);
        Token token2 = Token.getToken(accessToken);
        if (token1==null||token2==null){
            return MsgType.TOKEN_INVALID;
        }
        if (!token1.isValid()){
            return MsgType.TOKEN_REFRESH_INVALID;
        }
        if (!token2.isValid()){
            return MsgType.TOKEN_ACCESS_INVALID;
        }
        return MsgType.TOKEN_AVAILABLE;
    }

    /**
     * 刷新 短期token
     * */
    public static String refreshAccessToken(String accessToken) throws Throwable{
        //解析成token数据结构
        Token token = Token.getToken(accessToken);
        if (token==null){
            return null;
        }
        //token中获取用户名
        String name = token.getUserInfo();
        //生成新的短期token
        Token newToken = Token.generateAccessToken(name);
        //更新短期token
        boolean ret = saveToken(name,newToken.getEncodedToken(),1);
        if (!ret){
            return null;
        }else {
            return newToken.getEncodedToken();
        }

    }

    /**
     * 检索用户名
     * @param refreshToken 长期令牌
     * */
    public static String getNameByToken(String refreshToken)throws Throwable{
        Connection connection = connect();
        String sql = "SELECT user_name FROM user WHERE refresh_token = '"+refreshToken+"'";
        try {
            String name = null;
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                name = rs.getString("user_name");
            }
            // 释放
            st.close();
            connection.close();
            return name;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 存储操作日志
     * @param refreshToken 用户长期令牌
     * @param type 操作类型
     * @param message 操作信息
     */
    public static boolean saveOpLog(String refreshToken,String type,String message)throws Throwable{
        String name = getNameByToken(refreshToken);
        if (name==null){
            return false;
        }
        return saveOpLogByName(name,type,message);
    }

    /**
     * 存储操作日志
     * @param name 用户名
     * @param type 操作类型
     * @param message 操作信息
     */
    public static boolean saveOpLogByName(String name,String type,String message)throws Throwable{
        Connection connection = connect();
        String date = DateUtil.getDate(DateUtil.YMDHMS);
        String sql = "INSERT INTO entity(op_date,op_user,op_type,op_message) VALUES('"+date+"','"
                +name+"','"+type+"','"+message+"')";
        try {
            Statement st = connection.createStatement();
            int row = st.executeUpdate(sql);
            st.close();
            connection.close();
            return row > 0;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }
}
