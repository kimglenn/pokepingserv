package pokeping;

import java.sql.*;

public class Pokepingservice {
    public static void main(String[] args) throws SQLException {
        Connection conn = makeConnection();
        findAllPokeping(conn);
        buyPokeping(conn, 4);
        findminePokeping(conn,"mingyu");
    }

    private static void findminePokeping(Connection conn, int i) {
    }

    public static Connection makeConnection() {
        String url = "jdbc:mysql://169.211.207.102:3308/pokeping?serverTimezone=Asia/Seoul";
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("데이터베이스 연결중 ...");
            conn = DriverManager.getConnection(url, "poketeam", "0000");
            System.out.println("데이터베이스 연결 성공");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC 드라이버 검색 오류");
        } catch (SQLException e) {
            System.out.println("데이터베이스 연결 실패");
        }
        return conn;
    }

    public static void findAllPokeping(Connection conn) throws SQLException {
        String sql = "select * from pokeping";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet resultSet = pstmt.executeQuery();
        while (resultSet.next()) {
            int id = resultSet.getInt("poke_id");
            String name = resultSet.getString("name");
            String type = resultSet.getString("type");
            int power = resultSet.getInt("power");
            int hp = resultSet.getInt("hp");
            int price = resultSet.getInt("price");
            int count = resultSet.getInt("count");
            System.out.printf("id: %d, name: %s, type: %s, attack: %d, health: %s price: %d, count: %d\n", id, name, type, power, hp, price, count);
        }
    }


    public static void buyPokeping(Connection conn, int pokeid) throws SQLException {
        String userid = "11";
        try {
            conn.setAutoCommit(false);

            // 유저 테이블에 코인 차감
            String sql = "update user set user_coin = user_coin - (select price from pokeping where pokeping.poke_id =?)" +
                    "where user_id = ? and user_coin >= (select price from pokeping where pokeping.poke_id =?);";
            PreparedStatement updateUserStmt = conn.prepareStatement(sql);
            updateUserStmt.setInt(1, pokeid);
            updateUserStmt.setString(2, userid);
            updateUserStmt.setInt(3, pokeid);
            if (updateUserStmt.executeUpdate() <= 0) {
                throw new SQLException("코인이 부족합니다.");
            }

            // 마이포켓핑에 업데이트
            sql = "insert into mypokeping (user_id, poke_id) values (?,?);";
            PreparedStatement insertMypokepingStmt = conn.prepareStatement(sql);
            insertMypokepingStmt.setString(1, userid);
            insertMypokepingStmt.setInt(2, pokeid);
            insertMypokepingStmt.executeUpdate();

            // 포켓핑 구매 카운트 올리기
            sql = "update pokeping set count = count + 1 where poke_id =?;";
            PreparedStatement updatePokepingCountStmt = conn.prepareStatement(sql);
            updatePokepingCountStmt.setInt(1, pokeid);
            updatePokepingCountStmt.executeUpdate();

            conn.commit();
            System.out.println("포켓핑 구매에 성공하였습니다.");
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("오류 발생, 롤백합니다.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeException) {
                    closeException.printStackTrace();
                }
            }
        }
    }

    public static void findminePokeping(Connection conn, String userid) throws SQLException {
        String sql = "select * from pokeping p join mypokeping m on p.poke_id = m.poke_id where user_id = ?;";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, userid);
        ResultSet resultSet = pstmt.executeQuery();
        while (resultSet.next()) {
            int id = resultSet.getInt("poke_id");
            String name = resultSet.getString("name");
            String type = resultSet.getString("type");
            int power = resultSet.getInt("power");
            int hp = resultSet.getInt("hp");
            int count = resultSet.getInt("count");
            int price = resultSet.getInt("price");
            System.out.printf("id: %d, name: %s, type: %s, power: %d, hp: %d, count: %d, price: %d\n", id, name, type, power, hp, price, count);
        }
        String sql2 = "select user_coin from user where user_id =?;";
        PreparedStatement pstmt2 = conn.prepareStatement(sql2);
        pstmt2.setString(1, userid);
        ResultSet resultSet2 = pstmt2.executeQuery();
        if(resultSet2.next()){
            int coin = resultSet2.getInt("user_coin");
            System.out.println("보유 코인: "+coin);
        }
        resultSet.close();
        resultSet2.close();
        pstmt.close();
        pstmt2.close();
        conn.close();
        System.out.println("조회 성공");
    }
    public static void sellPokeping(Connection conn, int poke_id, int price) throws SQLException {

    }
}


