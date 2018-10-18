import java.io.*;
import java.util.ArrayList;

public class operandCollect
{
    final char[] OPERATOR = {'=', '+', '-', '*', '/', '%','!', '>', '<', '&', '|', '?', '~', '^', ':'};

    private  char holder = '0';
    private  char opCase = '0';
    private  boolean operating = false;
    ArrayList<String> codeList = new ArrayList<>();

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
                    for (char c : OPERATOR) {
                        if (c != ':') {
                            if (c == (char) streamIt.ttype) {
                                holder = (char) streamIt.ttype;
                                operating = true;
                            }
                        }
                    }
                } else {
                    for (char c : OPERATOR) {
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
                        /*else
                            codeList.add("=");*/
                        reset();
                        break;

                    case '+':
                        if(opCase == '+')
                            codeList.add("++");
                        else if(opCase == '=')
                            codeList.add("+=");
                        /*else
                            codeList.add("+");*/
                        reset();
                        break;

                    case '-':
                        if(opCase == '-')
                            codeList.add("--");
                        else if(opCase == '=')
                            codeList.add("-=");
                        /*else
                            codeList.add("-");*/
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
                        /*else
                            codeList.add("!");*/
                        reset();
                        break;

                    case '>':
                        if(opCase == '=')
                            codeList.add(">=");
                        else if(opCase == '>')
                            break;
                        /*else
                            codeList.add(">");*/
                        reset();
                        break;

                    case '<':
                        if(opCase == '=')
                            codeList.add("<=");
                        else if(opCase == '<')
                            codeList.add("<<");
                        /*else
                            codeList.add("<");*/
                        reset();
                        break;

                    case '&':
                        if(opCase == '&')
                            codeList.add("&&");
                        else if(opCase == '=')
                            codeList.add("&=");
                        /*else
                            codeList.add("&");*/
                        reset();
                        break;

                    case '|':
                        if(opCase == '|')
                            codeList.add("||");
                        else if(opCase == '=')
                            codeList.add("|=");
                        /*else
                            codeList.add("|");*/
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

    private void reset(){
        holder = '0';
        opCase = '0';
        operating = false;
    }
}
