package dao;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import vo.BoardListVO;

public class BoardListDAO {
	String driver = DbConfig.DRIVER;
	String url = DbConfig.URL;
	String user = DbConfig.USER;
	String password = DbConfig.PASSWORD;
	Connection conn = null;
	PreparedStatement pstmt = null;
	StringBuffer sb = new StringBuffer();
	ResultSet rs = null;

	public BoardListDAO() {
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);
			System.out.println(conn);
		} catch (ClassNotFoundException e) {
			System.out.println("드라이버 로딩 실패");
		} catch (SQLException e) {
			System.out.println("DB 연결 실패");
		}
	}
	public ArrayList<BoardListVO> selectALL(){
		sb.setLength(0);
		sb.append("SELECT BOARD_NO,USER_NO,CITY_NO, DETAIL_OPTION_NO, to_char(regdate,'yyyy/mm/dd')REGDATE,TITLE, CONTENTS,  HITS, RECOMMEND_CNT,REPORT_CNT,STATUS,COMENT,HIDE_DATE ");
		sb.append("from board ");
		ArrayList<BoardListVO> list = new ArrayList<BoardListVO>();
		try {
			pstmt = conn.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int board_no = rs.getInt("board_no");
				int user_no = rs.getInt("user_no");
				int city_no = rs.getInt("city_no");
				int detail_option_no = rs.getInt("detail_option_no");
				String regdate = rs.getString("regdate");
				String title = rs.getString("title");
				String contents = rs.getString("contents");
				int hits = rs.getInt("hits");
				int recommend_cnt = rs.getInt("recommend_cnt");
				int report_cnt = rs.getInt("report_cnt");
				int status = rs.getInt("status");
				String coment = rs.getString("coment");
				String hide_date = rs.getString("hide_date");
				BoardListVO vo = new BoardListVO(board_no, user_no, city_no, detail_option_no, regdate, title, contents, hits, recommend_cnt, report_cnt, status, coment, hide_date);
				list.add(vo);
			}
			     	
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return list;	
	}
	
	/* 지역, 디테일 옵션 선택 */
	public ArrayList<BoardListVO> selectAll_cdo(int startNum, int endNum, int city_no, int detail_option_no){
		ArrayList<BoardListVO> list = new ArrayList<BoardListVO>();
		sb.setLength(0);
		sb.append("SELECT RN, BOARD_NO,USER_NO,CITY_NO, DETAIL_OPTION_NO, REGDATE, TITLE, CONTENTS,  HITS, RECOMMEND_CNT,REPORT_CNT,STATUS,COMENT,HIDE_DATE ");
		sb.append("FROM(SELECT ROWNUM RN, BOARD_NO,USER_NO,CITY_NO, DETAIL_OPTION_NO, REGDATE, TITLE, CONTENTS,  HITS, RECOMMEND_CNT,REPORT_CNT,STATUS,COMENT,HIDE_DATE ");
		sb.append("FROM(SELECT BOARD_NO,USER_NO,CITY_NO, DETAIL_OPTION_NO, to_char(regdate,'yyyy/mm/dd') REGDATE, TITLE, CONTENTS,  HITS, RECOMMEND_CNT,REPORT_CNT,STATUS,COMENT,HIDE_DATE FROM board ");
		sb.append("WHERE CITY_NO = ? AND DETAIL_OPTION_NO = ? ORDER BY BOARD_NO DESC) ");
		sb.append("WHERE ROWNUM <= ?) ");
		sb.append("WHERE RN >= ? "); 
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, city_no);
			pstmt.setInt(2, detail_option_no);
			pstmt.setInt(3, endNum);
			pstmt.setInt(4, startNum);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int board_no = rs.getInt("board_no");
				int user_no = rs.getInt("user_no");
				String regdate = rs.getString("regdate");
				String title = rs.getString("title");
				String contents = rs.getString("contents");
				int hits = rs.getInt("hits");
				int recommend_cnt = rs.getInt("recommend_cnt");
				int report_cnt = rs.getInt("report_cnt");
				int status = rs.getInt("status");
				String coment = rs.getString("coment");
				String hide_date = rs.getString("hide_date");
				BoardListVO vo = new BoardListVO(board_no, user_no, city_no, detail_option_no, regdate, title, contents, hits, recommend_cnt, report_cnt, status, coment, hide_date);
				list.add(vo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/* 지역, 디테일 옵션 선택 총 게시물 수 */
	public int getTotal_cdo(int city_no, int detail_option_no){
		int cnt = 0;
		sb.setLength(0);
		sb.append("SELECT count(*) count ");
		sb.append("FROM board ");
		sb.append("WHERE CITY_NO = ? AND DETAIL_OPTION_NO = ? ");
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, city_no);
			pstmt.setInt(2, detail_option_no);
			rs = pstmt.executeQuery();
			rs.next();
			cnt = rs.getInt("count");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cnt;
	}
	
	/* 디테일 옵션 선택 */
	public ArrayList<BoardListVO> selectAll_do(int startNum, int endNum, int detail_option_no){
		ArrayList<BoardListVO> list = new ArrayList<BoardListVO>();
		sb.setLength(0);
		sb.append("SELECT RN, BOARD_NO,USER_NO,CITY_NO, DETAIL_OPTION_NO, REGDATE, TITLE, CONTENTS,  HITS, RECOMMEND_CNT,REPORT_CNT,STATUS,COMENT,HIDE_DATE ");
		sb.append("FROM(SELECT ROWNUM RN, BOARD_NO,USER_NO,CITY_NO, DETAIL_OPTION_NO, REGDATE, TITLE, CONTENTS,  HITS, RECOMMEND_CNT,REPORT_CNT,STATUS,COMENT,HIDE_DATE ");
		sb.append("FROM(SELECT BOARD_NO,USER_NO,CITY_NO, DETAIL_OPTION_NO, to_char(regdate,'yyyy/mm/dd') REGDATE, TITLE, CONTENTS,  HITS, RECOMMEND_CNT,REPORT_CNT,STATUS,COMENT,HIDE_DATE FROM board ");
		sb.append("WHERE DETAIL_OPTION_NO = ? ORDER BY BOARD_NO DESC) ");
		sb.append("WHERE ROWNUM <= ?) ");
		sb.append("WHERE RN >= ? ");
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, detail_option_no);
			pstmt.setInt(2, endNum);
			pstmt.setInt(3, startNum);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int board_no = rs.getInt("board_no");
				int user_no = rs.getInt("user_no");
				String regdate = rs.getString("regdate");
				String title = rs.getString("title");
				String contents = rs.getString("contents");
				int city_no = rs.getInt("city_no");
				int hits = rs.getInt("hits");
				int recommend_cnt = rs.getInt("recommend_cnt");
				int report_cnt = rs.getInt("report_cnt");
				int status = rs.getInt("status");
				String coment = rs.getString("coment");
				String hide_date = rs.getString("hide_date");
				BoardListVO vo = new BoardListVO(board_no, user_no, city_no, detail_option_no, regdate, title, contents, hits, recommend_cnt, report_cnt, status, coment, hide_date);
				list.add(vo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/* 디테일 옵션 선택 총 게시물 수 */
	public int getTotal_do(int detail_option_no){
		int cnt = 0;
		sb.setLength(0);
		sb.append("SELECT count(*) count ");
		sb.append("FROM board ");
		sb.append("WHERE DETAIL_OPTION_NO = ? ");
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, detail_option_no);
			rs = pstmt.executeQuery();
			rs.next();
			cnt = rs.getInt("count");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cnt;
	}

	/* 좋아요 누른 게시물 리스트(좋아요 누른 사람 기준) */
	public ArrayList<BoardListVO> selectAll_u(int like_user_no){
		ArrayList<BoardListVO> list = new ArrayList<BoardListVO>();
		sb.setLength(0);
		sb.append("SELECT BOARD_NO,USER_NO,CITY_NO, DETAIL_OPTION_NO, to_char(regdate,'yyyy/mm/dd') REGDATE, TITLE, CONTENTS,  HITS, RECOMMEND_CNT,REPORT_CNT,STATUS,COMENT,HIDE_DATE FROM board ");
		sb.append("WHERE BOARD_NO IN(SELECT BOARD_NO FROM RECOMMENDED_POSTS WHERE USER_NO = ? ) ORDER BY REGDATE DESC ");
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, like_user_no);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int board_no = rs.getInt("board_no");
				int user_no = rs.getInt("user_no");
				String regdate = rs.getString("regdate");
				String title = rs.getString("title");
				String contents = rs.getString("contents");
				int detail_option_no = rs.getInt("detail_option_no");
				int city_no = rs.getInt("city_no");
				int hits = rs.getInt("hits");
				int recommend_cnt = rs.getInt("recommend_cnt");
				int report_cnt = rs.getInt("report_cnt");
				int status = rs.getInt("status");
				String coment = rs.getString("coment");
				String hide_date = rs.getString("hide_date");
				BoardListVO vo = new BoardListVO(board_no, user_no, city_no, detail_option_no, regdate, title, contents, hits, recommend_cnt, report_cnt, status, coment, hide_date);
				list.add(vo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	
	/* 좋아요 누른 게시물 리스트(작성자 기준) */
	public ArrayList<BoardListVO> selectAll_pu(int posting_user_no){
		ArrayList<BoardListVO> list = new ArrayList<BoardListVO>();
		sb.setLength(0);
		sb.append("SELECT BOARD_NO,USER_NO,CITY_NO, DETAIL_OPTION_NO, to_char(regdate,'yyyy/mm/dd') REGDATE, TITLE, CONTENTS,  HITS, RECOMMEND_CNT,REPORT_CNT,STATUS,COMENT,HIDE_DATE FROM board ");
		sb.append("WHERE user_no = ? ORDER BY REGDATE DESC ");
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, posting_user_no);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int board_no = rs.getInt("board_no");
				int user_no = rs.getInt("user_no");
				String regdate = rs.getString("regdate");
				String title = rs.getString("title");
				String contents = rs.getString("contents");
				int detail_option_no = rs.getInt("detail_option_no");
				int city_no = rs.getInt("city_no");
				int hits = rs.getInt("hits");
				int recommend_cnt = rs.getInt("recommend_cnt");
				int report_cnt = rs.getInt("report_cnt");
				int status = rs.getInt("status");
				String coment = rs.getString("coment");
				String hide_date = rs.getString("hide_date");
				BoardListVO vo = new BoardListVO(board_no, user_no, city_no, detail_option_no, regdate, title, contents, hits, recommend_cnt, report_cnt, status, coment, hide_date);
				list.add(vo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/* 좋아요 누른 총 게시물 수 */
	public int getTotal_u(int like_user_no){
		int cnt = 0;
		sb.setLength(0);
		sb.append("SELECT count(*) count ");
		sb.append("FROM board ");
		sb.append("WHERE board_no IN(SELECT board_no FROM recommend_posts WHERE user_no = ? ) ");
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, like_user_no);
			rs = pstmt.executeQuery();
			rs.next();
			cnt = rs.getInt("count");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cnt;
	}
	
	/* 작성한 총 게시물 수 */
	public int getTotal_pu(int post_user_no){
		int cnt = 0;
		sb.setLength(0);
		sb.append("SELECT count(*) count ");
		sb.append("FROM board ");
		sb.append("WHERE user_no = ? ");
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, post_user_no);
			rs = pstmt.executeQuery();
			rs.next();
			cnt = rs.getInt("count");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cnt;
	}
	
	
	/* 게시물 작성 */
	public void insertBoard(int user_no, int city_no, int detail_option_no, String title, String contents, String coment) {
		sb.setLength(0);
		sb.append("INSERT INTO board(BOARD_NO,USER_NO,CITY_NO, DETAIL_OPTION_NO, REGDATE, TITLE, CONTENTS,  HITS, RECOMMEND_CNT,REPORT_CNT,STATUS,COMENT ) ");
		sb.append("VALUES(board_no_seq.NEXTVAL, ?, ?,?, SYSDATE, ?, ?, 0, 0, 0, 1, ?) ");
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, user_no);
			pstmt.setInt(2, city_no);
			pstmt.setInt(3, detail_option_no);
			pstmt.setString(4, title);
			pstmt.setString(5, contents);
			pstmt.setString(6, coment);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/* 현재 시퀀스 번호 리턴 */
	public int returnSeq() {
		int seq_no = 0;
		sb.setLength(0);
		sb.append("SELECT max(board_no) no ");
		sb.append("FROM board ");
		try {
			pstmt = conn.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();
			rs.next();
			seq_no = rs.getInt("no");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return seq_no;
	}
	
	/* 작성한 게시물 수 가져오기 */
	public int postingCnt(int user_no) {
		int cnt = 0;
		sb.setLength(0);
		sb.append("SELECT count(*) count ");
		sb.append("FROM board ");
		sb.append("WHERE user_no = ? ");
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, user_no);
			rs = pstmt.executeQuery();
			rs.next();
			cnt = rs.getInt("count");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cnt;
	}
	
	/* 좋아요 받은 수 받아오기 */
	public int recommendTotal(int user_no) {
		int cnt = 0;
		sb.setLength(0);
		sb.append("SELECT sum(recommend_cnt) count ");
		sb.append("FROM board ");
		sb.append("WHERE user_no = ? ");
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, user_no);
			rs = pstmt.executeQuery();
			rs.next();
			cnt = rs.getInt("count");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cnt;
	}
	
	/* 여행가자에서 추천 수 많은 게시물 가져오기 */
	public ArrayList<BoardListVO> top3Travel(){
		ArrayList<BoardListVO> list = new ArrayList<BoardListVO>();
		sb.setLength(0);
		sb.append("select board_no, title ");
		sb.append("FROM board ");
		sb.append("where detail_option_no IN ");
		sb.append("(select detail_option_no from detail_option where option_no IN ");
		sb.append("(select option_no from main_option where menu_no = 1)) ");
		sb.append("order by recommend_cnt desc ");
		try {
			pstmt = conn.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();
			for(int i=0; i<3;i++) {
				rs.next();
				int board_no = rs.getInt("board_no");
				String title = rs.getString("title");
				BoardListVO vo = new BoardListVO();
				vo.setBoard_no(board_no);
				vo.setTitle(title);
				list.add(vo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/* 맛집가자에서 추천 수 많은 게시물 가져오기 */
	public BoardListVO foodBest(){
		BoardListVO vo = new BoardListVO();
		sb.setLength(0);
		sb.append("select board_no, title ");
		sb.append("FROM board ");
		sb.append("where detail_option_no IN ");
		sb.append("(select detail_option_no from detail_option where option_no IN ");
		sb.append("(select option_no from main_option where menu_no = 2)) ");
		sb.append("order by recommend_cnt desc ");
		try {
			pstmt = conn.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();
			rs.next();
			int board_no = rs.getInt("board_no");
			String title = rs.getString("title");
			vo.setBoard_no(board_no);
			vo.setTitle(title);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return vo;
	}
	
	/* 놀러가자에서 추천 수 많은 게시물 가져오기 */
	public BoardListVO enjoyBest(){
		BoardListVO vo = new BoardListVO();
		sb.setLength(0);
		sb.append("select board_no, title ");
		sb.append("FROM board ");
		sb.append("where detail_option_no IN ");
		sb.append("(select detail_option_no from detail_option where option_no IN ");
		sb.append("(select option_no from main_option where menu_no = 3)) ");
		sb.append("order by recommend_cnt desc ");
		try {
			pstmt = conn.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();
			rs.next();
			int board_no = rs.getInt("board_no");
			String title = rs.getString("title");
			vo.setBoard_no(board_no);
			vo.setTitle(title);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return vo;
	}
	
	/* 게시물 하나 받아오기 */
	public BoardListVO selectOne(int board_no) {
		BoardListVO vo = null;
		
		sb.setLength(0);
		sb.append("SELECT BOARD_NO,USER_NO,CITY_NO, DETAIL_OPTION_NO, to_char(regdate,'yyyy/mm/dd') REGDATE, TITLE, CONTENTS,  HITS, RECOMMEND_CNT,REPORT_CNT,STATUS,COMENT,HIDE_DATE FROM board ");
		sb.append("WHERE board_no = ? ");
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, board_no);
			rs = pstmt.executeQuery();
			rs.next();
			int user_no = rs.getInt("user_no");
			int city_no = rs.getInt("city_no");
			int detail_option_no = rs.getInt("detail_option_no");
			String regdate = rs.getString("regdate");
			String title = rs.getString("title");
			String contents = rs.getString("contents");
			int hits = rs.getInt("hits");
			int recommend_cnt = rs.getInt("recommend_cnt");
			int report_cnt = rs.getInt("report_cnt");
			int status = rs.getInt("status");
			String coment = rs.getString("coment");
			String hide_date = rs.getString("hide_date");
			vo = new BoardListVO(board_no, user_no, city_no, detail_option_no, regdate, title, contents, hits, recommend_cnt, report_cnt, status, coment, hide_date);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return vo;
	}
	 /* 게시물 삭제하기 */
	public void deleteBoard(int board_no) {
	      sb.setLength(0);
	      sb.append("DELETE FROM BOARD ");
	      sb.append("WHERE BOARD_NO = ? ");
	      try {
	         pstmt = conn.prepareStatement(sb.toString());
	         pstmt.setInt(1, board_no);
	         pstmt.executeUpdate();
	      } catch (SQLException e) {
	         e.printStackTrace();
	      }
	      
	   }
	
	
	/* 게시물 수정하기 */
	public void updateBoard(int board_no, String title, String contents, String coment) {
	      sb.setLength(0);
	      sb.append("UPDATE BOARD SET TITLE = ? , CONTENTS = ?, COMENT = ? ");
	      sb.append("WHERE board_no = ?  ");
	      
	      try {
	         pstmt = conn.prepareStatement(sb.toString());
	         pstmt.setString(1, title);
	         pstmt.setString(2, contents);
	         pstmt.setString(3, coment);
	         pstmt.setInt(4, board_no);
	         pstmt.executeUpdate();
	      } catch (SQLException e) {
	         e.printStackTrace();
	      }
	   }
	
	
	
	public void close() {
		try {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}