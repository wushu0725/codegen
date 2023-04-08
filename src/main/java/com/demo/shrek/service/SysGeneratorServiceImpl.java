package com.demo.shrek.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.demo.shrek.dao.SysGeneratorMapper;
import com.demo.shrek.util.GeneratorUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@Service
public class SysGeneratorServiceImpl implements SysGeneratorService{

    @Autowired
    private SysGeneratorMapper sysGeneratorMapper;


    public Map<String, String> queryTable(String tableName) {
        return sysGeneratorMapper.queryTable(tableName);
    }

    public List<Map<String, String>> queryColumns(String tableName) {
        return sysGeneratorMapper.queryColumns(tableName);
    }

    @Override
    public byte[] generatorCode(String tableName) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);

        List clomes = sysGeneratorMapper.queryColumns(tableName);


        Map<String, String> table = queryTable(tableName);
        //查询列信息
        List<Map<String, String>> columns = queryColumns(tableName);

        GeneratorUtils.generatorCode(table, columns, zip);

        System.out.println(clomes.size());

        IOUtils.closeQuietly(zip);
        return outputStream.toByteArray();
    }

    @Override
    public void generatorCodeForEachModule(JSONObject module,ZipOutputStream zip,ByteArrayOutputStream outputStream) {
        String name = module.getString("name");
        JSONArray tables = module.getJSONArray("table");

        Iterator iterator = tables.iterator();
        while (iterator.hasNext()){
            JSONObject table = (JSONObject) iterator.next();
            String tableName = table.getString("name");

            Map<String, String> tableInfo = queryTable(tableName);
            List<Map<String, String>> columns = queryColumns(tableName);

            if(!columns.isEmpty()){
                GeneratorUtils.generatorCode(tableInfo,columns,name,zip);
            }
        }
    }

    @Override
    public void generatorOtherFile(ZipOutputStream zip, ByteArrayOutputStream outputStream) {
        GeneratorUtils.generatorPomAndPropertiesFile(zip);
        org.apache.commons.io.IOUtils.closeQuietly(zip);
    }
}
