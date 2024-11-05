package Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ceshi.DatabaseConnector;

public class DeveloperDao {

    // �����ݿ��л�ȡ�����ߵĹ��Һ�������׺
    public String[] getCountryAndDomain(String developerName) {
        String country = "��"; // Ĭ��ֵ
        String domainSuffix = "��"; // Ĭ��������׺

        String countrySql = "SELECT country FROM (SELECT country FROM 3d_projects WHERE developer_name = ? " +
                "UNION SELECT country FROM amp_projects WHERE developer_name = ? " +
                "UNION SELECT country FROM android_projects WHERE developer_name = ? " +
                "UNION SELECT country FROM api_projects WHERE developer_name = ?) AS countries " +
                "WHERE country IS NOT NULL LIMIT 1";

        String domainSql = "SELECT suffix FROM (SELECT suffix FROM 3d_projects WHERE developer_name = ? " +
                "UNION SELECT suffix FROM amp_projects WHERE developer_name = ? " +
                "UNION SELECT suffix FROM android_projects WHERE developer_name = ? " +
                "UNION SELECT suffix FROM api_projects WHERE developer_name = ?) AS suffixes " +
                "WHERE suffix IS NOT NULL LIMIT 1";

        try (Connection connection = DatabaseConnector.getConnection()) {
            // ��ѯ����
            try (PreparedStatement countryStatement = connection.prepareStatement(countrySql)) {
                countryStatement.setString(1, developerName);
                countryStatement.setString(2, developerName);
                countryStatement.setString(3, developerName);
                countryStatement.setString(4, developerName);

                ResultSet countryResultSet = countryStatement.executeQuery();
                if (countryResultSet.next()) {
                    country = countryResultSet.getString("country");
                }
            }

            // ��ѯ������׺
            try (PreparedStatement domainStatement = connection.prepareStatement(domainSql)) {
                domainStatement.setString(1, developerName);
                domainStatement.setString(2, developerName);
                domainStatement.setString(3, developerName);
                domainStatement.setString(4, developerName);

                ResultSet domainResultSet = domainStatement.executeQuery();
                if (domainResultSet.next()) {
                    domainSuffix = domainResultSet.getString("suffix");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new String[] { country, domainSuffix };
    }

    // ���뿪���ߵ��±�
    public void importDevelopersToNewTable() {
        String clearSql = "DELETE FROM developer_country";
        String selectSql = "SELECT developer_name, '3D' AS field FROM 3d_projects " +
                "UNION SELECT developer_name, 'AMP' AS field FROM amp_projects " +
                "UNION SELECT developer_name, 'Android' AS field FROM android_projects " +
                "UNION SELECT developer_name, 'API' AS field FROM api_projects";

        try (Connection connection = DatabaseConnector.getConnection()) {
            // ����±�
            try (PreparedStatement clearStatement = connection.prepareStatement(clearSql)) {
                clearStatement.executeUpdate();
            }

            // ��ѯ�ĸ����е����п�����
            try (PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                 ResultSet resultSet = selectStatement.executeQuery()) {

                String insertSql = "INSERT INTO developer_country (developer_name, country, field, domain_suffix, talent_rank) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                    while (resultSet.next()) {
                        String developerName = resultSet.getString("developer_name");
                        String field = resultSet.getString("field");

                        // ��ȡ���Һ�������׺
                        String[] countryAndDomain = getCountryAndDomain(developerName);
                        String country = countryAndDomain[0];
                        String domainSuffix = countryAndDomain[1];

                        // Ԥ����ң�������predictCountry������
                        CountryPredictionResult prediction = NAshow2.predictCountry(developerName, domainSuffix);
                        country = prediction.country; // ʹ��Ԥ��Ĺ���

                        // ���� TalentRank��������calculateTalentRank������
                        double talentRank = calculateTalentRank(developerName);

                        // ��������
                        insertStatement.setString(1, developerName);
                        insertStatement.setString(2, country);
                        insertStatement.setString(3, field);
                        insertStatement.setString(4, domainSuffix);
                        insertStatement.setDouble(5, talentRank);
                        insertStatement.executeUpdate();
                    }
                }
            }

            System.out.println("���п�������Ϣ�ѳɹ����뵽 developer_country ���С�");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ���� TalentRank��ʾ�����룩
    private double calculateTalentRank(String developerName) {
        // ʵ���߼�
        return 0.0; // ʾ������ֵ
    }
}
