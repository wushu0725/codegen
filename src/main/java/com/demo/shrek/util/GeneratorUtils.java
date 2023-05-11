package com.demo.shrek.util;

import com.demo.shrek.model.test.ColumnEntity;
import com.demo.shrek.model.test.TableEntity;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器   工具类
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年12月19日 下午11:40:24
 */
public class GeneratorUtils {

    public static List<String> getTemplates() {
        List<String> templates = new ArrayList<String>();
        templates.add("template/index.vue.vm");
        templates.add("template/mapper.xml.vm");
        templates.add("template/service.java.vm");
        templates.add("template/serviceimpl.java.vm");
        templates.add("template/dao.java.vm");
        templates.add("template/mybatis-plus/controller.java.vm");

        return templates;
    }

    public static List<String> getPlusTemplates() {
        List<String> templates = new ArrayList<String>();
        templates.add("template/mybatis-plus/controller.java.vm");
        templates.add("template/mybatis-plus/service.java.vm");
        templates.add("template/mybatis-plus/mapper.java.vm");
        templates.add("template/mybatis-plus/mapper.xml.vm");
        templates.add("template/mybatis-plus/entity.java.vm");
        return templates;
    }

    public static List<String> getOtherTemplates(String isPlus) {
        List<String> templates = new ArrayList<String>();
        if ("1".equals(isPlus)) {
            templates.add("template/mybatis-plus/pom.xml.vm");
            templates.add("template/mybatis-plus/MybatisPlusConfig.java.vm");
            //templates.add("template/mybatis-plus/CrossConfig.java.vm");
            templates.add("template/mybatis-plus/Application.java.vm");
            templates.add("template/application.properties.vm");
            templates.add("template/Result.java.vm");
        }
        if (StringUtils.isEmpty(isPlus)) {
            templates = getOtherTemplates();
        }
        return templates;
    }

    public static List<String> getOtherTemplates() {
        List<String> templates = new ArrayList<String>();
        templates.add("template/pom.xml.vm");
        templates.add("template/application.properties.vm");
        templates.add("template/Application.java.vm");
        templates.add("template/Result.java.vm");
        return templates;
    }

    /**
     * 生成TableEntity
     */
    private static TableEntity initTableEntity(Map<String, String> table, List<Map<String, String>> columns) {
        //配置信息
        Configuration config = getConfig();

        //表信息
        TableEntity tableEntity = new TableEntity();
        tableEntity.setTableName(table.get("tableName"));
        //表名转换成Java类名
        String className = tableToJava(tableEntity.getTableName(), config.getString("tablePrefix"));
        tableEntity.setClassName(className);
        tableEntity.setClassname(StringUtils.uncapitalize(className));
        tableEntity.setPathName(StringUtils.lowerCase(className));
        //列信息
        List<ColumnEntity> columsList = new ArrayList<>();
        for (Map<String, String> column : columns) {
            ColumnEntity columnEntity = new ColumnEntity();
            columnEntity.setColumnName(column.get("columnName"));
            columnEntity.setDataType(column.get("dataType"));
            columnEntity.setComments(column.get("columnComment"));
            columnEntity.setExtra(column.get("extra"));

            //列名转换成Java属性名
            String attrName = columnToJava(columnEntity.getColumnName());
            columnEntity.setAttrName(attrName);
            columnEntity.setAttrname(StringUtils.uncapitalize(attrName));

            //列的数据类型，转换成Java类型
            String attrType = config.getString(columnEntity.getDataType(), "unknowType");
            columnEntity.setAttrType(attrType);

            //是否主键
            if ("PRI".equalsIgnoreCase(column.get("columnKey")) && tableEntity.getPk() == null) {
                tableEntity.setPk(columnEntity);
            }

            columsList.add(columnEntity);
        }
        tableEntity.setColumns(columsList);

        //没主键，则第一个字段为主键
        if (tableEntity.getPk() == null) {
            tableEntity.setPk(tableEntity.getColumns().get(0));
        }
        return tableEntity;
    }

    public static void generatorCode(Map<String, String> table, List<Map<String, String>> columns, String moduleName, ZipOutputStream zip) {

        //配置信息
        Configuration config = getConfig();

        //表信息
        TableEntity tableEntity = initTableEntity(table, columns);

        //封装模板数据
        VelocityContext context = initVelocityContext(tableEntity, moduleName);

        // 模板文件
        List<String> templates = getTemplates();

        // 输出zip
        writeZip(null, templates, tableEntity, context, config, zip, moduleName);
    }

    public static void generatePlusCode(String isPlus, Map<String, String> table, List<Map<String, String>> columns, String moduleName, ZipOutputStream zip) {

        //配置信息
        Configuration config = getConfig();

        //表信息
        TableEntity tableEntity = initTableEntity(table, columns);

        //封装模板数据
        VelocityContext context = initVelocityContext(tableEntity, moduleName);

        // 模板文件
        List<String> templates = getPlusTemplates();

        // 输出zip
        writeZip(isPlus, templates, tableEntity, context, config, zip, moduleName);

    }

    private static void writeZip(String isPlus, List<String> templates, TableEntity tableEntity, VelocityContext context, Configuration config, ZipOutputStream zip, String moduleName) {
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);
            try {
                //添加到zip
                zip.putNextEntry(new ZipEntry(
                        getFileName(isPlus, tableEntity.getTableName(), template,
                                config.getString("project"), tableEntity.getClassName(), config.getString("package"), config.getString("mainModule"), moduleName)));
                IOUtils.write(sw.toString(), zip, "UTF-8");
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
            } catch (IOException e) {
                throw new RuntimeException("渲染模板失败，表名：" + tableEntity.getTableName(), e);
            }
        }
    }

    /**
     * 生成代码
     */
    public static void generatorCode(Map<String, String> table,
                                     List<Map<String, String>> columns, ZipOutputStream zip) {
        //配置信息
        Configuration config = getConfig();

        //表信息
        TableEntity tableEntity = initTableEntity(table, columns);

        //封装模板数据
        VelocityContext context = initVelocityContext(tableEntity, null);

        //获取模板列表
        List<String> templates = getTemplates();
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);

            try {
                //添加到zip
                zip.putNextEntry(new ZipEntry(
                        getFileName(tableEntity.getTableName(), template, config.getString("project"), tableEntity.getClassName(), config.getString("package"), config.getString("mainModule"))));
                IOUtils.write(sw.toString(), zip, "UTF-8");
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
            } catch (IOException e) {
                throw new RuntimeException("渲染模板失败，表名：" + tableEntity.getTableName(), e);
            }
        }
    }

    /**
     * 根据TableEntity封装VelocityContext
     *
     * @param tableEntity
     * @param moduleName  后端模块名称
     * @return
     */
    private static VelocityContext initVelocityContext(TableEntity tableEntity, String moduleName) {
        //配置信息
        Configuration config = getConfig();
        //表名转换成Java类名
        String className = tableToJava(tableEntity.getTableName(), config.getString("tablePrefix"));
        //设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);
        //封装模板数据
        Map<String, Object> map = new HashMap<>();
        map.put("secondModuleName", toLowerCaseFirstOne(className));
        map.put("backmodule", moduleName);
        putTabEntityField2VelocityContext(map, tableEntity);
        putConfig2VelocityContext(map, config);
        return new VelocityContext(map);
    }

    /**
     * 将Velocity配置文件内容放入context
     *
     * @param map
     * @param config
     * @return
     */
    private static VelocityContext putConfig2VelocityContext(Map<String, Object> map, Configuration config) {
        Iterator<String> keys = config.getKeys();
        while (keys.hasNext()) {
            String next = keys.next();
            if (StringUtils.isNotBlank(next)) {
                map.put(next, config.getString(next));
            }
        }
        map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_PATTERN));
        if (map.containsKey("delColumn")) {
            if (!map.containsKey("delVal") || !map.containsKey("unDelVal")) {
                throw new RuntimeException("逻辑删除字段缺少逻辑删除值!");
            }
        }
        return new VelocityContext(map);
    }

    /**
     * TableEntity的get方法放入context
     *
     * @param map
     * @param tableEntity
     */
    private static void putTabEntityField2VelocityContext(Map<String, Object> map, TableEntity tableEntity) {
        Class<? extends TableEntity> tableEntityClass = tableEntity.getClass();
        List<Field> fields = Arrays.asList(tableEntityClass.getDeclaredFields());
        Iterator<Field> iterator = fields.iterator();

        while (iterator.hasNext()) {
            Field field = iterator.next();
            try {
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(), tableEntityClass);
                map.put(field.getName(), pd.getReadMethod().invoke(tableEntity));
            } catch (Exception e) {
                throw new RuntimeException("初始化模板出错: " + e);
            }
        }
    }


    /**
     * 列名转换成Java属性名
     */
    public static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
    }

    /**
     * 表名转换成Java类名
     */
    public static String tableToJava(String tableName, String tablePrefix) {
        if (StringUtils.isNotBlank(tablePrefix)) {
            tableName = tableName.replace(tablePrefix, "");
        }
        return columnToJava(tableName);
    }

    /**
     * 获取配置信息
     */
    public static Configuration getConfig() {
        try {
            return new PropertiesConfiguration("generator.properties");
        } catch (ConfigurationException e) {
            throw new RuntimeException("获取配置文件失败，", e);
        }
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String tableName, String template, String projectName, String
            className, String packageName, String moduleName) {
        String packagePath = projectName + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator;
        String frontPath = "ui" + File.separator;
        if (StringUtils.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator + tableName + File.separator;
        }

        if (template.contains("index.js.vm")) {
            return frontPath + "api" + File.separator + moduleName + File.separator + toLowerCaseFirstOne(className) + File.separator + "index.js";
        }

        if (template.contains("index.vue.vm")) {
            return frontPath + "views" + File.separator + moduleName + File.separator + toLowerCaseFirstOne(className) + File.separator + "index.vue";
        }

        if (template.contains("service.java.vm")) {
            return packagePath + "service" + File.separator + className + "Service.java";
        }

        if (template.contains("serviceimpl.java.vm")) {
            return packagePath + "service" + File.separator + "impl" + File.separator + className + "ServiceImpl.java";
        }

        if (template.contains("mapper.java.vm")) {
            return packagePath + "dao" + File.separator + className + "Dao.java";
        }

        if (template.contains("controller.java.vm")) {
            return packagePath + "controller" + File.separator + className + "Controller.java";
        }
        if (template.contains("mapper.xml.vm")) {
            return projectName + File.separator + "src" + File.separator + "main" + "resources" + File.separator + "mapper" + File.separator + className + "Mapper.xml";
        }

        if (template.contains("application.properties.vm")) {
            return projectName + File.separator + "src" + File.separator + "main" + File.separator + "java  " + File.separator + "resources" + File.separator + "application.properties";
        }

        if (template.contains("pom.xml.vm")) {
            return projectName + File.separator + "pom.xml";
        }

        return null;
    }


    /**
     * 获取文件名
     */
    public static String getFileName(String isPlus, String tableName, String template, String projectName, String
            className, String packageName, String fontModule, String backModule) {
        String packagePath = "src" + File.separator + "main" + File.separator + "java" + File.separator;
        String frontPath = "ui" + File.separator;
        if (StringUtils.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator + backModule + File.separator;
        }
        if (StringUtils.isEmpty(isPlus)) {
            if (template.contains("index.js.vm")) {
                return frontPath + "api" + File.separator + fontModule + File.separator + toLowerCaseFirstOne(className) + File.separator + "index.js";
            }
            if (template.contains("index.vue.vm")) {
                return frontPath + "views" + File.separator + fontModule + File.separator + toLowerCaseFirstOne(className) + File.separator + "index.vue";
            }

            if (template.contains("service.java.vm")) {
                return packagePath + "service" + File.separator + className + "Service.java";
            }

            if (template.contains("serviceimpl.java.vm")) {
                return packagePath + "service" + File.separator + "impl" + File.separator + className + "ServiceImpl.java";
            }

            if (template.contains("mapper.java.vm")) {
                return packagePath + "dao" + File.separator + className + "Dao.java";
            }

            if (template.contains("controller.java.vm")) {
                return packagePath + "controller" + File.separator + className + "Controller.java";
            }
            if (template.contains("mapper.xml.vm")) {
                return "src" + File.separator + "main" + File.separator + "resources" + File.separator + backModule + File.separator + "mapper" + File.separator + className + "Mapper.xml";
            }
        }

        if ("1".equals(isPlus)) {
            if (template.contains("mapper.xml.vm")) {
                return "src" + File.separator + "main" + File.separator + "resources" + File.separator + backModule + File.separator + "mapper" + File.separator + className + "Mapper.xml";
            }
            if (template.contains("mapper.java.vm")) {
                return packagePath + "mapper" + File.separator + className + "Mapper.java";
            }
            if (template.contains("controller.java.vm")) {
                return packagePath + "controller" + File.separator + className + "Controller.java";
            }
            if (template.contains("service.java.vm")) {
                return packagePath + "service" + File.separator + className + "Service.java";
            }
            if (template.contains("entity.java.vm")) {
                return packagePath + "entity" + File.separator + className + ".java";
            }

        }

        return null;
    }

    //首字母转小写
    public static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        } else {
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
        }
    }

    // 生成pom文件、配置文件和启动类
    public static void generatorPomAndPropertiesFile(String isPlus, ZipOutputStream zip) {

        Configuration config = getConfig();
        //设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);
        //封装模板数据
        Map<String, Object> map = new HashMap<>();
        VelocityContext context = putConfig2VelocityContext(map, config);
        context.put("isPlus", isPlus);

        // 模板文件
        List<String> templates = getOtherTemplates(isPlus);
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);
            try {
                //添加到zip
                zip.putNextEntry(new ZipEntry(getFileName(template, config.getString("package"), config.getString("common"))));
                IOUtils.write(sw.toString(), zip, "UTF-8");
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
            } catch (IOException e) {
                throw new RuntimeException("渲染模板失败" + e);
            }
        }
    }

    private static String getFileName(String template, String aPackage, String common) {
        if (template.contains("application.properties.vm")) {
            return File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "application.properties";
        }

        if (template.contains("pom.xml.vm")) {
            return File.separator + "pom.xml";
        }

        if (template.contains("Application.java.vm")) {
            return File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + aPackage.replace(".", File.separator) + File.separator + "Application.java";
        }

        if (template.contains("Result.java.vm")) {
            return File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + aPackage.replace(".", File.separator) + File.separator + common + File.separator + "Result.java";
        }

        if (template.contains("CrossConfig.java.vm")) {
            return File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + aPackage.replace(".", File.separator) + File.separator + common + File.separator + "config" + File.separator + "CrossConfig.java";
        }

        if (template.contains("MybatisPlusConfig.java.vm")) {
            return File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + aPackage.replace(".", File.separator) + File.separator + common + File.separator + "config" + File.separator + "MybatisPlusConfig.java";
        }

        if (template.contains("PageInterceptor.java.vm")) {
            template = template.replace(".vm", "");
            return File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + aPackage.replace(".", File.separator) + File.separator + common + File.separator + "config" + File.separator + template;
        }
        return null;
    }
}
