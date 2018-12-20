package com.demo.shrek.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 代码生成器
 *
 */
public interface SysGeneratorMapper {

	/**
	 * 查询表信息
	 *
	 * @param tableName 表名称
	 * @return
	 */
	Map<String, String> queryTable(String tableName);

	/**
	 * 查询表列信息
	 *
	 * @param tableName 表名称
	 * @return
	 */
	List<Map<String, String>> queryColumns(String tableName);
}
