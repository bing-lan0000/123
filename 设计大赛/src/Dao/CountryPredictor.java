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

    // ���ֺ�׺��������׺�����Ȩ�ص�ӳ��
    private static final Map<String, Map<String, Float>> NAME_DOMAIN_COUNTRY_MAP = new HashMap<>();

    static {
        // ��ʼ�����ֺ�������׺��ӳ��
        Map<String, Float> chineseNames = new HashMap<>();
        chineseNames.put("China", 0.8f);
        NAME_DOMAIN_COUNTRY_MAP.put("Li", chineseNames);
        NAME_DOMAIN_COUNTRY_MAP.put("Wang", chineseNames);

        Map<String, Float> englishNames = new HashMap<>();
        englishNames.put("USA", 0.9f);
        NAME_DOMAIN_COUNTRY_MAP.put("Smith", englishNames);

        // ��ʼ��������׺�����Ȩ�ص�ӳ��
        Map<String, Float> devDomains = new HashMap<>();
        devDomains.put("Unknown", 0.1f);  // һ�㲻�ض�������

        Map<String, Float> comDomains = new HashMap<>();
        comDomains.put("USA", 0.5f);  // ���� com �����󲿷�������

        Map<String, Float> ioDomains = new HashMap<>();
        ioDomains.put("UK", 0.7f);  // ���� io �����󲿷���Ӣ��

        Map<String, Float> shDomains = new HashMap<>();
        shDomains.put("China", 0.6f);  // ���� sh �����󲿷����й�

        // ����Щӳ����ӵ� NAME_DOMAIN_COUNTRY_MAP
        NAME_DOMAIN_COUNTRY_MAP.put("dev", devDomains);
        NAME_DOMAIN_COUNTRY_MAP.put("com", comDomains);
        NAME_DOMAIN_COUNTRY_MAP.put("io", ioDomains);
        NAME_DOMAIN_COUNTRY_MAP.put("sh", shDomains);
    }

    public String predictCountry(String developerName, String domainSuffix) {
        String predictedCountry = "Unknown";
        float maxWeight = 0;

        // ���������Ʋ����
        for (Map.Entry<String, Map<String, Float>> entry : NAME_DOMAIN_COUNTRY_MAP.entrySet()) {
            String key = entry.getKey();
            if (developerName.contains(key) || domainSuffix.equalsIgnoreCase(key)) {
                for (Map.Entry<String, Float> countryEntry : entry.getValue().entrySet()) {
                    float weight = countryEntry.getValue();
                    if (weight > maxWeight) {
                        predictedCountry = "Ԥ�� " + countryEntry.getKey();
                        maxWeight = weight;
                    }
                }
            }
        }

        // ����������׺�Ʋ����
        if (domainSuffix != null && NAME_DOMAIN_COUNTRY_MAP.containsKey(domainSuffix)) {
            for (Map.Entry<String, Float> entry : NAME_DOMAIN_COUNTRY_MAP.get(domainSuffix).entrySet()) {
                float weight = entry.getValue();
                if (weight > maxWeight) {
                    predictedCountry = "Ԥ�� " + entry.getKey();
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
                            domainSuffix = rs.getString("domainsuffix"); // ��Ϊ domainsuffix

                            if (country != null && !country.equals("��")) {
                                System.out.println("������ " + developerName + " �Ĺ���: " + country);
                                return;
                            }
                        }
                    }
                }
            }

            if (country == null || country.equals("��")) {
                String predictedCountry = predictCountry(developerName, domainSuffix);
                System.out.println("������ " + developerName + " �Ĺ���: " + predictedCountry);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CountryPredictor predictor = new CountryPredictor();
        Scanner scanner = new Scanner(System.in);

        System.out.print("�����뿪���ߵ�����: ");
        String developerName = scanner.nextLine();

        predictor.showDeveloperCountry(developerName);
        scanner.close();
    }
}
