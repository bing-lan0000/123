package Dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TalentRank {
    // ���㲢�������п����ߵ� TalentRank
    public static void calculateAndUpdateTalentRank() {
        String url = "jdbc:mysql://localhost:3306/��������"; // �滻Ϊ������ݿ�����URL
        String user = "root"; // �滻Ϊ������ݿ��û���
        String password = "2003227Cxm"; // �滻Ϊ������ݿ�����

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            // ��ѯ���п�����
            String selectSql = "SELECT developer_name FROM developer_country";
            PreparedStatement selectStatement = connection.prepareStatement(selectSql);
            ResultSet resultSet = selectStatement.executeQuery();

            // ����ÿ�������ߵ� TalentRank
            String updateSql = "UPDATE developer_country SET talent_rank = ? WHERE developer_name = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateSql);

            while (resultSet.next()) {
                String developerName = resultSet.getString("developer_name");
                double talentRank = calculateTalentRank(developerName);
                
                // �������ݿ�
                updateStatement.setDouble(1, talentRank);
                updateStatement.setString(2, developerName);
                updateStatement.executeUpdate();
            }

            System.out.println("���п����ߵ� TalentRank �ѳɹ����¡�");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ����ĳ�������ߵ� TalentRank
    private static double calculateTalentRank(String developerName) {
        double stars = 0;
        double commits = 0;
        double pr = 0;

        String url = "jdbc:mysql://localhost:3306/��������"; // �滻Ϊ������ݿ�����URL
        String user = "root"; // �滻Ϊ������ݿ��û���
        String password = "2003227Cxm"; // �滻Ϊ������ݿ�����

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

        // ʹ�ü򵥵ļ�Ȩ�������� TalentRank
        return stars * 0.4 + commits * 0.4 + pr * 0.2;
    }
}
