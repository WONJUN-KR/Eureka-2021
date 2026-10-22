package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import vo.Detail_optionVO;

public class Detail_optionDAO {
	String driver = DbConfig.DRIVER;
	String url = DbConfig.URL;
	String user = DbConfig.USER;
	String password = DbConfig.PASSWORD;
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	StringBuffer sb = new StringBuffer();
	
	public Detail_optionDAO() {
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
	
	/* menu_no 기준으로 전체 리스트 가져오기 */
	public ArrayList<Detail_optionVO> selectAll(int menu_no){
		ArrayList<Detail_optionVO> list = new ArrayList<Detail_optionVO>();
		sb.setLength(0);
		sb.append("SELECT detail_option_no, option_no, detail_option_name ");
		sb.append("FROM detail_option ");
		sb.append("WHERE option_no IN(SELECT option_no FROM main_option where menu_no = ?) ");
		sb.append("ORDER BY detail_option_no asc ");
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, menu_no);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int detail_option_no = rs.getInt("detail_option_no");
				int option_no = rs.getInt("option_no");
				String detail_option_name = rs.getString("detail_option_name");
				Detail_optionVO vo = new Detail_optionVO(detail_option_no, option_no, detail_option_name);
				list.add(vo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/* 디테일 옵션 이름과 메인 옵션 이름으로 디테일 옵션 번호 받아오기*/
	public int getDetail_option_no(String option_name, String detail_option_name) {
		int detail_option_no = 0;
		sb.setLength(0);
		sb.append("SELECT detail_option_no ");
		sb.append("FROM detail_option ");
		sb.append("WHERE detail_option_name = ? AND ");
		sb.append("option_no = ( SELECT option_no FROM main_option WHERE option_name = ?) ");
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, detail_option_name);
			pstmt.setString(2, option_name);
			rs = pstmt.executeQuery();
			rs.next();
			detail_option_no = rs.getInt("detail_option_no");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return detail_option_no;
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
