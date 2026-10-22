package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ImageDAO {
	String driver = DbConfig.DRIVER;
	String url = DbConfig.URL;
	String user = DbConfig.USER;
	String password = DbConfig.PASSWORD;
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	StringBuffer sb = new StringBuffer();
	
	public ImageDAO() {
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
	
	/* 게시물 번호로 썸네일 이미지 가져오기 */
	public String getThumbnail(int board_no) {
		String thumbnail = null;
		sb.setLength(0);
		sb.append("SELECT img_url ");
		sb.append("FROM image ");
		sb.append("WHERE board_no = ? ");
		sb.append("ORDER BY img_no asc ");
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, board_no);
			rs = pstmt.executeQuery();
			rs.next();
			thumbnail = rs.getString("img_url");  
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return thumbnail;
	}
	
	/* 이미지 삽입 */
	public void insertImages(int board_no, String img_url) {
		sb.setLength(0);
		sb.append("INSERT INTO image(img_no, board_no, img_url, img_status) ");
		sb.append("VALUES(image_no_seq.NEXTVAL, ?, ?, 1) ");
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, board_no);
			pstmt.setString(2, img_url);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/* 게시물 번호로 해당 이미지 모두 가져오기 */
	public ArrayList<String> getImages(int board_no){
		ArrayList<String> list = new ArrayList<String>();
		sb.setLength(0);
		sb.append("SELECT img_url FROM image ");
		sb.append("WHERE board_no = ? ");
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, board_no);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				String img_url = rs.getString("img_url");
				list.add(img_url);
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
