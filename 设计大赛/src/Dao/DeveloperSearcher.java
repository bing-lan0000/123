package Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import ceshi.DatabaseConnector;

public class DeveloperSearcher {

    // ��������Ϳ�ѡ��������������
    public void searchDevelopers(String field, String country) {
        String sql = "SELECT developer_name, TalentRank FROM developer_country WHERE field = ?";

        // ���ָ���˹��������������
        if (country != null && !country.isEmpty()) {
            sql += " AND country = ?";
        }

        sql += " ORDER BY TalentRank DESC"; // �� TalentRank ����

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, field); // ��������
            if (country != null && !country.isEmpty()) {
                pstmt.setString(2, country); // ���ù���
            }

            ResultSet rs = pstmt.executeQuery();

            System.out.println("�������б�:");
            while (rs.next()) {
                String developerName = rs.getString("developer_name");
                float talentRank = rs.getFloat("TalentRank");
                System.out.println("������: " + developerName + ", TalentRank: " + talentRank);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DeveloperSearcher searcher = new DeveloperSearcher();
        Scanner scanner = new Scanner(System.in);

        System.out.print("�����뿪���������� 3D��AMP��Android��API��: ");
        String field = scanner.nextLine(); // ��ȡ����

        System.out.print("�����뿪���߹�������ѡ���������Ҫ��ֱ�ӻس���: ");
        String country = scanner.nextLine(); // ��ȡ����

        searcher.searchDevelopers(field, country); // ������������������

        scanner.close(); // �ر�ɨ����
    }
}
