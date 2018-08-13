
Azkar Android App
=============================



What is this?
-------------
* This program is used to display Islamic Azkar.
  


Technology
----------

* This is a standard Android Studio application. If some file is missing, please contact me.
* Data is stored in Sqlite file. 
* full text indexing feature is used to search inside the Sqlite file.


Database Structure:
----------------------
Here is a list of fields:
* book_code: Stores a code to distinguish the book, so you can store multiple books.
* page_id:   Stores a unique id for within some book_code. it must be unique. 
* parent_id: Stores parent id, so a table of contents can be showed.
* title:     Store contents title
* page:      Store contents or paragraphs that has vowels for display purposes. 
* page_fts:  Stores contents with no vowels, so that sqlite full text index can work on it.


License
--------
* Usage and contributions are welcomed.
* The text and code is license as GNU 3.0 located at: https://www.gnu.org/copyleft/gpl.html

