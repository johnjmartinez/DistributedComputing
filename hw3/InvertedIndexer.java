import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

//API DOC -- https://hadoop.apache.org/docs/r2.6.1/api/
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

// You may need to import other packages -- WTF


public class InvertedIndexer {

    // The mapper class, you should modify T1, T2, T3, T4 to your desired types
    public static class InvertedIndexMapper extends MapReduceBase implements Mapper<T1, T2, T3, T4> {
    //MAYBE Mapper<LongWritable, Text, Text, LongWritable>?

        // TODO: MAYBE SIGNATURE = (key, "word chapter count")?
        private Text word = new Text();
        private LongWritable count = new LongWritable();

        public void map(T1 key, T2 val, OutputCollector<T3, T4> output, Reporter reporter)
          throws IOException { //context is Outputcollector + Reporter????
        // TODO: implement your map function

        }
    }


    // The reducer class, you should modify T1, T2, T3, T4 to your desired types
    public static class InvertedIndexReducer extends MapReduceBase
      implements Reducer<T1, T2, T3, T4> {

        public void reduce(T1 key, Iterator<T2> values, OutputCollector<T3, T4> output, Reporter reporter)
          throws IOException { //context is Outputcollector + Reporter????
        // TODO: implement your reduce function

            //SUM ocurrences of KEY word?
            long sum = 0;
            for (LongWritable val : values) {
                sum += val.get();
            }
        }
    }

  /**
   * The actual main() method for our program; this is the
   * "driver" for the MapReduce job.
   *
   * COUNTING INDEXER
   - identify files
   - calculate occurrences of every word that appear in given set of files above
   - convert every character into lower-case
   - mask non-alphabetic characters by white-space
   - sorted on occurrence-frequency
   - print earlier chapter if tie

   *IMPLEMENTATION
   - Single-Node run, Hadoop 2.6.1

   *output
   word1
   <file-name1, occurrence-frequency1>
   <file-name2, occurrence-frequency2>
   .
   word2
   <file-name3, occurrence-frequency3>
   <file-name4, occurrence-frequency4>
   .
   */

    public static void main(String[] args) {
    // TODO: configure the hadoop job and run the job
        //CONFIGURATION
        Configuration conf = new Configuration();
        Job job = new Job(conf, "wordChapterCounter");

        //OR
        //Job job = new Job(getConf());
        //job.setJarByClass(getClass());
        //job.setJobName(getClass().getSimpleName());

        job.setJarByClass(InvertedIndexer.class);
        job.setMapperClass(InvertedIndexMapper.class);
        job.setReducerClass(InvertedIndexReducer.class);

        //KEYS AND I/O SETUP --- NEED TO CHANGE THESE
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setInputFormatClass(KeyValueTextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);


        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        boolean result = job.waitForCompletion(true);
        System.exit(result ? 0 : 1);
    }
  }
}
