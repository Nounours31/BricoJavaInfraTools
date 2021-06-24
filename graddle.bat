@echo on

set Server=dell2259dsy

if /i "%Server%" == "dell2259dsy" (
	set JAVA_HOME=C:\java\jdk1.8.0_251
	set GRADDLE_HOME=E:\programfiles\gradle\gradle-6.8.3
) else (
	set JAVA_HOME=C:\java\jdk1.8.0_241
	set GRADDLE_HOME=E:\ProgramFiles\gradle-7.1
)

set PATH=%PATH%;%JAVA_HOME%;%JAVA_HOME%\bin;%GRADDLE_HOME%;%GRADDLE_HOME%\bin


