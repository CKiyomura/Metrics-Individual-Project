import picocli.CommandLine;

public class MetricsApp {
    public static void main(String[] args)
    {
        IMetrics myMetrics = new Metrics();
        CommandLine.run((Metrics) myMetrics, args);
    }
}
