# Troubleshooting

Troubleshooting is a form of problem solving, often applied to repair failed products. It is a logical, systematic search for the source of a problem in order to solve it, and make the product or process operational again. 

In this section you will find common problems that affect this product, and potencial solutions on how to solve them.

## Error: Too many open files

Sometimes in logs, you may see errors like:

```
RODA_HOME/logs/roda-wui.log:pt.gov.dgarq.roda.core.common.RODAClientException: Error connecting to Login service - Too many open files
RODA_HOME/logs/roda-wui.log:Caused by: java.net.SocketException: Too many open files
```

This can happen when server has a lot of files deployed. To see how many files server has open, get the PID of the process, and then run lsof | grep <PID> | wc. On many computers, the default maximum number of files that one process could open is low (e.g. 1024). 

To modify this limit, edit `/etc/security/limits.conf` adding the following:

```
* soft nofile 2048
* hard nofile 2048
```

This will allow process run by anyone to have 2048 files open. You will need to restart the computer for this changes to apply. You can also use the `ulimit` command to change it at runtime, but this command changes will not persist on the next boot.
