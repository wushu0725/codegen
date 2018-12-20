package com.demo.shrek.service;

import com.demo.shrek.dao.SysGeneratorMapper;
import com.demo.shrek.util.GeneratorUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
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
}
