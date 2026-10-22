package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import vo.CityVO;

public class CityDAO {
	String driver = DbConfig.DRIVER;
	String url = DbConfig.URL;
	String user = DbConfig.USER;
	String password = DbConfig.PASSWORD;
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	StringBuffer sb = new StringBuffer();
	
	public CityDAO() {
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
	
	/* 전체 시티 받아오기 */
	public ArrayList<CityVO> selectAll(){
		ArrayList<CityVO> list = new ArrayList<CityVO>();
		sb.setLength(0);
		sb.append("SELECT city_no, area_no, city_name ");
		sb.append("FROM city ");
		try {
			pstmt = conn.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int city_no = rs.getInt("city_no");
				int area_no = rs.getInt("area_no");
				String city_name = rs.getString("city_name");
				CityVO vo = new CityVO(city_no, area_no, city_name);
				list.add(vo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/* 시/도 번호로 시/도 이름 가져오기 */
	public String getCity_name(int city_no) {
		String city_name = null;
		sb.setLength(0);
		sb.append("SELECT city_name ");
		sb.append("FROM city ");
		sb.append("WHERE city_no = ? ");
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, city_no);
			rs = pstmt.executeQuery();
			rs.next();
			city_name = rs.getString("city_name");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return city_name;
	}
	
	/* 시/도 이름으로 시/도 번호 가져오기 */
	public int getCity_no(String city_name) {
		int city_no = 0;
		sb.setLength(0);
		sb.append("SELECT city_no ");
		sb.append("FROM city ");
		sb.append("WHERE city_name = ? ");
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, city_name);
			rs = pstmt.executeQuery();
			rs.next();
			city_no = rs.getInt("city_no");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return city_no;
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
