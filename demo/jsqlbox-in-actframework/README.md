### Transaction App Demo
This is a demo project to show use jSqlBox in ActFramework.
 
A simple Transaction Application built on top of ActFramework and jSqlBox 
To run this application: 
1. Inside the demo project folder jsqlbox-in-actframework, type `mvn clean compile exec:exec`
2. Go to your browser and type `http://localhost`. 
This demo use H2 memory database do test.

这个项目用来演示jSqlBox在ActFramework中的配置和使用。因为ActFramework不支持AOP切面编程，所以这里用了独立的第三方IOC/AOP工具Guice。

命令行下输入:  mvn clean compile exec:exec  
浏览器下查看： http://localhost  