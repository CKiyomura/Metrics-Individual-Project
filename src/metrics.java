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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class metrics
{
    static operandCollect collector = new operandCollect();

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
                "    -H     will print Halstead's metrics" +
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

        //variables for handling operandCollect
        int N1Total = 0;
        int N2Total = 0;
        int nOneTotal = 0;
        int nTwoTotal = 0;
        int programVocabTotal = 0;
        int ProgramLengthTotal = 0;
        double calcProgLengthTotal = 0;
        double volumeTotal = 0;
        double difficultyTotal = 0;
        double effortTotal = 0;
        double timeReqTotal = 0;
        double bugsTotal = 0;

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
                    if(argFlags[5])
                        printHalstead(collector.N1, collector.N2, collector.nOne, collector.nTwo, collector.programVocab,
                                collector.programLength, collector.calcProgLength, collector.volume, collector.difficulty,
                                collector.effort, collector.timeReq, collector.bugs);
                    System.out.println("   " + currentFile);
                    //accumulates the file by file amounts into totals.
                    //need to add a part to accumulate source code lines
                    for (int j = 0; j < totals.length; j++) {
                        totals[j] += imidiates[j];
                    }
                    N1Total += collector.N1;
                    N2Total += collector.N2;
                    nOneTotal += collector.nOne;
                    nTwoTotal += collector.nTwo;
                    programVocabTotal += collector.programVocab;
                    ProgramLengthTotal += collector.programLength;
                    calcProgLengthTotal += collector.calcProgLength;
                    volumeTotal += collector.volume;
                    difficultyTotal += collector.difficulty;
                    effortTotal += collector.effort;
                    timeReqTotal += collector.timeReq;
                    bugsTotal += collector.bugs;
                }
                catch (FileNotFoundException e){
                    System.out.println("metrics: "+args[i]+": No such file or directory");
                }
            }
        }
        if(counter <= 0){
            instruction();
        } else 
            if (counter > 1){
                printCount(argFlags, totals, "total");
                if(argFlags[5])
                    printHalstead(N1Total, N2Total, nOneTotal, nTwoTotal, programVocabTotal, ProgramLengthTotal,
                            calcProgLengthTotal, volumeTotal, difficultyTotal, effortTotal, timeReqTotal, bugsTotal);
                System.out.println("   " + "total");
            }
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
        boolean halsFlag = false;
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
                    if(args[i].charAt(j)=='H')
                        halsFlag = true;
                }
            }
        }
        //if flags were not passed then default to all true
        if(!lineFlag && !wordFlag && !charFlag && !slocFlag && !commFlag && !halsFlag)
            return new boolean[] {true, true, true, true, true, true};
        //if flags were passed then deliver flags
        return new boolean[] {lineFlag, wordFlag, charFlag, slocFlag, commFlag, halsFlag};
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

    private static void countOperators(BufferedReader readIt)throws Exception
    {
        collector.parseOps(readIt);
        collector.countN();
    }

    private static void printHeader(boolean[] flags)
    {
        if(flags[3] || flags[4] || flags[5]) {
            //Print Header
            if (flags[0])
                System.out.print("  Lines   ");
            if (flags[1])
                System.out.print("Words   ");
            if (flags[2])
                System.out.print("Char    ");
            if (flags[3])
                System.out.print("SLoC    ");
            if (flags[4])
                System.out.print("Comment ");
            if (flags[5]){
                if (flags[4]){
                  //System.out.print("Comment");
                    System.out.print("N1      ");
                    System.out.print("N2      ");
                    System.out.print("n1      ");
                    System.out.print("n2      ");
                    System.out.print("ProVoc  ");
                    System.out.print("ProLen  ");
                    System.out.print("CProLen ");
                    System.out.print("Volume  ");
                    System.out.print("Diff    ");
                    System.out.print("Effort  ");
                    System.out.print("TimeReq ");
                    System.out.print("Bugs    ");
                }
            }
            System.out.println();
        }
    }

    //uses the flags parsed earlier to decide which word count stats get printed
    private static void printCount(boolean[] flags, int[] stats, String currFile)
    {
        //If the SLoC flag or comment flag is true
        System.out.print("  ");
        int spareLength;
        for (int i = 0; i < 5; i++) {
            if(flags[i]){
                System.out.print(stats[i]);
                spareLength = 8 - (int) (Math.log10(stats[i]) + 1);
                for (int j = 0; j < spareLength; j++) {
                    System.out.print(" ");
                }
            }
        }
        //System.out.println("   " + currFile);
    }

    private static void printHalstead(int N1, int N2, int nOne, int nTwo, int programVocab,
                                      int programLength, double calcProgLength, double volume,
                                      double difficulty, double effort, double timeReq, double bugs)
    {
        System.out.print(N1);
        printHalsSpacer(N1);
        System.out.print(N2);
        printHalsSpacer(N2);
        System.out.print(nOne);
        printHalsSpacer(nOne);
        System.out.print(nTwo);
        printHalsSpacer(nTwo);
        System.out.print(programVocab);
        printHalsSpacer(programVocab);
        System.out.print(programLength);
        printHalsSpacer(programLength);
        System.out.printf("%.2f", calcProgLength);
        printHalsFSpacer(calcProgLength);
        System.out.printf("%.2f", volume);
        printHalsFSpacer(volume);
        System.out.printf("%.2f", difficulty);
        printHalsFSpacer( difficulty);
        System.out.printf("%.2f", effort);
        printHalsFSpacer(effort);
        System.out.printf("%.2f", timeReq);
        printHalsFSpacer(timeReq);
        System.out.printf("%.2f", bugs);
    }

    private static void printHalsSpacer(double num){
        int spareLength;
        spareLength = 8 - (int) (Math.log10(num) + 1);
        for (int j = 0; j < spareLength; j++) {
            System.out.print(" ");
        }
    }

    private static void printHalsFSpacer(double num){
        int spareLength;
        spareLength = 5 - (int) (Math.log10(num) + 1);
        for (int j = 0; j < spareLength; j++) {
            System.out.print(" ");
        }
    }
}

class operandCollect
{
    final char[] HALF_OPERATOR = {'=', '+', '-', '*', '/', '%','!', '>', '<', '&', '|', '?', '~', '^', ':'};

    final String[] FULL_OPERATOR = {"=", "==", "+", "++", "+=", "-", "--", "-=", "*", "*=", "/", "/=", "%", "%=",
            "!", "!=", ">", ">=", ">>", ">>>", "<", "<=", "<<", "&", "&&", "&=", "|", "||", "|=", "?:", "~", "^", "^="};

    private  char holder = '0';
    private  char opCase = '0';
    private  boolean operating = false;
    int N1;
    int N2;
    int nOne;
    int nTwo;
    int programVocab;
    int programLength;
    double calcProgLength;
    double volume;
    double difficulty;
    double effort;
    double timeReq;
    double bugs;
    ArrayList<String> codeList = new ArrayList<>();
    ArrayList<String> operatorList = new ArrayList<>();
    ArrayList<String> operandList = new ArrayList<>();

    public void parseOps(BufferedReader readFile)throws Exception
    {
        /*File currentFile = new File("NthPrime.java");
        BufferedReader reader = new BufferedReader(new FileReader(currentFile));*/
        StreamTokenizer streamIt = new StreamTokenizer(readFile);

        streamIt.slashStarComments(true);
        while(streamIt.nextToken() != streamIt.TT_EOF){
            if(streamIt.sval != null || streamIt.ttype == StreamTokenizer.TT_NUMBER) {
                if(holder == '>' && opCase == '>'){
                    codeList.add(">>");
                    reset();
                } else if (holder != '0' && holder != '?') {
                    codeList.add("" + holder);
                    reset();
                }
            }
            if (streamIt.sval != null) {
                codeList.add(streamIt.sval);
                operating = false;
            } else if (streamIt.ttype == StreamTokenizer.TT_NUMBER) {
                codeList.add("" + streamIt.nval);
                operating = false;
            } else {
                if (holder == '>' && opCase == '>') {
                    if((char)streamIt.ttype == '>') {
                        codeList.add(">>>");
                    } else if((char)streamIt.ttype == '=') {
                        codeList.add(">>=");
                    }
                    reset();
                } else if (!operating) {
                    for (char c : HALF_OPERATOR) {
                        if (c != ':') {
                            if (c == (char) streamIt.ttype) {
                                holder = (char) streamIt.ttype;
                                operating = true;
                            }
                        }
                    }
                } else {
                    for (char c : HALF_OPERATOR) {
                        if (c == (char) streamIt.ttype) {
                            opCase = (char) streamIt.ttype;
                        }
                    }
                }
            }
            if(operating && opCase != '0') {
                switch (holder){
                    case '=':
                        if(opCase == '=')
                            codeList.add("==");
                        reset();
                        break;

                    case '+':
                        if(opCase == '+')
                            codeList.add("++");
                        else if(opCase == '=')
                            codeList.add("+=");
                        reset();
                        break;

                    case '-':
                        if(opCase == '-')
                            codeList.add("--");
                        else if(opCase == '=')
                            codeList.add("-=");
                        reset();
                        break;

                    case '*':
                        if(opCase == '=')
                            codeList.add("*=");
                        reset();
                        break;

                    case '/':
                        if(opCase == '=')
                            codeList.add("/=");
                        reset();
                        break;

                    case '%':
                        if(opCase == '=')
                            codeList.add("%=");
                        reset();
                        break;

                    case '!':
                        if(opCase == '=')
                            codeList.add("!=");
                        reset();
                        break;

                    case '>':
                        if(opCase == '=')
                            codeList.add(">=");
                        else if(opCase == '>')
                            break;
                        reset();
                        break;

                    case '<':
                        if(opCase == '=')
                            codeList.add("<=");
                        else if(opCase == '<')
                            codeList.add("<<");
                        reset();
                        break;

                    case '&':
                        if(opCase == '&')
                            codeList.add("&&");
                        else if(opCase == '=')
                            codeList.add("&=");
                        reset();
                        break;

                    case '|':
                        if(opCase == '|')
                            codeList.add("||");
                        else if(opCase == '=')
                            codeList.add("|=");
                        reset();
                        break;

                    case '?':
                        if(opCase == ':')
                            codeList.add("?:");
                        reset();
                        break;

                    case '^':
                        if(opCase == '=')
                            codeList.add("^=");
                        reset();
                        break;

                    default:
                        reset();
                        break;
                }
            }
        }
    }

    public void countN()
    {
        int operandIndex = -1;

        for (int i = 0; i < this.codeList.size(); i++) {
            for (String s : FULL_OPERATOR) {
                if(this.codeList.get(i).equals(s)){
                    if(this.codeList.get(i).equals("~")){                                               //~ is in front of operand
                        operatorList.add(this.codeList.get(i));
                        operandList.add(this.codeList.get(i+1));
                        operandIndex = i+1;
                    } else if(this.codeList.get(i).equals("++") || this.codeList.get(i).equals("--")){  //behind operand
                        operatorList.add(this.codeList.get(i));
                        if(i-1 != operandIndex)
                            operandList.add(this.codeList.get(i-1));
                    } else {                                                                            //operands on either side
                        operatorList.add(this.codeList.get(i));
                        operandList.add(this.codeList.get(i+1));
                        if(i-1 != operandIndex)
                            operandList.add(this.codeList.get(i-1));
                        operandIndex = i+1;
                    }
                }
            }
        }
        N1 = operatorList.size();
        N2 = operandList.size();
        Set<String> uniqueOperator = new HashSet<>(operatorList);
        Set<String> uniqueOperand = new HashSet<>(operandList);
        nOne = uniqueOperator.size();
        nTwo = uniqueOperand.size();
        programVocab = nOne + nTwo;
        programLength = N1 + N2;
        calcProgLength = nOne * (Math.log(nOne)/Math.log(2)) + nTwo * (Math.log(nTwo)/Math.log(2));
        volume = programLength * (Math.log(programVocab)/Math.log(2));
        difficulty = (nOne/2)*(N2/nTwo);
        effort = difficulty * volume;
        timeReq = effort/18;
        bugs = volume/3000;
    }

    private void reset(){
        holder = '0';
        opCase = '0';
        operating = false;
    }
}

class lineCollect {
    //This code has been acquired from https://gist.github.com/shiva27/143229. I have extended their javadoc code
    //to include both an @author and an @see to the page of this code. This code has been slightly modified
    //to fit my needs.
    /**
     * @author shiva27
     * @see <a href="https://gist.github.com/shiva27/1432290">https://gist.github.com/shiva27/1432290</a>
     */
    public static int[] getNumberOfLines(BufferedReader bReader)
            throws IOException {
        int countSource = 0;
        int countComment = 0;
        boolean commentBegan = false;
        String line = null;

        while ((line = bReader.readLine()) != null) {
            line = line.trim();
            if ("".equals(line)) {
                continue;
            }
            if (line.startsWith("//")) {
                countComment++;
                continue;
            }
            if (commentBegan) {
                countComment++;
                if (commentEnded(line)) {
                    line = line.substring(line.indexOf("*/") + 2).trim();
                    commentBegan = false;
                    if ("".equals(line)) {
                        continue;
                    }
                    if (line.startsWith("//")) {
                        continue;
                    }
                } else {
                    continue;
                }
            }
            if (isSourceCodeLine(line)) {
                countSource++;
            }
            if (commentBegan(line)) {
                commentBegan = true;
            }
        }
        return new int[] {countSource, countComment};
    }

    /**
     * @author shiva27
     * @see <a href="https://gist.github.com/shiva27/1432290">https://gist.github.com/shiva27/1432290</a>
     * @param line
     * @return This method checks if in the given line a comment has begun and has not ended
     */
    private static boolean commentBegan(String line) {
        // If line = /* */, this method will return false
        // If line = /* */ /*, this method will return true
        int index = line.indexOf("/*");
        if (index < 0) {
            return false;
        }
        int quoteStartIndex = line.indexOf("\"");
        if (quoteStartIndex != -1 && quoteStartIndex < index) {
            while (quoteStartIndex > -1) {
                line = line.substring(quoteStartIndex + 1);
                int quoteEndIndex = line.indexOf("\"");
                line = line.substring(quoteEndIndex + 1);
                quoteStartIndex = line.indexOf("\"");
            }
            return commentBegan(line);
        }
        return !commentEnded(line.substring(index + 2));
    }

    /**
     * @author shiva27
     * @see <a href="https://gist.github.com/shiva27/1432290">https://gist.github.com/shiva27/1432290</a>
     * @param line
     * @return This method checks if in the given line a comment has ended and no new comment has not begun
     */
    private static boolean commentEnded(String line) {
        // If line = */ /* , this method will return false
        // If line = */ /* */, this method will return true
        int index = line.indexOf("*/");
        if (index < 0) {
            return false;
        } else {
            String subString = line.substring(index + 2).trim();
            if ("".equals(subString) || subString.startsWith("//")) {
                return true;
            }
            if(commentBegan(subString))
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }

    /**
     * @author shiva27
     * @see <a href="https://gist.github.com/shiva27/1432290">https://gist.github.com/shiva27/1432290</a>
     * @param line
     * @return This method returns true if there is any valid source code in the given input line. It does not worry if comment has begun or not.
     * This method will work only if we are sure that comment has not already begun previously. Hence, this method should be called only after {@link #commentBegan(String)} is called
     */
    private static boolean isSourceCodeLine(String line) {
        boolean isSourceCodeLine = false;
        line = line.trim();
        if ("".equals(line) || line.startsWith("//")) {
            return isSourceCodeLine;
        }
        if (line.length() == 1) {
            return true;
        }
        int index = line.indexOf("/*");
        if (index != 0) {
            return true;
        } else {
            while (line.length() > 0) {
                line = line.substring(index + 2);
                int endCommentPosition = line.indexOf("*/");
                if (endCommentPosition < 0) {
                    return false;
                }
                if (endCommentPosition == line.length() - 2) {
                    return false;
                } else {
                    String subString = line.substring(endCommentPosition + 2)
                            .trim();
                    if ("".equals(subString) || subString.indexOf("//") == 0) {
                        return false;
                    } else {
                        if (subString.startsWith("/*")) {
                            line = subString;
                            continue;
                        }
                        return true;
                    }
                }

            }
        }
        return isSourceCodeLine;
    }
}
