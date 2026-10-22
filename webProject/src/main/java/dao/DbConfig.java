package dao;

/**
 * DB 접속 정보.
 * 2021 원본은 DAO 마다 팀 AWS RDS 주소·계정이 그대로 적혀 있었음 → 환경변수로 받고, 없으면 local/ 의 docker Oracle 기본값.
 */
public final class DbConfig {
	public static final String DRIVER = "oracle.jdbc.driver.OracleDriver";
	public static final String URL = env("EUREKA_DB_URL", "jdbc:oracle:thin:@//localhost:1531/FREEPDB1");
	public static final String USER = env("EUREKA_DB_USER", "eureka");
	public static final String PASSWORD = env("EUREKA_DB_PASSWORD", "eurekalocal");

	private DbConfig() {
	}

	private static String env(String key, String fallback) {
		String v = System.getenv(key);
		return v == null || v.isEmpty() ? fallback : v;
	}
}
