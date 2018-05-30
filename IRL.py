import pandas
from scipy.stats import zscore

# 0 --> Non-Spammer, 1 --> Spammer
def scoringZ():
	zscoreMap = {}
	for i in range(1,11):
		namaFile = str(i)+".csv"
		hari1 = pandas.read_csv(namaFile,low_memory=False,header=None)
		hari1[12] = hari1[12].astype('category')
		hari1[12] = hari1[12].cat.codes
		hari1[hari1.select_dtypes('int64').columns]=hari1[hari1.select_dtypes('int64').columns].apply(zscore)
		zscoreMap[namaFile] = hari1
	return zscoreMap	

allData = scoringZ()

def pengujian(data=allData):
	for i in range(1,11):
		dataLatih = data[str(i)+".csv"]
		dataUji = None
		for j in range(1,11):
			if(j != i):
				namaFile = str(j)+".csv"
				if(dataUji is None):
					dataUji = data[namaFile]
				else:
					dataUji=dataUji.append(data[namaFile])
		from sklearn import svm
		from sklearn.model_selection import train_test_split
		X_train = dataLatih.iloc[:,0:12]
		y_train = dataLatih.iloc[:,12]
		X_test = dataUji.iloc[:,0:12]
		y_test = dataUji.iloc[:,12]
		clf = svm.SVC(cache_size=2000)
		clf.fit(X_train,y_train)
		from sklearn.metrics import precision_recall_fscore_support
		precision,recall,fbeta_score,support = precision_recall_fscore_support(y_test, clf.predict(X_test))
		with open('NRF.txt', 'a') as the_file:
			the_file.write("Day: "+str(i)+"\n")
			the_file.write("Precision: "+str(precision[1])+"\n")
			the_file.write("Recall: "+str(recall[1])+"\n")
			the_file.write("FMeasure: "+str(fbeta_score[1])+"\n")
			the_file.write("****************"+"\n")
		print("Pass Day: ",i)
pengujian()