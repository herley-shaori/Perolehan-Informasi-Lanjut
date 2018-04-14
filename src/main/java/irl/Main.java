/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irl;

import core.SVM;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import org.apache.commons.text.StringTokenizer;

/**
 *
 * @author herley
 */
public class Main {

    public static void main(String[] args) {
        Connection connection = null;
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            connection = DriverManager.getConnection("jdbc:derby://localhost:1527/coba", args[0], args[1]);

            //<editor-fold defaultstate="collapsed" desc="Raw Data Preparation">
            /**
             * Raw Data.
             */
            for (int i = 8; i <= 10; i++) {
                final File file = new File("D:\\Dataset\\Formatted Spam Drift - IRL\\" + i + ".csv");
                final BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String total = "";
                while ((total = bufferedReader.readLine()) != null) {
                    List<String> tokenList = new StringTokenizer(total, ",").getTokenList();
                    PreparedStatement ps = connection.prepareStatement("insert into hari" + i + " (account_age, no_follower, no_following, no_userfavourites, no_list, no_tweets, no_retweets, no_hashtag, no_usermention, no_urls, no_char, no_digits, class) values (?,?,?,?,?,?,?,?,?,?,?,?,?)");
                    for (int j = 1; j <= 12; j++) {
                        ps.setInt(j, Integer.parseInt(tokenList.get(j - 1)));
                    }
                    if (tokenList.get(12).equals("spammer")) {
                        ps.setInt(13, 0);
                    } else {
                        ps.setInt(13, 1);
                    }
                    ps.executeUpdate();
                }
            }
//</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Zscore">
//            final BigList<Double> account_age = new BigList<>();
//            final BigList<Double> no_follower = new BigList<>();
//            final BigList<Double> no_following = new BigList<>();
//            final BigList<Double> no_userfavourites = new BigList<>();
//            final BigList<Double> no_list = new BigList<>();
//            final BigList<Double> no_tweets = new BigList<>();
//            final BigList<Double> no_retweets = new BigList<>();
//            final BigList<Double> no_hashtag = new BigList<>();
//            final BigList<Double> no_usermention = new BigList<>();
//            final BigList<Double> no_urls = new BigList<>();
//            final BigList<Double> no_char = new BigList<>();
//            final BigList<Double> no_digits = new BigList<>();
//            final BigList<Double> classer = new BigList<>();
//
//            for (int k = 2; k <= 10; k++) {
//                ResultSet rs = connection.prepareStatement("SELECT avg(account_age) as rerata from hari"+k).executeQuery();
//                rs.next();
//                DescriptiveStatistics stats = new DescriptiveStatistics();
//                rs = connection.prepareStatement("SELECT account_age  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    stats.addValue(rs.getDouble(1));
//                }
//
//                rs = connection.prepareStatement("SELECT account_age  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    double z = (rs.getDouble(1) - stats.getMean()) / stats.getStandardDeviation();
//                    account_age.add(z);
//                }
//                stats.clear();
//
//                rs = connection.prepareStatement("SELECT no_follower  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    stats.addValue(rs.getDouble(1));
//                }
//                rs = connection.prepareStatement("SELECT no_follower  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    double z = (rs.getDouble(1) - stats.getMean()) / stats.getStandardDeviation();
//                    no_follower.add(z);
//                }
//                stats.clear();
//
//                rs = connection.prepareStatement("SELECT no_following  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    stats.addValue(rs.getDouble(1));
//                }
//                rs = connection.prepareStatement("SELECT no_following  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    double z = (rs.getDouble(1) - stats.getMean()) / stats.getStandardDeviation();
//                    no_following.add(z);
//                }
//                stats.clear();
//
//                rs = connection.prepareStatement("SELECT no_userfavourites  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    stats.addValue(rs.getDouble(1));
//                }
//                rs = connection.prepareStatement("SELECT no_userfavourites  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    double z = (rs.getDouble(1) - stats.getMean()) / stats.getStandardDeviation();
//                    no_userfavourites.add(z);
//                }
//                stats.clear();
//
//                rs = connection.prepareStatement("SELECT no_list  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    stats.addValue(rs.getDouble(1));
//                }
//                rs = connection.prepareStatement("SELECT no_list  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    double z = (rs.getDouble(1) - stats.getMean()) / stats.getStandardDeviation();
//                    no_list.add(z);
//                }
//                stats.clear();
//
//                rs = connection.prepareStatement("SELECT no_tweets  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    stats.addValue(rs.getDouble(1));
//                }
//                rs = connection.prepareStatement("SELECT no_tweets  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    double z = (rs.getDouble(1) - stats.getMean()) / stats.getStandardDeviation();
//                    no_tweets.add(z);
//                }
//                stats.clear();
//
//                rs = connection.prepareStatement("SELECT no_retweets  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    stats.addValue(rs.getDouble(1));
//                }
//                rs = connection.prepareStatement("SELECT no_retweets  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    double z = (rs.getDouble(1) - stats.getMean()) / stats.getStandardDeviation();
//                    no_retweets.add(z);
//                }
//                stats.clear();
//
//                rs = connection.prepareStatement("SELECT no_hashtag  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    stats.addValue(rs.getDouble(1));
//                }
//                rs = connection.prepareStatement("SELECT no_hashtag  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    double z = (rs.getDouble(1) - stats.getMean()) / stats.getStandardDeviation();
//                    no_hashtag.add(z);
//                }
//                stats.clear();
//
//                rs = connection.prepareStatement("SELECT no_usermention from hari"+k).executeQuery();
//                while (rs.next()) {
//                    stats.addValue(rs.getDouble(1));
//                }
//                rs = connection.prepareStatement("SELECT no_usermention from hari"+k).executeQuery();
//                while (rs.next()) {
//                    double z = (rs.getDouble(1) - stats.getMean()) / stats.getStandardDeviation();
//                    no_usermention.add(z);
//                }
//                stats.clear();
//
//                rs = connection.prepareStatement("SELECT no_urls  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    stats.addValue(rs.getDouble(1));
//                }
//                rs = connection.prepareStatement("SELECT no_urls  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    double z = (rs.getDouble(1) - stats.getMean()) / stats.getStandardDeviation();
//                    no_urls.add(z);
//                }
//                stats.clear();
//
//                rs = connection.prepareStatement("SELECT no_char  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    stats.addValue(rs.getDouble(1));
//                }
//                rs = connection.prepareStatement("SELECT no_char  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    double z = (rs.getDouble(1) - stats.getMean()) / stats.getStandardDeviation();
//                    no_char.add(z);
//                }
//                stats.clear();
//
//                rs = connection.prepareStatement("SELECT no_digits  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    stats.addValue(rs.getDouble(1));
//                }
//                rs = connection.prepareStatement("SELECT no_digits  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    double z = (rs.getDouble(1) - stats.getMean()) / stats.getStandardDeviation();
//                    no_digits.add(z);
//                }
//                stats.clear();
//
//                rs = connection.prepareStatement("SELECT class  from hari"+k).executeQuery();
//                while (rs.next()) {
//                    classer.add(rs.getDouble(1));
//                }
//                stats.clear();
//
//                for (int i = 0; i < 20000; i++) {
//                    PreparedStatement ps = connection.prepareStatement("insert into zhari"+k+" (account_age, no_follower, no_following, no_userfavourites, no_list, no_tweets, no_retweets, no_hashtag, no_usermention, no_urls, no_char, no_digits, class) values (?,?,?,?,?,?,?,?,?,?,?,?,?)");
//                    ps.setDouble(1, account_age.get(i));
//                    ps.setDouble(2, no_follower.get(i));
//                    ps.setDouble(3, no_following.get(i));
//                    ps.setDouble(4, no_userfavourites.get(i));
//                    ps.setDouble(5, no_list.get(i));
//                    ps.setDouble(6, no_tweets.get(i));
//                    ps.setDouble(7, no_retweets.get(i));
//                    ps.setDouble(8, no_hashtag.get(i));
//                    ps.setDouble(9, no_usermention.get(i));
//                    ps.setDouble(10, no_urls.get(i));
//                    ps.setDouble(11, no_char.get(i));
//                    ps.setDouble(12, no_digits.get(i));
//                    ps.setDouble(13, classer.get(i));
//                    ps.executeUpdate();
//                    if (i % 1000 == 0) {
//                        System.out.println("Passing: " + i);
//                    }
//                }
//                
//            System.out.println("Hari "+k+" selesai");
//            }
//</editor-fold>
        } catch (Exception e) {
            e.printStackTrace();
        }

        //<editor-fold defaultstate="collapsed" desc="Normalisasi Z-Score">
//</editor-fold>
        final SVM svm = new SVM(connection);
    }
}
