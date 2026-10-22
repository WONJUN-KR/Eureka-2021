-- 2021 유레카(teamZoom) 로컬 재현용 스키마 + 시드 (원본 DB 덤프가 없어 DAO SQL·VO·write.js 기준으로 재구성, 로컬 전용)
SET DEFINE OFF;
WHENEVER SQLERROR CONTINUE;
DROP TABLE recommend_posts PURGE;
DROP TABLE recommended_posts PURGE;
DROP TABLE image PURGE;
DROP TABLE board PURGE;
DROP TABLE recommend_city PURGE;
DROP TABLE city PURGE;
DROP TABLE area PURGE;
DROP TABLE detail_option PURGE;
DROP TABLE main_option PURGE;
DROP TABLE users PURGE;
DROP SEQUENCE board_no_seq;
DROP SEQUENCE image_no_seq;
DROP SEQUENCE user_no_seq;
WHENEVER SQLERROR EXIT FAILURE;
CREATE TABLE area (area_no NUMBER PRIMARY KEY, territorial VARCHAR2(50));
CREATE TABLE city (city_no NUMBER PRIMARY KEY, area_no NUMBER REFERENCES area, city_name VARCHAR2(50));
CREATE TABLE main_option (option_no NUMBER PRIMARY KEY, menu_no NUMBER, option_name VARCHAR2(50));
CREATE TABLE detail_option (detail_option_no NUMBER PRIMARY KEY, option_no NUMBER REFERENCES main_option, detail_option_name VARCHAR2(50));
CREATE TABLE users (user_no NUMBER PRIMARY KEY, id VARCHAR2(30) UNIQUE, pw VARCHAR2(100), nickname VARCHAR2(30), name VARCHAR2(30),
  email VARCHAR2(100), birth VARCHAR2(20), gender VARCHAR2(10), mailok VARCHAR2(10), grade NUMBER, status NUMBER);
CREATE TABLE board (board_no NUMBER PRIMARY KEY, user_no NUMBER, city_no NUMBER, detail_option_no NUMBER, regdate DATE, title VARCHAR2(200),
  contents VARCHAR2(4000), hits NUMBER, recommend_cnt NUMBER, report_cnt NUMBER, status NUMBER, coment VARCHAR2(1000), hide_date DATE);
CREATE TABLE image (img_no NUMBER PRIMARY KEY, board_no NUMBER, img_url VARCHAR2(300), img_status NUMBER);
CREATE TABLE recommend_city (rc_no NUMBER PRIMARY KEY, city_no NUMBER, small_img VARCHAR2(200), big_img VARCHAR2(200));
CREATE TABLE recommend_posts (user_no NUMBER, board_no NUMBER);
CREATE TABLE recommended_posts (user_no NUMBER, board_no NUMBER);
CREATE SEQUENCE board_no_seq START WITH 100;
CREATE SEQUENCE image_no_seq START WITH 100;
CREATE SEQUENCE user_no_seq START WITH 100;

-- 지역·도시 (write.js 배열 그대로)
INSERT INTO area VALUES (1,'수도권');
INSERT INTO area VALUES (2,'강원도');
INSERT INTO area VALUES (3,'경상도/부산');
INSERT INTO area VALUES (4,'제주도');
INSERT INTO area VALUES (5,'전라도/광주');
INSERT INTO area VALUES (6,'충청도/대전');
INSERT INTO city VALUES (1,1,'서울');
INSERT INTO city VALUES (2,1,'경기');
INSERT INTO city VALUES (3,1,'인천');
INSERT INTO city VALUES (4,2,'강릉');
INSERT INTO city VALUES (5,2,'속초');
INSERT INTO city VALUES (6,2,'춘천');
INSERT INTO city VALUES (7,2,'동해');
INSERT INTO city VALUES (8,2,'정선');
INSERT INTO city VALUES (9,3,'부산');
INSERT INTO city VALUES (10,3,'포항');
INSERT INTO city VALUES (11,3,'경주');
INSERT INTO city VALUES (12,3,'거제');
INSERT INTO city VALUES (13,3,'고성');
INSERT INTO city VALUES (14,4,'서귀포시');
INSERT INTO city VALUES (15,4,'제주시');
INSERT INTO city VALUES (16,4,'서제주');
INSERT INTO city VALUES (17,4,'동제주');
INSERT INTO city VALUES (18,5,'여수');
INSERT INTO city VALUES (19,5,'순천');
INSERT INTO city VALUES (20,5,'담양');
INSERT INTO city VALUES (21,5,'해남');
INSERT INTO city VALUES (22,6,'대전');
INSERT INTO city VALUES (23,6,'공주');
INSERT INTO city VALUES (24,6,'태안');
INSERT INTO city VALUES (25,6,'단양');

-- 메뉴(1 여행가자 / 2 맛집가자 / 3 놀러가자) → 누구와(main_option) → 세부 테마(detail_option)
INSERT INTO main_option VALUES (1,1,'나홀로 여행');
INSERT INTO main_option VALUES (2,1,'가족 여행');
INSERT INTO main_option VALUES (3,1,'우정 여행');
INSERT INTO main_option VALUES (4,1,'연인과 함께');
INSERT INTO main_option VALUES (5,1,'아이들 데리고');
INSERT INTO main_option VALUES (6,1,'반려동물과 함께');
INSERT INTO main_option VALUES (7,2,'어른들 모시고');
INSERT INTO main_option VALUES (8,2,'데이트 코스');
INSERT INTO main_option VALUES (9,2,'TV에 나온 맛집');
INSERT INTO main_option VALUES (10,2,'인스타 감성 맛집');
INSERT INTO main_option VALUES (11,2,'키즈존');
INSERT INTO main_option VALUES (12,2,'반려동물 입장 가능');
INSERT INTO main_option VALUES (13,3,'혼자놀기');
INSERT INTO main_option VALUES (14,3,'친구들과 같이');
INSERT INTO main_option VALUES (15,3,'커플끼리');
INSERT INTO main_option VALUES (16,3,'가족끼리');
INSERT INTO main_option VALUES (17,3,'어린아이들');
INSERT INTO main_option VALUES (18,3,'반려동물');

DECLARE
  TYPE t IS TABLE OF VARCHAR2(50);
  travel2 t := t('산으로','바다로','도심속으로','역사속으로','국내 속 작은 해외로');
  food2   t := t('한식','중식','일식','양식','분식','카페');
  joy     t;
  n NUMBER := 1;
  PROCEDURE add(opt NUMBER, items t) IS BEGIN
    FOR i IN 1..items.COUNT LOOP INSERT INTO detail_option VALUES (n, opt, items(i)); n := n + 1; END LOOP; END;
BEGIN
  FOR o IN 1..6 LOOP add(o, travel2); END LOOP;
  FOR o IN 7..12 LOOP add(o, food2); END LOOP;
  add(13, t('활동적인','힐링/휴식','자기개발','사진관'));
  add(14, t('스파/워터파크','테마파크','레저/액티비티','게임/스포츠','노래방/코노','사진관'));
  add(15, t('전시/공연','동물/식물원','공방/체험','만화/북카페','영화관','사진관'));
  add(16, t('전시/공연','동물/식물원','체험/레저','캠핑','사진관'));
  add(17, t('키즈카페','체험학습','전시/공연','동물/식물원'));
  add(18, t('반려동물 놀이터','반려동물 유치원','반려동물 물놀이','펫페어'));
END;
/

-- 테스트 계정 (로컬 전용): eureka / eureka1234
INSERT INTO users VALUES (1,'eureka','eureka1234','유레카','오원준','eureka@local.test','1995-01-01','남','N',1,0);
INSERT INTO users VALUES (2,'traveler','eureka1234','여행자','김여행','traveler@local.test','1996-01-01','여','N',1,0);

-- 추천 도시 (recommendTravel 10곳)
INSERT INTO recommend_city VALUES (1,1,'seoul.jpg','seoul2.jpg');
INSERT INTO recommend_city VALUES (2,4,'gangneung.jpg','gangneung2.jpg');
INSERT INTO recommend_city VALUES (3,5,'sokcho.jpg','sokcho2.jpg');
INSERT INTO recommend_city VALUES (4,3,'songdo.jpg','songdo2.jpg');
INSERT INTO recommend_city VALUES (5,9,'busan.jpg','busan2.jpg');
INSERT INTO recommend_city VALUES (6,18,'yeosu.jpg','yeosu2.jpg');
INSERT INTO recommend_city VALUES (7,15,'jeju.jpg','jeju2.jpg');
INSERT INTO recommend_city VALUES (8,2,'gapyeong.jpg','gapyeong2.jpg');
INSERT INTO recommend_city VALUES (9,20,'damyang.jpg','damyang2.jpg');
INSERT INTO recommend_city VALUES (10,10,'pohang.jpg','pohang2.jpg');

-- 게시글 12개 (메뉴·도시 골고루, 추천 수 다르게). detail_option_no: 여행 1~30 / 맛집 31~66 / 놀러가자 67~
INSERT INTO board VALUES (1,1,4,2,SYSDATE-12,'강릉 안목해변 커피거리','바다 보면서 커피 한 잔. 해 질 녘이 제일 예뻐요.',57,9,0,1,'주차는 해변 공영주차장',NULL);
INSERT INTO board VALUES (2,2,1,5,SYSDATE-11,'서울 서래마을 유럽 골목','작은 해외 같은 골목 산책 코스.',33,4,0,1,'평일 오전 추천',NULL);
INSERT INTO board VALUES (3,1,15,6,SYSDATE-10,'제주시 사려니숲길','숲 냄새 가득한 길. 비 오는 날도 좋아요.',41,6,0,1,'편한 신발 필수',NULL);
INSERT INTO board VALUES (4,2,9,13,SYSDATE-9,'부산 감천문화마을','알록달록 계단 마을, 사진 찍기 좋아요.',25,3,0,1,'오르막 많아요',NULL);
INSERT INTO board VALUES (5,1,5,35,SYSDATE-8,'속초 중앙시장 닭강정','줄 서서 먹는 이유가 있어요.',62,8,0,1,'오전에 가면 덜 기다려요',NULL);
INSERT INTO board VALUES (6,2,18,37,SYSDATE-7,'여수 낭만포차 해물','밤바다 보면서 해물 삼합.',38,5,0,1,'저녁 7시 이후 붐벼요',NULL);
INSERT INTO board VALUES (7,1,20,36,SYSDATE-6,'담양 죽녹원 옆 카페','대나무 숲 보면서 쉬는 카페.',19,2,0,1,'죽녹원 입장권 따로',NULL);
INSERT INTO board VALUES (8,2,10,49,SYSDATE-5,'포항 구룡포 대게','대게 철엔 무조건.',27,3,0,1,'11~3월이 제철',NULL);
INSERT INTO board VALUES (9,1,18,68,SYSDATE-4,'여수 밤바다 산책','혼자 걸어도 좋은 해안 산책로.',44,7,0,1,'케이블카도 같이',NULL);
INSERT INTO board VALUES (10,2,3,73,SYSDATE-3,'송도 센트럴파크 수상택시','친구들이랑 타기 좋아요.',21,2,0,1,'주말엔 예약',NULL);
INSERT INTO board VALUES (11,1,2,86,SYSDATE-2,'가평 자라섬 캠핑','가족 캠핑 성지.',36,5,0,1,'캠핑장 예약 필수',NULL);
INSERT INTO board VALUES (12,2,11,79,SYSDATE-1,'경주 황리단길 공방','커플 공방 체험 추천.',15,1,0,1,'체험은 예약제',NULL);
INSERT INTO image VALUES (1,1,'../upload/gangneung2.jpg',1);
INSERT INTO image VALUES (2,1,'../upload/gangneung.jpg',1);
INSERT INTO image VALUES (3,2,'../upload/seoul2.jpg',1);
INSERT INTO image VALUES (4,3,'../upload/jeju2.jpg',1);
INSERT INTO image VALUES (5,4,'../upload/busan2.jpg',1);
INSERT INTO image VALUES (6,5,'../upload/sokcho2.jpg',1);
INSERT INTO image VALUES (7,6,'../upload/yeosu.jpg',1);
INSERT INTO image VALUES (8,7,'../upload/damyang2.jpg',1);
INSERT INTO image VALUES (9,8,'../upload/pohang2.jpg',1);
INSERT INTO image VALUES (10,9,'../upload/yeosu2.jpg',1);
INSERT INTO image VALUES (11,10,'../upload/songdo2.jpg',1);
INSERT INTO image VALUES (12,11,'../upload/gapyeong.jpg',1);
INSERT INTO image VALUES (13,12,'../upload/seoul.jpg',1);
INSERT INTO recommend_posts VALUES (1,2);
INSERT INTO recommended_posts VALUES (1,2);
COMMIT;
EXIT;
