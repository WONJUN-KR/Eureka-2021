#!/usr/bin/env bash
# 2021 원본(유레카) 로컬 실행 - Oracle Free(docker) + Tomcat 9
# 사용: bash local/run.sh        → http://localhost:8099/form/mainPage.jsp
#       bash local/run.sh stop   → 톰캣·DB 컨테이너 정지
# 필요: docker, JDK 17 (JAVA_HOME), curl, unzip
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
WORK="$ROOT/local/.work"            # 받은 톰캣·드라이버·빌드 결과 (gitignore)
TOMCAT_VER=9.0.123
OJDBC_VER=23.5.0.24.07
DB_PORT=1531
HTTP_PORT=8099
CONTAINER=eureka-oracle

if [ "${1:-}" = "stop" ]; then
  "$WORK/tomcat9/bin/catalina.sh" stop 2>/dev/null || true
  docker stop "$CONTAINER" >/dev/null 2>&1 || true
  echo "정지함"; exit 0
fi

mkdir -p "$WORK"

# 1) DB: Oracle Free 컨테이너 (원본은 Oracle 전용 SQL - ROWNUM·시퀀스·to_char)
if ! docker ps --format '{{.Names}}' | grep -qx "$CONTAINER"; then
  docker start "$CONTAINER" >/dev/null 2>&1 || docker run -d --name "$CONTAINER" -p $DB_PORT:1521 \
    -e ORACLE_PASSWORD=eurekalocal -e APP_USER=eureka -e APP_USER_PASSWORD=eurekalocal gvenzl/oracle-free:slim-faststart >/dev/null
fi
echo "DB 기동 대기..."
# grep -q 는 찾자마자 끝나서 docker logs 가 SIGPIPE 로 실패 → pipefail 때문에 무한 대기. 개수로 확인
until [ "$(docker logs "$CONTAINER" 2>&1 | grep -c "DATABASE IS READY TO USE")" -gt 0 ]; do sleep 3; done
docker cp "$ROOT/local/schema-seed.sql" "$CONTAINER:/tmp/schema-seed.sql"
docker exec "$CONTAINER" bash -c "sqlplus -s eureka/eurekalocal@//localhost:1521/FREEPDB1 @/tmp/schema-seed.sql" >/dev/null
echo "스키마·시드 적용"
echo "톰캣·드라이버 준비..."

# 2) Tomcat 9 · Oracle JDBC 드라이버 (원본 ojdbc6 는 Oracle 23 에 접속 안 돼서 ojdbc11 사용)
if [ ! -d "$WORK/tomcat9" ]; then
  # 최신판은 dlcdn(빠름), 지난 판은 archive(느림)에만 있음 → 차례로 시도, sha512 확인
  for base in https://dlcdn.apache.org https://archive.apache.org/dist; do
    url="$base/tomcat/tomcat-9/v$TOMCAT_VER/bin/apache-tomcat-$TOMCAT_VER.tar.gz"
    curl -sfL --max-time 300 "$url" -o "$WORK/tomcat.tgz" && curl -sfL "$url.sha512" -o "$WORK/tomcat.sha512" && break
  done
  [ "$(shasum -a 512 "$WORK/tomcat.tgz" | cut -d' ' -f1)" = "$(cut -d' ' -f1 "$WORK/tomcat.sha512")" ] || { echo "톰캣 체크섬 불일치"; exit 1; }
  tar xzf "$WORK/tomcat.tgz" -C "$WORK" && mv "$WORK/apache-tomcat-$TOMCAT_VER" "$WORK/tomcat9" && rm "$WORK/tomcat.tgz"
  sed -i.bak -e 's/<Server port="8005"/<Server port="-1"/' -e "s/<Connector port=\"8080\"/<Connector port=\"$HTTP_PORT\"/" "$WORK/tomcat9/conf/server.xml"
fi
OJDBC="$WORK/ojdbc11-$OJDBC_VER.jar"
[ -f "$OJDBC" ] || curl -sfL "https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc11/$OJDBC_VER/ojdbc11-$OJDBC_VER.jar" -o "$OJDBC"

# 3) 빌드·배치: webapp 을 ROOT 로, 자바 소스는 WEB-INF/classes 로 컴파일
APP="$WORK/tomcat9/webapps/ROOT"
rm -rf "$WORK/tomcat9/webapps/"*
cp -R "$ROOT/webProject/src/main/webapp" "$APP"
rm -f "$APP/WEB-INF/lib/ojdbc6.jar" && cp "$OJDBC" "$APP/WEB-INF/lib/"
mkdir -p "$APP/WEB-INF/classes" "$APP/upload"
cp "$ROOT/webProject/src/main/webapp/images/recommendImages/"*.jpg "$APP/upload/"   # 시드 글 사진
"${JAVA_HOME:-/usr}/bin/javac" -encoding UTF-8 -proc:full \
  -cp "$WORK/tomcat9/lib/servlet-api.jar:$APP/WEB-INF/lib/*" -d "$APP/WEB-INF/classes" \
  $(find "$ROOT/webProject/src/main/java" -name "*.java")

# 4) 실행
CATALINA_OPTS="-Dfile.encoding=UTF-8" "$WORK/tomcat9/bin/catalina.sh" start >/dev/null
until curl -s -o /dev/null "http://localhost:$HTTP_PORT/"; do sleep 1; done
echo "실행됨 → http://localhost:$HTTP_PORT/form/mainPage.jsp  (테스트 계정은 local/schema-seed.sql 주석)"
