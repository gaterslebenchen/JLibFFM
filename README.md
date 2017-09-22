JLibFFM
=====

A Java implementation of LIBFFM: A Library for Field-aware Factorization Machines

### Description ###
LIBFFM is an open source tool for field-aware factorization machines (FFM).For the formulation of FFM, please see [this paper](http://www.csie.ntu.edu.tw/~cjlin/papers/ffm.pdf). It has been used to win the top-3 in recent click-through rate prediction competitions ([Criteo](https://www.kaggle.com/c/criteo-display-ad-challenge), [Avazu](https://www.kaggle.com/c/avazu-ctr-prediction), [Outbrain](https://www.kaggle.com/c/outbrain-click-prediction), and [RecSys 2015](http://dl.acm.org/citation.cfm?id=2813511&dl=ACM&coll=DL&CFID=941880276&CFTOKEN=60022934)).
JLibFFM is the Java version of [LIBFFM](https://www.csie.ntu.edu.tw/~cjlin/libffm/).

### Dependencies and requirements ###
Please note that the code is written in [Java](https://www.oracle.com/java/index.html), and this project is a [Maven](https://maven.apache.org/) project.


### How to run ###
Please go to the project folder and run the command 
"mvn clean package", then we will get a archive file in the sub folder "target", it is "JLibFFM.jar".

You can find a description of supported data format at [here](https://github.com/guestwalk/libffm)("Data Format" section).

(1) an example:

     java -Xms10240M -Xmx20480M -jar JLibFFM.jar -e 0.1 -l 0.0001 -t 15 -k 8 -r true -n true -a false -s 8 -i tr_std.csv.sam -p va_std.csv.sam

(2) the meaning of the parameters:

      -p     set path to the validation set
      -a     stop at the iteration that achieves the best validation loss (must be used with -p).
      -help  print help
      -r     By default we do data shuffling, you can use  `-r false' to disable this function.
      -s     set number of threads (default 1)
      -t     set number of iterations (default 15)
      -e     set learning rate (default 0.2)
      -i     set path to the training set
      -k     set number of latent factors (default 4)
      -l     set regularization parameter (default 0.00002)
      -n     By default we do instance-wise normalization. That is, we normalize the 2-norm of each instance to 1. You can use  `-n false' to disable this function.
    
### Evaluation data ### 
The data comes from [Criteo](http://labs.criteo.com/2014/02/kaggle-display-advertising-challenge-dataset/).

We follow the approach which was proposed by [YuChin Juan, Wei-Sheng Chin, and Yong Zhuang](https://github.com/guestwalk/kaggle-2014-criteo).

(1) Download the data set from Criteo.
    
    http://labs.criteo.com/2014/02/kaggle-display-advertising-challenge-dataset/
(2) Decompress and make sure the files are correct.
    
    $ md5sum dac.tar.gz
    df9b1b3766d9ff91d5ca3eb3d23bed27  dac.tar.gz
    $ tar -xzf dac.tar.gz
    $ md5sum train.txt test.txt
    4dcfe6c4b7783585d4ae3c714994f26a  train.txt
    94ccf2787a67fd3d6e78a62129af0ed9  test.txt

(3) Use `CriteoDataPipeline' to convert training data(include Feature engineering).
    
    $ java -Xms10240M -Xmx10240M -cp JLibFFM.jar com.github.gaterslebenchen.libffm.examples.CriteoDataPipeline -f tr -s 8 -i train.txt -o train-ctiteo.csv
    
    the meaning of the parameters:    
	 -help           print parameters
	 -s              set number of threads (default 1)
	 -t              set the temporary Files path(default is current directory)
	 -f              use  `-f tr' for training data and `-f te' for test data
	 -i              set the input file path
	 -o              set the output file path
	 
(4) split data to train data and validation data.

     $ java -cp JLibFFM.jar com.github.gaterslebenchen.libffm.examples.SplitData train-ctiteo.csv '$your file output folder$' false
     
     '$your file output folder$' is your actual file output folder.
     
(5) run the training program:
     
      java -Xms10240M -Xmx30720M -jar JLibFFM.jar -e 0.1 -l 0.0001 -t 15 -k 8 -r true -n true -a false -s 8 -i tr_std.csv -p va_std.csv
      
with these parameters, our best loss is: ** 0.43853 **.

      
### How to save and load model ###
The Main class com.github.gaterslebenchen.libffm.Main has saveModel and loadModel methods.