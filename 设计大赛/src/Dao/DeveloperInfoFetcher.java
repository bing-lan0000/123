package Dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class DeveloperInfoFetcher {
    // ���ݿ�������Ϣ
    private static final String DB_URL = "jdbc:mysql://localhost:3306/��������"; // �滻Ϊ������ݿ�����URL
    private static final String USER = "root"; // �滻Ϊ������ݿ��û���
    private static final String PASSWORD = "2003227Cxm"; // �滻Ϊ������ݿ�����

    // ��ѯ��������Ϣ
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

            // ���ò���
            statement.setString(1, developerName);
            statement.setString(2, developerName);
            statement.setString(3, developerName);
            statement.setString(4, developerName);

            // ִ�в�ѯ
            ResultSet resultSet = statement.executeQuery();

            // չʾ���
            System.out.println("��������Ϣ��");
            while (resultSet.next()) {
                String type = resultSet.getString("type");
                int stars = resultSet.getInt("stars");
                int forks = resultSet.getInt("forks");
                int commits = resultSet.getInt("commits");
                int pr = resultSet.getInt("pr");
                String lastCommit = resultSet.getString("last_commit");
                String projectName = resultSet.getString("project_name");

                // ���� Talent ֵ
                double talentValue = calculateTalentValue(stars, forks, commits, pr);
                // ���� Talent ֵ�����ȼ�
                char rating = getRating(talentValue);

                // ����������
                System.out.printf("����: %s, Stars: %d, Forks: %d, Commits: %d, PR: %d, ����ύ: %s, ��Ŀ����: %s, Talent: %.2f, ����: %c%n",
                                  type, stars, forks, commits, pr, lastCommit, projectName, talentValue, rating);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ���� Talent ֵ�ķ���
    private double calculateTalentValue(int stars, int forks, int commits, int pr) {
        // ���ݲ�ͬ��Ȩ�ؼ��� Talent ֵ��������һ��ʾ��
        return (stars * 0.4) + (forks * 0.3) + (commits * 0.2) + (pr * 0.1);
    }

    // ���� Talent ֵ�����ȼ�
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
        // ʹ�� Scanner ��ȡ�û�����Ŀ���������
        Scanner scanner = new Scanner(System.in);
        System.out.print("�����뿪��������: ");
        String developerName = scanner.nextLine(); // ��ȡ�û�����
        fetcher.fetchDeveloperInfo(developerName);
        scanner.close(); // �ر�ɨ����
    }
}
