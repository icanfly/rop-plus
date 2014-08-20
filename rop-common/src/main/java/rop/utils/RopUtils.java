package rop.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rop.Constants;
import rop.RopException;
import rop.thirdparty.org.apache.commons.lang3.LocaleUtils;
import rop.thirdparty.org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.*;

/**
 * <pre>
 *   ROP框架工具类
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public class RopUtils {

	private static final Logger logger = LoggerFactory.getLogger(RopUtils.class);

	/**
	 * 使用<code>secret</code>对paramValues按以下算法进行签名： <br/>
	 * uppercase(hex(sha1(secretkey1value1key2value2...secret))
	 *
	 * @param paramValues 参数列表
	 * @param secret
	 * @return
	 */
	public static String sign(Map<String, String> paramValues,Map<String,String> headerMap,Map<String,String> extInfoMap, String secret) {
		return sign(paramValues, null,headerMap,extInfoMap, secret);
	}

	/**
	 * 对paramValues进行签名，其中ignoreParamNames这些参数不参与签名
	 *
	 * @param paramValues
	 * @param ignoreParamNames
	 * @param secret
	 * @return
	 */
	public static String sign(Map<String, String> paramValues, Set<String> ignoreParamNames,Map<String,String> headerMap,Map<String,String> extInfoMap,String secret) {
		try {
			//密钥
			String contactStr = secret;
			//参数内容
			contactStr += contactValues(paramValues,ignoreParamNames);
			//Header
			contactStr += contactValues(headerMap,null);
			//自定义扩展
			contactStr += contactValues(extInfoMap,null);

			byte[] sha1Digest = getSHA1Digest(contactStr);
			return byte2hex(sha1Digest);
		} catch (IOException e) {
			throw new RopException(e);
		}
	}

	private static String contactValues(Map<String,String> values,Set<String> ignoreParamNames){
		StringBuilder sb = new StringBuilder();
		List<String> paramNames = new ArrayList<String>(values.size());
		paramNames.addAll(values.keySet());
		if (ignoreParamNames != null && ignoreParamNames.size() > 0) {
			for (String ignoreParamName : ignoreParamNames) {
				paramNames.remove(ignoreParamName);
			}
		}
		Collections.sort(paramNames);

		for (String paramName : paramNames) {
			sb.append(paramName).append(values.get(paramName));
		}

		return sb.toString();
	}

	public static String utf8Encoding(String value, String sourceCharsetName) {
		try {
			return new String(value.getBytes(sourceCharsetName), Constants.UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static byte[] getSHA1Digest(String data) throws IOException {
		byte[] bytes = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			bytes = md.digest(data.getBytes(Constants.UTF8));
		} catch (GeneralSecurityException gse) {
			throw new IOException(gse);
		}
		return bytes;
	}

	private static byte[] getMD5Digest(String data) throws IOException {
		byte[] bytes = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			bytes = md.digest(data.getBytes(Constants.UTF8));
		} catch (GeneralSecurityException gse) {
			throw new IOException(gse);
		}
		return bytes;
	}

	/**
	 * 二进制转十六进制字符串
	 *
	 * @param bytes
	 * @return
	 */
	private static String byte2hex(byte[] bytes) {
		StringBuilder sign = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				sign.append("0");
			}
			sign.append(hex.toUpperCase());
		}
		return sign.toString();
	}

	public static String getUUID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString().toUpperCase();
	}

	public static Locale getLocale(String localeStr) {
		try {
			return LocaleUtils.toLocale(localeStr);
		} catch (Exception e) {
			return Locale.SIMPLIFIED_CHINESE;
		}
	}

	private static boolean isValidLocale(Locale locale) {
		if (Locale.SIMPLIFIED_CHINESE.equals(locale) || Locale.ENGLISH.equals(locale)) {
			return true;
		}

		return false;
	}

	public static String encryptExtInfo(Map<String,String> extInfoMap){
		StringBuilder sb = new StringBuilder("");
		if(extInfoMap == null || extInfoMap.isEmpty()){
			return null;
		}
		for(Map.Entry<String,String> entry : extInfoMap.entrySet()){
			sb.append(entry.getKey()).append("\001").append(entry.getValue());
			sb.append("\002");
		}
		sb.deleteCharAt(sb.length() - 1);
		try {
			return URLEncoder.encode(sb.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			//do nothing
			throw new RopException(e);
		}
	}

	public static Map<String,String> decryptExtInfo(String extInfoStr){
		if(StringUtils.isBlank(extInfoStr)){
			return new HashMap<String,String>(2);
		}

		String extInfo = null;
		try {
			extInfo = URLDecoder.decode(extInfoStr, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RopException(e);
		}

		Map<String,String> extInfoMap = new HashMap<String,String>(2);
		String[] params = StringUtils.split(extInfo,"\002");
		for(String param : params){
			String[] paramKV = StringUtils.split(param,"\001");
			if(paramKV.length != 2){
				throw new RopException("ext info decrypt failed. extInfo param:"+param);
			}
			extInfoMap.put(paramKV[0],paramKV[1]);
		}
		return extInfoMap;
	}
}

