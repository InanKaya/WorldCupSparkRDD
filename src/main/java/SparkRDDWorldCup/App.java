package SparkRDDWorldCup;

import com.google.common.collect.Iterators;
import com.mongodb.spark.sql.SparkSessionFunctions;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import javax.swing.text.Document;
import java.util.Iterator;

public class App {
    public static void main(String[] args) {
        System.setProperty("hadoop.home.dir","D:\\hadoop-2.7.1-bin");


        SparkContext sparkContext = new SparkContext();
        SparkSessionFunctions

        JavaSparkContext sc = new JavaSparkContext("local","WorldCupApp");
        JavaRDD<String> Raw_Data = sc.textFile("C:\\Users\\inan\\Desktop\\WorldCup\\WorldCupPlayers.csv");

        JavaRDD<PlayersModel> playersRDD = Raw_Data.map(new Function<String, PlayersModel>() {

            public PlayersModel call(String s) throws Exception {
                String[] dizi = s.split(",");

                return new PlayersModel(dizi[0], dizi[1], dizi[2], dizi[3], dizi[4], dizi[5], dizi[6], dizi[7], dizi[8]);
            }
        });
        JavaRDD<PlayersModel> tur = playersRDD.filter(new Function<PlayersModel, Boolean>() {
            public Boolean call(PlayersModel playersModel) throws Exception {
                return playersModel.getTeam().equals("TUR");
            }
        });

        JavaPairRDD<String, String> mapRDD = tur.mapToPair(new PairFunction<PlayersModel, String, String>() {
            public Tuple2<String, String> call(PlayersModel playersModel) throws Exception {
                return new Tuple2<String, String>(playersModel.getPlayerName(), playersModel.getMatchID());
            }
        });

        JavaPairRDD<String, Iterable<String>> groupPlayer = mapRDD.groupByKey();

        JavaRDD<groupPlayer> resultRDD = groupPlayer.map(new Function<Tuple2<String, Iterable<String>>, groupPlayer>() {
            public groupPlayer call(Tuple2<String, Iterable<String>> dizi) throws Exception {
                Iterator<String> iteratorraw = dizi._2().iterator();
                int size = Iterators.size(iteratorraw);
                return new groupPlayer(dizi._1, size);
            }
        });


        resultRDD.foreach(new VoidFunction<SparkRDDWorldCup.groupPlayer>() {
            public void call(groupPlayer groupPlayer) throws Exception {
                System.out.println(groupPlayer.getPlayerName() + " " + groupPlayer.getMatchCount());
            }
        });

        JavaRDD<Document> MongoRDD = resultRDD.map(new Function<groupPlayer, Document>() {
            public Document call(groupPlayer groupPlayer) throws Exception {
                return Document.parse("{PlayerName: " + " ' " + groupPlayer.getPlayerName() + "'"
                        + ","+"PlayerMatchCount: "+ groupPlayer.getMatchCount()
                        +"}");
            }
        });
    }
}
