# cmpp-物联网短信网关平台 v2

```
Q：如何打开？
A：我推荐使用Eclipse neon.3或者更高的版本
```

```
Q：是否需要配置信息？
A：你可能需要进行一些必要的参数配置
请在Config.properties中仿照示例进行配置更改
```

```
Q：如何启动项目？
A：导出成.jar文件（建议取名为cmpp.jar），再复制webapp/WEB-INF/lib目录与jar包同目录
目录结构
-cmpp.jar
-lib
   	--...
	--...
	--...
	...
然后输入如下命令：nohup java -jar cmpp.jar &
```

```
Q：怎么配置日志文件？
A：找到log4j.xml，
<param name="File" value="/home/xfsy/java/test/cmpp.out" /><!-- 设置File参数：日志输出文件名 -->
value的值就是日志文件的储存的地方，其他也可做相应的更改
```

```
Q：用java的webService调用是非不方便，我可以重新吗？
A：当然可以！！！
```

```
Q：有短信API吗？
A：有！名字叫叫"短信API.md"
```