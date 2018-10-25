public class MetricsApp {
    public static void main(String[] args)throws Exception
    {
        IMetrics myMetrics = new Metrics();
        ((Metrics) myMetrics).run(args);
    }
}
