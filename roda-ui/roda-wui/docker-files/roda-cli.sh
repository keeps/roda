#! /bin/bash


java -cp "/usr/local/tomcat/webapps/ROOT/WEB-INF/lib/*:/usr/local/tomcat/lib/*" org.roda.core.RodaCoreFactory $@
