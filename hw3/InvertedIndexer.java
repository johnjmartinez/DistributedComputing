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

    //NOTES:
    //* Collector gets ouputs from map() and reduce() Fxs. Explicitly defined.
    //  - This shite (output/reporter) is pre-0.20 Java MapReduce API .... Context is newer (WTF)
    //* Mapper called once per line from input files
    //  - Tokenize line into words using space AFTER lowercasing and converting non-alpha to space
    //  - Outputs: (key=word, value=chapter) ... 
    //* Reducer collects all files per key
    //  - Single output per key=word are sorted values by total_num_instances and chapter (filename)
    //  - Value=file(:total_num_instances))


    public static class InvertedIndexMapper extends MapReduceBase implements Mapper<Object, Text, Text, Text> {

        public void map(Object key, Text value, OutputCollector<Text, Text> output, Reporter reporter)
          throws IOException { // key not used?????
          //For every instance in line, output {word,chapter} record
            
            FileSplit fSplit = (FileSplit) reporter.getInputSplit();
            Text chapter = new Text(fSplit.getPath().getName());
            String line = value.toString().toLowerCase();

            line = line.replaceAll("\\W"," ");

            String[] words = line.split();
            Text word;

            for (String w: words) {
                word = new Text(w);

                //SPIT OUT NEW RECORD -- ONE PER INSTANCE
                output.collect(word, chapter);
            }
        }
    }

    public static class InvertedIndexReducer extends MapReduceBase implements Reducer<Text, Text, Text, SortedMapWritable> {

        public void reduce(Text word, Iterator<Text> values, OutputCollector<Text, SortedMapWritable> output, 
          Reporter reporter) throws IOException {

            SortedMapWritable numChapterMap = new SortedMapWritable();
            Text chapter = null;
            int num =null;

            while (values.hasNext()) {
                //Grab values.next() --> value=chapter
                chapter = (Text) values.next();
                
                //Aggregate results in Map ... get(chapter)++
                if (numChapterMap.containsValue(chapter)) {
                    num = numChapterMap.get(chapter).get();
                    num++;
                    numChapterMap.put(chapter, new IntWritable(num));
                }
                else {
                   numChapterMap.put(chapter, new IntWritable(1));
                }
            }

            output.collect(word, numChapterMap);
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
   - output earlier chapter if tie --- WHERE?????

   *IMPLEMENTATION
   - Single-Node run, Hadoop 2.6.1

   *OUTPUT
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
        //CONFIGURATION
        JobConf conf = new JobConf(InvertedIndexer.class);
        conf.setJobName("wordChapterCounter");

        conf.setMapperClass(InvertedIndexer.InvertedIndexMapper.class);
        conf.setReducerClass(InvertedIndexer.InvertedIndexReducer.class);

        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);
 
        FileInputFormat.setInputPaths(conf, new Path(args[0]));
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));
        
        JobClient.runJob(conf);

        //Configuration conf = new Configuration();
        //Job job = new Job(conf, "wordChapterCounter");

        //Job job = new Job(getConf());
        //job.setJarByClass(getClass());
        //job.setJobName(getClass().getSimpleName());
        //job.setJarByClass(InvertedIndexer.class);

        //KEYS AND I/O SETUP --- NEED TO CHANGE THESE
        //job.setOutputKeyClass(Text.class);
        //job.setOutputValueClass(Text.class);
        //job.setMapOutputKeyClass(Text.class);
        //job.setMapOutputValueClass(Text.class);
        //job.setInputFormatClass(KeyValueTextInputFormat.class);
        //job.setOutputFormatClass(TextOutputFormat.class);
        //FileInputFormat.addInputPath(job, new Path(args[0]));
        //FileOutputFormat.setOutputPath(job, new Path(args[1]));
        //boolean result = job.waitForCompletion(true);
        //System.exit(result ? 0 : 1);

    }
  }
}
