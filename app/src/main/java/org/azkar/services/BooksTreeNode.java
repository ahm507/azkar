package org.azkar.services;

public class BooksTreeNode {

	final private String page_id;		//matches sqlite field name
    final private String book_code;	//matches sqlite field name
    final private String title;
    final private String page;

    BooksTreeNode(String page_id, String book_code, String title, String page) {
        this.page_id = page_id;
        this.book_code = book_code;
        this.title = title;
        this.page = page;
    }

    public String getPage_id() {
		return page_id;
	}

	public String getBook_code() {
		return book_code;
	}

	public String getTitle() {
		return title;
	}

	public String getPage() {
		return page;
	}

	//Do not retrieve page_fts, as it it no-vowel text used for search only.
}
