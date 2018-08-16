package org.azkar.services;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Formatter;

public class BooksTreeService {

	private SQLiteDatabase db;
	private SQLiteInstaller sqLiteInstaller;

	public BooksTreeService(Context context) {
		sqLiteInstaller = new SQLiteInstaller(context);
	}

	public void open() throws SQLException {
		sqLiteInstaller.openDataBase();
		sqLiteInstaller.close();
		db = sqLiteInstaller.getReadableDatabase();
	}

	public void close() {
		sqLiteInstaller.close();
	}

	private ArrayList<BooksTreeNode> executeQuery(String sql, String args[]) {
		Cursor cursor = db.rawQuery(sql, args);
		ArrayList<BooksTreeNode> out = new ArrayList<>();
		while (cursor != null && cursor.moveToNext()) {
			out.add(getBooksTreeNodeObject(cursor));
		}
		if(cursor != null) {
		    cursor.close();
        }
		return out;
	}

    @NonNull
    private BooksTreeNode getBooksTreeNodeObject(@NonNull Cursor cursor) {
	    BooksTreeNode node = new BooksTreeNode(cursor.getString(0), cursor.getString(2),
                        cursor.getString(3), cursor.getString(4), cursor.getString(1));
	    return node;
    }

    public ArrayList<BooksTreeNode> findNode(String book_code, String page_id) {
		String sql = "SELECT * FROM pages where pages MATCH ?";
		String params = new Formatter().format("book_code:%s page_id:%s", book_code, page_id).toString();
		String args[] = new String[]{params};
		return executeQuery(sql, args);
	}

	public ArrayList<BooksTreeNode> findKidNodes(String book_code, String page_id) {
		String args[];
		String sql;
		if ("".equals(page_id)) {
			sql = "SELECT * FROM pages where parent_id MATCH 'NO_PARENT'";
			args = null;
		} else {
			sql = "SELECT * FROM pages where pages MATCH ?";
			String param = new Formatter().format("book_code:%s parent_id:%s", book_code, page_id).toString();
			args = new String[]{param};
		}

		return executeQuery(sql, args);
	}

	public boolean isLeafNode(String book_code, String page_id) {
		String sql = "SELECT * FROM pages where pages MATCH ?";
		String param = new Formatter().format("book_code:%s parent_id:%s", book_code, page_id).toString();
		String[] args = new String[]{param};
		Cursor cursor = db.rawQuery(sql, args);
		boolean existKids = (cursor != null && cursor.moveToNext());
		if(cursor!= null) {
			cursor.close();
		}
		return ( ! existKids );
	}

	public ArrayList<BooksTreeNode> search(String terms, int pageLength, int pageNo) {
		String sql = "SELECT * FROM pages where pages MATCH ? order by book_code,page_id LIMIT ? OFFSET ? ";
		String args[] = {terms, String.valueOf(pageLength), String.valueOf((pageNo - 1) * pageLength)};
		Cursor cursor = db.rawQuery(sql, args);
		ArrayList<BooksTreeNode> out = new ArrayList<>();
		while (cursor != null && cursor.moveToNext()) {
			out.add(getBooksTreeNodeObject(cursor));
		}
		if(cursor != null) {
			cursor.close();
		}
		return out;
	}

	public int getSearchHitsTotalCount(String book_code, String queryString) {
		String ftsQuery;
		String sql;
		String params[];
		if (book_code.isEmpty()) { //empty
			ftsQuery = queryString;
			sql = "SELECT count(*) AS total_count FROM pages WHERE page_fts MATCH ?";
		} else {
			ftsQuery = new Formatter().format("book_code:%s %s", book_code, queryString).toString();
			sql = "SELECT count(*) AS total_count FROM pages WHERE page_fts MATCH ?";
		}
		params = new String[]{ftsQuery};
		Cursor cursor = db.rawQuery(sql, params);
		int count;
		if (cursor != null && cursor.moveToNext()) {
			String countString = cursor.getString(0);
			count = Integer.parseInt(countString);
		} else {
			count = 0;
		}
		if(cursor!= null) {
			cursor.close();
		}
		return count;
	}

	public ArrayList<BooksTreeNode> findParents(String book_code, String parent_id) {
        String parentId = parent_id;
        ArrayList<BooksTreeNode> parents = new ArrayList<>();
        ArrayList<BooksTreeNode> singleNodeArray = findNode(book_code, parentId);
        while (! "NO_PARENT".equals(singleNodeArray.get(0).getParent_id())) {
            parents.add(singleNodeArray.get(0));
            parentId = singleNodeArray.get(0).getParent_id();
            singleNodeArray = findNode(book_code, parentId);
        }
        return parents;
    }


}
