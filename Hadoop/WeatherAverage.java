import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WeatherAverage {

  public static class WeatherMapper extends Mapper<Object, Text, Text, DoubleWritable> {

    private Text keyOutput = new Text();
    private DoubleWritable valueOutput = new DoubleWritable();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      String line = value.toString();
      String[] fields = line.split(",");

      // Assuming the temperature, dew point, and wind speed columns are at specific indices
      double temperature = Double.parseDouble(fields[2]);
      double dewPoint = Double.parseDouble(fields[3]);
      double windSpeed = Double.parseDouble(fields[4]);

      keyOutput.set("average");
      valueOutput.set(temperature);
      context.write(keyOutput, valueOutput);

      keyOutput.set("average");
      valueOutput.set(dewPoint);
      context.write(keyOutput, valueOutput);

      keyOutput.set("average");
      valueOutput.set(windSpeed);
      context.write(keyOutput, valueOutput);
    }
  }

  public static class WeatherReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {

    private DoubleWritable result = new DoubleWritable();

    public void reduce(Text key, Iterable<DoubleWritable> values, Context context)
        throws IOException, InterruptedException {
      double sum = 0.0;
      int count = 0;

      for (DoubleWritable val : values) {
        sum += val.get();
        count++;
      }

      double average = sum / count;
      result.set(average);
      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "Weather Average");
    job.setJarByClass(WeatherAverage.class);
    job.setMapperClass(WeatherMapper.class);
    job.setCombinerClass(WeatherReducer.class);
    job.setReducerClass(WeatherReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(DoubleWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}

/* run commands for ubuntu terminal

javac -classpath $(hadoop classpath) WeatherAverage.java

hadoop jar WeatherAverage.jar WeatherAverage /input/system.log /output

*/
