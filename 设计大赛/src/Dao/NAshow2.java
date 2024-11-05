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
    // ӳ���ϵ����ĸ/���� -> {����, Ȩ��}
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
            return new CountryPredictionResult("��", 0.0);
        }

        String predictedCountry = Collections.max(combinedScores.entrySet(), Map.Entry.comparingByValue()).getKey();
        double confidence = maxScore / (maxScore + 1e-6); // ���������

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
        String country = "��";
        String domainSuffix = "��";

        String url = "jdbc:mysql://localhost:3306/��������"; // �滻Ϊ������ݿ�����URL
        String user = "root"; // �滻Ϊ������ݿ��û���
        String password = "2003227Cxm"; // �滻Ϊ������ݿ�����

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
        System.out.print("�����뿪���ߵ�����: ");
        String developerName = scanner.nextLine();
        
        String[] countryAndDomain = getCountryAndDomainFromDatabase(developerName);
        String country = countryAndDomain[0];
        String domainSuffix = countryAndDomain[1];

        CountryPredictionResult prediction = predictCountry(developerName, domainSuffix);
        String actualCountry;
        
        if (!"��".equals(country)) {
            actualCountry = country; // ����������ڣ�ʹ����֪����
            System.out.println("������: " + actualCountry);
        } else {
            actualCountry = prediction.country; // ����ʹ��Ԥ��Ĺ���
            System.out.println("Ԥ��Ĺ�����: " + actualCountry);
        }

        // �ж����Ŷ�
        if (prediction.confidence < 0.5) { // ���Ŷȵ���0.5���ΪN/A
            System.out.println("�����Ŷ�Ϊ: N/A");
        } else {
            System.out.println("�����Ŷ�Ϊ: " + prediction.confidence);
        }
        
        scanner.close();
    }
}
