package com.demo.shrek.controller;

import com.demo.shrek.service.SysGeneratorService;
import com.sun.javafx.collections.MappingChange;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

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

    @PostMapping("/allTable")
    public void allTable(@RequestBody Map<String,String> map) {
        String name = map.get("name");

    }
}
