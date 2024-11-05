package Dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class DeveloperInfoFetcher {
    // 数据库连接信息
    private static final String DB_URL = "jdbc:mysql://localhost:3306/大赛数据"; // 替换为你的数据库连接URL
    private static final String USER = "root"; // 替换为你的数据库用户名
    private static final String PASSWORD = "2003227Cxm"; // 替换为你的数据库密码

    // 查询开发者信息
    public void fetchDeveloperInfo(String developerName) {
        String sql = "SELECT '3D' AS type, stars, forks, commits, pr, last_commit, project_name FROM 3d_projects WHERE developer_name = ? " +
                     "UNION ALL " +
                     "SELECT 'AMP' AS type, stars, forks, commits, pr, last_commit, project_name FROM amp_projects WHERE developer_name = ? " +
                     "UNION ALL " +
                     "SELECT 'Android' AS type, stars, forks, commits, pr, last_commit, project_name FROM android_projects WHERE developer_name = ? " +
                     "UNION ALL " +
                     "SELECT 'API' AS type, stars, forks, commits, pr, last_commit, project_name FROM api_projects WHERE developer_name = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // 设置参数
            statement.setString(1, developerName);
            statement.setString(2, developerName);
            statement.setString(3, developerName);
            statement.setString(4, developerName);

            // 执行查询
            ResultSet resultSet = statement.executeQuery();

            // 展示结果
            System.out.println("开发者信息：");
            while (resultSet.next()) {
                String type = resultSet.getString("type");
                int stars = resultSet.getInt("stars");
                int forks = resultSet.getInt("forks");
                int commits = resultSet.getInt("commits");
                int pr = resultSet.getInt("pr");
                String lastCommit = resultSet.getString("last_commit");
                String projectName = resultSet.getString("project_name");

                // 计算 Talent 值
                double talentValue = calculateTalentValue(stars, forks, commits, pr);
                // 根据 Talent 值评定等级
                char rating = getRating(talentValue);

                // 横排输出结果
                System.out.printf("类型: %s, Stars: %d, Forks: %d, Commits: %d, PR: %d, 最后提交: %s, 项目名称: %s, Talent: %.2f, 评级: %c%n",
                                  type, stars, forks, commits, pr, lastCommit, projectName, talentValue, rating);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 计算 Talent 值的方法
    private double calculateTalentValue(int stars, int forks, int commits, int pr) {
        // 根据不同的权重计算 Talent 值，这里是一个示例
        return (stars * 0.4) + (forks * 0.3) + (commits * 0.2) + (pr * 0.1);
    }

    // 根据 Talent 值评定等级
    private char getRating(double talentValue) {
        if (talentValue >= 80) {
            return 'A';
        } else if (talentValue >= 60) {
            return 'B';
        } else if (talentValue >= 40) {
            return 'C';
        } else {
            return 'D';
        }
    }

    public static void main(String[] args) {
        DeveloperInfoFetcher fetcher = new DeveloperInfoFetcher();
        // 使用 Scanner 获取用户输入的开发者名字
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入开发者名字: ");
        String developerName = scanner.nextLine(); // 读取用户输入
        fetcher.fetchDeveloperInfo(developerName);
        scanner.close(); // 关闭扫描器
    }
}
