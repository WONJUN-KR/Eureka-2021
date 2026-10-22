package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import vo.UsersVO;

public class UsersDAO {
	String driver = DbConfig.DRIVER;
	String url = DbConfig.URL;
	String user = DbConfig.USER;
	String password = DbConfig.PASSWORD;
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	StringBuffer sb = new StringBuffer();
	
	public UsersDAO() {
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
	
	/* 로그인 */
	public UsersVO loginUser(String id, String pw) {
		UsersVO vo = null;
		sb.setLength(0);
		sb.append("SELECT user_no, id, pw, nickname, name, email, birth, gender, mailok, grade, status ");
		sb.append("FROM users ");
		sb.append("WHERE id = ? AND pw = ? ");
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, id);
			pstmt.setString(2, pw);
			rs = pstmt.executeQuery();
			rs.next();
			int user_no = rs.getInt("user_no");
			String nickname = rs.getString("nickname");
			String name = rs.getString("name");
			String email = rs.getString("email");
			String birth = rs.getString("birth");
			String gender = rs.getString("gender");
			String mailok = rs.getString("mailok");
			int grade = rs.getInt("grade");
			int status = rs.getInt("status");
			vo = new UsersVO(user_no, id, pw, nickname, name, email, birth, gender, mailok, grade, status);
		} catch (SQLException e) {
			System.out.println("아이디 혹은 비밀번호가 틀렸습니다.");
		}
		return vo;
	}
	
	/* 회원가입 */
	public void insertUser(UsersVO vo) {
		sb.setLength(0);
		sb.append("INSERT INTO users(user_no, id, pw, nickname, name, email, birth, gender, mailok, grade, status)");
		sb.append("VALUES(user_no_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, 1, 0)");
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, vo.getId());
			pstmt.setString(2, vo.getPw());
			pstmt.setString(3, vo.getNickname());
			pstmt.setString(4, vo.getName());
			pstmt.setString(5, vo.getEmail());
			pstmt.setString(6, vo.getBirth());
			pstmt.setString(7, vo.getGender());
			pstmt.setString(8, vo.getMailok());
			
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/* 아이디 중복확인 */
	public boolean idDoubleCheck(String id) {
		boolean doubleCheck = false;
		sb.setLength(0);
		sb.append("SELECT id ");
		sb.append("FROM users ");
		sb.append("WHERE id = ? ");
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, id);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				doubleCheck = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return doubleCheck;
	}
	
	/* 닉네임 중복확인 */
	public boolean nicknameDoubleCheck(String nickname) {
		boolean doubleCheck = false;
		sb.setLength(0);
		sb.append("SELECT nickname ");
		sb.append("FROM users ");
		sb.append("WHERE nickname = ? ");
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, nickname);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				doubleCheck = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return doubleCheck;
	}
	
	/* 이메일 중복확인 */
	public boolean emailDoubleCheck(String email) {
		boolean doubleCheck = false;
		sb.setLength(0);
		sb.append("SELECT email ");
		sb.append("FROM users ");
		sb.append("WHERE email = ? ");
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, email);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				doubleCheck = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return doubleCheck;
	}
	
	/* 아이디 찾기 */
	public String findId(String name, String birth, String email) {
		String id = null;
		sb.setLength(0);
		sb.append("SELECT id ");
		sb.append("FROM users ");
		sb.append("WHERE name = ? AND birth = ? AND email = ? ");
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, name);
			pstmt.setString(2, birth);
			pstmt.setString(3, email);
			rs = pstmt.executeQuery();
			rs.next();
			id = rs.getString("id");
		} catch (SQLException e) {
			System.out.println("입력값에 해당하는 ID없음");
		}
		return id;
	}
	
	/* 비밀번호 찾기 */
	public boolean findPw(String id, String email) {
		boolean ok = false;
		sb.setLength(0);
		sb.append("SELECT pw ");
		sb.append("FROM users ");
		sb.append("WHERE id = ? AND email = ? ");
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, id);
			pstmt.setString(2, email);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				ok = true;
			}
		} catch (SQLException e) {
			System.out.println("입력값에 해당하는 정보 없음");
		}
		return ok;
	}
	
	/* 비밀번호 재설정 */
	public void resetPw(String id, String pw) {
		sb.setLength(0);
		sb.append("UPDATE users ");
		sb.append("SET pw = ? ");
		sb.append("WHERE id = ? ");
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, pw);
			pstmt.setString(2, id);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/* 회원 번호로 닉네임 가져오기 */
	public String getNickname(int user_no) {
		String nickname = null;
		sb.setLength(0);
		sb.append("SELECT nickname ");
		sb.append("FROM users ");
		sb.append("WHERE user_no = ? ");
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, user_no);
			rs = pstmt.executeQuery();
			rs.next();
			nickname = rs.getString("nickname");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return nickname;
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
