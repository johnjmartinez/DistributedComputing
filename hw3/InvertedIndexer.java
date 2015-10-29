import java.io.IOException;
import java.lang.Exception;
import java.lang.System;
import java.util.*;

//API DOC -- https://hadoop.apache.org/docs/r2.6.1/api/
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;


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
        @Override
        public void map(Object key, Text value, OutputCollector<Text, Text> output, Reporter reporter)
          throws IOException { // key not used?????
          //For every instance in line, output {word,chapter} record
            
            FileSplit fSplit = (FileSplit) reporter.getInputSplit();
            Text chapter = new Text(fSplit.getPath().getName());
            String line = value.toString().toLowerCase();

            line = line.replaceAll("\\W"," ");
            System.out.println(chapter+" line:"+line);

            String[] words = line.split("\\s+");
            Text word;

            for (String w: words) {
                word = new Text(w);

                //SPIT OUT NEW RECORD -- ONE PER INSTANCE
                output.collect(word, chapter);
            }
        }
    }

    public static class InvertedIndexReducer extends MapReduceBase implements Reducer<Text, Text, Text, MapWritable> {
        @Override
        public void reduce(Text word, Iterator<Text> values, OutputCollector<Text, MapWritable> output, Reporter reporter)
          throws IOException {

            MapWritable numChapterMap = new MapWritable();
            Text chapter = null;
            Integer num = null;
            IntWritable val = null;

            while (values.hasNext()) {
                //Grab values.next() --> value=chapter
                chapter = (Text) values.next();
                
                //Aggregate results in Map ... get(chapter)++
                if (numChapterMap.containsKey(chapter)) {
                    val = (IntWritable) numChapterMap.get(chapter);
                    num = val.get();
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
   **/

    public static void main(String[] args) {
        //CONFIGURATION
        JobConf conf = new JobConf(InvertedIndexer.class);
        conf.setJobName("wordChapterCounter");

        conf.setMapperClass(InvertedIndexer.InvertedIndexMapper.class);
        conf.setReducerClass(InvertedIndexer.InvertedIndexReducer.class);

        conf.setMapOutputKeyClass(Text.class);
        conf.setMapOutputValueClass(Text.class);

        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);
 
        FileInputFormat.setInputPaths(conf, new Path(args[0]));
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));

        try {
            JobClient.runJob(conf);
        }
        catch (Exception e){}

    }
}

