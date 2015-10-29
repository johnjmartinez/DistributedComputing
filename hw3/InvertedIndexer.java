import java.io.IOException;
import java.lang.*;
import java.util.*;
import java.util.Map.*;
import java.util.stream.*;

//API DOC -- https://hadoop.apache.org/docs/r2.6.1/api/
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;


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
                if (w.equals("")) { continue; }
                word = new Text(w);

                //SPIT OUT NEW RECORD -- ONE PER INSTANCE
                output.collect(word, chapter);
            }
        }
    }

    public static class InvertedIndexReducer extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
        @Override
        public void reduce(Text word, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter)
          throws IOException {

            Map<String, Integer> numChapterMap = new HashMap<>(62);
            Text chapter = null;
            Integer num = null;
            String out = "\n";
            String keyChptr = null;

            while (values.hasNext()) {
                //Grab values.next() --> value=chapter
                chapter = (Text) values.next();
                keyChptr = chapter.toString();

                //Aggregate results in Map ... get(chapter)++
                if (numChapterMap.containsKey(keyChptr)){
                    num = (Integer) numChapterMap.get(keyChptr);
                    num++;
                    numChapterMap.put(keyChptr, num);
                }
                else {
                   numChapterMap.put(keyChptr, 1);
                }
            }

            //SORT AND FORMAT OUTPUT
            numChapterMap = sortByValue( numChapterMap );
            for( Entry<String, Integer> entry : numChapterMap.entrySet()) {
                out += "<"+entry.getKey()+", "+entry.getValue()+">\n";
            }

            output.collect(word, new Text(out));
        }
    }

  /**
   * COUNTING INDEXER
   - identify files
   - calculate occurrences of every word that appear in given set of files above
   - convert every character into lower-case
   - mask non-alphabetic characters by white-space
   - sorted on occurrence-frequency
   - output earlier chapter if tie --- Assuming /output

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

    public static void main(String[] args) throws Exception {
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

        JobClient.runJob(conf);
    }

    //http://stackoverflow.com/questions/28709769
    public static Map<String,Integer> sortByValue( Map<String,Integer> map ) {

        Map<String,Integer> result = new LinkedHashMap<>();
        Stream <Entry<String,Integer>> st = map.entrySet().stream();

        //st.sorted((a,b) -> b.getValue().compareTo(a.getValue()))
        st.sorted( (Entry<String, Integer> o1, Entry<String, Integer> o2) ->
                o1.getValue().equals(o2.getValue()) ?
                        o1.getKey().compareTo(o2.getKey()) : o2.getValue().compareTo(o1.getValue())
            )
            .forEach(e -> result.put(e.getKey(), e.getValue()));

        return result;
    }

}



