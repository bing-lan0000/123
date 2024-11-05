package Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ceshi.DatabaseConnector;

public class TalentRankCalculator {

    // 从数据库中获取所有开发者的 TalentRank，并按 TalentRank 大小排序
    public void calculateAndFillTalentRank() {
        String selectDevelopers = "SELECT developer_name FROM developer_country";
        List<DeveloperRank> developerRanks = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectDevelopers);
             ResultSet rs = selectStmt.executeQuery()) {

            // 逐个开发者计算 TalentRank 并存入列表
            while (rs.next()) {
                String developerName = rs.getString("developer_name");
                float talentRank = calculateTalentRank(developerName);
                String grade = evaluateGrade(talentRank);
                
                developerRanks.add(new DeveloperRank(developerName, talentRank, grade));
            }

            // 根据 TalentRank 值进行降序排序
            Collections.sort(developerRanks, (a, b) -> Float.compare(b.talentRank, a.talentRank));

            // 将排序后的 TalentRank 和 grade 更新到数据库
            for (DeveloperRank developerRank : developerRanks) {
                updateDeveloperTalentRank(developerRank.developerName, developerRank.talentRank, developerRank.grade);
            }

            System.out.println("所有开发者的 TalentRank 已成功填充到 developer_country 表中，并按降序排序。");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 根据 stars、commits 和 pr 计算 TalentRank
    private float calculateTalentRank(String developerName) {
        String sql = "SELECT SUM(stars) AS total_stars, SUM(commits) AS total_commits, SUM(pr) AS total_pr " +
                     "FROM ( " +
                     "SELECT stars, commits, pr FROM 3d_projects WHERE developer_name = ? " +
                     "UNION ALL " +
                     "SELECT stars, commits, pr FROM amp_projects WHERE developer_name = ? " +
                     "UNION ALL " +
                     "SELECT stars, commits, pr FROM api_projects WHERE developer_name = ? " +
                     "UNION ALL " +
                     "SELECT stars, commits, pr FROM android_projects WHERE developer_name = ? " +
                     ") AS combined";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, developerName);
            pstmt.setString(2, developerName);
            pstmt.setString(3, developerName);
            pstmt.setString(4, developerName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int totalStars = rs.getInt("total_stars");
                int totalCommits = rs.getInt("total_commits");
                int totalPR = rs.getInt("total_pr");

                // 按权重计算 TalentRank
                return (totalStars * 0.4f) + (totalCommits * 0.4f) + (totalPR * 0.2f);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0f;
    }

    // 根据 TalentRank 值评定等级
    private String evaluateGrade(float talentRank) {
        if (talentRank >= 90) {
            return "A";
        } else if (talentRank >= 75) {
            return "B";
        } else if (talentRank >= 50) {
            return "C";
        } else {
            return "D";
        }
    }

    // 更新 developer_country 表中的 TalentRank 和 grade
    private void updateDeveloperTalentRank(String developerName, float talentRank, String grade) {
        String updateSql = "UPDATE developer_country SET TalentRank = ?, grade = ? WHERE developer_name = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            updateStmt.setFloat(1, talentRank);
            updateStmt.setString(2, grade);
            updateStmt.setString(3, developerName);
            updateStmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 内部类用于存储开发者的 TalentRank 及等级
    private static class DeveloperRank {
        String developerName;
        float talentRank;
        String grade;

        DeveloperRank(String developerName, float talentRank, String grade) {
            this.developerName = developerName;
            this.talentRank = talentRank;
            this.grade = grade;
        }
    }

    // 主方法执行 TalentRank 填充
    public static void main(String[] args) {
        TalentRankCalculator calculator = new TalentRankCalculator();
        calculator.calculateAndFillTalentRank();
    }
}
