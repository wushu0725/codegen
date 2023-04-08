package com.demo.shrek.service;

import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author lengleng
 * @date 2018/7/29
 */
public interface SysGeneratorService {
	/**
	 * 生成代码
	 *
	 * @param tableNames 表名称
	 * @return
	 */
	byte[] generatorCode(String tableNames);

	/**
	 * 根据模块生成代码，支持多表
	 * @param module 传入的模块JSON
	 * @param zip 输出流
	 * @param outputStream
	 */
	void generatorCodeForEachModule(JSONObject module, ZipOutputStream zip, ByteArrayOutputStream outputStream);

	/**
	 * 生成其他文件：pom、配置文件等
	 * @param zip
	 * @param outputStream
	 */
	void generatorOtherFile(ZipOutputStream zip, ByteArrayOutputStream outputStream);
}
