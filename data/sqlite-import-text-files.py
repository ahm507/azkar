#! /usr/bin/env python
# coding: utf-8
# -*- coding: utf_8 -*-


import sqlite3
import re
import unicodedata
import sys


class Parser:

    def __init__(self):
        self.stack = []
        self.current_header_level = u"H1"

    # def remove_vowels(arabic_text_with_vowels):
    #     vowels = u"[\u064B-\u065F]"  # vowel character range
    #     arabic_text = re.sub(vowels, u'', arabic_text_with_vowels)
    #     return arabic_text


    def remove_database_records(self, sqlite_name):
        conn = sqlite3.connect(sqlite_name)
        cur = conn.cursor()
        cur.execute('delete from pages')
        conn.commit()
        conn.close
        print ("All records are removed from " +sqlite_name)


    def strip_diacritics(self, text):
        import unicodedata
        # return ''.join([c for c in unicodedata.normalize('NFD', text) \
        # if unicodedata.category(c) != 'Mn'])
        if text and len(text) > 0:
            return ''.join([c for c in text if unicodedata.category(c) != 'Mn'])
        else:
            return ''


    def get_pop_count(self, cur_level, new_level):
        cur_level = cur_level.replace("H", "");
        new_level = new_level.replace("H", "")
        return int(cur_level) - int(new_level)



    def handle_level(self, line, page_id):
        if line.strip() > self.current_header_level:
            #print "Lower level : new level=", line, "; current level", current_header_level
            self.stack.append(page_id)
            self.current_header_level = line #update current line
        elif line.strip() < self.current_header_level:
            #print "Higher level: new level=", line, "; current level", current_header_level
            pop_count = self.get_pop_count(self.current_header_level, line)
            self.current_header_level = line
            print "poping count", pop_count
            while pop_count > 0:
                self.stack.pop()
                pop_count -= 1
        #else:
         #print "Same level  :", line, ";",  current_header_level

    def convert_text_to_sqlite(self, file_names, sqlite_name):

        print ("import text files and insert records into sqlite file")

        self.remove_database_records(sqlite_name)

        conn = sqlite3.connect(sqlite_name)
        cur = conn.cursor()

        page_id = 0
        book_index = 1
        # book_code_prefix = u"soura"

        for text_file_name in file_names:
            print "\nfile name:", text_file_name
            parent_id =""
            title =u""
            page =u""
            page_fts =u""
            # book_code =  book_code_prefix + str(book_index)
            book_code = text_file_name.replace(".", "_").replace("-", "_")
            print "working on file", book_code
            book_index += 1
            record =u""
            line = u""
            self.stack.append("NO_PARENT")


            with open(text_file_name, 'rU') as file:
                file.readlines
                for line in file:
                    line = line.strip()
                    if len(line) > 0 :
                        # print "line is[" + line + "]"
                        if line.find("H") != -1 : # H1, H2, H3, H4
	                        # print "line is [", line, "]"
	                        # handle stack of parent ids
	                        line = line.strip()
	                        #split lines to extract first line as title
	                        if(len(record) > 0) :
		                        lines = record.splitlines() #split on new line
		                        title = lines[0]
		                        # print "title is:", title
		                        lines[0] = ""
		                        joinedData = ""
		                        for single_line in lines:
			                        joinedData += "\r\n" + single_line

		                        record_fts = self.strip_diacritics(unicode(joinedData))
		                        parent_id = self.stack[len(self.stack)-1]
		                        joinedData = joinedData.strip()
		                        record_fts = record_fts.strip()
		                        # joinedData = joinedData.replace("\r\n", "\r\n<br>")
		                        topic = (page_id, parent_id, book_code, title, joinedData, record_fts)
		                        print "RECORD: page_id=", page_id, ";parent_id=", parent_id, ";title=", title
		                        cur.execute(u'insert into pages (page_id, parent_id, book_code, title, page, page_fts) Values (?, ?, ?, ?, ?, ?)', topic)
		                        record = "" # for the new line processing
		                        sys.stdout.flush()

		                        #handle parent id
		                        self.handle_level(line, page_id)   # updates current_header_level and stack

		                        page_id += 1
                        else:   # H!, H2, H3, H4
	                        record += line.decode("utf-8") + "\r\n"

        conn.commit()
        conn.close()  # call basic function
        print ""
        print "Warning: you have to add an empty record at the end of file to ensure flushing of data, as example H3 or H4"
        print "Conversion completed for records counted:" + str(page_id)



##################################################################################


file_names = ["azkar.txt"]
sqlite_name = 'azkar.sqlite'
parser = Parser()
parser.convert_text_to_sqlite(file_names, sqlite_name)

