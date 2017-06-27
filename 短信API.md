# 物联网短信平台API接口

请求URL：http://127.0.0.1:8989/cmpp/v2
（此地址需要根据实际运行环境进行相应的更改）


* 开启指定客户的短信服务
```
方法：startCustomer
参数：customer_Id(必需, String, 客户编号)
返回：[return] status 请求状态/data 服务数据/error 请求错误
```


* 关闭指定客户的短信服务
```
方法：stopCustomer
参数：customer_Id(必需, String, 客户编号)
返回：[return] status 请求状态/data 服务数据/error 请求错误
```


* 测试网关短信连接
```
方法：testConn
参数：customer_Id(必需, String, 客户编号)
返回：[return] status 请求状态/data 服务数据/error 请求错误
说明：无测试失败信息返回
```


* 测试发短信服务
```
方法：sendMsgTest
参数：customer_Id(必需, String, 客户编号), phone(必需, String, 手机号码), msg(必需, String, 短信内容), sequence(必需, int, 短信编号[1-255]),
返回：[return] status 请求状态/data 服务数据/error 请求错误
说明：无测试失败信息返回
```


* 短信发送任务
```
方法：sendTask
参数：无
返回：[return] status 请求状态/data 服务数据/error 请求错误
说明：执行时间取决于发送任务，不确定，建议主动关闭
```


*  检测指定客户短信服务状态
```
方法：status
参数：customer_Id(必需, String, 客户编号)
返回：[return] status 请求状态/data 服务数据/error 请求错误
```


*  开启全部客户短信服务
```
方法：start
参数：无
返回：[return] status 请求状态/data 返回短信服务开启失败的客户编号，以逗号(,)隔开/error 请求错误
```


* 关闭短信功能(全部客户)
```
方法：close
参数：无
返回：[return] status 请求状态/data 服务数据/error 请求错误
```