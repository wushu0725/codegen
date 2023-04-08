package com.demo.shrek.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.demo.shrek.service.SysGeneratorService;
import com.demo.shrek.util.GeneratorUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
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
        JSONObject paramObj = JSONObject.parseObject(JSON.toJSONString(map.get("param")));
        String project = paramObj.getString("project");
        JSONArray moduleArray = paramObj.getJSONArray("module");
        Iterator iterator = moduleArray.iterator();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(stream);

        List<byte[]> data = new ArrayList<>();
        while (iterator.hasNext()) {
            JSONObject module = (JSONObject) iterator.next();
            sysGeneratorService.generatorCodeForEachModule(module,zip,stream);
        }

        sysGeneratorService.generatorOtherFile(zip,stream);
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
    }
}
