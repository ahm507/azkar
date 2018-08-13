package org.azkar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.sonna.www.sonna.R;
import org.azkar.services.BooksTreeNode;
import org.azkar.services.BooksTreeService;
import org.azkar.services.SQLiteInstaller;
import org.azkar.services.SearchPaging;
import org.azkar.services.TextUtils;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Stack;

import org.azkar.services.DatabaseCopyException;


public class MainActivity extends AppCompatActivity
//		implements NavigationView.OnNavigationItemSelectedListener
{

	private static final String LOG_TAG = "MainActivity";
    private BooksTreeService booksService;
    TextUtils textUtils = new TextUtils();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Right button of dots
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		//install DB
		SQLiteInstaller sqLiteInstaller = new SQLiteInstaller(this);

		try {
			sqLiteInstaller.install();
		} catch (DatabaseCopyException exception) {
			Log.e(LOG_TAG, "open >>" + exception.toString());
            showErrorDialogue("خطأ في العمل", "يوجد خطأ في تهيئة العمل علي ملف البيانات.", exception);
		}

		//Open DB and display initial view
		booksService = new BooksTreeService(this);
		booksService.open();
        displayHomePage();
	}

	public void displayHomePage() {
        displayKids("azkar_txt", "0");
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		booksService.close();
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			if (historyStack.size() > 0) {
				displayPreviousContents();
			} else {
				super.onBackPressed();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.nav_home_screen:
                historyStack.push(curPageId);
                displayHomePage();
                findViewById(R.id.textViewDisplay).setVisibility(View.GONE);
                findViewById(R.id.listViewTabweeb).setVisibility(View.VISIBLE);
                return true;
            case R.id.nav_about_us:
                showAboutDialogue();
                return true;
            case R.id.action_exit:
                finish();
                //Go phone home
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                return true;
            case R.id.nav_normal_font:
                textUtils.setFontNormal();
                displayContent(curBookCode, curPageId, curSearchWords);
                return true;
            case R.id.nav_large_font:
                textUtils.setFontLarge();
                displayContent(curBookCode, curPageId, curSearchWords);
                return true;
        }

		return super.onOptionsItemSelected(item);
	}

	void displayPreviousContents() {
		String page_id = historyStack.pop();

		WebView display = (WebView) findViewById(R.id.textViewDisplay);
		ListView tabweeb = (ListView) findViewById(R.id.listViewTabweeb);

		if (booksService.isLeafNode(curBookCode, page_id)) {
			display.setVisibility(View.VISIBLE);
			tabweeb.setVisibility(View.GONE);
			displayContent(curBookCode, page_id, "");
		} else {
			display.setVisibility(View.GONE);
			emptyDisplay(display);
			tabweeb.setVisibility(View.VISIBLE);
			displayKids(curBookCode, page_id);
		}
	}

	private void emptyDisplay(WebView display) {
		display.loadData("", "text/html; charset=UTF-8", null);
	}

	String curBookCode = "", curPageId = "", curSearchWords = "";
	ArrayList<BooksTreeNode> curRecords = new ArrayList<>();
	Stack<String> historyStack = new Stack<>();

	@SuppressLint("ClickableViewAccessibility") // See https://stackoverflow.com/a/46964717/2787593
    protected void displayContent(String book_code, String page_id, String searchWords) {
		try {
            curSearchWords = searchWords;
			WebView displayTextView = (WebView) findViewById(R.id.textViewDisplay);
			displayTextView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return handleSwipeLeftAndRight(event);
				}
			});
			ArrayList<BooksTreeNode> records = booksService.findNode(book_code, page_id);

            //IF END OF BOOK REACHED
            if(records.size() == 0) {
                return; // DO NO THING
            }

			if (records.size() != 1) {
				emptyDisplay(displayTextView);

			} else {
				BooksTreeNode record = records.get(0);
				String content = record.getPage();
				String htmlContent = textUtils.decorate(searchWords, record.getTitle(), content);
//				displayTextView.loadData(htmlContent, "text/html; charset=UTF-8", null);
                displayTextView.loadDataWithBaseURL("file:///android_asset/",
                        htmlContent, "text/html", "UTF-8", null);
				curBookCode = record.getBook_code();
				curPageId = record.getPage_id();
			}
		} catch (Exception exception) {
			Log.e(LOG_TAG, "exception", exception);
		}
	}

	protected void displayKids(String book_code, String page_id) {
		try {
			curBookCode = book_code;
			curPageId = page_id;
			ArrayList<BooksTreeNode> records = booksService.findKidNodes(book_code, page_id);
			final ArrayList<String> list = new ArrayList<>();
			curRecords.clear();
			for (BooksTreeNode record : records) {
				list.add(TextUtils.removeTrailingDot(record.getTitle()));
				curRecords.add(record);
			}
			//populate the list of items into the ListView
			ListView listView = (ListView) findViewById(R.id.listViewTabweeb);
			listView.clearChoices();

			ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
					android.R.layout.simple_list_item_1, android.R.id.text1, list);
			listView.setAdapter(adapter);

			// ListView Item Click Listener
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					BooksTreeNode record = curRecords.get(position);
					historyStack.push(curPageId); //is going to change per user click
					WebView display = (WebView) findViewById(R.id.textViewDisplay);
					ListView tabweeb = (ListView) findViewById(R.id.listViewTabweeb);

					if (booksService.isLeafNode(record.getBook_code(), record.getPage_id())) {
						display.setVisibility(View.VISIBLE);
						tabweeb.setVisibility(View.GONE);
						displayContent(record.getBook_code(), record.getPage_id(), "");
					} else {
						display.setVisibility(View.GONE);
						emptyDisplay(display);
						tabweeb.setVisibility(View.VISIBLE);
						displayKids(record.getBook_code(), record.getPage_id());

					}
				}
			});
		} catch (Exception exception) {
			Log.e(LOG_TAG, "exception", exception);
			showErrorDialogue("خطأ", "خطأ في عرض البيانات.", exception);

		}
	}

	void showErrorDialogue(String title, String body, Throwable exp) {
        Log.e(LOG_TAG, "fatal error", exp);
	    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(body);
		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		alertDialog.show();
	}

	public void onSearch(View view) {
		searchDatabase(view, 1);
	}

    SearchPaging paging = new SearchPaging();
    int currentSearchPageNumber;

    public void searchDatabase(View view, int pageNumber) {
        currentSearchPageNumber = pageNumber;
		EditText searchEditor = (EditText) findViewById(R.id.search_edit_text);
		String searchWords = searchEditor.getText().toString();
		if (searchWords.trim().length() == 0) {
			return; //just do nothing
		}

        //Lengthy Search Task
        SearchAsyncTask searchTask = new SearchAsyncTask(this, view);
        searchTask.execute(searchWords, pageNumber, paging.getPageLength(), booksService);

    }

    public void updateSearchControls(View view, final String queryWords, ArrayList<String> list, final ArrayList<BooksTreeNode> curSearchHits, int totalHitsCount) {
        final String searchWords = queryWords;
        paging.init(totalHitsCount);
        String pagingString = paging.getPagingString(currentSearchPageNumber);

        //set text in between next and prev
        TextView pagingTextView = (TextView) findViewById(R.id.text_view_paging);
        pagingTextView.setText(Html.fromHtml(pagingString));

        setTotalHitsCountText(totalHitsCount);

        ListView listView = (ListView) findViewById(R.id.listView_search_hits);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.search_hits_list_view, android.R.id.text1, list);
        listView.setAdapter(adapter);

        if(totalHitsCount > 0) {
            hideKeyboard(view);
        }

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BooksTreeNode bookNode = curSearchHits.get(position);
                historyStack.push(curPageId); //is going to change per user click
                findViewById(R.id.textViewDisplay).setVisibility(View.VISIBLE);
                findViewById(R.id.listViewTabweeb).setVisibility(View.GONE);

                //searchWords
                displayContent(bookNode.getBook_code(), bookNode.getPage_id(), searchWords);
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                hideKeyboard(view);
            }
        });
    }

    private void setTotalHitsCountText(int totalHitsCount) {
        TextView hitsCountView = (TextView) findViewById(R.id.text_view_hits_count);
        String countMessage = (totalHitsCount == 0)? "لا توجد نتائج"
                : String.format(Locale.US, "%,d نتيجة", totalHitsCount);
        hitsCountView.setText(countMessage);
    }

    private void hideKeyboard(View view) {
        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(keyboard != null) {
            keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Swipe left and right
	private float x1;
	public boolean handleSwipeLeftAndRight(MotionEvent event) {
        final int MIN_DISTANCE = 150;
        float x2;
		if (findViewById(R.id.listViewTabweeb).getVisibility() == View.VISIBLE) {
			return super.onTouchEvent(event);
		}
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				x1 = event.getX();
				break;
			case MotionEvent.ACTION_UP:
				x2 = event.getX();
				float deltaX = x2 - x1;
				if (Math.abs(deltaX) > MIN_DISTANCE) {
					if (x2 > x1) { // Left to Right swipe action : NEXT
						displayContent(curBookCode, String.valueOf(Integer.parseInt(curPageId) + 1), "");
					} else {  // Right to left swipe action: PREVIOUS
						if (Integer.parseInt(curPageId) > 1) {
							displayContent(curBookCode, String.valueOf(Integer.parseInt(curPageId) - 1), "");
						}
					}
				}
				break;
		}
		return super.onTouchEvent(event);
	}

	void showAboutDialogue() {
		AlertDialog.Builder aboutAlert = new AlertDialog.Builder(
				MainActivity.this);
		LayoutInflater factory = LayoutInflater.from(MainActivity.this);
        WebView mainView = (WebView) findViewById(R.id.nav_about_us);
        final ImageView view = (ImageView) factory.inflate(R.layout.about_image_view, mainView);
		aboutAlert.setView(view);
		aboutAlert.setTitle("عن البرنامج");
		aboutAlert.setNeutralButton("إغلاق", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dlg, int which) {
				dlg.dismiss();
			}
		});
		aboutAlert.show();
	}

    public void onSearchNextPage(View view) {
        int newPageNumber = paging.getNextSearchPageNumber(currentSearchPageNumber);
        if(newPageNumber != currentSearchPageNumber) {
            searchDatabase(view, newPageNumber);
        }
    }

    public void onSearchPreviousPage(View view) {
        int newPageNumber = paging.getPreviousPageNumber(currentSearchPageNumber);
        if(newPageNumber != currentSearchPageNumber) {
            searchDatabase(view, newPageNumber);
        }
    }
}
