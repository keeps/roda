# 故障排除

故障排除是解决问题的一种形式，通常用于修复故障产品。它以逻辑性、系统性的方法，追根溯源解决问题，使产品或流程恢复运行。

在本节中，你将找到影响本产品的常见问题以及可能的解决方案。

## 错误：打开的文件过多

你可能会在日志中看到类似错误：

```
RODA_HOME/logs/roda-wui.log:pt.gov.dgarq.roda.core.common.RODAClientException：连接到登录服务器出错——打开的文件过多
RODA_HOME/logs/roda-wui.log:Caused by: java.net.SocketException：打开的文件过多
```

当服务器部署了大量文件时，会出现此情况。要查看服务器打开文件数，需获取进程PID，然后运行lsof | grep <PID> | wc。许多计算机默认的单进程最大打开文件数很低（如1024）。

要修改此限制，请编辑 `/etc/security/limits.conf`，添加以下内容：

```
* soft nofile 2048
* hard nofile 2048
```

这将允许任何人运行的进程可打开2048个文件。你需要重新启动计算机才能应用此更改。你也可以使用`ulimit`命令进行动态调整，但该调整在下次启动时将失效。
