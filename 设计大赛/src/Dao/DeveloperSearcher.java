package Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import ceshi.DatabaseConnector;

public class DeveloperSearcher {

    // 根据领域和可选国籍搜索开发者
    public void searchDevelopers(String field, String country) {
        String sql = "SELECT developer_name, TalentRank FROM developer_country WHERE field = ?";

        // 如果指定了国籍，则添加条件
        if (country != null && !country.isEmpty()) {
            sql += " AND country = ?";
        }

        sql += " ORDER BY TalentRank DESC"; // 按 TalentRank 排序

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, field); // 设置领域
            if (country != null && !country.isEmpty()) {
                pstmt.setString(2, country); // 设置国籍
            }

            ResultSet rs = pstmt.executeQuery();

            System.out.println("开发者列表:");
            while (rs.next()) {
                String developerName = rs.getString("developer_name");
                float talentRank = rs.getFloat("TalentRank");
                System.out.println("开发者: " + developerName + ", TalentRank: " + talentRank);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DeveloperSearcher searcher = new DeveloperSearcher();
        Scanner scanner = new Scanner(System.in);

        System.out.print("请输入开发者领域（如 3D、AMP、Android、API）: ");
        String field = scanner.nextLine(); // 读取领域

        System.out.print("请输入开发者国籍（可选，如果不需要请直接回车）: ");
        String country = scanner.nextLine(); // 读取国籍

        searcher.searchDevelopers(field, country); // 根据输入搜索开发者

        scanner.close(); // 关闭扫描器
    }
}
