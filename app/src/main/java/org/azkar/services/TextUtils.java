package org.azkar.services;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;

public class TextUtils {

    private static final String TAG = "TextUtils";

    enum FontSize {
        NORMAL, LARGE
    }

    private FontSize fontSize = FontSize.NORMAL;

    public void setFontLarge() {
        fontSize = FontSize.LARGE;
    }

    public void setFontNormal() {
        fontSize = FontSize.NORMAL;
    }

    private static String addVowels(String arabic) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < arabic.length(); i++) {
            result.append(arabic.charAt(i));
            result.append("[\u064B-\u065F]*"); //vowels
            //unicode vowels letters from url: http://unicode.org/charts/PDF/U0600.pdf
        }
        return result.toString();
    }

    @NonNull
    public static String highlight(String bodyString, String highlightWords) {
        final String spanStart = "<font color=\"red\">";
        final String spanEnd = "</font>";

        for (String word : highlightWords.split(" ")) {
            word = word.trim();
            if (word.length() > 0) {
                String processedWord = addVowels(word);
                bodyString = bodyString.replaceAll("(\\b" + processedWord + "\\b)", spanStart + "$1" + spanEnd);
            }
        }
        return bodyString;
    }

    @NonNull
    public String decorate(@NonNull String searchWords, @NonNull String title, @NonNull String content, ArrayList<BooksTreeNode> parents) {

        content = content.trim();
        content = removeTrailingHashes(content);
        content = content.trim();

        //FIXME: Use StringBuilder
        //FIXME: Use HTML templates
        String fontSizeStyle = (fontSize == FontSize.LARGE)? " font-size: 150%; " : " font-size: 110%; ";
        String font = "@font-face {font-family: 'custom';src: url('fonts/Amiri-Regular.ttf');} ";
        String style = "<style>" + font + " body {font-family: 'custom'; direction: rtl; "
                + "text-align:justify; align-content: right;  text-align=right;"
                + fontSizeStyle + "}</style>";
        String head = "<head>" + style + "</head>";
        final String htmlPagePrefix = "<html>" + head + "<body>";
        final String htmlPagePostfix = "</body></html>";

//        content = content.replaceAll("##", "<br><hr>");
        content = content.replaceAll("\n", "<br>");
        if(searchWords.trim().length() > 0) { //highlight search text
            content = TextUtils.highlight(content, searchWords);
        }

        String decoratedTitle = String.format("<font color='blue'>%s</font>", TextUtils.removeTrailingDot(title));
        String nodePathHtml = generateHeaderPath(decoratedTitle, parents);

        //Add title
        content = nodePathHtml + "<hr>" + content;
        String html = htmlPagePrefix + content + htmlPagePostfix;
        Log.i(TAG, html);
        return html;
    }

    @NonNull
    public static String removeTrailingHashes(@NonNull String content) {
        if(content.charAt(content.length()-1) == '#') {
            return content.substring(0, content.length()-2);
        }
        return content;
    }

    @NonNull
    public static String removeTrailingDot(@NonNull String content) {
        if(content.charAt(content.length()-1) == '.') {
            return content.substring(0, content.length()-1);
        }
        return content;
    }

    public String generateHeaderPath(String title, ArrayList<BooksTreeNode> parents) {
        StringBuilder header = new StringBuilder();
        String space = "";
        for(int i = parents.size()-1 ; i >= 0 ; i--) { //skip last root parent
//            header.append(String.format("<a href='%s'>%s</a><br>", parents.get(i).getPage_id(), parents.get(i).getTitle()));
            header.append(String.format("%s%s<br>", space, parents.get(i).getTitle()));
            space += "&nbsp;&nbsp;";
        }

        header.append(space + title);
        return header.toString();
    }

}
