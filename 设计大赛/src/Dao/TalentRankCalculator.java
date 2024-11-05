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

    // �����ݿ��л�ȡ���п����ߵ� TalentRank������ TalentRank ��С����
    public void calculateAndFillTalentRank() {
        String selectDevelopers = "SELECT developer_name FROM developer_country";
        List<DeveloperRank> developerRanks = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectDevelopers);
             ResultSet rs = selectStmt.executeQuery()) {

            // ��������߼��� TalentRank �������б�
            while (rs.next()) {
                String developerName = rs.getString("developer_name");
                float talentRank = calculateTalentRank(developerName);
                String grade = evaluateGrade(talentRank);
                
                developerRanks.add(new DeveloperRank(developerName, talentRank, grade));
            }

            // ���� TalentRank ֵ���н�������
            Collections.sort(developerRanks, (a, b) -> Float.compare(b.talentRank, a.talentRank));

            // �������� TalentRank �� grade ���µ����ݿ�
            for (DeveloperRank developerRank : developerRanks) {
                updateDeveloperTalentRank(developerRank.developerName, developerRank.talentRank, developerRank.grade);
            }

            System.out.println("���п����ߵ� TalentRank �ѳɹ���䵽 developer_country ���У�������������");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ���� stars��commits �� pr ���� TalentRank
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

                // ��Ȩ�ؼ��� TalentRank
                return (totalStars * 0.4f) + (totalCommits * 0.4f) + (totalPR * 0.2f);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0f;
    }

    // ���� TalentRank ֵ�����ȼ�
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

    // ���� developer_country ���е� TalentRank �� grade
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

    // �ڲ������ڴ洢�����ߵ� TalentRank ���ȼ�
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

    // ������ִ�� TalentRank ���
    public static void main(String[] args) {
        TalentRankCalculator calculator = new TalentRankCalculator();
        calculator.calculateAndFillTalentRank();
    }
}
