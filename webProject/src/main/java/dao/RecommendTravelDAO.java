package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import vo.RecommendTravelVO;

public class RecommendTravelDAO {
	String driver = DbConfig.DRIVER;
	String url = DbConfig.URL;
	String user = DbConfig.USER;
	String password = DbConfig.PASSWORD;
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	StringBuffer sb = new StringBuffer();
	
	public RecommendTravelDAO() {
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
	
	/* 전체 목록 가져오기 */
	public ArrayList<RecommendTravelVO> getAll(){
		ArrayList<RecommendTravelVO> list = new ArrayList<RecommendTravelVO>();
		sb.setLength(0);
		sb.append("SELECT rc_no, city_no, small_img, big_img ");
		sb.append("FROM recommend_city ");
		try {
			pstmt = conn.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int rc_no = rs.getInt("rc_no");
				int city_no = rs.getInt("city_no");
				String small_img = rs.getString("small_img");
				String big_img = rs.getString("big_img");
				RecommendTravelVO vo = new RecommendTravelVO(rc_no, city_no, small_img, big_img);
				list.add(vo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
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
