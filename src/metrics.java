/********************************************************************
*********************************************************************
Instructor:  Daryl Posnett, Ph.D.
Student: Cameron Kiyomura
Project 1: Metrics
Goal: Write a Java word count program that runs from the command line,
accepts wild card file statements, parses simple flags and is able to
count a source files number of comments or lines of code.
********************************************************************/
import java.io.*;

public class metrics
{
    //Following advice: Moved the instrucstions method to top of the class for readability
    //and self documentation
    private static void instruction()
    {
        System.out.println
                (
                "Usage: java JavaWC <OPTION> <filename>\n" +
                "    OPTIONAL\n" +
                "    -l     will print the line count of a file\n" +
                "    -c     will print the character count\n" +
                "    -w     will print the word count\n" +
                "    -s     will print the source line of code count\n" +
                "    -C     will print the comment count\n" +
                "    <filename> will print all of the above"
                );
    }

    public static void main(String[] args)throws Exception
    {
        if(args.length == 0){
            instruction();
            return;
        }

        //collects flags from command line arguements to know which to print
        boolean[] argFlags = parseFlags(args);
        
        //keeps track of whether the file is a java, c, cpp, h, hpp
        boolean isCode = false;
        
        //total array collect stats this order: lines, words, char, SLoC, Comments
        int[] totals = {0, 0, 0, 0, 0};
        int[] imidiates;

        //counter keeps track of how often files have been wordcounted
        int counter = 0;

        printHeader(argFlags);

        //at this point main attempts to read files and starts calling methods to collect stats
        for (int i = 0; i < args.length; i++) {
            if(args[i].charAt(0) != '-') {
                try {
                    File currentFile = new File(args[i]);

                    if(args[i].endsWith(".java") || args[i].endsWith(".c") || args[i].endsWith(".h")
                            || args[i].endsWith(".cpp") || args[i].endsWith(".hpp"))
                        isCode = true;

                    counter++;
                    imidiates = countFile(currentFile, isCode);
                    printCount(argFlags, imidiates, args[i]);
                    //accumulates the file by file amounts into totals.
                    //need to add a part to accumulate source code lines
                    for (int j = 0; j < totals.length; j++) {
                        totals[j] += imidiates[j];
                    }

                }
                catch (FileNotFoundException e){
                    System.out.println("metrics: "+args[i]+": No such file or directory");
                }
            }
        }
        if(counter <= 0){
            instruction();
        } else 
            if (counter > 1)
                printCount(argFlags, totals, "total");
    }

    private static boolean[] parseFlags(String[] args)
    {
        String entry;
        char element;
        boolean lineFlag = false;
        boolean wordFlag = false;
        boolean charFlag = false;
        boolean slocFlag = false;
        boolean commFlag = false;
        for (int i = 0; i < args.length; i++) {
            if(args[i].charAt(0) == '-'){
                for (int j = 1; j < args[i].length(); j++) {
                    if(args[i].charAt(j)=='l')
                        lineFlag = true;
                    if(args[i].charAt(j)=='w')
                        wordFlag = true;
                    if(args[i].charAt(j)=='c')
                        charFlag = true;
                    if(args[i].charAt(j)=='s')
                        slocFlag = true;
                    if(args[i].charAt(j)=='C')
                        commFlag = true;
                }
            }
        }
        //if flags were not passed then default to all true
        if(!lineFlag && !wordFlag && !charFlag && !slocFlag && !commFlag)
            return new boolean[] {true, true, true, true, true};
        //if flags were passed then deliver flags
        return new boolean[] {lineFlag, wordFlag, charFlag, slocFlag, commFlag};
    }

    //This method parses out the actual file by line, by letter and prints the tallied results for WC.
    private static int[] countFile(File toBeRead, boolean isSource)throws Exception
    {
        BufferedReader reader = new BufferedReader(new FileReader(toBeRead));
        String currentLine;
        int wordTally = 0;
        int lineTally = 0;
        int charTally = 0;
        char currentLetter;
        boolean isWord;
        //Some Linux shinanigan's need isFirstLine, explained more below
        boolean isFirstLine = true;
        while((currentLine = reader.readLine()) != null){
            isWord = false;
            charTally += currentLine.length();
            // Reproducing Linux's WC includes two caveats: the first line doesn't count and
            // newline adds two more to char's counter because Line Feed counts too.
            if(isFirstLine) {
                isFirstLine = false;
            } else {
                charTally += 2;
                lineTally++;
            }
            for (int i = 0; i < currentLine.length(); i++) {
                currentLetter = currentLine.charAt(i);
                if(currentLetter == ' ')
                    isWord = false;
                if(currentLetter != ' ' && !isWord){
                    wordTally++;
                    isWord = true;
                }
            }
        }

        reader = new BufferedReader(new FileReader(toBeRead));
        //Code Tally array order: Source Line of Code, Comment Line
        int [] codeTally = {0, 0};
        if(isSource){
            lineCollect lineCounter = new lineCollect();
            codeTally = lineCounter.getNumberOfLines(reader);
        }

        reader = new BufferedReader(new FileReader(toBeRead));
        countOperators(reader);
        return new int[] {lineTally, wordTally, charTally, codeTally[0], codeTally[1]};
    }

    private static int[] countOperators(BufferedReader readIt)throws Exception
    {
        operandCollect collector = new operandCollect();
        collector.parseOps(readIt);
        collector.countN();
        return new int[] {0, 0};
    }

    private static void printHeader(boolean[] flags)
    {
        if(flags[3] || flags[4]) {
            //Print Header
            if (flags[0])
                System.out.print("  Lines  ");
            if (flags[1])
                System.out.print("Words  ");
            if (flags[2])
                System.out.print("Char   ");
            if (flags[3])
                System.out.print("SLoC   ");
            if (flags[4])
                System.out.println("Comment");
        }
    }

    //uses the flags parsed earlier to decide which word count stats get printed
    private static void printCount(boolean[] flags, int[] stats, String currFile)
    {
        //If the SLoC flag or comment flag is true
        System.out.print("  ");
        int spareLength;
        for (int i = 0; i < flags.length; i++) {
            if(flags[i]){
                System.out.print(stats[i]);
                spareLength = 7 - (int) (Math.log10(stats[i]) + 1);
                for (int j = 0; j < spareLength; j++) {
                    System.out.print(" ");
                }
            }
        }
        System.out.println("   " + currFile);
    }
}