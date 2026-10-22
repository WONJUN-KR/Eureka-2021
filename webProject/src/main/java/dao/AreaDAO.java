package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AreaDAO {
	String driver = DbConfig.DRIVER;
	String url = DbConfig.URL;
	String user = DbConfig.USER;
	String password = DbConfig.PASSWORD;
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	StringBuffer sb = new StringBuffer();
	
	public AreaDAO() {
		try {
			Class.forName(driver);
			conn=DriverManager.getConnection(url, user, password);
			System.out.println(conn);
		} catch (ClassNotFoundException e) {
			System.out.println("드라이버 로딩 실패");
		} catch (SQLException e) {
			System.out.println("db 연결 실패");
		}
	}
	
	/* 지역번호로 지역 이름 가져오기 */
	public String getTerritorial(int area_no) {
		String territorial = null;
		sb.setLength(0);
		sb.append("SELECT territorial ");
		sb.append("FROM area ");
		sb.append("WHERE area_no = ? ");
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, area_no);
			rs = pstmt.executeQuery();
			rs.next();
			territorial = rs.getString("territorial");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return territorial;
	}
	
	public void close() {
		try {
			if(rs!=null) rs.close();
			if(pstmt != null) pstmt.close();
			if(conn != null) conn.close();
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
