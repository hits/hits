from database.sql import *

def test_stringify():
    assert stringify("hello") == '"hello"'

def test_insert_statement():
    assert insert_statement({'name': 'joe'}, "NameTable") == \
            """INSERT INTO NameTable\n(name)\nVALUES\n("joe");"""
    assert insert_statement({'name': 'joe', 'age': '14'}, "NameTable") == \
            """INSERT INTO NameTable\n(age, name)\nVALUES\n("14", "joe");"""

def test_insert_many_statement():
    data = ({'name': 'joe',   'age': '14'}, {'name': 'alice', 'age': '22'})
    assert insert_many_statement(data, "NameTable") == \
            """INSERT INTO NameTable\n(age, name)\nVALUES\n("14", "joe"),\n("22", "alice");"""
