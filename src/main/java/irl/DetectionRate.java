/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author herley
 * Digunakan hanya untuk menghitung Detection Rate.
 */
public class DetectionRate {
    public DetectionRate(Connection connection){
        final HashMap<Integer,Double> detectionRateHashMap = new HashMap();
        try {
            final ResultSet rs = connection.prepareStatement("select * from detection_rate_history").executeQuery();
            while (rs.next()) {
                final int day = rs.getInt("day_number");
                final double tp = rs.getDouble("true_positive");
                final double fn = rs.getDouble("false_negative");
                
                double detectionRate = (tp)/(tp+fn);
                detectionRateHashMap.put(day, detectionRate);
            }
            
            final PreparedStatement ps = connection.prepareStatement("update detection_rate_history set detection_rate=? where day_number=?");
            Iterator<Map.Entry<Integer, Double>> iterSatu = detectionRateHashMap.entrySet().iterator();
            while (iterSatu.hasNext()) {
                Map.Entry<Integer, Double> ne = iterSatu.next();
                ps.setDouble(1, ne.getValue());
                ps.setInt(2, ne.getKey());
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
