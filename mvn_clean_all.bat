cd  core
call mvn clean
call mvn eclipse:clean

cd..
cd demo

cd jbooox
call mvn clean
call mvn eclipse:clean

cd..
cd jsqlbox-beetlsql
call mvn clean
call mvn eclipse:clean

cd..
cd jsqlbox-in-spring
call mvn clean
call mvn eclipse:clean

cd..
cd jsqlbox-in-java8
call mvn clean
call mvn eclipse:clean

cd..
cd jsqlbox-xa-atomikos
call mvn clean
call mvn eclipse:clean
del tmlog*.log
del tmlog.lck


cd..
cd..

