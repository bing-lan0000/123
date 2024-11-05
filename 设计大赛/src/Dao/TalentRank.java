package Dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TalentRank {
    // 计算并更新所有开发者的 TalentRank
    public static void calculateAndUpdateTalentRank() {
        String url = "jdbc:mysql://localhost:3306/大赛数据"; // 替换为你的数据库连接URL
        String user = "root"; // 替换为你的数据库用户名
        String password = "2003227Cxm"; // 替换为你的数据库密码

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            // 查询所有开发者
            String selectSql = "SELECT developer_name FROM developer_country";
            PreparedStatement selectStatement = connection.prepareStatement(selectSql);
            ResultSet resultSet = selectStatement.executeQuery();

            // 更新每个开发者的 TalentRank
            String updateSql = "UPDATE developer_country SET talent_rank = ? WHERE developer_name = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateSql);

            while (resultSet.next()) {
                String developerName = resultSet.getString("developer_name");
                double talentRank = calculateTalentRank(developerName);
                
                // 更新数据库
                updateStatement.setDouble(1, talentRank);
                updateStatement.setString(2, developerName);
                updateStatement.executeUpdate();
            }

            System.out.println("所有开发者的 TalentRank 已成功更新。");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 计算某个开发者的 TalentRank
    private static double calculateTalentRank(String developerName) {
        double stars = 0;
        double commits = 0;
        double pr = 0;

        String url = "jdbc:mysql://localhost:3306/大赛数据"; // 替换为你的数据库连接URL
        String user = "root"; // 替换为你的数据库用户名
        String password = "2003227Cxm"; // 替换为你的数据库密码

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT SUM(stars) AS total_stars, SUM(commits) AS total_commits, SUM(pr) AS total_pr " +
                    "FROM ( " +
                    "SELECT stars, commits, pr FROM 3d_projects WHERE developer_name = ? " +
                    "UNION ALL SELECT stars, commits, pr FROM amp_projects WHERE developer_name = ? " +
                    "UNION ALL SELECT stars, commits, pr FROM android_projects WHERE developer_name = ? " +
                    "UNION ALL SELECT stars, commits, pr FROM api_projects WHERE developer_name = ? " +
                    ") AS totals";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, developerName);
            statement.setString(2, developerName);
            statement.setString(3, developerName);
            statement.setString(4, developerName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                stars = resultSet.getDouble("total_stars");
                commits = resultSet.getDouble("total_commits");
                pr = resultSet.getDouble("total_pr");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 使用简单的加权方法计算 TalentRank
        return stars * 0.4 + commits * 0.4 + pr * 0.2;
    }
}
