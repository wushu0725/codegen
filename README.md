# Java代码生成器
## 介绍
这是一个基于数据库表<br/>
基于springboot的
生成代码的代码生成器，生成结构<br/>
包括xml,dao,service,controller,vue页面代码，<br/>
完全按照 REST 增删改查风格，<br/>
没有业务逻辑，只有单表的增删改查，分页，<br/>
需要其他功能可以自己改<br/>

## 2023-04-28 by Nott
1、新增多模块生成，调用接口，传JSON，格式如下
{
    "param": {
        // 导出文件名称
        "project":"act",
        // 模块
        "module": [
            {   
                // 模块名
                "name": "user",
                // 表
                "table": [
                    {
                        "name": "user"
                    },
                    {
                        "name": "user_type"
                    }
                ]
            }
        ]
    }
}
2、添加pom文件、配置文件、启动类及通用结果模板文件


## 步骤
1，下载代码至本地，修改本地数据库链接<br/>
2，本地数据建表，必须有主键 ID,自增长    逻辑删除字段  delete_status<br/>
   每个字段注释即是 生成VUE页面的table表头<br/>
3, 项目跑起来 本地访问  http://localhost:8763/generator/code?tablename=表名<br/>
