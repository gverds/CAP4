# CAP for Spring Framework 6.x
測試（有順序性）
### 1.Package
	mvn clean package -Dmaven.test.skip=true -q
### 2.USE H2 DATABASE(start DB)
	main class: org.h2.tools.Server
	參數: -webAllowOthers -tcpAllowOthers -tcpPort 51065 -webPort 51066 -ifNotExists
### 3.USE Maven plugin Jetty server(start Web Application)
	command: mvnDebug -Djetty.reload=automatic -Djetty.scan=2 jetty:run
	啟動時不想看到INFO -Dorg.slf4j.simpleLogger.defaultLogLevel=warn


*** ps.有誤的地方，請勘誤
