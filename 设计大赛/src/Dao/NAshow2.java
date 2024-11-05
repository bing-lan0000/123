package Dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NAshow2 {
    // 映射关系：字母/域名 -> {国家, 权重}
    private static Map<String, Map<String, Double>> letterCountryMap;
    private static Map<String, Map<String, Double>> domainCountryMap;

    static {
        ObjectMapper mapper = new ObjectMapper();
        try {
            letterCountryMap = mapper.readValue(
                NAshow2.class.getClassLoader().getResourceAsStream("letter_mappings.json"),
                new TypeReference<Map<String, Map<String, Double>>>() {}
            );

            domainCountryMap = mapper.readValue(
                NAshow2.class.getClassLoader().getResourceAsStream("domain_mappings.json"),
                new TypeReference<Map<String, Map<String, Double>>>() {}
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final double NAME_WEIGHT = 0.7;
    private static final double DOMAIN_WEIGHT = 0.3;

    public static CountryPredictionResult predictCountry(String name, String domain) {
        name = name.toUpperCase();
        domain = domain.toLowerCase();

        Map<String, Double> nameScores = calculateScores(name, letterCountryMap);
        Map<String, Double> domainScores = calculateScores(domain, domainCountryMap);

        Map<String, Double> combinedScores = new HashMap<>();
        double maxScore = 0.0;

        for (String country : nameScores.keySet()) {
            double score = nameScores.get(country) * NAME_WEIGHT + domainScores.getOrDefault(country, 0.0) * DOMAIN_WEIGHT;
            combinedScores.put(country, score);
            maxScore = Math.max(maxScore, score);
        }

        if (combinedScores.isEmpty()) {
            return new CountryPredictionResult("无", 0.0);
        }

        String predictedCountry = Collections.max(combinedScores.entrySet(), Map.Entry.comparingByValue()).getKey();
        double confidence = maxScore / (maxScore + 1e-6); // 避免除以零

        return new CountryPredictionResult(predictedCountry, confidence);
    }

    private static Map<String, Double> calculateScores(String str, Map<String, Map<String, Double>> map) {
        Map<String, Double> scores = new HashMap<>();
        for (char c : str.toCharArray()) {
            Map<String, Double> countryScores = map.getOrDefault(String.valueOf(c), Collections.emptyMap());
            for (Map.Entry<String, Double> entry : countryScores.entrySet()) {
                scores.put(entry.getKey(), scores.getOrDefault(entry.getKey(), 0.0) + entry.getValue());
            }
        }
        return scores;
    }

    private static String[] getCountryAndDomainFromDatabase(String developerName) {
        String country = "无";
        String domainSuffix = "无";

        String url = "jdbc:mysql://localhost:3306/大赛数据"; // 替换为你的数据库连接URL
        String user = "root"; // 替换为你的数据库用户名
        String password = "2003227Cxm"; // 替换为你的数据库密码

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String countrySql = "SELECT country FROM (SELECT country FROM 3d_projects WHERE developer_name = ? " +
                    "UNION SELECT country FROM amp_projects WHERE developer_name = ? " +
                    "UNION SELECT country FROM android_projects WHERE developer_name = ? " +
                    "UNION SELECT country FROM api_projects WHERE developer_name = ?) AS countries " +
                    "WHERE country IS NOT NULL LIMIT 1";
            PreparedStatement countryStatement = connection.prepareStatement(countrySql);
            countryStatement.setString(1, developerName);
            countryStatement.setString(2, developerName);
            countryStatement.setString(3, developerName);
            countryStatement.setString(4, developerName);

            ResultSet countryResultSet = countryStatement.executeQuery();
            if (countryResultSet.next()) {
                country = countryResultSet.getString("country");
            }

            String domainSql = "SELECT suffix FROM (SELECT suffix FROM 3d_projects WHERE developer_name = ? " +
                    "UNION SELECT suffix FROM amp_projects WHERE developer_name = ? " +
                    "UNION SELECT suffix FROM android_projects WHERE developer_name = ? " +
                    "UNION SELECT suffix FROM api_projects WHERE developer_name = ?) AS suffixes " +
                    "WHERE suffix IS NOT NULL LIMIT 1";
            PreparedStatement domainStatement = connection.prepareStatement(domainSql);
            domainStatement.setString(1, developerName);
            domainStatement.setString(2, developerName);
            domainStatement.setString(3, developerName);
            domainStatement.setString(4, developerName);

            ResultSet domainResultSet = domainStatement.executeQuery();
            if (domainResultSet.next()) {
                domainSuffix = domainResultSet.getString("suffix");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[] { country, domainSuffix };
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入开发者的名字: ");
        String developerName = scanner.nextLine();
        
        String[] countryAndDomain = getCountryAndDomainFromDatabase(developerName);
        String country = countryAndDomain[0];
        String domainSuffix = countryAndDomain[1];

        CountryPredictionResult prediction = predictCountry(developerName, domainSuffix);
        String actualCountry;
        
        if (!"无".equals(country)) {
            actualCountry = country; // 如果国籍存在，使用已知国籍
            System.out.println("国籍是: " + actualCountry);
        } else {
            actualCountry = prediction.country; // 否则使用预测的国籍
            System.out.println("预测的国籍是: " + actualCountry);
        }

        // 判断置信度
        if (prediction.confidence < 0.5) { // 置信度低于0.5标记为N/A
            System.out.println("该置信度为: N/A");
        } else {
            System.out.println("该置信度为: " + prediction.confidence);
        }
        
        scanner.close();
    }
}
