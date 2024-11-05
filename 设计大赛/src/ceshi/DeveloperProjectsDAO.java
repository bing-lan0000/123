package ceshi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DeveloperProjectsDAO {
    
    // 打印开发者项目信息
    public void printDeveloperInfo(String developerName) {
        printDeveloperInfo(developerName, null, null); // 默认不筛选
    }

    // 新增的打印开发者项目信息方法，支持根据领域和国籍筛选
    public void printDeveloperInfo(String developerName, String domainsuffix, String country) {
        String sql = "SELECT '3D' AS type, project_name, stars, forks, commits, pr, last_commit FROM 3d_projects WHERE developer_name = ? " +
                     "AND (? IS NULL OR domainsuffix = ?) " +  // 更新为 domainsuffix
                     "AND (? IS NULL OR country = ?) " +
                     "UNION ALL " +
                     "SELECT 'AMP' AS type, project_name, stars, forks, commits, pr, last_commit FROM amp_projects WHERE developer_name = ? " +
                     "AND (? IS NULL OR domainsuffix = ?) " +  // 更新为 domainsuffix
                     "AND (? IS NULL OR country = ?) " +
                     "UNION ALL " +
                     "SELECT 'API' AS type, project_name, stars, forks, commits, pr, last_commit FROM api_projects WHERE developer_name = ? " +
                     "AND (? IS NULL OR domainsuffix = ?) " +  // 更新为 domainsuffix
                     "AND (? IS NULL OR country = ?) " +
                     "UNION ALL " +
                     "SELECT 'Android' AS type, project_name, stars, forks, commits, pr, last_commit FROM android_projects WHERE developer_name = ? " +
                     "AND (? IS NULL OR domainsuffix = ?) " +  // 更新为 domainsuffix
                     "AND (? IS NULL OR country = ?)";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, developerName);
            pstmt.setString(2, domainsuffix);
            pstmt.setString(3, domainsuffix);
            pstmt.setString(4, country);
            pstmt.setString(5, country);
            pstmt.setString(6, developerName);
            pstmt.setString(7, domainsuffix);
            pstmt.setString(8, domainsuffix);
            pstmt.setString(9, country);
            pstmt.setString(10, country);
            pstmt.setString(11, developerName);
            pstmt.setString(12, domainsuffix);
            pstmt.setString(13, domainsuffix);
            pstmt.setString(14, country);
            pstmt.setString(15, country);
            pstmt.setString(16, developerName);
            pstmt.setString(17, domainsuffix);
            pstmt.setString(18, domainsuffix);
            pstmt.setString(19, country);
            pstmt.setString(20, country);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String type = rs.getString("type");
                String projectName = rs.getString("project_name");
                int stars = rs.getInt("stars");
                int forks = rs.getInt("forks");
                int commits = rs.getInt("commits");
                int pr = rs.getInt("pr");
                String lastCommit = rs.getString("last_commit");

                System.out.println("领域: " + type + ", 项目名: " + projectName + ", Stars: " + stars +
                                   ", Forks: " + forks + ", 提交次数: " + commits + 
                                   ", 合并 PR 数: " + pr + ", 最后提交时间: " + lastCommit);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
