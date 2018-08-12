package org.azkar;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.view.View;

import org.azkar.services.BooksTreeNode;
import org.azkar.services.BooksTreeService;
import org.azkar.services.TextUtils;

import java.util.ArrayList;

public class SearchAsyncTask extends AsyncTask<Object, Void, String> {

    private ProgressDialog dialog;
    private ArrayList<String> list = new ArrayList<>();
    private ArrayList<BooksTreeNode> curSearchHits = new ArrayList<>();
    private int totalHitsCount;
    private String searchWords;

    private MainActivity mainActivity;
    private View view;

    public ArrayList<String> getHitsStrings() {
        return list;
    }

    public ArrayList<BooksTreeNode> getSearchHits() {
        return curSearchHits;
    }

    public int getTotalHitsCount() {
        return totalHitsCount;
    }

    public SearchAsyncTask(MainActivity activity, View view) {
        this.mainActivity = activity;
        this.view = view;
        dialog = new ProgressDialog(activity);
    }

    protected void onPreExecute() {
        dialog.setMessage("جاري البحث...");
        dialog.show();
    }

    @Override
    protected String doInBackground(Object... params) {
        searchWords = (String) params[0];
        Integer pageNumber = (Integer) params[1];
        Integer pageLength = (Integer) params[2];
        BooksTreeService booksService = (BooksTreeService) params[3];

        //Lengthy Search Operation...
        totalHitsCount = booksService.getSearchHitsTotalCount("", searchWords);
        ArrayList<BooksTreeNode> hits = booksService.search(searchWords, pageLength, pageNumber);

        for (BooksTreeNode record : hits) {
            list.add(TextUtils.removeTrailingDot(record.getTitle()));
            curSearchHits.add(record);
        }

        return " ";
    }

    @Override
    protected void onPostExecute(String str) {
        //Update your UI here.... Get value from doInBackground ....
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        //Update search controls with search results
        mainActivity.updateSearchControls(view, searchWords, list, curSearchHits, totalHitsCount);
    }
}
