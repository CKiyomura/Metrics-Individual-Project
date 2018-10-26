/********************************************************************
*********************************************************************
Instructor:  Daryl Posnett, Ph.D.
Student: Cameron Kiyomura
Project 1: Metrics
Goal: Write a Java word count program that runs from the command line,
accepts wild card file statements, parses simple flags and is able to
count a source files number of comments or lines of code.
********************************************************************/

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@CommandLine.Command(name = "Metrics", mixinStandardHelpOptions = true, version = "Metrics Sprint 4")
public class Metrics implements IMetrics, Runnable
{
    @Option(names = { "-l", "--line" }, description = "will print the line count of a file")
    private boolean[] lines = new boolean[0];

    @Option(names = { "-c", "--character" }, description = "will print the character count")
    private boolean[] characters = new boolean[0];

    @Option(names = { "-w", "--word" }, description = "will print the word count")
    private boolean[] words = new boolean[0];

    @Option(names = { "-s", "--Source" }, description = "will print the source line of code count")
    private boolean[] sources = new boolean[0];

    @Option(names = { "-C", "--comment" }, description = "will print the comment count")
    private boolean[] comments = new boolean[0];

    @Option(names = { "-H", "--halstead" }, description = "will print Halstead's Metrics")
    private boolean[] halsteads = new boolean[0];

    @Parameters(arity = "1..*", paramLabel = "FILE", description = "File(s) to process.")
    private File[] inputFiles;

    operandCollect collector = new operandCollect();
    docInfo totalDoc = new docInfo();

    //Following advice: Moved the instrucstions method to top of the class for readability
    //and self documentation
    private void instruction()
    {
        System.out.println
                (
                "Usage: java MetricsApp <OPTION> <filename>\n" +
                "    OPTIONAL\n" +
                "    -l     will print the line count of a file\n" +
                "    -c     will print the character count\n" +
                "    -w     will print the word count\n" +
                "    -s     will print the source line of code count\n" +
                "    -C     will print the comment count\n" +
                "    -H     will print Halstead's Metrics\n" +
                "    <filename> will print all of the above\n" +
                "    -h     will print these instructions\n"
                );
    }

    public void run()
    {
        //keeps track of whether the file is a java, c, cpp, h, hpp
        boolean isCode = false;

        int[] imidiates;

        //counter keeps track of how often files have been wordcounted
        int counter = 0;

        docInfo currDoc = new docInfo();

        totalDoc.prepDoc();

        //collects flags from command line arguments to know which to print
        parseFlags(totalDoc);

        printHeader(totalDoc);

        //at this point main attempts to read files and starts calling methods to collect stats
        for(int i = 0; i < inputFiles.length; i++){
            try {
                currDoc.prepDoc();
                currDoc.currentFile = inputFiles[i];
                countFile(currDoc);
                currDoc.matchFlags(totalDoc);

                if(inputFiles[i].toString().endsWith(".java") || inputFiles[i].toString().endsWith(".c") ||
                        inputFiles[i].toString().endsWith(".h") || inputFiles[i].toString().endsWith(".cpp") ||
                        inputFiles[i].toString().endsWith(".hpp")){
                    isCode = true;
                    countLines(currDoc);
                }
                if(totalDoc.halsFlag && isCode){
                    countOperator(currDoc);
                    currDoc.computeHalstead();
                }

                counter++;

                printCount(currDoc);

                if(currDoc.halsFlag)
                    printHalstead(currDoc);

                System.out.println("   " + currDoc.currentFile);

                //need to accumulate the current info into a sum total to print later
                totalDoc.accumulateInfo(currDoc);
            }
            catch (Exception e){
                System.out.println("Metrics: "+inputFiles[i].toString()+": No such file or directory");
            }

        }

        totalDoc.computeHalstead();

        if(counter <= 0){
            instruction();
        } else
            if (counter > 1){
                printCount(totalDoc);
                if(totalDoc.halsFlag)
                    printHalstead(totalDoc);
                System.out.println("   " + "total");
            }
    }

    private void parseFlags(docInfo currentDocument)
    {
        boolean lineFlag;
        boolean wordFlag;
        boolean charFlag;
        boolean slocFlag;
        boolean commFlag;
        boolean halsFlag;
        if(lines.length < 1)
            lineFlag = false;
        else
            lineFlag = true;
        if(words.length < 1)
            wordFlag = false;
        else
            wordFlag = true;
        if(characters.length < 1)
            charFlag = false;
        else
            charFlag = true;
        if(sources.length < 1)
            slocFlag = false;
        else
            slocFlag = true;
        if(comments.length < 1)
            commFlag = false;
        else
            commFlag = true;
        if(halsteads.length < 1)
            halsFlag = false;
        else
            halsFlag = true;
        //if no flags were passed then default to all true
        if(!lineFlag && !wordFlag && !charFlag && !slocFlag && !commFlag && !halsFlag){
            //return new boolean[] {true, true, true, true, true, true};
            currentDocument.lineFlag = true;
            currentDocument.wordFlag = true;
            currentDocument.charFlag = true;
            currentDocument.slocFlag = true;
            currentDocument.commFlag = true;
            currentDocument.halsFlag = true;
        } else {
            //if flags were passed then deliver flags
            currentDocument.lineFlag = lineFlag;
            currentDocument.wordFlag = wordFlag;
            currentDocument.charFlag = charFlag;
            currentDocument.slocFlag = slocFlag;
            currentDocument.commFlag = commFlag;
            currentDocument.halsFlag = halsFlag;
        }
    }

    //This method parses out the actual file by line, by letter and prints the tallied results for WC.
    private void countFile(docInfo currentDocument)throws Exception
    {
        BufferedReader reader = new BufferedReader(new FileReader(currentDocument.currentFile));
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

        currentDocument.lines = lineTally;
        currentDocument.words = wordTally;
        currentDocument.chars = charTally;
    }

    //This method counts the number of sources lines of code
    private void countLines(docInfo currentDocument)throws Exception
    {
        BufferedReader reader = new BufferedReader(new FileReader(currentDocument.currentFile));
        int [] codeTally = {0, 0};
        lineCollect lineCounter = new lineCollect();
        codeTally = lineCounter.getNumberOfLines(reader);
        currentDocument.SLoC = codeTally[0];
        currentDocument.comments = codeTally[1];
    }

    //This method counts the halstead Metrics of the file
    private void countOperator(docInfo currentDocument)throws Exception
    {
        BufferedReader reader = new BufferedReader(new FileReader(currentDocument.currentFile));
        collector.wipe();
        collector.parseOps(reader);
        collector.countN();

        currentDocument.N1 = collector.N1;
        currentDocument.N2 = collector.N2;
        currentDocument.nOne = collector.nOne;
        currentDocument.nTwo = collector.nTwo;
    }

    private void printHeader(docInfo currentDocument)
    {
        if(currentDocument.slocFlag || currentDocument.commFlag || currentDocument.halsFlag) {
            System.out.print("  ");
            //Print Header
            if (currentDocument.lineFlag)
                System.out.print("Lines   ");
            if (currentDocument.wordFlag)
                System.out.print("Words   ");
            if (currentDocument.charFlag)
                System.out.print("Char    ");
            if (currentDocument.slocFlag)
                System.out.print("SLoC    ");
            if (currentDocument.commFlag)
                System.out.print("Comment ");
            if (currentDocument.halsFlag){
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
            System.out.println();
        }
    }

    //uses the flags parsed earlier to decide which word count stats get printed
    private void printCount(docInfo currentDocument)
    {
        System.out.print("  ");
        if(currentDocument.lineFlag){
            System.out.print(currentDocument.lines);
            printSpacer(currentDocument.lines);
        }
        if(currentDocument.wordFlag){
            System.out.print(currentDocument.words);
            printSpacer(currentDocument.words);
        }
        if(currentDocument.charFlag){
            System.out.print(currentDocument.chars);
            printSpacer(currentDocument.chars);
        }
        if(currentDocument.slocFlag){
            System.out.print(currentDocument.SLoC);
            printSpacer(currentDocument.SLoC);
        }
        if(currentDocument.commFlag){
            System.out.print(currentDocument.comments);
            printSpacer(currentDocument.comments);
        }
    }

    private void printHalstead(docInfo currentDocument)
    {
        System.out.print(currentDocument.N1);
        printSpacer(currentDocument.N1);

        System.out.print(currentDocument.N2);
        printSpacer(currentDocument.N2);

        System.out.print(currentDocument.nOne);
        printSpacer(currentDocument.nOne);

        System.out.print(currentDocument.nTwo);
        printSpacer(currentDocument.nTwo);

        System.out.print(currentDocument.programVocab);
        printSpacer(currentDocument.programVocab);

        System.out.print(currentDocument.programLength);
        printSpacer(currentDocument.programLength);

        System.out.printf("%.2f", currentDocument.calcProgLength);
        printFSpacer(currentDocument.calcProgLength);

        System.out.printf("%.2f", currentDocument.volume);
        printFSpacer(currentDocument.volume);

        System.out.printf("%.2f", currentDocument.difficulty);
        printFSpacer(currentDocument.difficulty);

        System.out.printf("%.2f", currentDocument.effort);
        printFSpacer(currentDocument.effort);

        System.out.printf("%.2f", currentDocument.timeReq);
        printFSpacer(currentDocument.timeReq);

        System.out.printf("%.2f", currentDocument.bugs);
    }

    private void printSpacer(double num){
        int spareLength;
        spareLength = 8 - (int) (Math.log10(num) + 1);
        for (int j = 0; j < spareLength; j++) {
            System.out.print(" ");
        }
    }

    private void printFSpacer(double num){
        int spareLength;
        spareLength = 5 - (int) (Math.log10(num) + 1);
        for (int j = 0; j < spareLength; j++) {
            System.out.print(" ");
        }
    }

    public boolean setPath(String path){
        File test = new File(path);
        if(test.canRead())
            return true;
        return false;
    }

    public boolean isSource(){
        return totalDoc.isCode;
    }

    //basic file Metrics
    public int getLineCount(){
        return totalDoc.lines;
    }
    public int getWordCount(){
        return totalDoc.words;
    }
    public int getCharacterCount(){
        return totalDoc.chars;
    }

    //Source file Metrics
    public int getSourceLineCount(){
        return totalDoc.SLoC;
    }
    public int getCommentLineCount(){
        return totalDoc.comments;
    }

    //Halstead Metrics
    public int getHalsteadn1(){
        return totalDoc.nOne;
    }
    public int getHalsteadn2(){
        return totalDoc.nTwo;
    }
    public int getHalsteadN1(){
        return totalDoc.N1;
    }
    public int getHalsteadN2(){
        return totalDoc.N2;
    }
    public int getHalsteadVocabulary(){
        return totalDoc.programVocab;
    }
    public int getHalsteadProgramLength(){
        return totalDoc.programLength;
    }
    public int getHalsteadCalculatedProgramLenght(){
        return (int)totalDoc.calcProgLength;
    }
    public int getHalsteadVolume(){
        return (int)totalDoc.volume;
    }
    public int getHalsteadDifficulty(){
        return (int)totalDoc.difficulty;
    }
    public int getHalsteadEffort(){
        return (int)totalDoc.effort;
    }
    public int getHalsteadTime(){
        return (int)totalDoc.timeReq;
    }
    public int getHalsteadBugs(){
        return (int)totalDoc.bugs;
    }

}

//This class acts as a database for Metrics on a file. Additionally, it computes halstead
class docInfo
{
    File currentFile;

    boolean lineFlag;
    boolean wordFlag;
    boolean charFlag;
    boolean slocFlag;
    boolean commFlag;
    boolean halsFlag;

    boolean isCode;
    int lines;
    int words;
    int chars;
    int SLoC;
    int comments;
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

    public void prepDoc()
    {

        lineFlag = false;
        wordFlag = false;
        charFlag = false;
        slocFlag = false;
        commFlag = false;
        halsFlag = false;
        isCode = false;
        lines = 0;
        words = 0;
        chars = 0;
        SLoC = 0;
        comments = 0;
        N1 = 0;
        N2 = 0;
        nOne = 0;
        nTwo = 0;
        programVocab = 0;
        programLength = 0;
        calcProgLength = 0;
        volume = 0;
        difficulty = 0;
        effort = 0;
        timeReq = 0;
        bugs = 0;
    }

    public void matchFlags(docInfo currentDocument)
    {
        this.lineFlag = currentDocument.lineFlag;
        this.wordFlag = currentDocument.wordFlag;
        this.charFlag = currentDocument.charFlag;
        this.slocFlag = currentDocument.slocFlag;
        this.commFlag = currentDocument.commFlag;
        this.halsFlag = currentDocument.halsFlag;
    }

    public void accumulateInfo(docInfo fromDoc)
    {
        this.lines += fromDoc.lines;
        this.words += fromDoc.words;
        this.chars += fromDoc.chars;
        this.SLoC += fromDoc.SLoC;
        this.comments += fromDoc.comments;
        this.N1 += fromDoc.N1;
        this.N2 += fromDoc.N2;
        this.nOne += fromDoc.nOne;
        this.nTwo += fromDoc.nTwo;
    }

    public void computeHalstead()
    {
        this.programVocab = this.nOne + this.nTwo;
        this.programLength = this.N1 + this.N2;
        this.calcProgLength = this.nOne * (Math.log(this.nOne)/Math.log(2))
                + this.nTwo * (Math.log(this.nTwo)/Math.log(2));
        this.volume = this.programLength * (Math.log(this.programVocab)/Math.log(2));
        this.difficulty = (this.nOne/2)*(this.N2/this.nTwo);
        this.effort = this.difficulty * this.volume;
        this.timeReq = this.effort/18;
        this.bugs = this.volume/3000;
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
    ArrayList<String> codeList;
    ArrayList<String> operatorList;
    ArrayList<String> operandList;

    public void wipe()
    {
        holder = '0';
        opCase = '0';
        operating = false;
        N1 = 0;
        N2 = 0;
        nOne = 0;
        nTwo = 0;
        codeList = new ArrayList<>();
        operatorList = new ArrayList<>();
        operandList = new ArrayList<>();
    }

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
