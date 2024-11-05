package Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import ceshi.DatabaseConnector;

public class CountryPredictor {

    // 名字后缀、域名后缀与国家权重的映射
    private static final Map<String, Map<String, Float>> NAME_DOMAIN_COUNTRY_MAP = new HashMap<>();

    static {
        // 初始化名字和域名后缀的映射
        Map<String, Float> chineseNames = new HashMap<>();
        chineseNames.put("China", 0.8f);
        NAME_DOMAIN_COUNTRY_MAP.put("Li", chineseNames);
        NAME_DOMAIN_COUNTRY_MAP.put("Wang", chineseNames);

        Map<String, Float> englishNames = new HashMap<>();
        englishNames.put("USA", 0.9f);
        NAME_DOMAIN_COUNTRY_MAP.put("Smith", englishNames);

        // 初始化域名后缀与国家权重的映射
        Map<String, Float> devDomains = new HashMap<>();
        devDomains.put("Unknown", 0.1f);  // 一般不特定的域名

        Map<String, Float> comDomains = new HashMap<>();
        comDomains.put("USA", 0.5f);  // 假设 com 域名大部分是美国

        Map<String, Float> ioDomains = new HashMap<>();
        ioDomains.put("UK", 0.7f);  // 假设 io 域名大部分是英国

        Map<String, Float> shDomains = new HashMap<>();
        shDomains.put("China", 0.6f);  // 假设 sh 域名大部分是中国

        // 将这些映射添加到 NAME_DOMAIN_COUNTRY_MAP
        NAME_DOMAIN_COUNTRY_MAP.put("dev", devDomains);
        NAME_DOMAIN_COUNTRY_MAP.put("com", comDomains);
        NAME_DOMAIN_COUNTRY_MAP.put("io", ioDomains);
        NAME_DOMAIN_COUNTRY_MAP.put("sh", shDomains);
    }

    public String predictCountry(String developerName, String domainSuffix) {
        String predictedCountry = "Unknown";
        float maxWeight = 0;

        // 根据名字推测国籍
        for (Map.Entry<String, Map<String, Float>> entry : NAME_DOMAIN_COUNTRY_MAP.entrySet()) {
            String key = entry.getKey();
            if (developerName.contains(key) || domainSuffix.equalsIgnoreCase(key)) {
                for (Map.Entry<String, Float> countryEntry : entry.getValue().entrySet()) {
                    float weight = countryEntry.getValue();
                    if (weight > maxWeight) {
                        predictedCountry = "预测 " + countryEntry.getKey();
                        maxWeight = weight;
                    }
                }
            }
        }

        // 根据域名后缀推测国籍
        if (domainSuffix != null && NAME_DOMAIN_COUNTRY_MAP.containsKey(domainSuffix)) {
            for (Map.Entry<String, Float> entry : NAME_DOMAIN_COUNTRY_MAP.get(domainSuffix).entrySet()) {
                float weight = entry.getValue();
                if (weight > maxWeight) {
                    predictedCountry = "预测 " + entry.getKey();
                    maxWeight = weight;
                }
            }
        }

        return predictedCountry;
    }

    public void showDeveloperCountry(String developerName) {
        String[] tables = {"3d_projects", "amp_projects", "android_projects", "api_projects"};
        String country = null;
        String domainSuffix = null;

        try (Connection conn = DatabaseConnector.getConnection()) {
            for (String table : tables) {
                String sql = "SELECT country, domainsuffix FROM " + table + " WHERE developer_name = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, developerName);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            country = rs.getString("country");
                            domainSuffix = rs.getString("domainsuffix"); // 改为 domainsuffix

                            if (country != null && !country.equals("无")) {
                                System.out.println("开发者 " + developerName + " 的国籍: " + country);
                                return;
                            }
                        }
                    }
                }
            }

            if (country == null || country.equals("无")) {
                String predictedCountry = predictCountry(developerName, domainSuffix);
                System.out.println("开发者 " + developerName + " 的国籍: " + predictedCountry);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CountryPredictor predictor = new CountryPredictor();
        Scanner scanner = new Scanner(System.in);

        System.out.print("请输入开发者的名字: ");
        String developerName = scanner.nextLine();

        predictor.showDeveloperCountry(developerName);
        scanner.close();
    }
}
