import java.io.BufferedReader;
import java.io.IOException;

public class lineCollect {
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
