import java.io.IOException;
import java.util.StringTokenizer;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WordCooccurPairs {
    
    public static class PairsMapper
    extends Mapper<Object, Text, Text, IntWritable>{
        
        private final static IntWritable one = new IntWritable(1);
        private Text word_pair = new Text();
        private String first ;
        private String second;
        // help: kpshadoop.blogspot.com/2014/06/word-co-occurrence-problem.html
        
        public void map(Object key, Text value, Context context
                        ) throws IOException, InterruptedException {
            String line_string = value.toString();  
            String[] tokens = line_string.split("\\W+");
            
            for(int i=0; i<=tokens.length-1;i++){
                for(int j=1; i+j<=tokens.length-1;j++){
                    
                    // remove the  RT , blank ,emojis , numbers , co , u, single character as the first token
                    
                    if( tokens[i].contains("\\<") || 
                        tokens[i].contains("\\>") || 
                        tokens[i].matches(".*\\d+.*") || 
                        tokens[i].equalsIgnoreCase("u") || 
                        tokens[i].equalsIgnoreCase("RT") || 
                        tokens[i].equalsIgnoreCase("co") || 
                        tokens[i].equalsIgnoreCase("") || 
                        tokens[i].length() == 1 || 
                        tokens[i].contains("\\_") || 
                        tokens[i].contains("\\t+")){
                        continue;    
                    }
                    
                    first = tokens[i];
                    second = tokens[i+j];
                    
                    word_pair.set(first+","+second);
                    context.write(word_pair,one);
                }
                
                
                
            }
            
        }
        
        
    }
    
    public static class PairSumReducer
    extends Reducer<Text,IntWritable,Text,IntWritable> {
        private IntWritable result = new IntWritable();
        
        public void reduce(Text key, Iterable<IntWritable> values,Context context) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable value : values) {
                sum += value.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }
    
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Pairs Approach for Word Co Occurence");
        job.setJarByClass(WordCooccurPairs.class);
        job.setMapperClass(PairsMapper.class);
        job.setCombinerClass(PairSumReducer.class);
        job.setReducerClass(PairSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
