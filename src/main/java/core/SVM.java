/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import org.magicwerk.brownies.collections.BigList;

/**
 *
 * @author herley
 */
public class SVM {

    private Connection connection;
    private int hariMasaLampau;
    private int hariMasaDepan = 10;
    private svm_model currentSVMModel;

    // Constant
    public static double MANUAL_TWEET_SELECTION = 0.4;

    public SVM(Connection connection) {

        try {
            this.connection = connection;
            this.deleteTableRecords(connection);
            this.hariMasaLampau = 1;

            while (hariMasaLampau <= this.hariMasaDepan) {
                System.out.println("Fase konfigurasi data...");
                ResultSet rs = connection.prepareStatement("select count(*) as jumlah from zhari" + this.hariMasaLampau).executeQuery();
                rs.next();
                // Berarti 40% data latih dan 60% data uji.
                int jumlahData = rs.getInt("jumlah");
//                int jumlahData = 1000;
                double dataSeparation = 0.4;
                int jumlahDataLatih = new Double(dataSeparation * jumlahData).intValue();

                final BigList<BigList<Double>> dataLatih = new BigList();
                final BigList<BigList<Double>> dataUji = new BigList();
                //tex<--tLabelled+tSpam, merge spam tweets into tLabelled (dataLatih)
                final BigList<BigList<Double>> tex = new BigList();
//                rs = connection.prepareStatement("select * from zHARI" + this.hariAwal + " fetch first 1000 rows only").executeQuery();
                rs = connection.prepareStatement("select * from zHARI" + this.hariMasaLampau).executeQuery();
                while (rs.next()) {
                    if (dataLatih.size() < jumlahDataLatih) {
                        final BigList<Double> dalamData = new BigList();
                        for (int i = 1; i <= 13; i++) {
                            dalamData.add(rs.getDouble(i));
                        }
                        dataLatih.add(dalamData);
                    } else {
                        final BigList<Double> dalamData = new BigList();
                        for (int i = 1; i <= 13; i++) {
                            dalamData.add(rs.getDouble(i));
                        }
                        dataUji.add(dalamData);
                    }
                }

                System.out.println("Data Latih: "+jumlahData);
                
                // Tambahkan data dari classifier sebelumnya dari Tabel permanen.
                if (this.hariMasaLampau != 1) {
                    rs = connection.prepareStatement("select * from permanent").executeQuery();
                    while (rs.next()) {
                        if (dataLatih.size() < jumlahDataLatih) {
                            final BigList<Double> dalamData = new BigList();
                            for (int i = 1; i <= 13; i++) {
                                dalamData.add(rs.getDouble(i));
                            }
                            dataLatih.add(dalamData);
                        } else {
                            final BigList<Double> dalamData = new BigList();
                            for (int i = 1; i <= 13; i++) {
                                dalamData.add(rs.getDouble(i));
                            }
                            dataUji.add(dalamData);
                        }
                    }
                }

                System.out.println("Fase Pelatihan...");
                this.trainingPreparation(dataLatih);

                double[][] xTest = new double[dataUji.size()][12];
                double[][] yTest = new double[dataUji.size()][1];
                for (int i = 0; i < dataUji.size(); i++) {
                    final BigList<Double> dalamData = dataUji.get(i);
                    for (int j = 0; j < dalamData.size() - 1; j++) {
                        xTest[i][j] = dalamData.get(j);
                    }
                    yTest[i][0] = dalamData.get(12);
                }

                double[] svmPrediction = this.svmTesting(xTest, yTest, this.currentSVMModel, tex);
                System.out.println("Fase Pengujian...");
                double truePositive = 0, trueNegative = 0, falsePositive = 0, falseNegative = 0, precision, recall, fmeasure;
                for (int i = 0; i < svmPrediction.length; i++) {
                    if (svmPrediction[i] == yTest[i][0]) {
                        // true positive-spammer
                        if (svmPrediction[i] == 0.0) {
                            truePositive++;
                        } else {
                            trueNegative++;
                        }
                    } else {
                        // false positive
                        if (svmPrediction[i] == 0.0 && yTest[i][0] == 1.0) {
                            falsePositive++;
                        } else {
                            falseNegative++;
                        }
                    }
                }
                precision = (truePositive) / (truePositive + falsePositive);
                recall = (truePositive) / (truePositive + falseNegative);
                fmeasure = (2 * truePositive) / ((2 * truePositive) + falsePositive + falseNegative);
                // Prediction_History
                System.out.println("Menyimpan hasil pengujian untuk hari ke: " + this.hariMasaLampau);
                {
                    final PreparedStatement ps = connection.prepareStatement("insert into prediction_history (day_number, true_positive, true_negative, false_positive, false_negative, precision, recall, fmeasure) values (?,?,?,?,?,?,?,?) ");
                    ps.setInt(1, this.hariMasaLampau);
                    ps.setDouble(2, truePositive);
                    ps.setDouble(3, trueNegative);
                    ps.setDouble(4, falsePositive);
                    ps.setDouble(5, falseNegative);
                    ps.setDouble(6, precision);
                    ps.setDouble(7, recall);
                    ps.setDouble(8, fmeasure);
                    ps.executeUpdate();
                }

                // 5. Retrain Classifier with tex
                {
                    System.out.println("Memulai pelatihan kedua...");
//                    dataLatih.addAll(tex);
//                    this.trainingPreparation(dataLatih);
                    System.out.println("Pelatihan kedua selesai");
                }
                // 6-16. Manual Selection based on Probability
                System.out.println("Pemilihan Jawaban Manual... ");
                {
                    rs = this.connection.prepareStatement("select count(*) as jumlah from accumulator").executeQuery();
                    rs.next();
                    double jumlah = rs.getInt("jumlah");
                    int totalPilihan = new Double(SVM.MANUAL_TWEET_SELECTION * jumlah).intValue();
                    // delete isi tabel permanent, karena semua yang terbaik ada di tabel accumulator.
                    this.connection.prepareStatement("delete from permanent").executeUpdate();
                    this.connection.prepareStatement("insert into permanent (account_age, no_follower, no_following, no_userfavourites, no_list, no_tweets, no_retweets, no_hashtag, no_usermention, no_urls, no_char, no_digits, class) select account_age, no_follower, no_following, no_userfavourites, no_list, no_tweets, no_retweets, no_hashtag, no_usermention, no_urls, no_char, no_digits, class from accumulator order by age_total_score desc fetch first " + totalPilihan + " rows only").executeUpdate();
                }
                System.out.println("Hari : " + this.hariMasaLampau + " Selesai");
                System.out.println("#########################################");
                this.hariMasaLampau++;
            }
            // save svm model
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            final String fileName = timestamp+"SVM";
            svm.svm_save_model(fileName, this.currentSVMModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private svm_model svmTraining(double[][] xtrain, double[][] ytrain) {
        svm_problem prob = new svm_problem();
        int recordCount = xtrain.length;
        int featureCount = xtrain[0].length;
        prob.y = new double[recordCount];
        prob.l = recordCount;
        prob.x = new svm_node[recordCount][featureCount];

        for (int i = 0; i < recordCount; i++) {
            double[] features = xtrain[i];
            prob.x[i] = new svm_node[features.length];
            for (int j = 0; j < features.length; j++) {
                svm_node node = new svm_node();
                node.index = j;
                node.value = features[j];
                prob.x[i][j] = node;
            }
            prob.y[i] = ytrain[i][0];
        }

        svm_parameter param = new svm_parameter();
        param.svm_type = svm_parameter.NU_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.degree = 3;
        param.gamma = 0.5;
        param.coef0 = 0;
        param.nu = 0.5;
        param.cache_size = 4000;
        param.C = 1;
        param.eps = 1e-9;
        param.p = 0.1;
        param.shrinking = 1;
        param.probability = 1;
        param.nr_weight = 0;
        param.weight_label = new int[0];
        param.weight = new double[0];
        svm_model model = svm.svm_train(prob, param);

        return model;
    }

    /**
     * SVM Test.
     *
     * @param xtest
     * @param ytest
     * @param model
     * @param tex
     * @return
     */
    private double[] svmTesting(double[][] xtest, double[][] ytest, svm_model model, BigList<BigList<Double>> tex) {

        double[] yPred = new double[xtest.length];
        for (int k = 0; k < xtest.length; k++) {

            double[] fVector = xtest[k];

            svm_node[] nodes = new svm_node[fVector.length];
            for (int i = 0; i < fVector.length; i++) {
                svm_node node = new svm_node();
                node.index = i;
                node.value = fVector[i];
                nodes[i] = node;
            }

            int totalClasses = 2;
            int[] labels = new int[totalClasses];
            svm.svm_get_labels(model, labels);

            double[] prob_estimates = new double[totalClasses];
            yPred[k] = svm.svm_predict_probability(model, nodes, prob_estimates);
            double predicted = svm.svm_predict_probability(model, nodes, prob_estimates);
            
            // 4. Spam Tweets Classified
            if (predicted == 0.0) {
                final BigList<Double> dalam = new BigList();
                for (Double d : fVector) {
                    dalam.add(d);
                }
                dalam.add(0.0);
                tex.add(dalam);
            }

            if (predicted == ytest[k][0]) {

                try {
                    PreparedStatement ps = this.connection.prepareStatement("insert into accumulator (account_age, no_follower, no_following, no_userfavourites, no_list, no_tweets, no_retweets, no_hashtag, no_usermention, no_urls, no_char, no_digits, class, post_age, probability, age_total_score) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                    for (int i = 0; i < fVector.length; i++) {
                        ps.setDouble((i + 1), fVector[i]);
                    }
                    ps.setDouble(13, ytest[k][0]);
                    ps.setInt(14, this.hariMasaLampau);
                    ps.setDouble(15, Math.max(prob_estimates[0], prob_estimates[1]));
                    // Dibagi 10 karena ada sepuluh hari. Hari terakhir mendapatkan nilai tertinggi. 
                    ps.setDouble(16, Math.max(prob_estimates[0], prob_estimates[1]) + (new Double(this.hariMasaLampau) / 10));
                    ps.executeUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        return yPred;
    }

    /**
     * Delete Zhari and accumulators table.
     *
     * @param connection
     */
    private void deleteTableRecords(Connection connection) {
        try {
            connection.prepareStatement("delete from accumulator").executeUpdate();
            connection.prepareStatement("delete from prediction_history").executeUpdate();
            connection.prepareStatement("delete from permanent").executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Persiapan pelatihan SVM. Pemanggilan pelatihan SVM.
     *
     * @param dataLatih
     */
    private void trainingPreparation(BigList<BigList<Double>> dataLatih) {
        double[][] xtrain = new double[dataLatih.size()][13];
        double[][] ytrain = new double[dataLatih.size()][1];
        for (int i = 0; i < dataLatih.size(); i++) {
            final BigList<Double> dalamData = dataLatih.get(i);
            for (int j = 0; j < dalamData.size() - 1; j++) {
                xtrain[i][j] = dalamData.get(j);
            }
            ytrain[i][0] = dalamData.get(12);
        }
        
        this.currentSVMModel = this.svmTraining(xtrain, ytrain);
    }
}
