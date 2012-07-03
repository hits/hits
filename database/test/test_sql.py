from database.sql import *

def test_stringify():
    assert stringify("hello") == '"hello"'

def test_insert_statement():
    assert insert_statement({'name': 'joe'}, "NameTable") == \
            """INSERT INTO NameTable\n(name)\nVALUES\n("joe");"""
    assert insert_statement({'name': 'joe', 'age': '14'}, "NameTable") == \
            """INSERT INTO NameTable\n(age, name)\nVALUES\n("14", "joe");"""



