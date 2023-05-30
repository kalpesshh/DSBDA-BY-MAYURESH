import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class LogAnalyzer {

  public static class LogMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(LongWritable key, Text value, Context context)
        throws IOException, InterruptedException {
      // Parse the log file and extract relevant information
      String line = value.toString();
      // Process the log line and extract required information
      // Emit key-value pairs for further processing
      context.write(word, one);
    }
  }

  public static class LogReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values, Context context)
        throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "Log Analysis");
    job.setJarByClass(LogAnalyzer.class);
    job.setMapperClass(LogMapper.class);
    job.setCombinerClass(LogReducer.class);
    job.setReducerClass(LogReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}

/*To compile and run this code on an Ubuntu terminal with Hadoop installed, you can follow these steps:

javac -classpath $(hadoop classpath) LogAnalyzer.java

jar cf loganalyzer.jar LogAnalyzer*.class

hadoop jar loganalyzer.jar LogAnalyzer <input_directory> <output_directory>

*/
