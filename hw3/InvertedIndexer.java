import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

// You may need to import other packages


public class InvertedIndexer {


  // The mapper class, you should modify T1, T2, T3, T4 to your desired
  // types
  public static class InvertedIndexMapper extends MapReduceBase
      implements Mapper<T1, T2, T3, T4> {


    public void map(T1 key, T2 val,
        OutputCollector<T3, T4> output, Reporter reporter)
      throws IOException {
      // TODO: implement your map function
    }
  }


  // The reducer class, you should modify T1, T2, T3, T4 to your desired
  // types
  public static class InvertedIndexReducer extends MapReduceBase
      implements Reducer<T1, T2, T3, T4> {

    public void reduce(T1 key, Iterator<T2> values,
        OutputCollector<T3, T4> output, Reporter reporter)
      throws IOException {
      // TODO: implement your reduce function
    }
  }


  /**
   * The actual main() method for our program; this is the
   * "driver" for the MapReduce job.
   */
  public static void main(String[] args) {
    // TODO: configure the hadoop job and run the job
  }
}
