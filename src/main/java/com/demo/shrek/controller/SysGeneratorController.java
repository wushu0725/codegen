package com.demo.shrek.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.demo.shrek.service.SysGeneratorService;
import com.demo.shrek.util.GeneratorUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器
 */
@RestController
@AllArgsConstructor
@RequestMapping("/generator")
@Slf4j
public class SysGeneratorController {

    private final SysGeneratorService sysGeneratorService;

    /**
     * 生成代码
     */
    @GetMapping("/code")
    public void generatorCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String tablename = request.getParameter("tablename");
        String proname = request.getParameter("proname");
        System.out.println(tablename);
        byte[] data = sysGeneratorService.generatorCode(tablename);

        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\"" + proname + "\"" + ".zip");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");

        IOUtils.write(data, response.getOutputStream());

    }

    /**
     * 多模块生成
     */
    @PostMapping("/allTable")
    public void allTable(@RequestBody Map<String, Object> map) throws IOException {
        long start = System.currentTimeMillis();
        log.info("Start to generate code ------------------");

        JSONObject paramObj = JSONObject.parseObject(JSON.toJSONString(map.get("param")));
        String project = paramObj.getString("project");
        JSONArray moduleArray = paramObj.getJSONArray("module");
        Iterator iterator = moduleArray.iterator();
        String isPlus = paramObj.getString("isPlus");
        log.info("Query param：{}", paramObj.toJSONString());
        log.info("protect type：[{}]", StringUtils.isNotBlank(isPlus) && "1".equals(isPlus) ? "mybatis-plus" : "mybatis");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(stream);

        List<byte[]> data = new ArrayList<>();
        while (iterator.hasNext()) {
            JSONObject module = (JSONObject) iterator.next();
            sysGeneratorService.generatorCodeForEachModule(isPlus, module, zip, stream);
        }

        sysGeneratorService.generatorOtherFile(isPlus, zip, stream);
        IOUtils.closeQuietly(zip);
        data.add(stream.toByteArray());

        int length = 0;
        for (byte[] arr : data) {
            length += arr.length;
        }
        byte[] finallyData = new byte[length];
        int index = 0;
        for (byte[] datum : data) {
            System.arraycopy(datum, 0, finallyData, index, datum.length);
            index += datum.length;
        }
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\"" + project + "\"" + ".zip");
        response.addHeader("Content-Length", "" + finallyData.length);
        response.setContentType("application/octet-stream; charset=UTF-8");

        IOUtils.write(finallyData, response.getOutputStream());
        log.info("project {} generated [{}]ms ------------------", project, System.currentTimeMillis() - start);
    }
}
